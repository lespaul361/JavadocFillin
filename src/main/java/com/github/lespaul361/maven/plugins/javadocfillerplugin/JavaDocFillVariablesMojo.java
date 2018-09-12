/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.lespaul361.maven.plugins.javadocfillerplugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 *Fills in that variables in the files
 * @author David Hamilton
 */
@Mojo( name = "fill-variables", requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true )
@Execute( phase = LifecyclePhase.COMPILE )
public class JavaDocFillVariablesMojo extends AbstractJavadocFillInMojo{

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        
    }

}
