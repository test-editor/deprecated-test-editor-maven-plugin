/*******************************************************************************
 * Copyright (c) 2012 - 2016 Signal Iduna Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Signal Iduna Corporation - initial API and implementation
 * akquinet AG
 *******************************************************************************/
package org.testeditor.testenv.vagrant;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "testExec")
public class VagrantTestExecutionMojo extends AbstractVagrantMojo {

	@Parameter(property = "project.build.directory")
	private File outputPath;

	@Parameter(property = "projectDirInTestEnv")
	private String projectDirInTestEnv;

	@Parameter(property = "mavenReporInTestEnv")
	private String mavenReporInTestEnv;

	public void execute() throws MojoExecutionException, MojoFailureException {
		String command;
		try {
			String test = "";
			if (System.getProperties().containsKey("test")) {
				test = System.getProperty("test");
			}
			command = createExecCommand(test);
		} catch (IOException e) {
			throw new MojoExecutionException("", e);
		}
		int exitCode = executeVagrantCommand("vagrant", "ssh", "-c", command);
		if (exitCode > 0) {
			throw new MojoFailureException("Error executing tests. " + command);
		}
	}

	/**
	 * Creates an script to execute the tests in the target system and returns
	 * the command to launch it with vagrant.
	 * 
	 * @param testStructure
	 *            to be executed as test.
	 * @return string with the command used in the vagrant ssh call.
	 * @throws IOException
	 *             on failure.
	 */
	protected String createExecCommand(String testName) throws IOException {
		List<String> lines = Files.readAllLines(new File(getVagrantFileDir(), "Vagrantfile").toPath(),
				StandardCharsets.UTF_8);
		boolean isLinux = true;
		for (String string : lines) {
			if (!string.trim().startsWith("#")) {
				if (string.contains("config.vm.communicator") && string.contains("winrm")) {
					isLinux = false;
				}
			}
		}
		String executionScript = null;
		String execCommand = null;
		String execScript = null;
		if (isLinux) {
			executionScript = getExecutionScriptForLinux(testName);
			execCommand = projectDirInTestEnv + "/target/executeTest.sh";
			execScript = "executeTest.sh";
		} else {
			executionScript = getExecutionScriptForWindows(testName);
			execCommand = "/cygdrive/c/pstools/PsExec.exe -i 1 -u vagrant -p vagrant c:" + projectDirInTestEnv
					+ "/target/executeTest.bat";
			execScript = "executeTest.bat";
		}
		File launcher = new File(outputPath, execScript);
		Files.write(launcher.toPath(), executionScript.toString().getBytes());
		launcher.setExecutable(true);
		getLog().info("Created launcher " + launcher.toString());
		return execCommand;
	}

	/**
	 * Creates the content of a test execution script on windows.
	 * 
	 * @param testStructure
	 *            used for test execution.
	 * @return string with the script content.
	 */
	protected String getExecutionScriptForWindows(String testName) {
		StringBuilder sb = new StringBuilder();
		sb.append("@echo off").append("\n");
		sb.append("set PATH=%PATH%;c:/vagrant_bin/jdk/bin/").append("\n");
		sb.append("set JAVA_HOME=c:/vagrant_bin/jdk/").append("\n");
		sb.append("set MAVEN_HOME=c:/m2_repo/tools/apache-maven-3.2.1/bin/").append("\n");
		sb.append("c:/m2_repo/tools/apache-maven-3.2.1/bin/mvn.bat -f ");
		sb.append(projectDirInTestEnv).append("/pom.xml");
		sb.append(" test ");
		sb.append("-Dmaven.repo.local=").append(mavenReporInTestEnv);
		if (!testName.equals("")) {
			sb.append(" -Dtest=").append(testName);
		}
		sb.append("\n");
		return sb.toString();
	}

	/**
	 * Creates the content of a test execution script on linux.
	 * 
	 * @param testStructure
	 *            used for test execution.
	 * @param isXvfb
	 *            true when virtual framebuffer X-server is used
	 * @return string with the script content.
	 */
	protected String getExecutionScriptForLinux(String testName) {
		StringBuilder sb = new StringBuilder();
		sb.append("#!/bin/bash").append("\n");
		sb.append("sudo startx &").append("\n");
		sb.append("export DISPLAY=:0").append("\n");
		sb.append("sudo /usr/bin/mvn -f ");
		sb.append(projectDirInTestEnv).append("/pom.xml");
		sb.append(" test ");
		sb.append("-Dmaven.repo.local=").append(mavenReporInTestEnv);
		if (!testName.equals("")) {
			sb.append(" -Dtest=").append(testName);
		}
		sb.append("\n");
		sb.append("sudo pkill X").append("\n");
		return sb.toString();
	}

}
