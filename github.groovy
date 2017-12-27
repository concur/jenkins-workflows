import com.concur.*;

concurPipeline  = new Commands()
concurGit       = new Git()
concurGitHubApi = new GitHubApi()
concurUtil      = new Util()

public createPullRequest(yml, args) {
  try {
    def gitData     = concurGit.getGitData()
    def fromBranch  = args?.fromBranch  ?: env.BRANCH_NAME
    def toBranch    = args?.toBranch    ?: yml.tools?.github?.master  ?: 'master'
    def githubHost  = args?.host        ?: yml.tools?.github?.host    ?: gitData.host
    def org         = args?.org         ?: gitData.org
    def repo        = args?.repo        ?: gitData.repo
    def title       = args?.title       ?: "Merge {{ from_branch }} into {{ target_branch }}"
    def summary     = args?.summary     ?: "Created by Buildhub run {{ build_url }}."

    // env.CHANGE_FORK is set to the organization the fork is from and is only set on a PR build
    if (env.CHANGE_FORK) {
      concurPipeline.debugPrint('Workflow :: GitHub :: createPullRequest', 'Skipping pull request creation for forked repo.')
      return
    }

    def replaceOptions  = ['from_branch': fromBranch, 'target_branch': toBranch]

    concurPipeline.debugPrint('Workflow :: GitHub :: createPullRequest', [
      'fromBranch'    : fromBranch,
      'toBranch'      : toBranch,
      'org'           : org,
      'repo'          : repo,
      'replaceOptions': replaceOptions,
      'title'         : title,
      'summary'       : summary
    ])
    def pullRequestResult = concurGitHubApi.createPullRequest(concurUtil.mustacheReplaceAll(title, replaceOptions),
                                                              fromBranch,
                                                              toBranch,
                                                              org,
                                                              repo,
                                                              concurUtil.mustacheReplaceAll(summary, replaceOptions))
    concurPipeline.debugPrint('Workflow :: GitHub :: createPullRequest', ['pullRequestResult': pullRequestResult])
    if (pullRequestResult instanceof List) {
      println "A pull request already existed and can be viewed at ${pullRequestResult[0].url}."
    } else {
      println "Created pull request created and can be viewed at ${pullRequestResult.url}."
    }
  } catch (Exception e) {
    error("""|Failed to create pull request.
            |---------------------
            |Error returned:
            |${e}""".stripMargin())
  }
}

/*
 ******************************* COMMON *******************************
 This a section for common utilities being called from the runSteps method in com.concur.Commands
 */

public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'createPullRequest':
      def fromBranch  = args?.fromBranch  ?: env.BRANCH_NAME
      def toBranch    = args?.toBranch    ?: yml.tools?.github?.master ?: 'master'
      return (fromBranch && toBranch) ? "github: createPullRequest: $fromBranch -> $toBranch"
  }
}

return this;
