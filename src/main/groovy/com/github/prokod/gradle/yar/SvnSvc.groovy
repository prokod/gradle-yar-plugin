package com.github.prokod.gradle.yar

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.tmatesoft.svn.core.SVNDirEntry
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl
import org.tmatesoft.svn.core.io.SVNRepository
import org.tmatesoft.svn.core.io.SVNRepositoryFactory
import org.tmatesoft.svn.core.wc.*

import java.util.regex.Pattern

class SvnSvc extends ScmSvc {

    private Pattern releaseTagPattern
    private def SVNStatus svnStatus
    private def svnClientManager
    private SVNRepository svnRepo

    protected SvnSvc() {
        super(ProjectBuilder.builder().withName('parent').withProjectDir(new File(System.getProperty('java.io.tmpdir'))).build())
    }

    SvnSvc(Project project) {
        super(project)

        project.logger.info("Creating SvnService for $project")

        // Don't let svnkit try to upgrade the working copy version unless it tries to create a tag
        System.setProperty("svnkit.upgradeWC", "false");

        // do some basic setup
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
        DAVRepositoryFactory.setup();

        //set an auth manager which will provide user credentials
        project.logger.info("SvnService authentication manager setup")
        def ISVNAuthenticationManager authManager
        if (scmCreditenialsProvided()) {
            project.logger.lifecycle("Release plugin is using subversion scm with provided authentication details (user "+project.extensions.release.username+")")
            authManager= SVNWCUtil.createDefaultAuthenticationManager(project.extensions.release.username, project.extensions.release.password);
        } else {
            project.logger.lifecycle("Release plugin is using subversion scm with authentication details from svn config")
            authManager= SVNWCUtil.createDefaultAuthenticationManager();
        }

        // svn client manager has to be defined with authManager, otherwise it will use the default one (important for the performTagging)
        project.logger.info("Creating SvnClient")
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        svnClientManager = SVNClientManager.newInstance(options, authManager);

        project.logger.info("Getting working copy status")
        svnStatus = svnClientManager.getStatusClient().doStatus(project.projectDir,false)
        project.logger.info("Got svn version: "+getScmVersion())

        //not sure if the code below is required
        def svnRepo = SVNRepositoryFactory.create(svnStatus.getURL())
        svnRepo.setAuthenticationManager(authManager);
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
        return getScmRemoteUrl().getPath().contains("/tags/")
    }

    @Override
    def String getBranchName() {
        List splitPath = Arrays.asList(getScmRemoteUrl().getPath().split("/"))

        // if svn URL contains "/tags/", then find the name of the branch
        if (isOnTag()) {
            assert(splitPath.indexOf("tags") > 0)
            assert(splitPath.indexOf("tags") < splitPath.size()-1)
            return splitPath.get(splitPath.size()-1)
        }

        // if svn URL contains "/branches/", then find the name of the branch
        if (onBranch()) {
            assert(splitPath.indexOf("branches") > 0)
            assert(splitPath.indexOf("branches") < splitPath.size()-1)
            return splitPath.get(splitPath.size()-1)
        }

        return "trunk"
    }

    @Override
    def String getScmVersion() {
        return svnStatus.getRevision().getNumber().toString()
    }

    @Override
    def String getLatestReleaseTag(String currentBranch) {
        def entries = svnRepo.getDir( "tags", -1 , null , (Collection) null );
        SVNDirEntry max = entries.max{it2->
            def matcher = releaseTagPattern.matcher(it2.name);
            if (matcher.matches() && branchName.equals(matcher.group(1))) {
                return Integer.valueOf(matcher.group(2))
            } else {
                return null
            }
        }
        return max.name
    }

    @Override
    def boolean localIsAheadOfRemote() {
        return false
    }

    @Override
    def boolean hasLocalModifications() {
        def repoStatus = new SvnStatusHandler()
        svnClientManager.getStatusClient().doStatus(project.rootDir, true, false, false, false, repoStatus)
        return repoStatus.hasModifications()
    }

    @Override
    def boolean remoteIsAheadOfLocal() {
        return (getScmVersion() != svnRepo.getLatestRevision())
    }

    @Override
    def performTagging(String tag, String message) {
        project.logger.info("$project, Tagging release: $tag")

        def tagsURL = createTagsUrl(tag)

        //javadoc helper :
        //doCopy(SVNCopySource[] sources, SVNURL dst,
        //                          boolean isMove, boolean makeParents, boolean failWhenDstExists,
        //                          java.lang.String commitMessage, SVNProperties revisionProperties)
        svnClientManager.getCopyClient().doCopy(
                [new SVNCopySource(SVNRevision.HEAD, SVNRevision.HEAD, svnStatus.URL)] as SVNCopySource[],
                tagsURL,
                false,
                true,
                true,
                message,
                null)
    }

    def boolean scmCreditenialsProvided() {
        def boolean userDefined = project.extensions.release.username != null && project.extensions.release.username.length() > 0
        def boolean passwordDefined = project.extensions.release.password != null && project.extensions.release.password.length() > 0
        return userDefined && passwordDefined
    }

    def SVNURL getScmRemoteUrl() {
        return svnStatus.getURL()
    }

    // calculates the svn root URL as string
    def String getSvnRootURL() {
        String svnRootUrlString = getScmRemoteUrl().toString()

        // if svn URL contains "/tags/", then find the name of the branch
        if (svnRootUrlString.contains("/tags/")) {
            return svnRootUrlString.substring(0, svnRootUrlString.indexOf("/tags/"))
        }

        if (svnRootUrlString.contains("/branches/")) {
            return svnRootUrlString.substring(0, svnRootUrlString.indexOf("/branches/"))
        }
        
        return svnRootUrlString.substring(0, svnRootUrlString.indexOf("/trunk"))
    }

    def boolean onBranch() {
        return getScmRemoteUrl().getPath().contains("/branches/")
    }

    /**
    * creates a url for the new tag
    */
    def SVNURL createTagsUrl(String tag) {
        if (project != null) {
            project.logger.info("${project.name}, Crafting new tag: $tag")
        }
        // root url to use for tagging
        def SVNURL rootURL = SVNURL.parseURIEncoded(getSvnRootURL())

        def SVNURL tagsURL = rootURL.appendPath("tags",false)

        // need to preseve the path elements after 'branches'/'tags'/'trunk' and the branch name
        String urlTail = getScmRemoteUrl().toString().replace(getSvnRootURL(),"").replace("branches", "").replace("tags", "").replace(getBranchName(), "")

        List splitUrlTail = Arrays.asList(urlTail.split("/"))
        for (String pathElement : splitUrlTail) {
            if (pathElement.length() > 0) {
                tagsURL = tagsURL.appendPath(pathElement, false);
            }
        }
        tagsURL = tagsURL.appendPath(tag,false)

        return tagsURL
    }

    class SvnStatusHandler implements ISVNStatusHandler {
        boolean hasModifications = false;

        public boolean hasModifications() {
            return hasModifications
        }

        public void handleStatus(SVNStatus status) throws SVNException {
            SVNStatusType statusType = status.getContentsStatus()
            if (statusType != SVNStatusType.STATUS_NONE && statusType != SVNStatusType.STATUS_NORMAL && statusType != SVNStatusType.STATUS_IGNORED) {
                hasModifications = true
            }
        }
    }
}