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
    string(name: 'branch', defaultValue: 'main', description: 'Git Branch')
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
          script {
            def tools = ['java', 'mvn', 'gradle', 'ansible', 'git', 'terraform', 'ruby', 'aws', 'az', 'node']
            tools.each { tool ->
              sh "$tool --version"
            }
            sh 'ant -version'
          }
        }
      }
    }
    stage('Build') {
      steps {
        script {
          withSonarQubeEnv('SonarQube') {
            def buildStages = [:]
            buildStages['Maven'] = {
              dir('maven') {
                maven()
              }
            }
            buildStages['Gradle'] = {
              dir('gradle') {
                gradle()
              }
            }
            buildStages['Ant'] = {
              dir('ant') {
                ant()
              }
            }
            buildStages['NodeJS'] = {
              dir('nodejs') {
                nodejs()
              }
            }
            parallel buildStages
          }
        }
      }
    }
  }
  post {
    success {
      echo "The build is Green."
    }
    failure {
      echo "The build failed."
    }
    cleanup {
      deleteDir()
    }
  }
}