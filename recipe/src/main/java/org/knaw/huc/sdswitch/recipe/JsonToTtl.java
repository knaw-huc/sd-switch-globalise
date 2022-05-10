package org.knaw.huc.sdswitch.recipe;

import mjson.Json;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonToTtl {
    private static final Pattern VAR_PATTERN = Pattern.compile("\\{(.*?)}");

    private String rdfSubject;
    private String rdfType;
    private final Map<String, String[]> predicates = new HashMap<>();

    public JsonToTtl(Document document) {
        readSchema(document);
    }

    public String getRdfSubject() {
        return rdfSubject;
    }

    public String getRdfType() {
        return rdfType;
    }

    public String toTtl(String json) {
        Json jsonObject = Json.read(json);
        String rdfSubject = this.rdfSubject;
        Matcher m = VAR_PATTERN.matcher(rdfSubject);
        while (m.find()) {
            String identifier = "" + jsonObject.at(m.group(1)).getValue();
            rdfSubject = rdfSubject.replace("{" + m.group(1) + "}", identifier);
            // String s = m.group(1);
            // s now contains "BAR"
        }
        String id = "" + jsonObject.at("id").getValue();
        rdfSubject = rdfSubject.replace("{id}", id);
        StringBuilder ttl = new StringBuilder("<" + rdfSubject + "> a <" + rdfType + ">");
        for (Map.Entry<String, String[]> entry : predicates.entrySet()) {
            // ook hier {identifiers} vervangen
            String key = entry.getKey();
            String name = entry.getValue()[0];
            String node = entry.getValue()[1];
            if (jsonObject.at(name) != null && !jsonObject.at(name).isNull()) {
                String value = "" + jsonObject.at(name).getValue();
                String temp = "";
                m = VAR_PATTERN.matcher(node);
                if (node.contains("{.}")) {
                    temp += "<" + node.replace("{.}", value) + ">";
                    value = "";
                }
                while (m.find()) {
                    try {
                        String identifier = "" + jsonObject.at(m.group(1)).getValue();
                        key = node.replace("{" + m.group(1) + "}", identifier) + "/" + value;
                    } catch (NullPointerException ignored) {
                    }
                }
                if (!value.isBlank()) {
                    value = "\"" + value + "\"";
                }
                ttl.append(";\n  <").append(key).append("> ").append(temp).append(value);
            }
        }
        ttl.append(".");
        return ttl.toString();
    }

    private void readSchema(Document doc) {
        NodeList list = doc.getElementsByTagName("persoon");
        Node node = list.item(0);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            rdfSubject = element.getAttribute("rdf:subject");
            rdfType = element.getAttribute("rdf:type");
            NodeList children = node.getChildNodes();
            for (int temp = 0; temp < children.getLength(); temp++) {
                if (children.item(temp).getNodeType() == Node.ELEMENT_NODE) {
                    Element child = (Element) children.item(temp);
                    String nodePredicate = child.getAttribute("rdf:predicate");
                    String nodeObject = child.getAttribute("rdf:object");
                    if (!nodePredicate.isBlank()) {
                        String nodeName = child.getNodeName();
                        String[] temp_2 = {nodeName, nodeObject};
                        predicates.put(nodePredicate, temp_2);
                    }
                }
            }
        }
    }
}
