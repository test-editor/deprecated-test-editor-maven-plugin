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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractVagrantMojo extends AbstractMojo {

	@Parameter(property = "vagrantFileDir", defaultValue = "vagrant-vm")
	private File vagrantFileDir;

	public File getVagrantFileDir() {
		return vagrantFileDir;
	}

	protected int executeVagrantCommand(String... command) throws MojoExecutionException {
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(getVagrantFileDir());
		getLog().debug(getVagrantFileDir().toString());
		try {
			Process upPrc = builder.start();
			createAndRunLoggerOnStream(upPrc.getInputStream(), false);
			createAndRunLoggerOnStream(upPrc.getErrorStream(), false);
			while (isAlive(upPrc)) {
				Thread.sleep(100);
			}
			return upPrc.exitValue();
		} catch (IOException e) {
			getLog().error(e);
			throw new MojoExecutionException("", e);
		} catch (InterruptedException e) {
			getLog().error(e);
			throw new MojoExecutionException("", e);
		}
	}

	/**
	 * Creates and starts a runnable watching the input stream. The content of
	 * the stream is redirected to the logger.
	 * 
	 * @param inputStream
	 *            to be redirected to the logger.
	 * @param errorStream
	 *            to indicate the log level.
	 * @param monitor
	 *            to show log entries.
	 */
	protected void createAndRunLoggerOnStream(final InputStream inputStream, final boolean errorStrem) {
		new Thread(new Runnable() {
			public void run() {
				char[] cbuf = new char[8192];
				int len = -1;
				try {
					InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
					while ((len = reader.read(cbuf)) > 0) {
						String message = new String(cbuf, 0, len);
						if (errorStrem) {
							getLog().error(message);
						} else {
							getLog().info(message);
						}
					}
				} catch (IOException e) {
					getLog().debug("Error reading remote Process Stream", e);
				}
			}
		}).start();

	}

	/**
	 * Checks if a process is alive.
	 * 
	 * @param process
	 *            to be checked.
	 * @return true if process is alive otherwise false.
	 */
	protected boolean isAlive(Process process) {
		try {
			process.exitValue();
			return false;
		} catch (IllegalThreadStateException e) {
			return true;
		}
	}

}
