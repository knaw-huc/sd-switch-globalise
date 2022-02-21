package org.knaw.huc.sdswitch.recipe;

import org.junit.Assert;
import org.junit.Test;

public class JsonToTtlTest {

  @Test
  void runTestJsonTtl() throws Exception {
    String json = "{ \"id\":1 }";
    String expectedResult = "<https://humanities.knaw.nl/raa/person/1> a <https://humanities.knaw.nl/person>.";
    String result = JsonToTtl.jsonToTtl(json);
    Assert.assertEquals(expectedResult,result);
  }
}
