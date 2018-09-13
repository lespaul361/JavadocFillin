/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.lespaul361.javadocplugin;

import com.github.lespaul361.maven.plugins.javadocfiller.JavadocFillInUtils;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author David Hamilton
 */
public class NewClass {
    public static void main(String[] args) {
        StringBuilder sb=new StringBuilder();
        sb.append("    /**").append(System.lineSeparator());
        sb.append("     * ").append(System.lineSeparator());
        sb.append("     * @throws Exception").append(System.lineSeparator());
        sb.append("     * test").append(System.lineSeparator());
        sb.append("     */").append(System.lineSeparator());
        Map<String,String> exs=new HashMap<>();
        exs.put("Exception", "test");
        JavadocFillInUtils.addGenericDescriptionForException(sb.substring(0), exs);
    }
}
