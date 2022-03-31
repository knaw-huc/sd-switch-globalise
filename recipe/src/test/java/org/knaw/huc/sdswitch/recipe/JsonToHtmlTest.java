package org.knaw.huc.sdswitch.recipe;

import mjson.Json;
import org.junit.Assert;
import org.junit.Test;

public class JsonToHtmlTest {

  @Test
  public void runTestJsonHtml() {
    String json = "{ \"id\":1, \"voornaam\":\"Willem Frederik\" }";
    String expectedResult = getExpectedTestOne();
    String result = JsonToHtml.jsonToHtml(json);
    Assert.assertEquals(expectedResult, result);
  }

  private String getExpectedTestOne() {
    String expectedResult = "<!DOCTYPE HTML><html xmlns:js=\"http://www.w3.org/2005/xpath-functions\">\n" +
        "   <head>\n" +
        "      <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
        "      <title></title>\n" +
        "   </head>\n" +
        "   <style>\n" +
        "                table,\n" +
        "                td,\n" +
        "                th {\n" +
        "                border: 1px solid;\n" +
        "                border-collapse: collapse;\n" +
        "                }\n" +
        "                td {\n" +
        "                padding: 0 1ex 0 1ex;\n" +
        "                }\n" +
        "            </style>\n" +
        "   <body>\n" +
        "      <table>\n" +
        "         <tbody>\n" +
        "            <tr>\n" +
        "               <th>Veld</th>\n" +
        "               <th>Waarde</th>\n" +
        "            </tr>\n" +
        "            <tr>\n" +
        "               <td>id</td>\n" +
        "               <td>1</td>\n" +
        "            </tr>\n" +
        "            <tr>\n" +
        "               <td>voornaam</td>\n" +
        "               <td>Willem Frederik</td>\n" +
        "            </tr>\n" +
        "         </tbody>\n" +
        "      </table>\n" +
        "   </body>\n" +
        "</html>";
    return expectedResult;
  }

}
