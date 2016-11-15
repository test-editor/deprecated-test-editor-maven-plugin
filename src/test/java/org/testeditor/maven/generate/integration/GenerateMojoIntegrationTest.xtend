package org.testeditor.maven.generate.integration

import org.junit.Test
import static org.junit.Assert.*

class GenerateMojoIntegrationTest extends AbstractMavenIntegrationTest {

	@Test
	def void compilesSimpleTestCase() throws Exception {
		// given
		write("pom.xml", generatePom)
		write("src/test/java/com/example/Example.tcl", '''
			package com.example
			
			# Example
		''')

		// when
		runMavenBuild("clean", "verify")

		// then
		val compiledJava = read("src-gen/test/java/com/example/Example.java")
		assertTrue(compiledJava.contains("public class Example {"))
	}

	private def String generatePom() '''
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
						<configuration></configuration>
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
