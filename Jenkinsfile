#!groovy
nodeWithProperWorkspace {

    stage('checkout') {
        checkout scm
        if (isMaster()) {
            // git by default checks out detached, we need a local branch
            sh "git checkout $env.BRANCH_NAME" // workaround for https://issues.jenkins-ci.org/browse/JENKINS-31924
            sh 'git fetch --prune origin +refs/tags/*:refs/tags/*' // delete all local tags
            sh "git reset --hard origin/master"
            if (isVersionTag()) {
                // Workaround: we don't want infinite releases.
                echo "Aborting build as the current commit on master is already tagged."
                currentBuild.displayName = "checkout-only"
                return
            }
            sh "git clean -ffdx"
        } else {
            sh "git clean -ffd"
        }
    }

    def preReleaseVersion = getCurrentVersion()
    if (isMaster()) {
        prepareRelease()
    }

    stage('Build') {
        withMavenEnv {
            mvn 'clean compile'
        }
    }
    
    stage('Test') {
        withMavenEnv {
           mvn 'verify'
        }
    }
   
    if (isMaster()) {
        postRelease(preReleaseVersion)
    }

    step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
}

/**
 * Remove SNAPSHOT from version and set the display name for this job.
 */
void prepareRelease() {
    stage('Prepare release') {
        // Remove SNAPSHOT version
        def String noSnapshotVersion = '\\${parsedVersion.majorVersion}.\\${parsedVersion.minorVersion}.\\${parsedVersion.incrementalVersion}'
        echo 'Removing SNAPSHOT from version'
        setVersion(noSnapshotVersion)
        // Set the display name for the job to the version
        String version = getCurrentVersion()
        currentBuild.displayName = version
        echo "Version to release is: $version"
    }
}

/**
 * Tag the current release and push to master.
 * Checkout develop, merge from master and increment develop version.
 */
void postRelease(String preReleaseVersion) {
    stage('Tag release') {
        def version = "v${getCurrentVersion()}"
        echo "Tagging release as $version"
        sh "git add ."
        sh "git commit -m '[release] $version'"
        sh "git tag $version"
        // workaround: cannot push without credentials using HTTPS => push using SSH
        sh "git remote set-url origin ${getGithubUrlAsSsh()}"
        sh "git push origin master --tags"
    }

    stage('Increment develop version') {
        sh "git checkout develop"
        sh "git fetch origin"
        sh "git reset --hard origin/develop"
        def developVersion = getCurrentVersion()
        if (developVersion == preReleaseVersion) {
            sh "git merge origin/master"
            def nextSnapshotVersion = '\\${parsedVersion.majorVersion}.\\${parsedVersion.nextMinorVersion}.0-SNAPSHOT'
            setVersion(nextSnapshotVersion)
            sh "git add *"
            sh "git commit -m '[release] set version ${getCurrentVersion()}'"
            sh "git push origin develop"
        } else {
            echo "Version on develop not incremented as it differs from the preReleaseVersion."
        }
    }
}

String getCurrentVersion() {
    def pom = readMavenPom file: 'pom.xml'
    return pom.version
}

void setVersion(String newVersion, String rootPom = null) {
    withMavenEnv {
        def goals = 'build-helper:parse-version release:update-versions'
        def pom = rootPom ? "-f $rootPom " : ''
        sh "mvn $pom$goals -DdevelopmentVersion=$newVersion"
    }
}