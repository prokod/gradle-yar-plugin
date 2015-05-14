package com.github.prokod.gradle.yar

import org.tmatesoft.svn.core.SVNURL

/**
 * This is a mock service used only for testing
*/
class TestSvnSvc extends SvnSvc {

    String remoteURL

    def TestSvnSvc() {
    }

    def SVNURL getScmRemoteUrl() {
        return SVNURL.parseURIEncoded(remoteURL)
    }

    def setScmRemoteUrl(String remoteURL) {
        this.remoteURL = remoteURL
    }
}