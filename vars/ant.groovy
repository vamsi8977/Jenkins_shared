def call(Map stageParams) {
    sh "ant -buildfile build.xml"
    sh "sonar-scanner"
    sh "jf rt u build/jar/*.jar ant/"
    sh "jf scan build/jar/*.jar --fail-no-op --build-name=ant --build-number=$BUILD_NUMBER"
    archiveArtifacts artifacts: "build/jar/*.jar"
}