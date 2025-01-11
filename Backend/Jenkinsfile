pipeline {
    agent any

    environment {
        BRANCH_DEV = 'origin/develop'
        BRANCH_PROD = 'origin/main'
        NEXUS_PROXY = "http://localhost:9082"
        NEXUS_PRIVATE = "http://localhost:9083"
        NEXUS_CREDENTIALS_ID = "nexus-credentials"
        SERVICES = "config-service,discovery-service,gateway-service,participant-service,training-service"
        TEST_SERVER = '192.168.11.138'
        TEST_SERVER_USER = 'chouay'
        PROJECT_PATH = '/home/chouay/first-aid'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD", returnStdout: true).trim()
                    env.MODIFIED_SERVICES = SERVICES.split(',').findAll { service ->
                        changedFiles.contains(service)
                    }.join(',')
                    echo "Modified services: ${env.MODIFIED_SERVICES}"
                }
            }
        }

        stage('Tests') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Sonarqube Analysis') {
            steps {
                script {
                    withSonarQubeEnv('sonar-server') {
                        sh "mvn sonar:sonar -Dintegration-tests.skip=true -Dmaven.test.failure.ignore=true"
                    }
                    timeout(time: 1, unit: 'MINUTES') {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                    }
                }
            }
        }


    }
}

def getEnvVersion(service, envName) {
    // Don't use dir() inside dir() - this was causing the path issue
    def pom = readMavenPom file: "${service}/pom.xml"
    def artifactVersion = pom.version
    def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()

    def versionNumber = gitCommit ?
            "${artifactVersion}-${envName}.${env.BUILD_NUMBER}.${gitCommit.take(8)}" :
            "${artifactVersion}-${envName}.${env.BUILD_NUMBER}"

    echo "Build version for service ${service}: ${versionNumber}"
    return versionNumber
}