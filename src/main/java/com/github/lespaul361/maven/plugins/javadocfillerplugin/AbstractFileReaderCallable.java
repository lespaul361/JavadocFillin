/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lespaul361.maven.plugins.javadocfillerplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

/**
 *
 * @author David Hamilton
 */
abstract class AbstractFileReaderCallable implements Callable<String>, JavaDocFillInConstants {

    private final File file;
    private final String encoding;
    protected boolean isUpdated = false;
    StringBuilder sbNewFile = new StringBuilder(500_000);

    public AbstractFileReaderCallable(File file, String encoding) {
        this.file = file;
        this.encoding = encoding;
    }

    public boolean isUpdated() {
        return this.isUpdated;
    }

    @Override
    public String call() throws java.lang.Exception {
        readFile();
        if (isUpdated) {
            return file.getAbsolutePath() + " updated";
        }
        return file.getAbsolutePath() + " done";
    }

    private void readFile() {
        try (FileInputStream fis = new FileInputStream(file)) {
            try (InputStreamReader isr = new InputStreamReader(fis)) {
                try (BufferedReader br = new BufferedReader(isr)) {
                    String line = null;
                    boolean isJavadoc = false;
                    StringBuilder commentBuilder = new StringBuilder(2_000);
                    while ((line = br.readLine()) != null) {
                        if (!isJavadoc && !line.trim().startsWith(START_JAVADOC)) {
                            sbNewFile.append(line);
                            sbNewFile.append(EOL);
                            continue;
                        } else if (!isJavadoc && line.trim().startsWith(START_JAVADOC)) {
                            isJavadoc = true;
                            commentBuilder.append(line).append(EOL);
                            continue;
                        } else if (isJavadoc && !line.trim().startsWith(END_JAVADOC)) {
                            commentBuilder.append(line).append(EOL);
                            continue;
                        } else if (isJavadoc && line.trim().startsWith(END_JAVADOC)) {
                            commentBuilder.append(line).append(EOL);
                            String processed = processJavaDoc(commentBuilder.toString());
                            if (!commentBuilder.toString().equals(processed)) {
                                isUpdated = true;
                                sbNewFile.append(processed);
                            }
                            commentBuilder = new StringBuilder(2_000);
                            isJavadoc = false;
                        }
                    }
                    br.close();
                    isr.close();
                    fis.close();

                } catch (java.lang.Exception ebr) {
                    ebr.printStackTrace(System.err);
                }
            } catch (java.lang.Exception eisr) {
                eisr.printStackTrace(System.err);
            }
        } catch (java.lang.Exception efis) {
            efis.printStackTrace(System.err);
        }
    }

    abstract String processJavaDoc(String javadoc);
}
