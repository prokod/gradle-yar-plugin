package com.github.prokod.gradle.yar

class YaReleasePluginExtension {

  private boolean failOnSnapshotDependencies = true

  private versionStrategy = { currentVersion -> new BigDecimal(currentVersion).add(BigDecimal.ONE).toPlainString() }
  private startVersion = { currentBranch -> "1" }

  private final YaReleasePlugin plugin
  private String scm
  private String username
  private String password
  private boolean releaseDryRun = false
  private boolean allowLocalModifications = false
  private String taggingFormat = '#{b}-RELEASE-#{v}'

  public YaReleasePluginExtension(YaReleasePlugin plugin) {
    this.plugin = plugin
  }

  /**
   * Read only property for getting the version of this project.
   * The version is derived from the following rules:
   * 1. If the releaseVersion property was passed to gradle via the -P command line option, then use that.
   * 2. If the version control system is currently pointing to a tag, then use a version derived from the name of the tag
   * 3. Use the name of the branch (or trunk/head) as the version appended with "-SNAPSHOT"
   * @return - project version
   */
  public getProjectVersion() {
    return plugin.projectVersion
  }

  /**
   * Extension read-only property for getting the version which the source control system is pointing to.
   *
   * @return current HEAD revision.
   */
  public String getScmVersion() {
  	return plugin.getScmVersion()
  }

  /**
   * Get the value for this property as was set previously (through default/build.gradle)
   *
   * @return the value for this property
   */
  public boolean getFailOnSnapshotDependencies() {
  	return failOnSnapshotDependencies
  }

  /**
   * A configurable option which defaults to true. Will fail the release task if any dependency is
   * currently pointing to a SNAPSHOT
   *
   * @param failOnSnapshotDependencies
   * @return
   */
  public setFailOnSnapshotDependencies(boolean failOnSnapshotDependencies) {
  	this.failOnSnapshotDependencies = failOnSnapshotDependencies
  }

  /*
    Define the type of version control system in use for this project. Current valid values are:
    * svn
    * git
  */
  public setScm(String scm) {
    this.scm = scm
  }

  /*
    Get the previously set value for this property
  */
  public getScm() {
    return scm
  }

  /*
    Define the scm username
  */
  public setUsername(String username) {
    this.username = username
  }

  /*
    Get the previously set value for this property
  */
  public getUsername() {
    return username
  }

  /*
    Define the scm password
  */
  public setPassword(String password) {
    this.password = password
  }

  /*
    Get the previously set value for this property
  */
  public getPassword() {
    return password
  }

  /*
    Set simulate variable, releaseDryRun == true means no actual commit to the scm
  */
  public setReleaseDryRun(boolean releaseDryRun) {
    this.releaseDryRun=releaseDryRun
  }

  /*
    Get the previously set value for this property
  */
  public getReleaseDryRun() {
    return releaseDryRun
  }

  /*
    Set allowLocalModifications variable, allowing to skip the working copy for local changes
  */
  public setAllowLocalModifications(boolean allowLocalModifications) {
    this.allowLocalModifications=allowLocalModifications
  }

  /*
    Get the previously set value for this property
  */
  public getAllowLocalModifications() {
    return allowLocalModifications
  }

  /**
   * Get the format string that will be used for tagging SCM
   * #{b} - current branch name
   * #{v} - project version
   */
  public getTaggingFormat() {
    return taggingFormat
  }

  /**
   * Set the format string that will be used for tagging SCM
   */
  public setTaggingFormat(String taggingFormat) {
    this.taggingFormat = taggingFormat
  }
}