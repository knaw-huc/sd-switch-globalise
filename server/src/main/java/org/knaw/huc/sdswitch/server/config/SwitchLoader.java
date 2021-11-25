package org.knaw.huc.sdswitch.server.config;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import nl.mpi.tla.util.Saxon;
import org.knaw.huc.sdswitch.recipe.Recipe;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SwitchLoader {
    private final Map<String, Recipe> recipes;

    public SwitchLoader() {
        recipes = Recipe.getRecipes();
    }

    public Set<Switch> loadSwitches(InputStream stream) throws SwitchException {
        try {
            Set<Switch> switches = new HashSet<>();

            XdmNode conf = Saxon.buildDocument(new StreamSource(stream));
            for (XdmItem switchItem : Saxon.xpathList(conf, "/sd-switch/switch")) {
                if (!Saxon.hasAttribute(switchItem, "recipe"))
                    throw new SwitchException("Switch is missing a 'recipe' attribute");

                if (!Saxon.hasAttribute(switchItem, "url-pattern"))
                    throw new SwitchException("Switch is missing an 'url-pattern' attribute");

                String className = Saxon.xpath2string(switchItem, "@recipe");
                if (!recipes.containsKey(className))
                    throw new SwitchException("Recipe '" + className + "' not found!");

                Recipe recipe = recipes.get(className);
                String urlPattern = Saxon.xpath2string(switchItem, "@url-pattern");

                switches.add(new Switch(recipe, urlPattern, switchItem));
            }

            return switches;
        } catch (SaxonApiException e) {
            throw new SwitchException("Failed to parse switches config XML", e);
        }
    }
}
