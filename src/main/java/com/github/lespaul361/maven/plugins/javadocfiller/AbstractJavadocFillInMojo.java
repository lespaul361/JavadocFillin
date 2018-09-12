/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lespaul361.maven.plugins.javadocfiller;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author David Hamilton
 */
public abstract class AbstractJavadocFillInMojo
        extends AbstractMojo {

    protected String encoding = null;
    protected Map<String, String> fillersMap = new HashMap<>();
    protected Map<String, String> exceptionsMap = new HashMap<>();
    protected List<File> javaFiles = null;

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Parameter
    private List<Filler> fillers;

    @Parameter
    private List<com.github.lespaul361.maven.plugins.javadocfiller.Exception> exceptions;

    @Parameter(defaultValue = "")
    private String configurationFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Properties properties = project.getProperties();
        encoding = properties.getProperty("project.build.sourceEncoding", "");
        encoding = encoding.isEmpty() ? "UTF-8" : encoding;
        if (configurationFile != null && !configurationFile.trim().isEmpty()) {
            File configFile = new File(configurationFile);
            loadExceptionsFromConfigFile(configFile);
            loadFillersFromConfigFile(configFile);
        }
        loadFillersFromPOM(fillers);
        loadExceptionsFromPOM(exceptions);
        this.javaFiles = getFiles();
        doExecute();
    }

    /**
     * Loads in the information from the configuration file if provided
     *
     * @param file the location of the configuration file
     */
    protected void loadFillersFromConfigFile(File file) {
        if (!file.exists()) {
            getLog().warn(String.format("Configuation file was not found at %s",
                    file.getAbsolutePath()));
            return;
        }

        try {
            SAXBuilder sax = new SAXBuilder();
            Document doc = sax.build(file);
            Element root = doc.getRootElement();
            Element element = root.getChild("fillers");
            List<Element> elements = element.getChildren();
            elements.forEach(el -> {
                if (el.getChildText("variable") != null
                        && !el.getChildText("variable").isEmpty()) {
                    fillersMap.put(el.getChildText("variable"),
                            el.getChildText("text"));
                }
            });

            sax = null;
        } catch (java.lang.Exception e) {
            e.printStackTrace(System.err);
        }

    }

    /**
     * Loads in the information from the configuration file if provided
     *
     * @param file the location of the configuration file
     */
    protected void loadExceptionsFromConfigFile(File file) {
        if (!file.exists()) {
            getLog().warn(String.format("Configuation file was not found at %s",
                    file.getAbsolutePath()));
            return;
        }

        try {
            SAXBuilder sax = new SAXBuilder();
            Document doc = sax.build(file);
            Element root = doc.getRootElement();
            Element element = root.getChild("fillers");
            List<Element> elements = element.getChildren();
            elements.forEach(el -> {
                if (el.getChildText("name") != null
                        && !el.getChildText("name").isEmpty()) {
                    exceptionsMap.put(el.getChildText("name"),
                            el.getChildText("description"));
                }
            });
            sax = null;
        } catch (java.lang.Exception e) {
            e.printStackTrace(System.err);
        }

    }

    /**
     * Loads in the fillers from the list made in the POM
     *
     * @param fillers the list of fillers made in the POM
     */
    protected void loadFillersFromPOM(List<Filler> fillers) {
        fillers.forEach(filler -> {
            if (filler.variable != null && !filler.variable.isEmpty()) {
                fillersMap.put(filler.variable, filler.text);
            }
        });

    }

    /**
     * Loads in the fillers from the list made in the POM
     *
     * @param exceptions the list of exceptions made in the POM
     */
    protected void loadExceptionsFromPOM(List<com.github.lespaul361.maven.plugins.javadocfiller.Exception> exceptions) {
        exceptions.forEach(exception -> {
            if (exception.name != null && !exception.name.isEmpty()) {
                fillersMap.put(exception.name, exception.description);
            }
        });
    }

    /**
     * Finds all the java files in the src\main directory
     *
     * @return a list of files
     */
    protected List<File> getFiles() {
        File baseDir = new File(project.getBasedir().getAbsolutePath() + "\\src\\main");
        ArrayDeque<File> allFiles = new ArrayDeque<>();
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
        files = files.stream()
                .filter(file -> file.getAbsolutePath().toLowerCase().endsWith(".java"))
                .collect(Collectors.toList());
        return files;
    }

    private List<File> recurseDirectories(File dir) {
        ArrayDeque<File> allFiles = new ArrayDeque<File>();
        allFiles.addAll(Arrays.asList(dir.listFiles()));
        List<File> files = new ArrayList<>();
        File curFile = null;
        while ((curFile = allFiles.poll()) != null) {
            if (curFile.isDirectory()) {
                files.addAll(recurseDirectories(curFile));
            } else {
                files.add(curFile);
            }
        }
        return files;
    }

    /**
     * Executes the code
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    public abstract void doExecute() throws MojoExecutionException, MojoFailureException;
}
