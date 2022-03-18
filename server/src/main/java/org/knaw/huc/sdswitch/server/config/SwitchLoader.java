package org.knaw.huc.sdswitch.server.config;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import nl.mpi.tla.util.Saxon;
import org.knaw.huc.sdswitch.server.recipe.Recipe;
import org.knaw.huc.sdswitch.server.recipe.RecipeParseException;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toSet;

public class SwitchLoader {
    private static final Pattern PATH_PATTERN = Pattern.compile("(?<=[{<]).+?(?=[}>])");

    private final Map<String, Recipe<?>> recipes;

    public SwitchLoader() {
        recipes = Recipe.getRecipes();
    }

    public Map<String, Set<Switch<?>>> loadSwitches(InputStream stream) throws SwitchException {
        try {
            Map<String, Set<Switch<?>>> switches = new HashMap<>();

            XdmNode conf = Saxon.buildDocument(new StreamSource(stream));
            for (XdmItem switchItem : Saxon.xpathList(conf, "/sd-switch/switch")) {
                if (!Saxon.hasAttribute(switchItem, "recipe"))
                    throw new SwitchException("A switch is missing a 'recipe' attribute");

                String className = Saxon.xpath2string(switchItem, "@recipe");
                if (!recipes.containsKey(className))
                    throw new SwitchException("Recipe '" + className + "' not found!");

                Recipe<?> recipe = recipes.get(className);

                List<XdmItem> subSwitchItems = Saxon.xpathList(switchItem, "sub-switch");
                if (subSwitchItems.isEmpty())
                    withSwitchConfig(switches, recipe, switchItem, null);
                else {
                    for (XdmItem subSwitchItem : subSwitchItems)
                        withSwitchConfig(switches, recipe, subSwitchItem, switchItem);
                }
            }

            return switches;
        } catch (SaxonApiException | RecipeParseException e) {
            throw new SwitchException("Failed to parse switches config XML", e);
        }
    }

    private static <C> void withSwitchConfig(Map<String, Set<Switch<?>>> switches, Recipe<C> recipe,
                                             XdmItem config, XdmItem parentConfig)
            throws RecipeParseException, SwitchException, SaxonApiException {
        List<XdmItem> urlItems = Saxon.xpathList(config, "url");
        if (urlItems.isEmpty())
            throw new SwitchException("A switch is missing at least one 'url' item");

        for (XdmItem urlItem : urlItems) {
            if (!Saxon.hasAttribute(urlItem, "pattern"))
                throw new SwitchException("Switch is missing a 'pattern' attribute");

            String urlPattern = Saxon.xpath2string(urlItem, "@pattern");
            String acceptMimeType = Saxon.hasAttribute(urlItem, "accept")
                    ? Saxon.xpath2string(urlItem, "@accept") : null;

            Set<Switch<?>> urlSwitches = switches.getOrDefault(urlPattern, new HashSet<>());
            if (urlSwitches.stream().anyMatch(aSwitch -> aSwitch.getAcceptMimeType() == null))
                throw new SwitchException("There is already a switch configured with URL pattern '" + urlPattern + "'!");

            urlSwitches.add(createSwitch(recipe, urlPattern, acceptMimeType, config, parentConfig));
            switches.putIfAbsent(urlPattern, urlSwitches);
        }
    }

    private static <C> Switch<C> createSwitch(Recipe<C> recipe, String urlPattern, String acceptMimeType,
                                              XdmItem config, XdmItem parentConfig)
            throws RecipeParseException, SwitchException {
        Set<String> pathParams = PATH_PATTERN
                .matcher(urlPattern)
                .results()
                .map(MatchResult::group)
                .collect(toSet());

        Optional<String> missingPathParam = recipe
                .requiredPathParams()
                .stream()
                .filter(pathParam -> !pathParams.contains(pathParam))
                .findAny();

        if (missingPathParam.isPresent())
            throw new SwitchException(String.format("Missing required path param '%s'", missingPathParam.get()));

        return Switch.createSwitch(recipe, urlPattern, acceptMimeType, recipe.parseConfig(config, parentConfig));
    }
}
