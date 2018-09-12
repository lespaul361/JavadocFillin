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
public class ThrowsFillerCallable extends AbstractFileReaderCallable {

    final Map<String, String> exceptionMap;

    public ThrowsFillerCallable(Map<String, String> exceptionMap, File file, String encoding) {
        super(file, encoding);
        this.exceptionMap = exceptionMap;
    }

    @Override
    String processJavaDoc(String javadoc) {
        String ret = JavadocFillInUtils.addGenericDescriptionForException(
                javadoc, this.exceptionMap);
        if (!ret.equals(javadoc)) {
            this.isUpdated = true;
            return ret;
        }
        return javadoc;
    }

}
