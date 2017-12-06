import org.codehaus.groovy.runtime.GStringImpl;

testHelper      = new com.concur.test.TestHelper()
concurArtUtil   = new com.concur.ArtifactoryUtil()
concurPipeline  = new com.concur.ConcurCommands()
concurUtil      = new com.concur.Util()
concurGit       = new com.concur.Git()
concurGitHub    = new com.concur.GitHubApi()

public build(yml, args) {
  def orgAndRepo    = concurGitHub.getGitHubOrgAndRepo()
  def baseVersion   = yml.general?.version?.base  ?: "0.1.0"
  def buildVersion  = concurGit.getVersion(baseVersion)
  def buildDate     = new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))

  def dockerfile    = args?.dockerfile            ?: yml.tools?.docker?.dockerfile
  def buildArgs     = args?.buildArgs             ?: yml.tools?.docker?.buildArgs
  def imageName     = args?.imageName             ?: yml.tools?.docker?.imageName   ?: "${orgAndRepo.org}/${orgAndRepo.repo}"
  def imageTag      = args?.imageTag              ?: yml.tools?.docker?.imageTag    ?: buildVersion
  def context       = args?.contextPath           ?: yml.tools?.docker?.contextPath ?: '.'
  def vcsUrl        = args?.vcsUrl                ?: yml.tools?.github?.uri         ?: "https://github.concur.com/${orgAndRepo.org}/${orgAndRepo.repo}"

  def additionalArgs = ""

  if (dockerfile) {
    additionalArgs = "${additionalArgs} --file ${dockerfile}"
  }

  if (buildArgs) {
    def tmpArgs = ""
    if (buildArgs instanceof Map) {
      tmpArgs = buildArgs.collect { "--build-arg ${it.key}=${it.value}" }.join(' ')
    } else if (buildArgs instanceof List) {
      tmpArgs = buildArgs.collect { "--build-arg ${it}" }.join(' ')
    } else if (buildArgs instanceof String) {
      tmpArgs = buildArgs
    } else {
      error("Error with format docker.build of pipelines.yml, please verify the buildArgs in tools.docker.buildArgs or the parameter passed to the step. The buildArgs node should be a Map, a List or a String. The provided data is a [${buildArgs.getClass()}]")
    }
    additionalArgs = "${additionalArgs} ${tmpArgs}"
  }

  additionalArgs = concurUtil.mustacheReplaceAll("${additionalArgs} ${context}", [
    'BUILD_VERSION' : buildVersion,
    'COMMIT_SHA'    : env.GIT_COMMIT,
    'VCS_URL'       : vcsUrl,
    'BUILD_DATE'    : buildDate
  ])

  def fullImageName = concurUtil.mustacheReplaceAll("${imageName}:${imageTag}")

  concurPipeline.debugPrint('Workflows :: docker :: build', [
    'dockerfile'    : dockerfile,
    'buildArgs'     : buildArgs,
    'imageName'     : imageName,
    'baseVersion'   : baseVersion,
    'buildVersion'  : buildVersion,
    'imageTag'      : imageTag,
    'additionalArgs': additionalArgs,
    'fullImageName' : fullImageName,
    'vcsUrl'        : vcsUrl
  ])

  docker.build(fullImageName, additionalArgs)
}

public push(yml, args) {
  def baseVersion    = yml.general?.version?.base ?: "0.1.0"
  def buildVersion   = concurGit.getVersion(baseVersion)
  def orgAndRepo     = concurGitHub.getGitHubOrgAndRepo()
  def imageName      = args?.imageName      ?: yml.tools?.docker?.imageName ?: "${orgAndRepo.org}/${orgAndRepo.repo}"
  def imageTag       = args?.imageTag       ?: yml.tools?.docker?.imageTag  ?: buildVersion
  def dockerEndpoint = args?.uri            ?: yml.tools?.docker?.uri       ?: env.DOCKER_URI
  def additionalTags = args?.additionalTags ?: yml.tools?.docker?.additionalTags
  def credentials    = args?.credentials    ?: yml.tools?.docker?.credentials

  def dockerCredentialId

  assert imageName  : "No [imageName] provided in [tools.docker] or as a parameter to the docker.push step."
  assert imageTag   : "No [imageTag] provided in [tools.docker] or as a parameter to the docker.push step."

  dockerEndpoint = concurUtil.mustacheReplaceAll(dockerEndpoint)

  if (credentials != null) {
    assert (credentials instanceof Map) :
    """|Credentials are provided either in [tools.docker.credentials] or as a parameter to this step. 
       |The data provided is not a map. 
       |Credentials should be defined in your pipelines.yml as:
       |----------------------------------------
       |tools:
       |  docker:
       |    credentials:
       |      description: example docker credentials""".stripMargin()
    dockerCredentialId = concurPipeline.getCredentialsWithCriteria(credentials).id
  }

  def fullImageName = concurUtil.mustacheReplaceAll("${imageName}:${imageTag}")

  concurPipeline.debugPrint([
    'baseVersion'         : baseVersion,
    'imageName'           : imageName,
    'buildVersion'        : buildVersion,
    'imageTag'            : imageTag,
    'fullImageName'       : fullImageName,
    'dockerEndpoint'      : dockerEndpoint,
    'dockerCredentialId'  : dockerCredentialId,
    'additionalTags'      : additionalTags
  ])

  bhDockerPublish {
    image         = fullImageName
    dockerUri     = dockerEndpoint
    credentialId  = dockerCredentialId
    tags          = additionalTags
  }
}

/*
 ******************************* COMMON *******************************
 This a section for common utilities being called from the runSteps method in com.concur.Commands
 */

public getStageName(yml, args, stepName) {
  switch(stepName) {
    case 'build':
      def dockerfile = args?.dockerfile ?: yml.tools?.docker?.dockerfile
      if (dockerfile) {
        return "docker: build: ${dockerfile}"
      } else {
        return 'docker: build'
      }
      break
    case 'push':
      def dockerUri = args?.uri ?: yml.tools?.docker?.uri
      if (dockerUri) {
        return "docker: push: ${dockerUri}"
      } else {
        return 'docker: push'
      }
      break
  }
}

/*
 ******************************* REQUIRED TESTING *******************************
 This area is for testing your workflow, and is a required part of workflow files.
 All tests must pass in order for your workflow to be merged into the master branch.
 */

def tests(yml) {
  println testHelper.header("Testing docker.groovy...")

  def fakeYaml = concurUtil.parseYAML(readFile(yml.testing.dockerTest.testYaml)).pipelines

  // Mock environment data
  env.COMMIT_SHA = "fake_commit_SHA"
  env.GIT_COMMIT = "fake_git_commit"
  
  def fakeDockerfile = readFile yml.testing.dockerTest.payload

  // Variables used for promotion
  def fromRepo = ""
  def toRepo = ""
  def dockerImageName = "testing"

  // Method tests
  boolean passed = true
  try {
    println testHelper.debug("Creating [Dockerfile]...")
    writeFile encoding: 'utf-8', file: 'Dockerfile', text: fakeDockerfile
    println testHelper.debug("Echoing [Dockerfile]...")
    sh "cat Dockerfile"
    println testHelper.debug("Calling [build] function...")
    build(fakeYaml, [:])
    println testHelper.debug("Calling [push] function...")
    push(fakeYaml, [:])
  } catch (e) {
    passed = false
    println testHelper.fail("""|Errors with docker.groovy
                                |----------------------------
                                |$e""".stripMargin())
  } finally {
    if (passed) {
      println testHelper.success("Testing for docker.groovy passed")
      env.passedTests = (env.passedTests.toInteger() + 1)
    } else {
      println testHelper.fail("docker.groovy Testing failed")
      env.failedTests = (env.failedTests.toInteger() + 1)
    }
  }
}

return this;
