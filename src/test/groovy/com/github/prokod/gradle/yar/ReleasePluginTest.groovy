package com.github.prokod.gradle.yar

import org.gradle.api.Project
import org.junit.Test
import org.junit.Before
import org.gradle.testfixtures.ProjectBuilder

class ReleasePluginTest {

	private Project project
	private Project childProject
	
	@Before
	public void setUp() {
		project = ProjectBuilder.builder().build()
		project.task("clean") // a clean task is needed for the plugin to work
		project.task("build") // also build
		
		childProject = ProjectBuilder.builder().withParent(project).build()

		project.allprojects {
			project.apply plugin: 'ya-release'
			release {
				scm = "test"
			}
			version = release.projectVersion
		}
	}

	@Test
	public void releasePluginAddsReleaseTaskToProject() {
		assert project.tasks.release != null
	}

	@Test
	public void releaseVersionIsValid() {
		assert project.version == "xyz-SNAPSHOT"
	}

	@Test
	public void childReleaseVersionIsValid() {
		assert childProject.version == "xyz-SNAPSHOT"
	}

	@Test
	public void checkScmVersion() {
		assert project.release.scmVersion == "master"
	}
}