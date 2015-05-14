package com.github.prokod.gradle.yar

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency

class YaReleasePlugin implements Plugin<Project> {
    private final String TASK_RELEASE = 'release'
    private ScmSvc scmService
    private Project project

    def void apply(Project project) {
        this.project = project
        project.logger.info("Applying ya-release to project: "+project.name)
        YaReleasePluginExtension extension = project.getExtensions().create("release", YaReleasePluginExtension.class, this);

        Task releaseTask = project.task(TASK_RELEASE) {
            doFirst {
                if (project.release.failOnSnapshotDependencies) {

                    def snapshotDependencies = [] as Set

                    project.allprojects.each { currentProject ->
                        currentProject.configurations.each { configuration ->
                            project.logger.info("Checking for snapshot dependencies in [project: $currentProject.path / configuration: $configuration.name]")
                            configuration.allDependencies.each { Dependency dependency ->
                                if (dependency.version?.contains('SNAPSHOT') && !(dependency instanceof ProjectDependency)) {
                                    snapshotDependencies.add("${dependency.group}:${dependency.name}:${dependency.version}")
                                }
                            }
                        }
                    }

                    if (!snapshotDependencies.isEmpty()) {
                        throw new GradleException("Project contains SNAPSHOT dependencies: ${snapshotDependencies}")
                    }
                }

                def taggingFormat = new TaggingFormat(extension.taggingFormat)
                getScmService().setReleaseTagPattern(taggingFormat.tagPattern)

                if (getScmService().hasLocalModifications() && !extension.getAllowLocalModifications()) {
                    throw new GradleException('Project contains non commited changes under the source tree')
                }

                if (getScmService().localIsAheadOfRemote()) {
                    throw new GradleException('Project contains changes which are not pushed to the remote repository.');
                }

                if (! getScmService().isOnTag()) {
                    def msg = "YA-Release Auto Msg: Release ${project.version} from branch ${getScmService().getBranchName()}"
                    def tag = taggingFormat.generateTag(getScmService().getBranchName(), project.version.toString())
                    if (extension.getReleaseDryRun()) {
                        project.logger.lifecycle("SCM tagging with tag ${tag} suppressed - releaseDryRun was specified.");
                    } else {
                        project.logger.lifecycle("SCM Tag added. Release plugin will create a new branch ${getScmService().getBranchName()} for project ${project.name} with tag ${tag}");
                        getScmService().performTagging(tag, msg)
                    }
                }
            }
        }

        releaseTask.description = "Release a project by setting a release version and having the underlying SCM tagged."
    }

    def String getProjectVersion() {

        if (project.hasProperty('releaseVersion')) {
            project.logger.info("Release version specified: " +project.releaseVersion+ " release task won't attempt to use scm service to auto version the project")
            return project.releaseVersion
        }

        if (getScmService().isOnTag()) {
            project.logger.info("build based on branch, using branch name as project version")
            return getScmService().getBranchName()
        }

        project.logger.info("build based on trunk, using SNAPSHOT project version")
        return getScmService().getBranchName() + "-SNAPSHOT"
    }

    def String getScmVersion() {
        return getScmService().getScmVersion()
    }

    /**
     * Instantiates the ScmService lazily
     *
     * @return ScmSvc impl.
     */
    def ScmSvc getScmService() {
        if (scmService) return scmService

        Class c
        try {
            // capitalise the first letter
            def className = project.release.scm[0].toUpperCase() + project.release.scm[1..-1] + "Svc"
            c = this.getClass().classLoader.loadClass("com.github.prokod.gradle.yar." + className)
        } catch (all) {
            throw new GradleException("SCM value '${project.release.scm}' is invalid. Supported SCM variants are 'git' or 'svn'")
        }
        scmService = c.newInstance(project)
        return scmService
    }

}