package org.testeditor.maven.generate.integration

import com.google.common.io.Files
import java.io.File
import java.nio.charset.StandardCharsets
import org.apache.maven.execution.MavenExecutionResult
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.testing.MojoRule
import org.eclipse.aether.DefaultRepositorySystemSession
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.repository.LocalRepository
import org.junit.Rule
import org.junit.rules.TemporaryFolder

import static org.junit.Assert.*

abstract class AbstractMavenIntegrationTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder

	@Rule
	public MojoRule mojo = new MojoRule

	def void runMavenBuild(String goal) {
		executeMojo(folder.root, goal)
	}

	private def MavenExecutionResult executeMojo(File baseDir, String goal) throws Exception {
		val project = mojo.readMavenProject(baseDir)
		val session = mojo.newMavenSession(project)
		session.fixRepositorySystemSession
		val execution = mojo.newMojoExecution(goal)
		mojo.executeMojo(session, project, execution)
		return session.result
	}

	/**
	 * {@link MojoRule} does not initialize the {@link RepositorySystemSession} properly.
	 * 
	 * @see <a href="https://wiki.eclipse.org/Aether/Creating_a_Repository_System_Session">Creating a Repository System Session</a>
	 */
	private def void fixRepositorySystemSession(MavenSession mavenSession) {
		val session = mavenSession.repositorySession as DefaultRepositorySystemSession
		val localRepo = new LocalRepository("target/local-repo")
		val system = mojo.lookup(RepositorySystem)
		session.localRepositoryManager = system.newLocalRepositoryManager(session, localRepo)
	}

	protected def void write(String filePath, CharSequence contents) {
		val file = new File(folder.root, filePath)
		file.parentFile.mkdirs
		Files.write(contents, file, StandardCharsets.UTF_8)
	}

	protected def String read(String filePath) {
		val file = assertExists(filePath)
		return Files.readLines(file, StandardCharsets.UTF_8).join(System.lineSeparator)
	}

	protected def File assertExists(String filePath) {
		val file = new File(folder.root, filePath)
		assertTrue('''File with path='«filePath»' does not exist.''', file.exists)
		return file
	}

}
