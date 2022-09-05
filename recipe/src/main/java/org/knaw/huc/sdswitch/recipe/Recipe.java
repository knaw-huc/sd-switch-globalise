package org.knaw.huc.sdswitch.recipe;

import net.sf.saxon.s9api.XdmItem;

import java.util.Set;

public interface Recipe<C> {
    C parseConfig(XdmItem config, Set<String> pathParams) throws RecipeParseException;

    RecipeResponse withData(RecipeData<C> data) throws RecipeException;
}
