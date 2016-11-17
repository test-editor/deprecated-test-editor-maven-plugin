package org.testeditor.maven.generate

import org.apache.maven.project.MavenProject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner
import org.twdata.maven.mojoexecutor.MojoExecutor.Element

import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner)
class GenerateMojoTest {

	GenerateMojo mojo = new GenerateMojo
	@Mock MavenProject project

	@Before
	def void setupMojo() {
		mojo.testEditorVersion = "1.1.0"
		mojo.project = project
		mojo.testEditorOutput = "my/output/dir"
	}

	@Test
	def void emptySourceRootsWhenProjectHasNone() {
		// when
		val sourceRoots = mojo.sourceRoots

		// then
		sourceRoots.assertEquals("<sourceRoots/>")
	}

	@Test
	def void configuresSourceRoots() {
		// given
		when(project.compileSourceRoots).thenReturn(#["src/main/java", "src/main/groovy"])
		when(project.testCompileSourceRoots).thenReturn(#["src/test/java", "src/it/java"])

		// when
		val sourceRoots = mojo.sourceRoots

		// then
		sourceRoots.assertEquals('''
			<sourceRoots>
			  <sourceRoot>src/main/java</sourceRoot>
			  <sourceRoot>src/main/groovy</sourceRoot>
			  <sourceRoot>src/test/java</sourceRoot>
			  <sourceRoot>src/it/java</sourceRoot>
			</sourceRoots>
		''')
	}

	@Test
	def void languageSetupFor_1_1_0() {
		// given
		mojo.testEditorVersion = "1.1.0"

		// when
		val languages = mojo.languages

		// then
		languages.assertEquals('''
			<languages>
			  <language>
			    <setup>org.testeditor.aml.dsl.AmlStandaloneSetup</setup>
			  </language>
			  <language>
			    <setup>org.testeditor.tsl.dsl.TslStandaloneSetup</setup>
			  </language>
			  <language>
			    <setup>org.testeditor.tcl.dsl.TclStandaloneSetup</setup>
			    <outputConfigurations>
			      <outputConfiguration>
			        <outputDirectory>«mojo.testEditorOutput»</outputDirectory>
			      </outputConfiguration>
			    </outputConfigurations>
			  </language>
			  <language>
			    <setup>org.testeditor.tml.dsl.TmlStandaloneSetup</setup>
			  </language>
			</languages>
		''')
	}

	@Test
	def void languageSetupFor_1_2_0() {
		// given
		mojo.testEditorVersion = "1.2.0"

		// when
		val languages = mojo.languages

		// then
		languages.assertEquals('''
			<languages>
			  <language>
			    <setup>org.testeditor.aml.dsl.AmlStandaloneSetup</setup>
			  </language>
			  <language>
			    <setup>org.testeditor.tsl.dsl.TslStandaloneSetup</setup>
			  </language>
			  <language>
			    <setup>org.testeditor.tcl.dsl.TclStandaloneSetup</setup>
			    <outputConfigurations>
			      <outputConfiguration>
			        <outputDirectory>«mojo.testEditorOutput»</outputDirectory>
			      </outputConfiguration>
			    </outputConfigurations>
			  </language>
			</languages>
		''')
	}

	private def void assertEquals(Element actual, CharSequence expected) {
		val fullXml = actual.toDom.toString
		val xml = fullXml.substring(fullXml.indexOf('\n') + 1) // remove header
		Assert.assertEquals(expected.toString.trim, xml)
	}

}
