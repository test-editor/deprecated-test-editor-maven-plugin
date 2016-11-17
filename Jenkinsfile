#!groovy
nodeWithProperWorkspace {

    stage('checkout') {
        checkout scm
        sh "git clean -ffdx"
    }

    // TODO get rid of the install parameter (currently need it for the integration test) 
    stage('Build') {
        withMavenEnv {
            mvn 'clean install -DskipTests'
        }
    }
    
    stage('Test') {
        withMavenEnv {
           mvn 'test'
        }
    }

}