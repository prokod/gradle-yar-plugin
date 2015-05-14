package com.github.prokod.gradle.yar

import java.util.regex.Pattern

/**
 * This is a mock service used only for testing
*/
class TestSvc extends ScmSvc {
    Pattern releaseTagPattern = ~/^(\S+)-RELEASE-(\d+)$/

    def TestSvc(project) {
        super(project)
    }

    def boolean localIsAheadOfRemote() {
        return false
    }

    def boolean hasLocalModifications() {
        return false
    }

    def boolean remoteIsAheadOfLocal() {
        return false
    }

    def String getLatestReleaseTag(String currentBranch) {
        return "test-RELEASE-1.1"
    }

    String getScmVersion() {
        return "master"
    }

    @Override
    Pattern getReleaseTagPattern() {
        releaseTagPattern
    }

    @Override
    void setReleaseTagPattern(Pattern releaseTagPattern) {
        this.releaseTagPattern = releaseTagPattern
    }

    def boolean isOnTag() {
        return false
    }

    def String getBranchName() {
        return "xyz"
    }

    def performTagging(String tag, String message) { }
}