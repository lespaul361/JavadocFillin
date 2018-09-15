/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lespaul361.maven.plugins.javadocfiller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 * 
 * @author David Hamilton
 */
public class NewClass {

    public static void main(String[] args) {
        Map<String, String> exceptionsMap = new HashMap<>();
        File file = new File("D:\\Java9Projects\\commons-commonRoutines\\src\\main\\resources\\JavaFillInConfig.xml");
        try {
            SAXBuilder sax = new SAXBuilder();
            Document doc = sax.build(file);
            Element root = doc.getRootElement();
            Element element = root.getChild("exceptions");
            List<Element> elements = element.getChildren();
            elements.forEach(el -> {
                if (el.getChildText("name") != null
                        && !el.getChildText("name").isEmpty()) {
                    exceptionsMap.put(el.getChildText("name"),
                            el.getChildText("text"));
                }
            });

            sax = null;
        } catch (java.lang.Exception e) {
            e.printStackTrace(System.err);
        }
        File codeFile = new File("D:\\Java9Projects\\commons-commonRoutines\\src\\"
                + "main\\java\\com\\github\\lespaul361\\commons\\commonroutines\\"
                + "utilities\\Streams\\XML\\XMLStream.java");
        try {
            CompleteFillerCallable c = new CompleteFillerCallable(exceptionsMap, null, codeFile, "UTF-8");
            String ret = c.call();
        } catch (java.lang.Exception e) {
        }

    }
}
