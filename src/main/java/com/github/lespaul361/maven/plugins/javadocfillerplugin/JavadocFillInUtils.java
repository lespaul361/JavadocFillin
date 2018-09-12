/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lespaul361.maven.plugins.javadocfillerplugin;

import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author David Hamilton
 */
class JavadocFillInUtils {

    public static String replaceVariables(String javadocComment, Map<String, String> variables) {
        Iterator<String> iterator = variables.keySet().iterator();
        String var = null;
        while (iterator.hasNext()) {
            var = iterator.next();
            javadocComment = javadocComment.replace(var, variables.get(var));
        }
        return javadocComment;
    }

    public static String addGenericDescriptionForException(String javadocComment, Map<String, String> exs) {
        Iterator<String> iterator = exs.keySet().iterator();
        String[] lines = javadocComment.split(JavaDocFillInConstants.EOL);
        StringBuilder sbRet = new StringBuilder(600);
        int lineCount = lines.length;
        int curLine = 0;
        while (true) {
            String tmpLine = new String(lines[curLine]);
            if (!tmpLine.toLowerCase().contains("throws")) {
                sbRet.append(lines[curLine]).append(JavaDocFillInConstants.EOL);
                continue;
            }else{
            //TODO write code for line with throws and check to see if this or next line has info
            }
        }

        return javadocComment;
    }

}
