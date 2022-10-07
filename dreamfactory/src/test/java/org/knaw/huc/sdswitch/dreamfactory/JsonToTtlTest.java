package org.knaw.huc.sdswitch.dreamfactory;

import mjson.Json;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class JsonToTtlTest {
    private Document ttlSchema;
    private JsonToTtl toTtl;

    @Before
    public void init() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        ttlSchema = db.parse(JsonToTtlTest.class.getClassLoader().getResourceAsStream("schema_raa.xml"));
        toTtl = new JsonToTtl(ttlSchema);
    }

    //@Test
    public void runTestJsonTtl() {
        String json = "{ \"id\":1, \"voornaam\":\"Willem Frederik\" }";
        String expectedResult = "<https://humanities.knaw.nl/raa/person/1> a <https://humanities.knaw.nl/person>;\n" +
                // "  <adellijketitel> \"http://example.com/title\";\n" +
                "  <pnv:givenName> \"Willem Frederik\".";
        // "  <doopjaar> \"http://example.com/baptismDate\".";
        String result = toTtl.toTtl(json);
        Assert.assertEquals(expectedResult, result);
    }

    //@Test
    public void runTestSchema() {
        System.out.println("Root Element :" + ttlSchema.getDocumentElement().getNodeName());
        String result = ttlSchema.getDocumentElement().getNodeName();
        Assert.assertEquals("s:S", result);
        Assert.assertEquals("https://humanities.knaw.nl/raa/person/{id}", toTtl.getRdfSubject());
        Assert.assertEquals("https://humanities.knaw.nl/person", toTtl.getRdfType());
    }

    //@Test
    public void runTestElements() {
        String json = "{ \"id\":3233, \"voornaam\":\"Dirk\", " +
                "\"adellijke_titel\":\"baron\"," +
                "\"geboortedatum\":\"1746-04-12\" }";
        String expectedResult = """
                <https://humanities.knaw.nl/raa/person/3233> a <https://humanities.knaw.nl/person>;
                  <pnv:infixTitle> <https://humanities.knaw.nl/title/baron>;
                  <pnv:givenName> "Dirk";
                  <http://example.com/birthDate> <https://humanities.knaw.nl/date/1746-04-12>.""";
        String result = toTtl.toTtl(json);
        Assert.assertEquals(expectedResult, result);
    }

    //@Test
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
        String expectedResult = """
                <https://humanities.knaw.nl/raa/person/3233> a <https://humanities.knaw.nl/person>;
                  <http://example.com/deathPlace> <https://humanities.knaw.nl/place/'s-Gravenhage>;
                  <http://example.com/baptismDate> <https://humanities.knaw.nl/year/0>;
                  <pnv:infixTitle> <https://humanities.knaw.nl/title/baron>;
                  <pnv:baseSurname> "Boetzelaer";
                  <pnv:givenName> "Dirk";
                  <http://example.com/birthPlace> <https://humanities.knaw.nl/place/Leiden>;
                  <https://humanities.knaw.nl/remarks> "Adelspredicaat: 1814/adelstitel: 1819.rnHeerlijkheden: verwerving Kijfhoek 1768.";
                  <http://example.com/deathDate> <https://humanities.knaw.nl/date/1819-11-05>;
                  <https://humanities.knaw.nl/searchable> "van Boetzelaer";
                  <https://humanities.knaw.nl/heerlijkheid> "null";
                  <https://humanities.knaw.nl/endcontrol> "null";
                  <http://example.com/birthDate> <https://humanities.knaw.nl/date/1746-04-12>;
                  <https://humanities.knaw.nl/period> "2";
                  <pnv:surnamePrefix> "van";
                  <http://example.com/title> <https://humanities.knaw.nl/title/mr .>.""";
        String result = toTtl.toTtl(json);
        Assert.assertEquals(expectedResult, result);
    }

    //@Test
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
