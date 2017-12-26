import com.concur.*;

concurPipeline  = new Commands()
concurGit       = new Git()
concurUtil      = new Util()

public glide(Map yml, Map args) {
  def dockerImage     = args?.buildImage      ?: yml.tools?.golang?.buildImage
  def additionalArgs  = args?.additionalArgs  ?: yml.tools?.glide?.additionalArgs
  def goPath          = args?.goPath          ?: yml.tools?.golang?.goPath        ?: getGoPath()
  def command         = args?.command         ?: "install"

  assert goPath      : "Workflows :: Golang :: glide :: [goPath] is required in [tools.golang] or as a parameter to the test step."
  assert dockerImage : "Workflows :: Golang :: glide :: [buildImage] is needed in [tools.golang] or as a parameter to the test step."

  dockerImage = concurUtil.mustacheReplaceAll(dockerImage)

  def glideCommand = "glide ${command}"

  /**
   * Define additional args as any of the following
   * ----------------------------------------------
   * golang:
   *   - glide:
   *       additionalArgs: "--force --skip-test -v"
   * ----------------------------------------------
   * golang:
   *   - glide:
   *       additionalArgs:
   *         - "--force"
   *         - "--skip-test"
   *         - "-v"
   * ----------------------------------------------
   */
  if (additionalArgs) {
    if (additionalArgs instanceof List) {
      glideCommand = "${glideCommand} ${additionalArgs.join(' ')}"
    } else {
      glideCommand = "${glideCommand} ${additionalArgs}"
    }
  }

  glideCommand = concurUtil.mustacheReplaceAll(glideCommand)

  concurPipeline.debugPrint('Workflows :: golang :: glide', [
    'dockerImage'     : dockerImage,
    'goPath'          : goPath,
    'command'         : command,
    'additionalArgs'  : additionalArgs,
    'glideCommand'    : glideCommand
  ])

  // -u 0:0 runs as root, -v mounts the current workspace to your gopath
  runCommandInDockerImage(dockerImage, goPath, {
    concurUtil.installGoPkg('glide', 'github.com/Masterminds/glide')
    sh "cd ${goPath} && ${glideCommand}"
  })
}

public godep(Map yml, Map args) {
  def dockerImage     = args?.buildImage      ?: yml.tools?.golang?.buildImage
  def additionalArgs  = args?.additionalArgs  ?: yml.tools?.glide?.additionalArgs
  def command         = args?.command         ?: "ensure"
  def goPath          = args?.goPath          ?: yml.tools?.golang?.goPath        ?: getGoPath()

  assert goPath      : "Workflows :: Golang :: godep :: [goPath] is required in [tools.golang] or as a parameter to the test step."
  assert dockerImage : "Workflows :: Golang :: godep :: [buildImage] is needed in [tools.golang] or as a parameter to the test step."

  dockerImage = concurUtil.mustacheReplaceAll(dockerImage)

  def godepCommand = "godep ${command}"

  /**
   * Define additional args as any of the following
   * ----------------------------------------------
   * golang:
   *   - godep:
   *       additionalArgs: "--force --skip-test -v"
   * ----------------------------------------------
   * golang:
   *   - godep:
   *       additionalArgs:
   *         - "--force"
   *         - "--skip-test"
   *         - "-v"
   * ----------------------------------------------
   */
  if (additionalArgs) {
    if (additionalArgs instanceof List) {
      godepCommand = "${godepCommand} ${additionalArgs.join(' ')}"
    } else {
      godepCommand = "${godepCommand} ${additionalArgs}"
    }
  }

  godepCommand = concurUtil.mustacheReplaceAll(godepCommand)

  concurPipeline.debugPrint('Workflows :: golang :: godep', [
    'dockerImage'     : dockerImage,
    'goPath'          : goPath,
    'command'         : command,
    'additionalArgs'  : additionalArgs,
    'godepCommand'    : godepCommand
  ])

  runCommandInDockerImage(dockerImage, goPath, {
    concurUtil.installGoPkg('godep', 'github.com/tools/godep')
    sh "cd ${goPath} && ${godepCommand}"
  })
}

public build(Map yml, Map args) {
  def dockerImage     = args?.buildImage    ?: yml.tools?.golang?.buildImage
  def outFile         = args?.outFile       ?: yml.tools?.golang?.outFile
  def goEnv           = args?.env           ?: yml.tools?.golang?.env
  def mainPath        = args?.mainPath      ?: yml.tools?.golang?.mainPath
  def goPath          = args?.goPath        ?: yml.tools?.golang?.goPath    ?: getGoPath()
  def additionalArgs  = args?.additionalArgs

  assert goPath      : "Workflows :: Golang :: build :: [goPath] is required in [tools.golang] or as a parameter to the test step."
  assert dockerImage : "Workflows :: Golang :: build :: [buildImage] is needed in [tools.golang] or as a parameter to the test step."

  dockerImage = concurUtil.mustacheReplaceAll(dockerImage)

  def goCommand = "go build"

  if (goEnv) {
    if (goEnv instanceof Map) {
      def envStr = goEnv.collect { "${it.key}=${it.value}" }.join(' ')
      goCommand = "${envStr} ${goCommand}"
    } else {
      goCommand = "${goEnv} ${goCommand}"
    }
  }

  if (outFile) {
    goCommand = "${goCommand} -o ${outFile}"
  }

  /**
   * Define additional args as any of the following
   * ----------------------------------------------
   * golang:
   *   - build:
   *       additionalArgs: "-a -n -p 5"
   * ----------------------------------------------
   * golang:
   *   - build:
   *       additionalArgs:
   *         - "-a"
   *         - "-n"
   *         - "-p 5"
   * ----------------------------------------------
   */
  if (additionalArgs) {
    if (additionalArgs instanceof List) {
      goCommand = "${goCommand} ${additionalArgs.join(' ')}"
    } else {
      goCommand = "${goCommand} ${additionalArgs}"
    }
  }

  if (mainPath) {
    goCommand = "${goCommand} ${mainPath}"
  }

  goCommand = concurUtil.mustacheReplaceAll(goCommand)

  concurPipeline.debugPrint('Workflow :: golang :: build', [
    'dockerImage'   : dockerImage,
    'goPath'        : goPath,
    'outFile'       : outFile,
    'goEnv'         : goEnv,
    'additionalArgs': additionalArgs,
    'goCommand'     : goCommand
  ])

  runCommandInDockerImage(dockerImage, goPath, {
    sh "cd ${goPath} && ${goCommand}"
  })
}

public test(Map yml, Map args) {
  def dockerImage     = args?.buildImage      ?: yml.tools?.golang?.buildImage
  def additionalArgs  = args?.additionalArgs  ?: yml.tools?.golang?.additionalArgs
  def goPath          = args?.goPath          ?: yml.tools?.golang?.goPath          ?: getGoPath()
  def testBinary      = args?.binary          ?: yml.tools?.golang?.testBinary      ?: "go test"
  def resultsPath     = args?.resultsPath     ?: yml.tools?.golang?.testResultsPath ?: "test_results"
  def gatherJunit     = args?.gatherJunit     ?: yml.tools?.golang?.gatherJunit     ?: false
  def junitPattern    = args?.junitPattern    ?: yml.tools?.golang?.junitPattern    ?: "${resultsPath}/*.xml"

  assert dockerImage  : "Workflows :: Golang :: [buildImage] is needed in [tools.golang] or as a parameter to the test step."
  assert goPath       : "Workflows :: Golang :: [goPath] is required in [tools.golang] or as a parameter to the test step."
  assert junitPattern : "Workflows :: Golang :: [junitPattern] is required in [tools.golang] or as a parameter to the test step."

  dockerImage = concurUtil.mustacheReplaceAll(dockerImage)

  def testCommand = testBinary

  /**
   * Define additional args as any of the following
   * ----------------------------------------------
   * golang:
   *   - test:
   *       additionalArgs: "-a -n -p 5"
   * ----------------------------------------------
   * golang:
   *   - test:
   *       additionalArgs:
   *         - "-a"
   *         - "-n"
   *         - "-p 5"
   * ----------------------------------------------
   */
  if (additionalArgs) {
    if (additionalArgs instanceof List) {
      testCommand = "${testCommand} ${additionalArgs.join(' ')}"
    } else {
      testCommand = "${testCommand} ${additionalArgs}"
    }
  }
  
  def shCmd = "cd ${goPath} && mkdir -p ${goPath}/${resultsPath} && ${testCommand}"

  shCmd = concurUtil.mustacheReplaceAll(shCmd)

  concurPipeline.debugPrint('Workflows :: golang :: test', [
    'dockerImage'   : dockerImage,
    'goPath'        : goPath,
    'testBinary'    : testBinary,
    'additionalArgs': additionalArgs,
    'resultsPath'   : resultsPath,
    'gatherJunit'   : gatherJunit,
    'junitPattern'  : junitPattern,
    'testCommand'   : testCommand,
    'shCmd'         : shCmd
  ])

  runCommandInDockerImage(dockerImage, goPath, {
    sh shCmd
  })
  if (gatherJunit) {
    junit junitPattern
  }
}

private getGoPath() {
  return "/go/src/${GIT_HOST}/${env.GIT_ORG}/${env.GIT_REPO}"
}

private runCommandInDockerImage(Map dockerImage, String goPath, Closure work) {
  docker.image(dockerImage).inside("-u 0:0 -v ${pwd()}:${goPath}") {
    work()
  }
}

/*
  ******************************* REQUIRED TESTING *************************************
  This area is for testing your workflow, and is a required part of workflow files.
  All tests must pass in order for your workflow to be merged into the master branch.
 */

def tests(Map yml, Map args) {
  println testHelper.header("Testing golang.groovy...")
  // Mock for the pipelines.yml used for testing
  def fakeYml = """"""

  // Mock environment data

  // Job variables

  // Method test
  boolean passed = true
  try {

  } catch (e) {
    passed = false
    println testHelper.fail("""|Errors with golang.groovy
                        |----------------------------
                        |$e""".stripMargin())
  } finally {
    if (passed) {
      println testHelper.success("Testing for golang.groovy passed")
      env.passedTests = (env.passedTests.toInteger() + 1)
    } else {
      println testHelper.fail("golang.groovy Testing failed")
      env.failedTests = (env.failedTests.toInteger() + 1)
    }
  }
}

return this;
