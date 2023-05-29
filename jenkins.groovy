#!/usr/bin/env groovy
@Library('shared-library') _

pipeline {
  agent any
  options {
    buildDiscarder(logRotator(numToKeepStr:'2' , artifactNumToKeepStr: '2'))
    timestamps()
  }
  environment {
    inventoryName = 'Bommasani'
  }
  parameters {
    choice(name: 'parallelOption', choices: ['branch', 'tag'], description: 'Select parallel option')
    string(name: 'url', defaultValue: 'https://github.com/vamsi8977/jenkinsfile.git', description: 'Git URL')
    string(name: 'branch', defaultValue: 'master', description: 'Git Branch')
    }
  stages {
    /* stage('SCM') {
      steps { // shows the latest tag
        checkout([$class: 'GitSCM',
          branches: [[name: "${params.TAG}"]],
          doGenerateSubmoduleConfigurations: false,
          extensions: [],
          gitTool: 'Default',
          submoduleCfg: [],
          userRemoteConfigs: [[url: 'git@github.com:vamsi8977/jenkinsfile.git']]
              ])
        }
    } */
    stage('SCM') {
      steps {
        cleanWs()
        script {
          def parallelStages = [:]
          switch (params.parallelOption) {
            case 'branch':
              parallelStages['Branch'] = {
                git branch: params.branch, url: params.url
              }
              break
            case 'tag':
              parallelStages['Tag'] = {
                sh "git fetch --tags"
                script {
                  def latestTag = sh(returnStdout: true, script: 'git describe --tags `git rev-list --tags --max-count=1`').trim()
                  checkout([$class: 'GitSCM', branches: [[name: "refs/tags/${latestTag}"]], userRemoteConfigs: [[url: params.url]]])
                }
              }
              break
            default:
              echo "Invalid option selected"
              return
          }
          parallel parallelStages
        }
      }
  }

    stage('Validate') {
      steps {
        ansiColor('xterm') {
          echo 'Check Tools Versions....'
          sh """
            java -version
            mvn --version
            gradle -version
            ant -version
            ansible --version
            git --version
            terraform -v
            ruby -v
            aws --version
            az --version
            node -v
          """
        }
      }
    }
    stage('Maven') {
      steps {
        withSonarQubeEnv('SonarQube') {
          echo 'Maven Build....'
          sh """
            cd ${WORKSPACE}/maven;
            mvn clean install
            mvn sonar:sonar -Dsonar.projectKey=maven -Dsonar.projectName='maven_sample'
            jf rt u target/javaparser-maven-sample-1.0-SNAPSHOT.jar maven/
            jf scan target/*.jar --fail-no-op --build-name=maven --build-number=$BUILD_NUMBER
          """
        }
      }
    }
    stage('Gradle') {
      steps {
        withSonarQubeEnv('SonarQube') {
          echo 'Gradle Build....'
          sh """
            cd ${WORKSPACE}/gradle;
            ./gradlew clean build
            ./gradlew sonar
            jf rt u build/libs/*.jar gradle/
            jf scan build/libs/*.jar --fail-no-op --build-name=gradle --build-number=$BUILD_NUMBER
          """
        }
      }
    }
    stage('Ant') {
      steps {
        withSonarQubeEnv('SonarQube') {
          echo 'Ant Build....'
          sh """
            cd ${WORKSPACE}/ant;
            ant -buildfile build.xml
            sonar-scanner
            jf rt u build/jar/*.jar ant/
            jf scan build/jar/*.jar --fail-no-op --build-name=ant --build-number=$BUILD_NUMBER
          """
        }
      }
    }
    stage('NodeJS') {
      steps {
        withSonarQubeEnv('SonarQube') {
          echo 'NodeJS Build....'
          sh """
            cd ${WORKSPACE}/nodejs;
            npm install
            npm audit fix --force
            npm test
            sonar-scanner
            jf rt u test/config.json nodejs/
            jf scan test/config.json --fail-no-op --build-name=nodejs --build-number=$BUILD_NUMBER
          """
        }
      }
    }
    stage ('Archive Artifacts') {
      steps {
        archiveArtifacts artifacts: "ant/build/jar/*.jar"
        archiveArtifacts artifacts: "gradle/build/libs/*.jar"
        archiveArtifacts artifacts: "maven/target/*.jar"
        archiveArtifacts artifacts: "nodejs/out/test-results.xml"
      }
    }
  }
  post {
    success {
      script {
        echo "The build is Green."
      }
    }
    failure {
      echo "The build failed."
    }
    cleanup {
      deleteDir()
    }
  }
}