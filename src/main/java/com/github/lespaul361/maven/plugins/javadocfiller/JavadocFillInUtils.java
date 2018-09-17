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

    public static synchronized String replaceVariables(String javadocComment,
            Map<String, String> variables) {
        if (javadocComment == null) {
            throw new NullPointerException("comment is null");
        }
        if (!javadocComment.contains(THROWS_TAG)
                || variables == null
                || variables.size() == 0) {
            return javadocComment;
        }
        Iterator<String> iterator = variables.keySet().iterator();
        String var = null;
        while (iterator.hasNext()) {
            var = iterator.next();
            javadocComment = javadocComment.replace(var, variables.get(var));
        }
        return javadocComment;
    }

    public static synchronized String addGenericDescriptionForException(
            String javadocComment, Map<String, String> exs) {
        if (javadocComment == null) {
            throw new NullPointerException("comment is null");
        }
        if (!javadocComment.contains(THROWS_TAG)
                || exs == null
                || exs.size() == 0) {
            return javadocComment;
        }

        String[] throwSplits = javadocComment.split("(\\bthrows\\b)");
        StringBuilder sbNewComment = new StringBuilder(200);
        sbNewComment.append(throwSplits[0]);
        String curThrowName = null;
        for (int i = 1; i < throwSplits.length; i++) {
            char sp = sbNewComment.charAt(sbNewComment.length() - 1);
            if (Character.compare(sp, " ".charAt(0)) == 0) {
                sbNewComment.replace(sbNewComment.length() - 2, sbNewComment.length() - 1, "");
            }
            String[] lines = throwSplits[i].split(EOL);
            String[] lineParts = lines[0].trim().split("\\s+");
            curThrowName = lineParts[0];
            sbNewComment.append(THROWS_TAG);
            if (lineParts.length == 1) {
                if (lines.length > 1) {
                    String[] nextLineParts = lines[1].trim().split("\\s+");
                    if (nextLineParts[0].trim().equals(END_JAVADOC.trim())
                            || nextLineParts[1].equals(SEPARATOR_JAVADOC.trim())
                            || nextLineParts[1].equals(START_OF_TAG.trim())) {
                        String desc = null;
                        if (curThrowName.contains(".")) {
                            String name = curThrowName.substring(curThrowName.lastIndexOf("."));
                            desc = exs.get(name);
                        } else {
                            desc = exs.get(curThrowName);
                        }

                        if (sbNewComment.charAt(sbNewComment.length() - 1) != " ".charAt(0)) {
                            sbNewComment.append(" ");
                        }
                        if (desc != null) {
                            sbNewComment.append(curThrowName)
                                    .append(" ")
                                    .append(desc)
                                    .append(EOL);
                            for (int ii = 1; ii < lines.length; ii++) {
                                sbNewComment.append(lines[ii]);
                            }
                        } else {
                            sbNewComment.append(curThrowName)
                                    .append(EOL);
                            if (lines.length > 0) {
                                for (int ii = 1; ii < lines.length; ii++) {
                                    sbNewComment.append(EOL).append(lines[ii]);
                                }
                            }
                        }
                    } else {
                        sbNewComment.append(throwSplits[i]).append(EOL);
                    }
                }
            } else {
                StringBuilder sbTemp = new StringBuilder(200);
                for (int j = 0; j < lineParts.length; j++) {
                    if (lineParts[j] != null && !lineParts[j].trim().isEmpty()) {
                        sbTemp.append(lineParts[j]);
                        if (j < lineParts.length - 1) {
                            sbTemp.append(" ");
                        }
                    }
                }
                if (sbTemp.length() > 0) {
                    sbNewComment.append(sbTemp);
                }
                if (lineParts.length > 0) {
                    for (int ii = 1; ii < lines.length; ii++) {
                        sbNewComment.append(lines[ii]);
                    }
                }
            }

        }

        return sbNewComment.toString();
    }
}
