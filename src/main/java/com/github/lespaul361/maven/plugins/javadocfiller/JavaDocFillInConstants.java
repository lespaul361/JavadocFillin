/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lespaul361.maven.plugins.javadocfiller;

/**
 *
 * @author David Hamilton
 */
interface JavaDocFillInConstants {

    /**
     * The vm line separator
     */
    static final String EOL = System.getProperty("line.separator");

    /**
     * Tag name for &#64;throws *
     */
    static final String THROWS_TAG = "throws";

    /**
     * Start Javadoc String i.e. <code>&#47;&#42;&#42;</code> *
     */
    static final String START_JAVADOC = "/**";

    /** 
     * End Javadoc String i.e. <code>&#42;&#47;</code> *
     */
    static final String END_JAVADOC = "*/";

    /**
     * Javadoc Separator i.e. <code> &#42; </code> *
     */
    static final String SEPARATOR_JAVADOC = " * ";
    
    /**
     * Javadoc tag start symbol i.e. <code>&#64;</code>
     */
    static final String START_OF_TAG = "@";
}
