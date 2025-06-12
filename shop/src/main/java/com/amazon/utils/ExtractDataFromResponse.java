package com.amazon.utils;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class ExtractDataFromResponse {
    public static void extractData(String html, String nameXpath, String priceXpath) {
        try {
            HtmlCleaner cleaner = new HtmlCleaner();
            TagNode node = cleaner.clean(html);
            org.w3c.dom.Document doc = new org.htmlcleaner.DomSerializer(cleaner.getProperties(), true).createDOM(node);
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nameNodes = (NodeList) xPath.evaluate(nameXpath, doc, XPathConstants.NODESET);
            NodeList priceNodes = (NodeList) xPath.evaluate(priceXpath, doc, XPathConstants.NODESET);
            int count = Math.min(nameNodes.getLength(), priceNodes.getLength());
            for (int i = 0; i < count; i++) {
                String name = nameNodes.item(i).getTextContent().trim();
                String price = priceNodes.item(i).getTextContent().trim();
                Node nameNode = nameNodes.item(i);
                String href = "";
                if (nameNode != null && nameNode.getAttributes() != null && nameNode.getAttributes().getNamedItem("href") != null) {
                    href = nameNode.getAttributes().getNamedItem("href").getTextContent();
                }
                String fullLink = href.isEmpty() ? "" : "https://amazon.in" + href;
                System.out.println("Name: " + name);
                System.out.println("Price: " + price);
                System.out.println("Link: " + fullLink);
                System.out.println("---");
            }
            if (count == 0) {
                System.out.println("No entries found for the given XPaths.");
            }
        } catch (Exception e) {
            System.out.println("Error extracting data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
