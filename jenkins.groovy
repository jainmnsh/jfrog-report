#!/usr/bin/env groovy

node ('master') {
    //Clone jfro project from GitHub repository
    git url: 'https://github.com/jainmnsh/jfrog-report.git', branch: 'master'
    def rtServer = Artifactory.server SERVER_ID
    def buildDepInfo = Artifactory.newBuildInfo()
    def buildInfo = Artifactory.newBuildInfo()
    def tagDockerApp
    def rtDocker
    buildDepInfo.env.capture = true
    // buildDepInfo.env.collect()
    buildInfo.env.capture = true
    // buildInfo.env.collect()

    //Build docker image named docker-app
    stage ('Build & Deploy') {
        dir ('./') {
            tagDockerApp = "${ARTDOCKER_REGISTRY}/artifact-report:${env.BUILD_NUMBER}"
            docker.build(tagDockerApp)
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: CREDENTIALS, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                rtDocker = Artifactory.docker server: rtServer
                rtDocker.push(tagDockerApp, REPO, buildInfo)
                buildInfo.append(buildDepInfo)
                rtServer.publishBuildInfo buildInfo
             }
        }
     }
    //Test docker image
     stage ('Test') {
        dir('automation/artifact-report/app-test') {
            sh 'docker rmi '+tagDockerApp+' || true'
            rtDocker.pull (tagDockerApp)
            if (testApp(tagDockerApp)) {
                  println "Setting property and promotion"
                  sh 'docker rmi '+tagDockerApp+' || true'
             } else {
                  currentBuild.result = 'UNSTABLE'
                  return
             }
        }
     }

    //Scan Build Artifacts in Xray
    stage('Xray Scan') {
         if (XRAY_SCAN == "YES") {
             def xrayConfig = [
                'buildName'     : env.JOB_NAME,
                'buildNumber'   : env.BUILD_NUMBER,
                'failBuild'     : false
              ]
              def xrayResults = rtServer.xrayScan xrayConfig
              echo xrayResults as String
         } else {
              println "No Xray scan performed. To enable set XRAY_SCAN = YES"
         }
         sleep 60
     }

    //Promote docker image from staging local repo to production repo in Artifactory
     stage ('Promote') {
        dir('automation/artifact-report/app-test') {
            def promotionConfig = [
              'buildName'          : env.JOB_NAME,
              'buildNumber'        : env.BUILD_NUMBER,
              'targetRepo'         : PROMOTE_REPO,
              'comment'            : 'App works with latest released version of most used artifacts',
              'sourceRepo'         : 'docker-stage-local',
              'status'             : 'Released',
              'includeDependencies': false,
              'copy'               : true
            ]
            rtServer.promote promotionConfig
            reTagLatest ()
        }
     }
}


def testApp (tag) {
    docker.image(tag).withRun('-p 9191:8181') {c ->
        sleep 10
        def stdout = sh(script: 'curl "http://localhost:9191/reports/artifacts/2"', returnStdout: true)
        if (stdout.contains("downloadCount")) {
            println "*** Passed Test: " + stdout
            return true
        } else {
            println "*** Failed Test: " + stdout
            return false
        }
    }
}


//Tag docker image
def reTagLatest () {

    def BUILD_NUMBER = env.BUILD_NUMBER
    sh 'sed -E "s/@/$BUILD_NUMBER/" retag.json > retag_out.json'
    sh 'sed -E "s/TARGETREPO/${PROMOTE_REPO}/" retag_out.json > retaga_out.json'
    sh 'cat retaga_out.json'
    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: CREDENTIALS, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        def curlString = "curl -u " + env.USERNAME + ":" + env.PASSWORD + " " + SERVER_URL
        def regTagStr = curlString +  "/api/docker/${PROMOTE_REPO}/v2/promote -X POST -H 'Content-Type: application/json' -T retaga_out.json"
        println "Curl String is " + regTagStr
        sh regTagStr
    }
}
