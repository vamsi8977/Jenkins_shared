def call(Map stageParams) {
    def templateContent = readFile('base-template.yml').trim()
    def renderedTemplate = templateContent.replaceAll('\\$\\{git-repo\\}', env.git_repo).
                                            replaceAll('\\$\\{git-branch\\}', env.git_branch).
                                            replaceAll('\\$\\{app-sonar\\}', env.app_sonar)
    writeFile file: 'rendered_template.yml', text: renderedTemplate
    sh 'git status'
    sh 'cat rendered_template.yml'
}