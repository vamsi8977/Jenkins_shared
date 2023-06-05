def call(Map stageParams) {
    sh "npm install"
    sh "npm audit fix --force"
    sh "npm run build"
    sh "npm test"
    sh "sonar-scanner"
    sh "jf rt u test/config.json nodejs/"
    sh "jf scan test/config.json --fail-no-op --build-name=nodejs --build-number=$BUILD_NUMBER"
}
