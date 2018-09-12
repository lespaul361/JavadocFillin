/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lespaul361.maven.plugins.javadocfillerplugin;

/**
 *
 * @author David Hamilton
 */
public class FillVariablesCallable extends AbstractFileReaderCallable {

    @Override
    String processJavaDoc(String javadoc) {
        String parts[] = javadoc.split(EOL);

    }

}
