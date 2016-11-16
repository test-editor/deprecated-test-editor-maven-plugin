package org.testeditor.maven.generate.integration

import java.io.File
import org.junit.Test

import static org.junit.Assert.*

class GenerateMojoIntegrationTest extends AbstractMavenIntegrationTest {

	@Test
	def void compilesAndRunsEmptyTestCase() {
		// given
		write("pom.xml", generatePom('''
			<testEditorVersion>1.1.0</testEditorVersion>
		'''))
		write("src/test/java/com/example/ExampleTest.tcl", '''
			package com.example
			
			# ExampleTest
		''')

		// when
		runMavenBuild("clean", "verify")

		// then
		read("src-gen/test/java/com/example/ExampleTest.java") => [
			assertTrue(contains("public class ExampleTest {"))
		]
		assertExists("target/test-classes/com/example/ExampleTest.class")
		read("target/surefire-reports/com.example.ExampleTest.txt") => [
			assertTrue(contains("Tests run: 1, Failures: 0, Errors: 0, Skipped: 0"))
		]
	}

	@Test
	def void failsOnMissingTestEditorVersion() {
		// given
		write("pom.xml", generatePom(''))
		write("src/test/java/com/example/ExampleTest.tcl", '''
			package com.example
			
			# ExampleTest
		''')

		// when
		runMavenBuild("clean", "verify")

		// then
		// TODO verify that the build actually failed
		val exampleTest = new File(folder.root, "src-gen/test/java/com/example/ExampleTest.java")
		assertFalse(exampleTest.exists)
	}

	private def String generatePom(CharSequence configuration) '''
		<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
			<modelVersion>4.0.0</modelVersion>
		
			<groupId>com.example</groupId>
			<artifactId>testeditor-maven-plugin-test</artifactId>
			<version>1.0-SNAPSHOT</version>
		
			<properties>
				<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
			</properties>
		
			<pluginRepositories>
				<pluginRepository>
					<snapshots>
						<enabled>false</enabled>
					</snapshots>
					<id>bintray-test-editor-maven</id>
					<name>bintray</name>
					<url>http://dl.bintray.com/test-editor/maven</url>
				</pluginRepository>
			</pluginRepositories>
		
			<build>
				<plugins>
					<plugin>
						<groupId>org.testeditor</groupId>
						<artifactId>testeditor-maven-plugin</artifactId>
						<version>1.0-SNAPSHOT</version>
						<configuration>
							«configuration»
						</configuration>
						<executions>
							<execution>
								<goals>
									<goal>generate</goal>
								</goals>
							</execution>
						</executions>
					</plugin>
				</plugins>
			</build>
		
			<dependencies>
				<dependency>
					<groupId>junit</groupId>
					<artifactId>junit</artifactId>
					<version>4.12</version>
					<!-- TODO this should be in scope test! <scope>test</scope> -->
				</dependency>
			</dependencies>
		
		</project>
	'''

}
