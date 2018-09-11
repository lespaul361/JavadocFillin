/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lespaul361.javadocplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.jdom2.Element;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author David Hamilton
 */
@Mojo(name = "javadocfiller", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class JavaDocFillIn extends AbstractMojo {
    
    private String encoding = null;
    @Parameter(defaultValue = "${project}")
    private MavenProject project;
    
    @Parameter
    private List<Filler> fillers;
    
    @Parameter(defaultValue = "")
    private String configurationFile;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("************************************");
            getLog().info("** Filling in Javadoc **");
            getLog().info("************************************");
            if (configurationFile != null && !configurationFile.trim().isEmpty()) {
                loadFromConfigFile();
            }
            Properties properties = project.getProperties();
            encoding = properties.getProperty("project.build.sourceEncoding", "");
            encoding = encoding.isEmpty() ? "UTF-8" : encoding;
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
            fillJavaDoc(files);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
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
    
    private void fillJavaDoc(List<File> files) {
        class ProcessFile implements Callable {
            
            private final File file;
            private final List<Filler> fillers;
            private final String encoding;
            private boolean isUpdated = false;
            
            public ProcessFile(File file, List<Filler> fillers, String encoding) {
                this.file = file;
                this.fillers = fillers;
                this.encoding = encoding;
            }
            
            @Override
            public Object call() throws Exception {
                if (!file.getAbsolutePath().toLowerCase().endsWith(".java")) {
                    return file;
                }
                String fileData = processFile();
                if (isUpdated) {
                    writeBackToFile(fileData);
                    getLog().info("Updated " + file.getAbsolutePath());
                }
                return file;
            }
            
            private String processFile() {
                StringBuilder sbNewFile = new StringBuilder(5000);
                try (FileInputStream fis = new FileInputStream(file)) {
                    try (InputStreamReader isr = new InputStreamReader(fis)) {
                        try (BufferedReader br = new BufferedReader(isr)) {
                            String line = null;
                            boolean isJavadoc = false;
                            while ((line = br.readLine()) != null) {
                                if (!isJavadoc && !line.trim().startsWith("/**")) {
                                    sbNewFile.append(line);
                                    sbNewFile.append(System.lineSeparator());
                                    isJavadoc = true;
                                    continue;
                                }
                                if (isJavadoc) {
                                    if (line.trim().startsWith("*/")) {
                                        sbNewFile.append(processLine(line));
                                        sbNewFile.append(System.lineSeparator());
                                        isJavadoc = false;
                                        continue;
                                    }
                                    sbNewFile.append(processLine(line));
                                    sbNewFile.append(System.lineSeparator());
                                }
                            }
                            br.close();
                            isr.close();
                            fis.close();
                            
                        } catch (Exception ebr) {
                            ebr.printStackTrace(System.err);
                        }
                    } catch (Exception eisr) {
                        eisr.printStackTrace(System.err);
                    }
                } catch (Exception efis) {
                    efis.printStackTrace(System.err);
                }
                return sbNewFile.toString();
            }
            
            private String processLine(String line) {
                for (Filler filler : fillers) {
                    if (line.contains(filler.variable)) {
                        line = line.replace(filler.variable, filler.text);
                        isUpdated = true;
                    }
                }
                return line;
            }
            
            private void writeBackToFile(String data) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    try (OutputStreamWriter osw = new OutputStreamWriter(fos, encoding)) {
                        osw.append(data);
                        osw.flush();
                        osw.close();
                        fos.close();
                    } catch (Exception oswe) {
                    }
                } catch (Exception fose) {
                }
            }
        }
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<File>> futures = new ArrayList<>();
        for (File file : files) {
            final List<Filler> fs = new ArrayList<>();
            fillers.forEach(filler -> {
                try {
                    if (filler.variable != null && !filler.variable.trim().isEmpty()) {
                        Filler ret = new Filler();
                        ret.variable = new String(filler.variable);
                        ret.text = new String(filler.text);
                        fs.add(ret);
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
                
            });
            Callable c = new ProcessFile(file, fs, this.encoding);
            futures.add(executor.submit(c));
        }
        
        futures.forEach(c -> {
            try {
                getLog().info(c.get().getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            
        });
    }
    
    private void loadFromConfigFile() {
        File configFile = new File(configurationFile);
        if (!configFile.exists()) {
            return;
        }
        List<Filler> fillerList = new ArrayList<>();
        
        try {
            SAXBuilder sax = new SAXBuilder();
            Document doc = sax.build(configFile);
            Element root = doc.getRootElement();
            List<Element> elements = root.getChildren();
            elements.forEach(element -> {
                Filler filler = new Filler();
                filler.variable = element.getChildText("variable");
                filler.text = element.getChildText("text");
                fillerList.add(filler);
            });
            fillerList.addAll(fillers);
            fillers = fillerList;
        } catch (Exception e) {
        }
        
    }
}
