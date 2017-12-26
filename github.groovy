testHelper      = new com.concur.test.TestHelper()
concurPipeline  = new com.concur.ConcurCommands()
concurGitHubApi = new com.concur.GitHubApi()
concurUtil      = new com.concur.Util()

public createPullRequest(yml, args) {
  try {
    def orgAndRepo      = concurGitHubApi.getGitHubOrgAndRepo()
    def fromBranch      = args?.fromBranch  ?: env.BRANCH_NAME
    def toBranch        = args?.toBranch    ?: yml.tools?.github?.master ?: 'master'
    def org             = args?.org         ?: orgAndRepo.org
    def repo            = args?.repo        ?: orgAndRepo.repo
    def title           = args?.title       ?: "Merge {{ from_branch }} into {{ target_branch }}"
    def summary         = args?.summary     ?: "Created by Buildhub run {{ build_url }}."

    // env.CHANGE_FORK is set to the organization the fork is from and is only set on a PR build
    if (env.CHANGE_FORK) {
      concurPipeline.debugPrint("workflows :: pullRequest :: create", "Skipping pull request creation for forked repo.")
      return
    }

    def replaceOptions  = ['from_branch': fromBranch, 'target_branch': toBranch]

    concurPipeline.debugPrint("Workflow :: PullRequest :: create", [
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
    concurPipeline.debugPrint("Workflow :: PullRequest :: create", ['pullRequestResult': pullRequestResult])
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
  ******************************* REQUIRED TESTING *************************************
  This area is for testing your workflow, and is a required part of workflow files.
  All tests must pass in order for your workflow to be merged into the master branch.
 */

def tests(yml) {

  println testHelper.header("Testing pullRequest.groovy...")
  // Mock for the pipelines.yml used for testing
  def fakeYml = """"""

  // Mock environment data

  // Job variables

  // Method test
  boolean passed = true
  try{

  } catch (e) {
    passed = false
    println testHelper.fail("""|Errors with pullRequest.groovy
                                |----------------------------
                                |$e""".stripMargin())
  } finally {
    if (passed) {
      println testHelper.success("Testing for pullRequest.groovy passed")
      env.passedTests = (env.passedTests.toInteger() + 1)
    } else {
      println testHelper.fail("pullRequest.groovy Testing failed")
      env.failedTests = (env.failedTests.toInteger() + 1)
    }
  }
}

return this;
