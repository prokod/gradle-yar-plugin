# Gradle (Yet Another) Release plugin

Inspired by other Gradle release plugins out there already, This plugin is aiming for simplicity while providing some finer settings in the form of changing tagging format for instance.


## Usage

### Quick startup guide:

#### 1. Add the following to your build.gradle file:

  ```groovy
  buildscript {
    repositories {
      mavenCentral()
    }

    dependencies {
      classpath 'com.github.prokod:gradle-yar-plugin:0.2'
    }
  }

  apply plugin: 'ya-release'

  release {
    failOnSnapshotDependencies = true
    allowLocalModifications = false
    releaseDryRun = false
    scm = 'git'
    username = 'star-lord'
    password = 'earthling'
    taggingFormat = '#{b}-RELEASE-#{v}'
  }

  version = release.projectVersion
  ````

  1. Include the buildscript section to pull this plugin into your project.
  2. Apply the plugin.
  3. Add release section to change yar default settings

    Available settings are:

    * failOnSnapshotDependencies: default is true. Will fail the release task if any dependency is currently pointing to a SNAPSHOT
    * allowLocalModifications: defaults to false. Will fail the release task if any uncommitted changes remain in your local version control. This prevents you from releasing a build which you cannot later reproduce because you don't have the complete set of source which went into the build.
    * releaseDryRun: this skips the commit of the tag to your version control system
    * scm: 'git' or 'svn'
    * username: a username for your version control system. This is mostly useful for running releases from a continuous integration server like Jenkins. If you don't pass this, the release plugin will take credentials from any cached on your system or prompt you for them.
    * password: a password to match the username
    * taggingFormat: Any string that may include the following expressions #{b} for branch name #{v} for release projectVersion

  4. Optional: Setting your project version from the output of this plugin - When opting for this, make sure the above code is at the top of the build.gradle file.

#### 2. Calling release task from command line:
  A typical use case to build a project would be to test, tag and upload their release artifacts:

   gradle release publish

#### 3. Gotchas
  In a multi module project you should apply this plugin only once in the root build.gradle otherwise gradle will fail while trying to tag the same tag multiple times. 


### Properties

You can access two properties from this plugin once you have applied it:

  release.projectVersion
    Read only property for getting the version of this project.
    The version is derived from the following rules:
    1. If the releaseVersion property was passed to gradle via the -P command line option, then use that.
    2. If the version control system is currently pointing to a tag, then use a version derived from the name of the tag
    3. Use the name of the branch (or trunk/head) as the version appended with "-SNAPSHOT"
  
  release.scmVersion
    Read only property for getting the version from the source control system.
    This will return the git hash (or the svn commit number) for the current state of the local repository. This value may be useful for as an entry in a Jar's manifest file (it can be more reliable than the public-facing branch/version string.


### Tasks

Tasks

| Task | Depends on | Type | Description |
|------|------------|------|-------------|
| release |   -     | Task | Project version numbering (using exposed property release.projectVersion) and release tagging (currently git and svn are supported) |

## Release notes

