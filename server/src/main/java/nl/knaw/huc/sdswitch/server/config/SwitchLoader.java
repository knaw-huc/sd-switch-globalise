package nl.knaw.huc.sdswitch.server.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import nl.knaw.huc.sdswitch.recipe.Recipe;
import nl.knaw.huc.sdswitch.recipe.RecipeValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SwitchLoader {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final Pattern PATH_PATTERN = Pattern.compile("(?<=[{<]).+?(?=[}>])");

    private final Map<String, Recipe<?>> recipes;

    public SwitchLoader() {
        recipes = Recipe.loadRecipes();
    }

    public Map<String, Set<Switch<?>>> loadSwitches(InputStream stream) throws SwitchException {
        try {
            Map<String, Set<Switch<?>>> switches = new HashMap<>();

            List<SwitchConfig.ParentSwitchConfig> switchConfigs = OBJECT_MAPPER.readValue(stream, new TypeReference<>() {
            });
            for (SwitchConfig.ParentSwitchConfig switchConfig : switchConfigs) {
                if (switchConfig.recipe() == null)
                    throw new SwitchException("A switch is missing a 'recipe' attribute");

                if (!recipes.containsKey(switchConfig.recipe()))
                    throw new SwitchException("Recipe '" + switchConfig.recipe() + "' not found!");

                Recipe<?> recipe = recipes.get(switchConfig.recipe());

                Set<SwitchConfig.SubSwitchConfig> subSwitchItems = switchConfig.subSwitches();
                if (subSwitchItems == null)
                    withSwitchConfig(switches, recipe, switchConfig, null);
                else {
                    for (SwitchConfig subSwitchConfig : subSwitchItems)
                        withSwitchConfig(switches, recipe, switchConfig, subSwitchConfig);
                }
            }

            return switches;
        } catch (IOException | RecipeValidationException e) {
            throw new SwitchException("Failed to parse switches config", e);
        }
    }

    private static <C> void withSwitchConfig(Map<String, Set<Switch<?>>> switches, Recipe<C> recipe,
                                             SwitchConfig parentConfig, SwitchConfig childConfig)
            throws JsonProcessingException, SwitchException, RecipeValidationException {
        Set<SwitchConfig.Url> urlItems = childConfig != null ? childConfig.urls() : parentConfig.urls();
        if (urlItems == null)
            throw new SwitchException("A switch is missing at least one 'url' item");

        for (SwitchConfig.Url urlItem : urlItems) {
            if (urlItem.pattern() == null)
                throw new SwitchException("Switch is missing a 'pattern' attribute");

            Set<Switch<?>> urlSwitches = switches.getOrDefault(urlItem.pattern(), new HashSet<>());
            if (urlSwitches.stream().anyMatch(aSwitch -> aSwitch.getAcceptMimeType() == null))
                throw new SwitchException("There is already a switch configured with URL pattern '" + urlItem.pattern() + "'!");

            JsonNode configNode = parentConfig.config();
            if (childConfig != null) {
                configNode = configNode.deepCopy();
                mergeJsonNodes(configNode, childConfig.config());
            }

            urlSwitches.add(createSwitch(recipe, urlItem.pattern(), urlItem.accept(), configNode));
            switches.putIfAbsent(urlItem.pattern(), urlSwitches);
        }
    }

    private static <C> Switch<C> createSwitch(Recipe<C> recipe, String urlPattern,
                                              String acceptMimeType, JsonNode configNode)
            throws JsonProcessingException, RecipeValidationException {
        Set<String> pathParams = PATH_PATTERN
                .matcher(urlPattern)
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toSet());

        Class<C> configClass = (Class<C>)
                ((ParameterizedType) recipe.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
        C config = OBJECT_MAPPER.treeToValue(configNode, configClass);
        recipe.validateConfig(config, pathParams);

        return Switch.createSwitch(recipe, urlPattern, acceptMimeType, config);
    }

    private static void mergeJsonNodes(JsonNode nodeA, JsonNode nodeB) {
        Iterator<String> fieldNames = nodeB.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode jsonNode = nodeA.get(fieldName);
            if (jsonNode != null && jsonNode.isObject()) {
                mergeJsonNodes(jsonNode, nodeB.get(fieldName));
            } else if (nodeA instanceof ObjectNode objectNodeA) {
                JsonNode value = nodeB.get(fieldName);
                objectNodeA.set(fieldName, value);
            }
        }
    }
}
