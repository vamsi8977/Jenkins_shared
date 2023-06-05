def call(Map stageParams) {
    def yamlContent = readFile('base-config.yml').trim()
    def yamlData = readYaml(text: yamlContent)
    env.git_repo = yamlData.'git-repo'
    env.git_branch = yamlData.'git-branch'
    env.app_sonar = yamlData.'app-sonar'
    echo "Repository URL: ${env.git_repo}"
    echo "Branch: ${env.git_branch}"
    echo "Sonar: ${env.app_sonar}"
}