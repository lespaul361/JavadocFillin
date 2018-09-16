/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lespaul361.maven.plugins.javadocfiller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

/**
 *
 * @author David Hamilton
 */
abstract class AbstractFileReaderCallable implements Callable<String>, JavaDocFillInConstants {

    private final File file;
    private final String encoding;
    private final String jdkVersion;
    protected boolean isUpdated = false;
    StringBuilder sbNewFile = new StringBuilder(500_000);
    private BufferedReader br = null;
    private StringBuilder sbOldFile = new StringBuilder(500_000);

    public AbstractFileReaderCallable(File file, String encoding, String jdkVersion) {
        this.file = file;
        this.encoding = encoding;
        this.jdkVersion = jdkVersion;
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
                try {
                    br = new BufferedReader(isr);
                    String javadocComment = null;
                    while ((javadocComment = getNextComment()) != null) {
                        String processed = processJavaDoc(javadocComment);
                        if (!processed.endsWith(System.lineSeparator())) {
                            processed += System.lineSeparator();
                        }
                        if (!processed.equals(javadocComment)) {
                            isUpdated = true;
                            sbNewFile.append(processed);
                        } else {
                            sbNewFile.append(javadocComment);
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

        if (isUpdated) {
            writeFile(file, sbNewFile.toString());
        }

    }

    private String getNextComment() throws IOException {
        String curLine = null;
        StringBuilder sbComment = new StringBuilder(200);
        boolean isJavadoc = false;
        while ((curLine = br.readLine()) != null) {
            //regular line
            if (!isJavadoc && !curLine.trim().equals(START_JAVADOC)) {
                sbOldFile.append(curLine).append(EOL);
                sbNewFile.append(curLine).append(EOL);
                continue;
            }
            //start of javadoc
            if (!isJavadoc && curLine.trim().equals(START_JAVADOC)) {
                sbComment = new StringBuilder(200);
                sbOldFile.append(curLine).append(EOL);
                sbComment.append(curLine).append(EOL);
                isJavadoc = true;
                continue;
            }
            //javadoc line
            if (isJavadoc && !curLine.trim().equals(END_JAVADOC)) {
                sbOldFile.append(curLine).append(EOL);
                sbComment.append(curLine).append(EOL);
                continue;
            }
            //end of javadoc
            if (isJavadoc && curLine.trim().equals(END_JAVADOC)) {
                sbOldFile.append(curLine).append(EOL);
                sbComment.append(curLine).append(EOL);
                return sbComment.toString();
            }

        }
        return null;
    }

    private void writeFile(File file, String fileData) {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(fileData);
            fw.flush();
            fw.close();
        } catch (java.lang.Exception e) {
            e.printStackTrace(System.err);
        }
    }

    abstract String processJavaDoc(String javadoc);
}
