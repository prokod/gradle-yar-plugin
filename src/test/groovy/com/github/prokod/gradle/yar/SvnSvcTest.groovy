package com.github.prokod.gradle.yar

import org.junit.Test

class SvnSvcTest {

    /**
     * test - parsing the root url with 'tags'
     */
    @Test
    public void testRootUrlOnTags() {
        TestSvnSvc service = new TestSvnSvc()
        service.setScmRemoteUrl("http://svn.prokod/test/tags/admin/10.0")
        assert service.getSvnRootURL() == "http://svn.prokod/test"

        service.setScmRemoteUrl("http://svn.prokod/test/another/tags/10.0")
        assert service.getSvnRootURL() == "http://svn.prokod/test/another"
    }

    /**
     * test - parsing the root url with 'branches'
     */
    @Test
    public void testRootUrlOnBranches() {
        TestSvnSvc service = new TestSvnSvc()
        service.setScmRemoteUrl("http://svn.prokod/test/branches/admin/stable")
        assert service.getSvnRootURL() == "http://svn.prokod/test"

        service.setScmRemoteUrl("http://svn.prokod/test/another/branches/stable")
        assert service.getSvnRootURL() == "http://svn.prokod/test/another"
    }

    /**
     * test - parsing the root url with 'trunk'
     */
    @Test
    public void testRootUrlOnTrunk() {
        TestSvnSvc service = new TestSvnSvc()
        service.setScmRemoteUrl("http://svn.prokod/test/trunk/admin")
        assert service.getSvnRootURL() == "http://svn.prokod/test"

        service.setScmRemoteUrl("http://svn.prokod/test/another/trunk")
        assert service.getSvnRootURL() == "http://svn.prokod/test/another"
    }

    /**
     * test - parsing the url with 'tags'
     */
    @Test
    public void testOnTag() {
        TestSvnSvc service = new TestSvnSvc()
        service.setScmRemoteUrl("http://svn.prokod/test/tags/admin/10.0")
        assert service.isOnTag()
        assert service.getBranchName() == "10.0"

        service.setScmRemoteUrl("http://svn.prokod/test/another/tags/10.0")
        assert service.isOnTag()
        assert service.getBranchName() == "10.0"
    }

    /**
     * test - parsing the url with 'branches'
     */
    @Test
    public void testOnBranch() {
        TestSvnSvc service = new TestSvnSvc()
        service.setScmRemoteUrl("http://svn.prokod/test/branches/admin/stable")
        assert !service.isOnTag()
        assert service.getBranchName() == "stable"

        service.setScmRemoteUrl("http://svn.prokod/test/another/branches/stable")
        assert !service.isOnTag()
        assert service.getBranchName() == "stable"
    }

    /**
     * test - parsing the url with 'trunk'
     */
    @Test
    public void testOnTrunk() {
        TestSvnSvc service = new TestSvnSvc()
        service.setScmRemoteUrl("http://svn.prokod/test/trunk/admin")
        assert !service.isOnTag()
        assert service.getBranchName() == "trunk"

        service.setScmRemoteUrl("http://svn.prokod/test/another/trunk")
        assert !service.isOnTag()
        assert service.getBranchName() == "trunk"
    }

    /**
     * test - creating tag url from url with 'trunk'
     */
    @Test
    public void testCreateTagsUrlOnTrunk() {
        TestSvnSvc service = new TestSvnSvc()
        service.setScmRemoteUrl("http://svn.prokod/test/trunk/admin")
        assert service.createTagsUrl("10.0").toDecodedString() == "http://svn.prokod/test/tags/admin/10.0"

        service.setScmRemoteUrl("http://svn.prokod/test/another/trunk")
         assert service.createTagsUrl("10.0").toDecodedString() == "http://svn.prokod/test/another/tags/10.0"
    }

    /**
     * test - creating tag url from url with 'branches'
     */
    @Test
    public void testCreateTagsUrlOnBranch() {
        TestSvnSvc service = new TestSvnSvc()
        service.setScmRemoteUrl("http://svn.prokod/test/branches/admin/stable")
        assert service.createTagsUrl("10.0").toDecodedString() == "http://svn.prokod/test/tags/admin/10.0"

        service.setScmRemoteUrl("http://svn.prokod/test/another/branches/stable")
         assert service.createTagsUrl("10.0").toDecodedString() == "http://svn.prokod/test/another/tags/10.0"
    }

    /**
     * test - creating tag url from url with 'tags'
     */
    @Test
    public void testCreateTagsUrlOnTags() {
        TestSvnSvc service = new TestSvnSvc()
        service.setScmRemoteUrl("http://svn.prokod/test/tags/admin/10.0")
        assert service.createTagsUrl("10.0.1").toDecodedString() == "http://svn.prokod/test/tags/admin/10.0.1"

        service.setScmRemoteUrl("http://svn.prokod/test/another/tags/10.0")
         assert service.createTagsUrl("10.0.1").toDecodedString() == "http://svn.prokod/test/another/tags/10.0.1"
    }
}