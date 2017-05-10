package org.testeditor.maven.generate;

import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

@Mojo(name = "generate", defaultPhase = GENERATE_SOURCES, requiresDependencyResolution = COMPILE)
public class GenerateMojo extends AbstractMojo {

	@Component
	/* @VisibleForTesting */
	protected MavenProject project;

	@Component
	private MavenSession session;

	@Component
	private BuildPluginManager pluginManager;

	@Parameter(required = true)
	/* @VisibleForTesting */
	protected String testEditorVersion;

	@Parameter(defaultValue = "${project.basedir}/src-gen/test/java")
	/* @VisibleForTesting */
	protected String testEditorOutput;

	@Parameter
	/* @VisibleForTesting */
	protected String xtextVersion;

	@Parameter(defaultValue = "1.12")
	private String buildHelperMavenPluginVersion;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		configureXtextVersion();
		getLog().info("Test-Editor version: " + testEditorVersion);
		getLog().info("Xtext version: " + xtextVersion);

		generateWithXtextPlugin();
		addTestSourceWithBuildHelper();
	}

	/* @VisibleForTesting */
	protected void configureXtextVersion() {
		if (xtextVersion == null) {
			if (isVersionGreaterOrEquals_1_6_0()) {
				xtextVersion = "2.11.0";
			} else {
				xtextVersion = "2.10.0";
			}
		}
	}

	private void generateWithXtextPlugin() throws MojoExecutionException {
		executeMojo(getXtextPlugin(), goal("generate"), configuration(getConfiguration()),
				executionEnvironment(project, session, pluginManager));
	}

	/**
	 * Adds the folder configured in {@link #testEditorOutput} as a test source
	 * using the build-helper-maven-plugin.
	 */
	private void addTestSourceWithBuildHelper() throws MojoExecutionException {
		Plugin plugin = plugin(groupId("org.codehaus.mojo"), artifactId("build-helper-maven-plugin"),
				version(buildHelperMavenPluginVersion));
		Xpp3Dom configuration = configuration(element("sources", element("source", testEditorOutput)));
		executeMojo(plugin, "add-test-source", configuration, executionEnvironment(project, session, pluginManager));
	}

	// @formatter:off
	private Plugin getXtextPlugin() {
		return plugin(
			groupId("org.eclipse.xtext"),
			artifactId("xtext-maven-plugin"),
			version(xtextVersion),
			getDependencies()
		);
	}
	// @formatter:on

	/**
	 * Handles the dependencies required for the Test-Editor generation.
	 */
	/* @VisibleForTesting */
	protected List<Dependency> getDependencies() {
		List<Dependency> dependencies = new ArrayList<>();
		if (isVersionGreaterOrEquals_1_6_0()) {
			// required since 1.6.0
			dependencies.add(dependency("com.google.code.gson", "gson", "2.7.0"));
		}
		// required since 1.2.0
		dependencies.add(dependency("org.apache.commons", "commons-lang3", "3.4"));
		dependencies.add(dependency("org.gradle", "gradle-tooling-api", "2.14.1"));
		dependencies.add(dependency("org.testeditor", "org.testeditor.dsl.common.model", testEditorVersion));
		// required for all versions
		dependencies.add(dependency("org.testeditor", "org.testeditor.dsl.common", testEditorVersion));
		dependencies.add(dependency("org.testeditor", "org.testeditor.aml.model", testEditorVersion));
		dependencies.add(dependency("org.testeditor", "org.testeditor.aml.dsl", testEditorVersion));
		dependencies.add(dependency("org.testeditor", "org.testeditor.tsl.model", testEditorVersion));
		dependencies.add(dependency("org.testeditor", "org.testeditor.tsl.dsl", testEditorVersion));
		dependencies.add(dependency("org.testeditor", "org.testeditor.tcl.model", testEditorVersion));
		dependencies.add(dependency("org.testeditor", "org.testeditor.tcl.dsl", testEditorVersion));
		return dependencies;
	}

	/**
	 * Provides the {@code <configuration>} section for calling the
	 * xtext-maven-plugin. The configuration section usually looks like this:
	 * 
	 * <pre>
	 * &lt;configuration&gt;
	 *   &lt;sourceRoots&gt;
	 *     &lt;sourceRoot&gt;${project.basedir}/src/main/java&lt;/sourceRoot&gt;
	 *      ...
	 *   &lt;/sourceRoots&gt;
	 *   &lt;languages&gt;
	 *     &lt;language&gt;
	 *       &lt;setup&gt;com.example.mydsl.MyLanguageStandaloneSetup&lt;/setup&gt;
	 *     &lt;/language&gt;
	 *      ...
	 *   &lt;/languages&gt;
	 * &lt;/configuration&gt;
	 * </pre>
	 * 
	 * @return the configuration section for calling the xtext-maven-plugin
	 */
	private Element[] getConfiguration() {
		Element[] configuration = new Element[2];
		configuration[0] = getSourceRoots();
		configuration[1] = getLanguages();
		return configuration;
	}

	/* @VisibleForTesting */
	protected Element getSourceRoots() {
		ArrayList<Element> sourceRoots = new ArrayList<>();
		for (String sourceRoot : project.getCompileSourceRoots()) {
			sourceRoots.add(element("sourceRoot", sourceRoot));
		}
		for (String sourceRoot : project.getTestCompileSourceRoots()) {
			sourceRoots.add(element("sourceRoot", sourceRoot));
		}
		return element("sourceRoots", sourceRoots.toArray(new Element[0]));
	}

	// @formatter:off
	/* @VisibleForTesting */
	protected Element getLanguages() {
		ArrayList<Element> languages = new ArrayList<>();
		languages.add(element("language", element("setup", "org.testeditor.aml.dsl.AmlStandaloneSetup")));
		languages.add(element("language", element("setup", "org.testeditor.tsl.dsl.TslStandaloneSetup")));
		languages.add(element("language", element("setup", "org.testeditor.tcl.dsl.TclStandaloneSetup"), element(
				"outputConfigurations", element("outputConfiguration", element("outputDirectory", testEditorOutput)))));

		return element("languages", languages.toArray(new Element[0]));
	}
	// @formatter:on

	private boolean isVersionGreaterOrEquals_1_6_0() {
		String[] versionSplit = testEditorVersion.split("\\.");
		int major = Integer.parseInt(versionSplit[0]);
		int minor = Integer.parseInt(versionSplit[1]);
		return (major == 1 && minor >= 6) || major > 1;
	}

}
