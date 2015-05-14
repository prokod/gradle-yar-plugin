package com.github.prokod.gradle.yar

import org.gradle.api.Project

import java.util.regex.Pattern

abstract public class ScmSvc {
	protected Project project

	public ScmSvc(Project project) {
		this.project = project
	}

	def abstract Pattern getReleaseTagPattern()

	def abstract void setReleaseTagPattern(Pattern releaseTagPattern)

	/**
	 * Local git repo is on tag or not (a tag was checked out)
	 *
	 * @return true if on tag, false otherwise.
	 */
	def abstract boolean isOnTag()

	/**
	 * The name of the current branch we are on. For svn, this is just the last part of the path to the repo
	 *
	 * @return branch name
	 */
	def abstract String getBranchName()

	/**
	 * A string which represents the SCM commit we are currently on
	 *
	 * @return
	 */
	def abstract String getScmVersion()

	/**
	 * The highest release tag in the repository for this branch. For example if the current branch is "master"
	 * and we have tags called 'master-RELEASE-1' and 'master-RELEASE-2' then this will return 'master-RELEASE-2'
	 *
	 * @param currentBranch branch to look for tags
	 * @return latest tag if any. null otherwise
	 */
	def abstract String getLatestReleaseTag(String currentBranch)

	/**
	 * Return true if the local checkout has changes which aren't in the remote repository
	 *
	 * @return true if ahead. false otherwise.
	 */
	def abstract boolean localIsAheadOfRemote()

	/**
	 * Checks for local changes that are not committed
	 *
	 * @return true if there are local changes not committed. otherwise false.
	 */
	def abstract boolean hasLocalModifications()

	/**
	 * Checks if local repo is in sync with remote repository
	 * @return true if local repo does not contain all the changes from the remote repository. false otherwise.s
	 */
	def abstract boolean remoteIsAheadOfLocal()

	/**
	 * Create a tag in the remote repo.
	 *
	 * @param tag tag string to use
	 * @param message commit message
	 * @return
	 */
	def abstract performTagging(String tag, String message)

	/**
	 * Increment the last tag in the repository in order to get the next version to create if the user hasn't supplied one
	 *
	 * @return
	 */
	def getNextVersion() {
        def currentBranch = getBranchName()

        def latestReleaseTag = getLatestReleaseTag(currentBranch)

        if (latestReleaseTag) {
	        def tagNameParts = latestReleaseTag.split('-').toList()
	        def currentVersion = tagNameParts[-1]
	        return project.release.versionStrategy.call(currentVersion)

        } else {
            return project.release.startVersion.call(currentBranch)
        }
    }



}