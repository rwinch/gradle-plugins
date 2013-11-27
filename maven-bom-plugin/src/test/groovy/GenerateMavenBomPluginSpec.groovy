
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification


class GenerateMavenBomPluginSpec extends Specification {

	Project project
	Project subProject1
	Project subProject2

	def setup() {
		project = ProjectBuilder.builder().withName('spring').build()

		subProject1 = ProjectBuilder.builder().withParent(project).build()
		subProject2 = ProjectBuilder.builder().withParent(project).build()

		project.allprojects*.group = 'org.springframework'
		project.allprojects*.version = '1'
	}

	def 'Applies plugin and checks default setup'() {
		when:
			project.apply plugin: GenerateMavenBomPlugin
		then:
			Task task = project.tasks.findByName(GenerateMavenBomPlugin.MAVEN_BOM_TASK_NAME)
			task != null
			task.group == 'Generate'
			task.description == 'Generates a Maven Build of Materials (BOM)'
		    task.groupId == "org.springframework"
			task.artifactId == "spring-bom"
			task.projects == project.subprojects
			task.pomFile == project.file("${project.buildDir}/maven-bom/${task.artifactId}-${project.version}.xml")
		and:
			project.tasks.findByName('clean') != null
	}
}
