testHelper      = new com.concur.test.TestHelper()
concurArtUtil   = new com.concur.ArtifactoryUtil()
concurPipeline  = new com.concur.ConcurCommands()
concurUtil      = new com.concur.Util()
concurGit       = new com.concur.Git()

public playbook(yml, args) {
  def dockerImage   = args?.buildImage  ?: yml.tools?.ansible?.buildImage
  def playbook      = args?.playbook    ?: yml.tools?.ansible?.playbook
  def inventory     = args?.inventory   ?: yml.tools?.ansible?.inventory
  def credentialDef = args?.credentials ?: yml.tools?.ansible?.credentials  ?: yml.defaults?.credentials
  def extraVars     = args?.extraVars   ?: yml.tools?.ansible?.extraVars    ?: ""
  def tags          = args?.tags        ?: yml.tools?.ansible?.tags         ?: ""
  def skippedTags   = args?.skippedTags ?: yml.tools?.ansible?.skippedTags  ?: ""
  def ymlVerbosity  = args?.verbosity   ?: yml.tools?.ansible?.verbosity    ?: ""
  def extras        = args?.extras      ?: yml.tools?.ansible?.extras       ?: ""
  def limit         = args?.limit       ?: yml.tools?.ansible?.limit        ?: env.JENKINS_DATACENTER
  def sudo          = args?.sudo        ?: yml.tools?.ansible?.sudo         ?: false
  def sudoUser      = args?.sudoUser    ?: yml.tools?.ansible?.sudoUser     ?: 'root'
  def forks         = args?.forks       ?: yml.tools?.ansible?.forks        ?: 10

  def verbosity = 2
  if (ymlVerbosity) {
    try {
      if (ymlVerbosity instanceof java.lang.Number) {
        verbosity = ymlVerbosity
      } else {
        verbosity = ymlVerbosity as Integer
      }
    } catch (java.lang.NumberFormatException nfe) {
      println "verbosity provided in args or yml.tools.ansible must be a number, defaulting to 2 levels of verbosity (eg. -vv)."
    }
  }

  concurPipeline.debugPrint('Workflows :: ansible :: playbook', [
    "dockerImage"   : dockerImage,
    "playbook"      : playbook,
    "inventory"     : inventory,
    "extras"        : extras,
    "verbosity"     : verbosity,
    "extraVars"     : extraVars,
    "tags"          : tags,
    "skippedTags"   : skippedTags,
    "limit"         : limit,
    "sudo"          : sudo,
    "sudoUser"      : sudoUser,
    "forks"         : forks
  ])

  assert dockerImage      : "[buildImage] not provided in [tools.ansible] or as a parameter to the ansible.playbook step."
  assert playbook         : "[playbook] not provided in [tools.ansible] or as a parameter to the ansible.playbook step."
  assert inventory        : "[inventory] not provided in [tools.ansible] or as a parameter to the ansible.playbook step."
  assert credentialDef    : "[credentials] not provided in [tools.ansible] or as a parameter to the ansible.playbook step."

  def credential = concurPipeline.getCredentialsWithCriteria(credentialDef)

  dockerImage = concurUtil.mustacheReplaceAll(dockerImage)

  if (extraVars) {
    assert (extraVars instanceof Map) : """extraVars provided but not as a map. Please ensure your extraVars are structured like this:
    |tools:
    |  ansible:
    |    extraVars:
    |      DOCKER_IMAGE_TAG: "{{ BUILDHUB_VERSION }}"
    """.stripMargin()
    def vars = extraVars.collect { "-e ${it.key}=\"${it.value}\"" }
    extras = "${extras} ${vars.join(' ')}"
  }
  if (verbosity > 0) {
    extras = "${extras} -${'v'*verbosity}"
  }

  if (extras) {
    extras = concurUtil.mustacheReplaceAll(extras, getExtraReplacements(args?.versionSkipLatestCommit, yml))
  }

  docker.image(dockerImage).inside('-u 0:0') {
    sh "ansible --version"
    ansiblePlaybook(colorized:      true,
                    credentialsId:  credential.id,
                    extras:         extras,
                    forks:          forks,
                    inventory:      inventory,
                    limit:          limit,
                    playbook:       playbook,
                    skippedTags:    skippedTags,
                    sudo:           sudo,
                    sudoUser:       sudoUser,
                    tags:           tags)
  }
}

private getExtraReplacements(skipLatest, yml) {
  def branchPattern = concurPipeline.checkBranch(yml)
  def replacements  = [:]
  
  if (concurPipeline.isBuildServer()) {
    replacements.BUILDHUB_VERSION = concurGit.getVersion()
  } else {
    def skipLatestCommit = skipLatest ?: yml.tools?.docker?.skipLatestCommit?."${branchPattern}"  == null ? false : yml.tools.docker.skipLatestCommit."${branchPattern}"
    def commitSha        = skipLatestCommit ? concurGit.getCommitSHA('.') : env.GIT_COMMIT

    replacements.BUILDHUB_VERSION = concurArtUtil.getDockerImageTagBySha(commitSha)
  }
  return replacements
}

/*
  ******************************* REQUIRED TESTING *************************************
  This area is for testing your workflow, and is a required part of workflow files.
  All tests must pass in order for your workflow to be merged into the master branch.
 */

def tests(yml) {
  println testHelper.header('Testing ansible.groovy...')
  
  // Mock for the pipelines.yml used for testing
  def fakeYml = concurUtil.parseYAML(readFile(yml.testing.ansibleTest.testYaml)).pipelines

  // Method test
  boolean passed = true
  try {
    println testHelper.debug("Calling [playbook] function...")
    playbook(fakeYml, [:])
    println testHelper.debug("Calling [playbook] function with args...")
    playbook(fakeYml, [
      'credentials':['description':'AutoCM SSH Key'],
      'extraVars': [
        'ansible_user':'autocm',
        'BUILDHUB_VERSION': "{{ buildhub_version }}"
      ]
    ])
  } catch (e) {
    passed = false
    println testHelper.fail("""|Errors with ansible.groovy
                               |----------------------------
                               |$e""".stripMargin())
  } finally {
    if (passed) {
      println testHelper.success("Testing for ansible.groovy passed")
      env.passedTests = (env.passedTests.toInteger() + 1)
    } else {
      println testHelper.fail("ansible.groovy Testing failed")
      env.failedTests = (env.failedTests.toInteger() + 1)
    }
  }
}

return this;
