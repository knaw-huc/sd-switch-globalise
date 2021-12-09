package org.knaw.huc.sdswitch.server.config;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import nl.mpi.tla.util.Saxon;
import org.knaw.huc.sdswitch.server.recipe.Recipe;
import org.knaw.huc.sdswitch.server.recipe.RecipeParseException;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.util.HashSet;
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

    public Set<Switch<?>> loadSwitches(InputStream stream) throws SwitchException {
        try {
            Set<Switch<?>> switches = new HashSet<>();

            XdmNode conf = Saxon.buildDocument(new StreamSource(stream));
            for (XdmItem switchItem : Saxon.xpathList(conf, "/sd-switch/switch")) {
                if (!Saxon.hasAttribute(switchItem, "recipe"))
                    throw new SwitchException("Switch is missing a 'recipe' attribute");

                if (!Saxon.hasAttribute(switchItem, "url-pattern"))
                    throw new SwitchException("Switch is missing an 'url-pattern' attribute");

                String className = Saxon.xpath2string(switchItem, "@recipe");
                if (!recipes.containsKey(className))
                    throw new SwitchException("Recipe '" + className + "' not found!");

                Recipe<?> recipe = recipes.get(className);
                String urlPattern = Saxon.xpath2string(switchItem, "@url-pattern");

                switches.add(createSwitch(recipe, urlPattern, switchItem));
            }

            return switches;
        } catch (SaxonApiException | RecipeParseException e) {
            throw new SwitchException("Failed to parse switches config XML", e);
        }
    }

    private static <C> Switch<C> createSwitch(Recipe<C> recipe, String urlPattern, XdmItem config)
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

        return Switch.createSwitch(recipe, urlPattern, recipe.parseConfig(config));
    }
}
