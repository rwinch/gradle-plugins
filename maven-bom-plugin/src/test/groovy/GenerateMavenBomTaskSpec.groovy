import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 *
 * @author Rob Winch
 */
class GenerateMavenBomTaskSpec extends Specification {

	Project project
	Project subProject1
	Project subProject2

	def setup() {
		project = ProjectBuilder.builder().withName('spring').build()

		subProject1 = ProjectBuilder.builder().withName('subProject1').withParent(project).build()
		subProject2 = ProjectBuilder.builder().withName('subProject2').withParent(project).build()

		[project,subProject1,subProject2]*.group = 'org.springframework'
		[project,subProject1,subProject2]*.version = '1.0.0.BUILD-SNAPSHOT'
	}

	def 'Generates Maven Bom'() {
		setup:
			Task task = project.tasks.create(name: GenerateMavenBomPlugin.MAVEN_BOM_TASK_NAME, type: GenerateMavenBomTask) {
				artifactId = 'spring-bom'
				groupId = 'org.springframework'
				gradleProjects = project.subprojects
				pomFile = File.createTempFile("GenerateMavenBomTaskSpec-maven-pom-bom",".xml")
				customizePom = { xml ->
					xml.url("https://github.com/spring-projects/spring-framework")
					xml.organization() {
						name("SpringSource")
						url("http://spring.io/spring-framework")
					}
					xml.licenses() {
						license() {
							name("The Apache Software License, Version 2.0")
							url("http://www.apache.org/licenses/LICENSE-2.0.txt")
							distribution("repo")
						}
					}
					xml.scm() {
						url("https://github.com/spring-projects/spring-framework")
						connection("scm:git:git://github.com/spring-projects/spring-framework")
						developerConnection("scm:git:git://github.com/spring-projects/spring-framework")
					}
					xml.developers() {
						developer() {
							id("jhoeller")
							name("Juergen Hoeller")
							email("jhoeller@vmware.com")
						}
					}
					xml.issueManagement() {
						system("Jira")
						url("https://jira.springsource.org/browse/SPR")
					}
				}
			}

			XMLUnit.setIgnoreWhitespace(true)
		when:
			task.generateMavenBom()
		then:
			task.pomFile.exists()
			def xmlDiff = new Diff("""<project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd'>
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.springframework</groupId>
  <artifactId>spring-bom</artifactId>
  <version>1.0.0.BUILD-SNAPSHOT</version>
  <packaging>pom</packaging>
  <url>https://github.com/spring-projects/spring-framework</url>
  <organization>
	<url>http://spring.io/spring-framework</url>
  </organization>
  <licenses>
	<license>
	  <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
	  <distribution>repo</distribution>
	</license>
  </licenses>
  <scm>
	<url>https://github.com/spring-projects/spring-framework</url>
	<connection>scm:git:git://github.com/spring-projects/spring-framework</connection>
	<developerConnection>scm:git:git://github.com/spring-projects/spring-framework</developerConnection>
  </scm>
  <developers>
	<developer>
	  <id>jhoeller</id>
	  <email>jhoeller@vmware.com</email>
	</developer>
  </developers>
  <issueManagement>
	<system>Jira</system>
	<url>https://jira.springsource.org/browse/SPR</url>
  </issueManagement>
  <dependencyManagement>
	<dependency>
	  <groupId>org.springframework</groupId>
	  <artifactId>subProject1</artifactId>
	  <version>1.0.0.BUILD-SNAPSHOT</version>
	</dependency>
	<dependency>
	  <groupId>org.springframework</groupId>
	  <artifactId>subProject2</artifactId>
	  <version>1.0.0.BUILD-SNAPSHOT</version>
	</dependency>
  </dependencyManagement>
</project>""",
			task.pomFile.text.toString())
			xmlDiff.similar()
		cleanup:
			task.pomFile.delete()
	}
}
