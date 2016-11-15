package org.testeditor.maven.generate.integration

import com.google.common.io.Files
import java.io.File
import java.nio.charset.StandardCharsets
import org.apache.maven.cli.MavenCli
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import static org.junit.Assert.*

abstract class AbstractMavenIntegrationTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder

	def void runMavenBuild(String... goals) {
		val cli = new MavenCli
		cli.doMain(goals, folder.root.toString, System.out, System.err)
	}

	protected def void write(String filePath, CharSequence contents) {
		val file = new File(folder.root, filePath)
		file.parentFile.mkdirs
		Files.write(contents, file, StandardCharsets.UTF_8)
	}

	protected def String read(String filePath) {
		val file = new File(folder.root, filePath)
		assertTrue('''File with path='«filePath»' does not exist.''', file.exists)
		return Files.readLines(file, StandardCharsets.UTF_8).join(System.lineSeparator)
	}

}
