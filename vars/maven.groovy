def call(Map stageParams) {
    sh "mvn clean install"
    sh "mvn sonar:sonar -Dsonar.projectKey=maven -Dsonar.projectName='maven_sample'"
    sh "jf rt u target/javaparser-maven-sample-1.0-SNAPSHOT.jar maven/"
    sh "jf scan target/*.jar --fail-no-op --build-name=maven --build-number=$BUILD_NUMBER"
    archiveArtifacts artifacts: "target/*.jar"
}