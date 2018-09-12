/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lespaul361.maven.plugins.javadocfiller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Fills in the exception descriptions in the files
 *
 * @author David Hamilton
 */
@Mojo(name = "fill-throws-description", requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
@Execute(phase = LifecyclePhase.COMPILE)
public class JavaDoxFillExceptionsMojo extends AbstractJavadocFillInMojo {

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<String>> futures = new ArrayList<>();
        for (File file : javaFiles) {
            Callable c = new ThrowsFillerCallable(exceptionsMap, file, encoding);
            futures.add(executor.submit(c));
        }
        futures.forEach(f -> {
            try {
                getLog().info(f.get());
            } catch (java.lang.Exception e) {
                e.printStackTrace(System.err);
            }

        });
    }

}
