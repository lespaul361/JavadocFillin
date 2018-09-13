/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lespaul361.maven.plugins.javadocfiller;

import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author David Hamilton
 */
public class JavadocFillInUtils implements JavaDocFillInConstants {

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
        String[] lines = javadocComment.split(System.lineSeparator());
        StringBuilder sbRet = new StringBuilder(600);
        String exceptionName = "";
        int curLine = 0;
        boolean isUpdated = false;
        while (true) {
            if(curLine==lines.length){
                break;
            }
            String tmpLine = new String(lines[curLine]);
            if (!tmpLine.toLowerCase().contains(THROWS_TAG.toLowerCase())) {
                sbRet.append(lines[curLine]).append(EOL);
                curLine++;
                continue;
            } else {
                exceptionName = getExceptionName(tmpLine);
                if (lineHasThrowsDescription(tmpLine)) {
                    sbRet.append(tmpLine).append(EOL);
                    curLine++;
                    continue;
                } else {
                    boolean noDesc = false;
                    int tmpCount = curLine + 1;
                    while (tmpCount < lines.length) {
                        if (newTagLine(lines[tmpCount]) || endOfJavadocComment(lines[tmpCount])) {
                            noDesc = true;
                            break;
                        }
                        if (lineHasThrowsDescription(lines[tmpCount])) {
                            break;
                        }
                        tmpCount++;
                    }
                    if (noDesc) {
                        sbRet.append(getThrowComment(tmpLine, exceptionName, exs));
                        isUpdated = true;
                        curLine++;
                        continue;
                    }
                }

            }
        }
        if(isUpdated){
            return sbRet.toString();
        }
        return javadocComment;
    }

    private static boolean lineHasThrowsDescription(String line) {
        if (line.toLowerCase().contains(THROWS_TAG.toLowerCase())) {
            String tmp = line.substring(line.toLowerCase().indexOf(THROWS_TAG.toLowerCase()) + THROWS_TAG.length());
            if (tmp.trim().isEmpty()) {
                return true;
            }
        }
        return false;

    }

    private static boolean newTagLine(String line) {
        if (line.trim().startsWith(SEPARATOR_JAVADOC)) {
            String tmpString = line.substring(line.indexOf(SEPARATOR_JAVADOC) + SEPARATOR_JAVADOC.length());
            if (tmpString.trim().startsWith(START_OF_TAG)) {
                return true;
            }
        }
        return false;
    }

    private static boolean endOfJavadocComment(String line) {
        return line.trim().startsWith(END_JAVADOC);
    }

    private static String getExceptionName(String line) {
        String[] lineParts = line.split("\\s+");

        int curLinePart = 0;

        while (curLinePart < lineParts.length) {
            if (lineParts[curLinePart].toLowerCase().contains(THROWS_TAG)) {
                break;
            } else {
                curLinePart++;
            }
        }
        curLinePart++;

        return lineParts[curLinePart];
    }

    private static String getThrowComment(String line, String exceptionName, Map<String, String> map) {
        StringBuilder sb = new StringBuilder(200);
        sb.append(line.substring(0, line.toLowerCase().indexOf(THROWS_TAG.toLowerCase()) + THROWS_TAG.length()));
        sb.append(" ").append(exceptionName);
        String desc = map.get(exceptionName);
        if (desc == null) {
            sb.append(System.lineSeparator());
            return sb.toString();
        }
        sb.append(" ").append(desc).append(EOL);        
        return sb.toString();

    }
}
