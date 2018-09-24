/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.lespaul361.maven.plugins.javadocfiller;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.lang.model.util.Elements;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.xalan.xsltc.dom.SAXImpl.NamespaceWildcardIterator;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author David Hamilton
 */
public abstract class AbstractJavadocFillInMojo extends AbstractMojo {

	protected String encoding = null;
	protected Map<String, String> fillersMap = new HashMap<>();
	protected Map<String, String> exceptionsMap = new HashMap<>();
	protected List<File> javaFiles = null;
	protected String jdkVersion = "";

	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Parameter
	private List<Filler> fillers;

	@Parameter
	private List<Filler> addFillersToConfig;

	@Parameter
	private List<com.github.lespaul361.maven.plugins.javadocfiller.Exception> addExceptionsConfig;

	@Parameter
	private List<com.github.lespaul361.maven.plugins.javadocfiller.Exception> exceptions;

	@Parameter(defaultValue = "")
	private String configurationFile;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Properties properties = project.getProperties();
		encoding = properties.getProperty("project.build.sourceEncoding", "");
		jdkVersion = properties.getProperty("maven.compiler.target", "");
		encoding = encoding.isEmpty() ? "UTF-8" : encoding;
		getLog().info("Encoding: " + encoding);
		if (configurationFile != null && !configurationFile.trim().isEmpty()) {
			File configFile = new File(configurationFile);
			getLog().info("Loaded " + configurationFile);
			loadExceptionsFromConfigFile(configFile);
			getLog().info("Loaded Exceptions From file");
			loadFillersFromConfigFile(configFile);
			getLog().info("Loaded Variables From file");
		}
		loadFillersFromPOM(fillers);
		getLog().info("Loaded Variables From POM");
		loadExceptionsFromPOM(exceptions);
		getLog().info("Loaded Exceptions From POM");
		exceptionsMap.keySet().forEach(key -> getLog().info(key + " = " + exceptionsMap.get(key)));
		this.javaFiles = getFiles();
		getLog().info("Total Java Files: " + this.javaFiles.size());
		doExecute();
	}

	/**
	 * Loads in the information from the configuration file if provided
	 *
	 * @param file
	 *            the location of the configuration file
	 */
	protected void loadFillersFromConfigFile(File file) {
		if (!file.exists()) {
			getLog().warn(String.format("Configuation file was not found at %s", file.getAbsolutePath()));
			return;
		}

		try {
			SAXBuilder sax = new SAXBuilder();
			Document doc = sax.build(file);
			Element root = doc.getRootElement();
			Element element = root.getChild("fillers");
			if (element == null) {
				return;
			}
			List<Element> elements = element.getChildren();

			elements.forEach(el -> {
				if (el.getChildText("variable") != null && !el.getChildText("variable").isEmpty()) {
					fillersMap.put(el.getChildText("variable"), el.getChildText("text"));
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
	 * @param file
	 *            the location of the configuration file
	 */
	protected void loadExceptionsFromConfigFile(File file) {
		if (!file.exists()) {
			getLog().warn(String.format("Configuation file was not found at %s", file.getAbsolutePath()));
			return;
		}

		try {
			SAXBuilder sax = new SAXBuilder();
			Document doc = sax.build(file);
			Element root = doc.getRootElement();
			Element element = root.getChild("exceptions");
			List<Element> elements = element.getChildren();
			elements.forEach(el -> {
				if (el.getChildText("name") != null && !el.getChildText("name").isEmpty()) {
					exceptionsMap.put(el.getChildText("name"), el.getChildText("text"));
				}
			});
			getLog().info(String.valueOf(exceptionsMap.size()));
			sax = null;
		} catch (java.lang.Exception e) {
			e.printStackTrace(System.err);
		}

	}

	/**
	 * Loads in the fillers from the list made in the POM
	 *
	 * @param fillers
	 *            the list of fillers made in the POM
	 */
	protected void loadFillersFromPOM(List<Filler> fillers) {
		if (fillers == null) {
			return;
		}
		fillers.forEach(filler -> {
			if (filler.variable != null && !filler.variable.isEmpty()) {
				fillersMap.put(filler.variable, filler.text);
			}
		});

	}

	/**
	 * Loads in the fillers from the list made in the POM
	 *
	 * @param exceptions
	 *            the list of exceptions made in the POM
	 */
	protected void loadExceptionsFromPOM(List<com.github.lespaul361.maven.plugins.javadocfiller.Exception> exceptions) {
		if (exceptions == null) {
			return;
		}
		exceptions.forEach(exception -> {
			if (exception.name != null && !exception.name.isEmpty()) {
				exceptionsMap.put(exception.name, exception.description);
			}
		});
		System.out.println(exceptionsMap.size());
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
		getLog().info(ROLE);
		while ((curFile = allFiles.poll()) != null) {

			if (curFile.isDirectory()) {
				files.addAll(recurseDirectories(curFile));
			} else {
				files.add(curFile);
			}
		}
		files = files.stream().filter(file -> file.getAbsolutePath().toLowerCase().endsWith(".java"))
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

	protected void addVariablesToConfig(File file) {
		if (addFillersToConfig == null || addFillersToConfig.isEmpty()) {
			return;
		}

		addFillersToConfig.sort((c1, c2) -> {
			return c1.variable.compareTo(c2.variable);
		});

		try {
			SAXBuilder sax = new SAXBuilder();
			Document doc = sax.build(file);
			Element root = doc.getRootElement();
			Element element = root.getChild("fillers");
			if (element == null) {
				element = new Element("fillers");
			}
			boolean updateFile = false;
			for (Filler filler : addFillersToConfig) {
				if (!(isFillExistsInList(element, "variable", filler.variable))) {
					Element eleVar = new Element("variable");
					eleVar.setText(filler.variable);
					Element eleText = new Element("text");
					eleText.setText(filler.text);
					Element f = new Element("filler");
					f.addContent(eleVar);
					f.addContent(eleText);
					element.addContent(f);
					updateFile = true;
				}
			}
			if (updateFile) {
				XMLOutputter outputter = new XMLOutputter();
				outputter.setFormat(Format.getPrettyFormat());
				outputter.output(doc, new FileWriter(configurationFile));
			}
			sax = null;
		} catch (java.lang.Exception e) {
			e.printStackTrace(System.err);
		}
	}

	protected void addExceptionsToConfig(File file) {
		if (addExceptionsConfig == null || addExceptionsConfig.isEmpty()) {
			return;
		}

		addExceptionsConfig.sort((c1, c2) -> {
			return c1.name.compareTo(c2.name);
		});

		try {
			SAXBuilder sax = new SAXBuilder();
			Document doc = sax.build(file);
			Element root = doc.getRootElement();
			Element element = root.getChild("exceptions");
			if (element == null) {
				element = new Element("exceptions");
			}
			boolean updateFile = false;
			for (Exception ex : addExceptionsConfig) {
				if (!(isFillExistsInList(element, "name", ex.name))) {
					Element eleVar = new Element("name");
					eleVar.setText(ex.name);
					Element eleText = new Element("text");
					eleText.setText(ex.description);
					Element f = new Element("exception");
					f.addContent(eleVar);
					f.addContent(eleText);
					element.addContent(f);
					updateFile = true;
				}
			}
			if (updateFile) {
				XMLOutputter outputter = new XMLOutputter();
				outputter.setFormat(Format.getPrettyFormat());
				outputter.output(doc, new FileWriter(configurationFile));
			}
			sax = null;
		} catch (java.lang.Exception e) {
			e.printStackTrace(System.err);
		}
	}

	private boolean isFillExistsInList(Element element, String elementName, String varName) {
		List<Element> children = element.getChildren();
		String eleText = null;
		if (children.size() == 0) {
			return false;
		}
		if (children.size() == 1) {
			eleText = children.get(0).getChildText(elementName);
			return eleText.compareTo(varName) == 0;

		}

		short top = (short) children.size();
		short bottom = 0;
		short index = (short) (((top - bottom) / 2) + bottom);
		short response = 0;

		while (index != 0) {
			eleText = children.get(0).getChildText(elementName);
			response = (short) eleText.compareTo(varName);
			if (response == 0) {
				return true;
			} else if (response < 0) {
				bottom = index;
				index += (short) ((top - bottom) / 2);
			} else {
				top = index;
				index -= (short) ((top - bottom) / 2);
			}
			if ((top - bottom) == 1) {
				index = (short) (((top - bottom) / 2) + bottom);
				eleText = children.get(index).getChildText(elementName);
				return eleText.compareTo(varName) == 0;
			}
		}
		return false;
	}
}
