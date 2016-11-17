#!groovy
nodeWithProperWorkspace {

    stage('checkout') {
        checkout scm
        sh "git clean -ffdx"
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

}