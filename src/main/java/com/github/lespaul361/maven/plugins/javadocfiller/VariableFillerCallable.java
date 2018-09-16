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
class VariableFillerCallable extends AbstractFileReaderCallable {

    final Map<String, String> variableMap;

    public VariableFillerCallable(Map<String, String> variableMap, File file,
            String encoding, String jdkVersion) {
        super(file, encoding, jdkVersion);
        this.variableMap = variableMap;
    }

    @Override
    String processJavaDoc(String javadoc) {
        String ret = JavadocFillInUtils.replaceVariables(javadoc, this.variableMap);
        if (!ret.equals(javadoc)) {
            this.isUpdated = true;
            return ret;
        }
        return javadoc;
    }

}
