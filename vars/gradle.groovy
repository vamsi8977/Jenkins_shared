def call(Map stageParams) {
    sh "./gradlew clean build"
    sh "./gradlew sonar"
    sh "jf rt u build/libs/*.jar gradle/"
    sh "jf scan build/libs/*.jar --fail-no-op --build-name=gradle --build-number=$BUILD_NUMBER"
    archiveArtifacts artifacts: "build/libs/*.jar"
}