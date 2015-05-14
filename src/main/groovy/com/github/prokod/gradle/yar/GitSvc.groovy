package com.github.prokod.gradle.yar

import org.eclipse.jgit.api.Git
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.process.internal.ExecException

import java.util.regex.Pattern

class GitSvc extends ScmSvc {

    private Pattern releaseTagPattern

    def GitSvc(Project project) {
            super(project)
    }

    @Override
    Pattern getReleaseTagPattern() {
        releaseTagPattern
    }

    @Override
    void setReleaseTagPattern(Pattern releaseTagPattern) {
        this.releaseTagPattern = releaseTagPattern
    }

    @Override
    def boolean isOnTag() {
        def tagName = tagNameOnCurrentRevision()
        if (tagName == null) false
        try {
            if (releaseTagPattern.matcher(tagName).matches()) {
                return true
            }
        } catch (all) { project.logger.debug('Could not match tag name on current revision to release tag pattern.') }
        return false
    }

    @Override
    def String getBranchName() {
        if (isOnTag()) {
            return tagNameOnCurrentRevision()
        }

        def refName = gitExec(['symbolic-ref', '-q', 'HEAD']).replaceAll("\\n", "")

        if (!refName) {
            throw new GradleException('Could not determine the current branch name.');
        } else if (!refName.startsWith('refs/heads/')) {
            throw new GradleException('Checkout the branch to release from.');
        }

        def prefixLength = 'refs/heads/'.length()
        def branchName = refName[prefixLength..-1]

        return branchName.replaceAll('[^\\w\\.\\-\\_]', '_')
    }

    @Override
    String getScmVersion() {
        def git = getGitRepository()
        git?.getRepository()?.resolve("HEAD")?.name
    }

    private def getGitRepository() {
        def gitDir = new File(project.rootProject.projectDir, "/.git")
        try {
            def git = Git.open(gitDir)
            git
        } catch (IOException ioe) {
            project.logger.lifecycle("Could not open git repository under ${gitDir.getPath()} [reason: ${ioe.getMessage()}]")
            null
        }
    }

    @Override
    def String getLatestReleaseTag(String currentBranch) {
        def tagSearchPattern = "${currentBranch}-RELEASE-*"

        gitExec(['for-each-ref', '--count=1', "--sort=-taggerdate",
                 "--format=%(refname:short)", "refs/tags/${tagSearchPattern}"])
    }

    @Override
    def boolean localIsAheadOfRemote() {
        gitExec(['status']).contains('Your branch is ahead')
    }

    @Override
    def boolean hasLocalModifications() {
        gitExec(['status', '--porcelain']) == true
    }

    @Override
    def boolean remoteIsAheadOfLocal() {
        return false
        //TODO requires implementation
    }

    @Override
    def performTagging(String tag, String message) {
        try {                
            gitExec(['tag', '-a', tag, '-m', message])
            gitExec(['push', '--tags'])
        } catch (ExecException e) {
            throw new GradleException("Failed to create or push git tag ${tag}")
        }
    }

    /**
     * Get the name of the most recent tag related to the branch we are on.
     * Originally was implemented using gitExec(['describe', '--exact-match', 'HEAD']) but this command returns fatal error in some cases ...
     *
     * @return - Tag name or null otherwise ..
     */
    private String tagNameOnCurrentRevision() {
        def tag = gitExec(['name-rev', '--name-only', '--tags', 'HEAD']).replaceAll("\\n", "")
        if (tag.contains('undefined')) null
        tag
    }

    def private gitExec(List gitArgs) {
        def stdout = new ByteArrayOutputStream()

        project.exec {
            executable = 'git'
            args = gitArgs
            standardOutput = stdout
        }

        if (stdout.toByteArray().length > 0) {
            return stdout.toString()
        } else {
            return null
        }
    }
}