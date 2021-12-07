package org.knaw.huc.sdswitch.server.recipe;

import net.sf.saxon.s9api.XdmItem;

import java.util.Map;

public record RecipeData(Map<String, String> pathParams, XdmItem config) {
}
