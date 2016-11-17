package org.testeditor.maven.generate.integration

import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

class GenerateMojoIntegrationTest extends AbstractMavenIntegrationTest {

	@Before
	def void setupBuild() {
		// given
		write("pom.xml", generatePom('''
			<testEditorVersion>1.1.0</testEditorVersion>
		'''))
		write("src/test/java/com/example/ExampleTest.tcl", '''
			package com.example
			
			# ExampleTest
		''')
	}

	@Test
	def void generatesJavaCode() {
		// when
		val result = executeMojo("generate")

		// then
		assertFalse(result.hasExceptions)
		read("src-gen/test/java/com/example/ExampleTest.java") => [
			assertTrue(contains("public class ExampleTest {"))
		]
	}

	@Test
	def void failsOnMissingTestEditorVersion() {
		// given
		write("pom.xml", generatePom(''))

		// when
		val result = executeMojo("generate")

		// then
		assertTrue(result.hasExceptions)
		assertNotExists("src-gen/test/java/com/example/ExampleTest.java")
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
				
				<!-- TODO remove this old repository once 1.2.0 is released and used here -->
				<pluginRepository>
					<snapshots>
						<enabled>false</enabled>
					</snapshots>
					<id>bintray-test-editor-maven-OLD</id>
					<name>bintray</name>
					<url>http://dl.bintray.com/test-editor/test-editor-maven</url>
				</pluginRepository>
			</pluginRepositories>
		
			<build>
				<plugins>
					<plugin>
						<groupId>org.testeditor</groupId>
						<artifactId>testeditor-maven-plugin</artifactId>
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
