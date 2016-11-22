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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "testEnvDestroy")
public class VagrantDestroyMojo extends AbstractVagrantMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Destroy test environment");
		executeVagrantCommand("vagrant", "destroy", "-f");
	}

}
