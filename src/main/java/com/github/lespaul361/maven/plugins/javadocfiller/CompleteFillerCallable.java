/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lespaul361.maven.plugins.javadocfiller;

import java.io.File;
import java.util.Map;

/**
 *
 * @author David Hamilton
 */
class CompleteFillerCallable extends AbstractFileReaderCallable {

    final Map<String, String> exceptionMap;
    final Map<String, String> variableMap;

    public CompleteFillerCallable(Map<String, String> exceptionMap, Map<String, String> variableMap, File file, String encoding) {
        super(file, encoding);
        this.exceptionMap = exceptionMap;
        this.variableMap = variableMap;
    }

    @Override
    String processJavaDoc(String javadoc) {
        String ret = JavadocFillInUtils.replaceVariables(javadoc, variableMap);
        ret = JavadocFillInUtils.addGenericDescriptionForException(ret, exceptionMap);
        if (!ret.equals(javadoc)) {
            this.isUpdated = true;
            return ret;
        }
        return javadoc;
    }

}
