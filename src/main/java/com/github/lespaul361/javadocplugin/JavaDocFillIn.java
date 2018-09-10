/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lespaul361.javadocplugin;

import edu.emory.mathcs.backport.java.util.Arrays;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author David Hamilton
 */
@Mojo(name = "javadocfiller", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class JavaDocFillIn extends AbstractMojo {
    
    @Parameter(defaultValue = "${project}")
    private MavenProject project;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("************************************");
        getLog().info("** Filling in Javadoc **");
        getLog().info("************************************");
        File baseDir = project.getBasedir();
        ArrayDeque<File> allFiles = new ArrayDeque<File>();
        allFiles.addAll(Arrays.asList(baseDir.listFiles()));
        List<File> files = new ArrayList<>();
        File curFile = null;
        while ((curFile = allFiles.poll()) != null) {
            if (curFile.isDirectory()) {
                files.addAll(recurseDirectories(curFile));
            } else {
                files.add(curFile);
            }
        }
        files.forEach(file -> getLog().info(file.getAbsolutePath()));
    }
    
    private List<File> recurseDirectories(File dir) {
        ArrayDeque<File> allFiles = new ArrayDeque<File>();
        allFiles.addAll(Arrays.asList(dir.listFiles()));
        List<File> files = new ArrayList<>();
        File curFile = null;
        while ((curFile = allFiles.poll()) != null) {
            if (curFile.isDirectory()) {
                files.addAll(recurseDirectories(dir));
            } else {
                files.add(curFile);
            }
        }
        return files;
    }
    
}
