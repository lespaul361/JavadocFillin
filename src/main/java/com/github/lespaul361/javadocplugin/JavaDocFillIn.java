/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lespaul361.javadocplugin;

import java.util.Properties;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author David Hamilton
 */
@Mojo(name = "javadocfiller")
public class JavaDocFillIn extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException , MojoFailureException{
        getLog().info("Hello, world.");
    }
    @Parameter 
    private Properties properties;
}

