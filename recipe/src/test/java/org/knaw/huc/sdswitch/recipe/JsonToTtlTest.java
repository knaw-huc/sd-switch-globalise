package org.knaw.huc.sdswitch.recipe;

// import org.json.JSONException;

import mjson.Json;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

public class JsonToTtlTest {

  @Test
  public void runTestJsonTtl() {
    String json = "{ \"id\":1, \"voornaam\":\"Willem Frederik\" }";
    String expectedResult = "<https://humanities.knaw.nl/raa/person/1> a <https://humanities.knaw.nl/person>;\n" +
        // "  <adellijketitel> \"http://example.com/title\";\n" +
        "  <pnv:givenName> \"Willem Frederik\".";
    // "  <doopjaar> \"http://example.com/baptismDate\".";
    String result = JsonToTtl.jsonToTtl(json);
    Assert.assertEquals(expectedResult, result);
  }

  @Test
  public void runTestSchema() {
    Document doc = JsonToTtl.readSchema();
    System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
    String result = doc.getDocumentElement().getNodeName();
    Assert.assertEquals("s:S", result);
    Assert.assertEquals("https://humanities.knaw.nl/raa/person/{id}", JsonToTtl.rdfSubject);
    Assert.assertEquals("https://humanities.knaw.nl/person", JsonToTtl.rdfType);
  }

  @Test
  public void runTestElements() {
    String json = "{ \"id\":3233, \"voornaam\":\"Dirk\", " +
        "\"adellijke_titel\":\"baron\"," +
        "\"geboortedatum\":\"1746-04-12\" }";
    String expectedResult = "<https://humanities.knaw.nl/raa/person/3233> a <https://humanities.knaw.nl/person>;\n" +
        "  <pnv:infixTitle> <https://humanities.knaw.nl/title/baron>;\n" +
        "  <pnv:givenName> \"Dirk\";\n" +
        "  <http://example.com/birthDate> <https://humanities.knaw.nl/date/1746-04-12>.";
    String result = JsonToTtl.jsonToTtl(json);
    Assert.assertEquals(expectedResult, result);
  }

  @Test
  public void runTestFull() {
    String json =
        "{\"opmerkingen\":\"Adelspredicaat: 1814/adelstitel: 1819.rnHeerlijkheden: verwerving Kijfhoek 1768.\", " +
            "\"overlijdensjaar\":\"1819\",\"tussenvoegsel\":\"van\",\"geslachtsnaam\":\"Boetzelaer\"," +
            "\"periode\":\"2\",\"academische_titel\":\"mr .\",\"adelspredikaat\":null,\"adellijke_titel\":\"baron\"," +
            "\"geboortemaand\":\"4\",\"id\":3233,\"doopjaar\":0, \"geboorteplaats\":\"Leiden\"," +
            "\"overlijdensdatum\":\"1819-11-05\",\"eindcontrole\":null, \"old_idregent\":\"negentiende_eeuw_2692\"," +
            "\"old_idadellijketitel\":\"negentiende_eeuw_9\",\"adel\":1, \"overlijdensmaand\":\"11\"," +
            "\"old_idacademischetitel\":\"negentiende_eeuw_2\",\"geboortedag\":\"12\",\"searchable\":\"van " +
            "Boetzelaer\",\"geboortedatum\":\"1746-04-12\",\"overlijdensplaats\":\"'s-Gravenhage\"," +
            "\"overlijdensdag\":\"5\", \"voornaam\":\"Dirk\",\"overlijdensdatum_als_bekend\":null," +
            "\"heerlijkheid\":null,\"geboortedatum_als_bekend\":null, \"geboortejaar\":\"1746\" }";
    String expectedResult = "<https://humanities.knaw.nl/raa/person/3233> a <https://humanities.knaw.nl/person>;\n" +
        "  <http://example.com/deathPlace> <https://humanities.knaw.nl/place/'s-Gravenhage>;\n" +
        "  <pnv:infixTitle> <https://humanities.knaw.nl/title/baron>;\n" +
        "  <http://example.com/baptismDate> <https://humanities.knaw.nl/year/0>;\n" +
        "  <pnv:baseSurname> \"Boetzelaer\";\n" +
        "  <pnv:givenName> \"Dirk\";\n" +
        "  <http://example.com/birthPlace> <https://humanities.knaw.nl/place/Leiden>;\n" +
        "  <https://humanities.knaw.nl/remarks> \"Adelspredicaat: 1814/adelstitel: 1819.rnHeerlijkheden: verwerving " +
        "Kijfhoek 1768.\";\n" +
        "  <http://example.com/deathDate> <https://humanities.knaw.nl/date/1819-11-05>;\n" +
        "  <https://humanities.knaw.nl/searchable> \"van Boetzelaer\";\n" +
        "  <https://humanities.knaw.nl/heerlijkheid> \"null\";\n" +
        "  <https://humanities.knaw.nl/endcontrol> \"null\";\n" +
        "  <http://example.com/birthDate> <https://humanities.knaw.nl/date/1746-04-12>;\n" +
        "  <https://humanities.knaw.nl/period> \"2\";\n" +
        "  <pnv:surnamePrefix> \"van\";\n" +
        "  <http://example.com/title> <https://humanities.knaw.nl/title/mr .>.";
    String result = JsonToTtl.jsonToTtl(json);
    Assert.assertEquals(expectedResult, result);
  }

  @Test
  public void runTestReference() {
    String text =
        "{ \"academischetitel_id\": 1," +
            "\"academische_titel_by_academischetitel_id\" :" +
            "{ \"academischetitel_id\" : 1, \"naam\":\"mr. \"} }";
    Json jsonObject = Json.read(text);
    String reference = "academische_titel_by_academischetitel_id";
    DreamFactoryRecipe.fillReference(jsonObject, reference);
    String expectedString = "{ \"academische_titel\": \"mr. \" }";
    Json expectedResult = Json.read(expectedString);
    Assert.assertEquals(expectedResult, jsonObject);
  }
}
