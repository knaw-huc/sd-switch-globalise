package org.knaw.huc.sdswitch.server.config;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import nl.mpi.tla.util.Saxon;
import org.knaw.huc.sdswitch.recipe.Recipe;
import org.knaw.huc.sdswitch.recipe.RecipeParseException;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SwitchLoader {
    private static final Pattern PATH_PATTERN = Pattern.compile("(?<=[{<]).+?(?=[}>])");

    private final Map<String, Recipe<?>> recipes;

    public SwitchLoader() {
        recipes = Recipe.loadRecipes();
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
                        withSwitchConfig(switches, recipe, switchItem, subSwitchItem);
                }
            }

            return switches;
        } catch (SaxonApiException | RecipeParseException e) {
            throw new SwitchException("Failed to parse switches config XML", e);
        }
    }

    private static <C> void withSwitchConfig(Map<String, Set<Switch<?>>> switches, Recipe<C> recipe,
                                             XdmItem parentConfig, XdmItem childConfig)
            throws RecipeParseException, SwitchException, SaxonApiException {
        List<XdmItem> urlItems = Saxon.xpathList(childConfig != null ? childConfig : parentConfig, "url");
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

            // Obtain all properties from both the parent and child config
            List<XdmItem> configItems = Saxon.xpathList(parentConfig, "./*[not(self::sub-switch) and not(self::url)]");
            if (childConfig != null)
                configItems.addAll(Saxon.xpathList(childConfig, "./*[not(self::url)]"));

            // Build a new element with the combined properties from both the parent and the child config
            StringBuilder builder = new StringBuilder("<switch>");
            for (XdmItem configItem : configItems)
                builder.append(configItem.toString());
            builder.append("</switch>");

            XdmNode config = Saxon.buildDocument(new StreamSource(new StringReader(builder.toString())));
            XdmItem configRoot = Saxon.xpathList(config, "/switch").get(0);

            urlSwitches.add(createSwitch(recipe, urlPattern, acceptMimeType, configRoot));
            switches.putIfAbsent(urlPattern, urlSwitches);
        }
    }

    private static <C> Switch<C> createSwitch(Recipe<C> recipe, String urlPattern,
                                              String acceptMimeType, XdmItem config) throws RecipeParseException {
        Set<String> pathParams = PATH_PATTERN
                .matcher(urlPattern)
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toSet());

        C parsedConfig = recipe.parseConfig(config, pathParams);
        return Switch.createSwitch(recipe, urlPattern, acceptMimeType, parsedConfig);
    }
}
