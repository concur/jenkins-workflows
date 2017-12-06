testHelper      = new com.concur.test.TestHelper()
concurPipeline  = new com.concur.ConcurCommands()
concurUtil      = new com.concur.Util()

public cargo(yml, args) {
  def dockerImage     = args?.buildImage      ?: yml.tools?.rust?.buildImage
  def additionalArgs  = args?.additionalArgs  ?: yml.tools?.cargo?.additionalArgs
  def command         = args?.command         ?: "install"

  assert dockerImage : "[buildImage] is needed in [tools.rust] or as a parameter to the test step."

  dockerImage = concurUtils.mustacheReplaceAll(dockerImage)

  def cargoCommand = "cargo ${command}"

  /**
   * Define additional args as any of the following
   * ----------------------------------------------
   * rust:
   *   - cargo:
   *       additionalArgs: "--force --skip-test -v"
   * ----------------------------------------------
   * rust:
   *   - cargo:
   *       additionalArgs:
   *         - "--force"
   *         - "--skip-test"
   *         - "-v"
   * ----------------------------------------------
   */
  if (additionalArgs) {
    if (additionalArgs instanceof List) {
      cargoCommand = "${cargoCommand} ${additionalArgs.join(' ')}"
    } else {
      cargoCommand = "${cargoCommand} ${additionalArgs}"
    }
  }

  concurPipeline.debugPrint([
    'dockerImage'     : dockerImage,
    'command'         : command,
    'additionalArgs'  : additionalArgs,
    'cargoCommand'    : cargoCommand
  ])

  // -u 0:0 runs as root, -v mounts the current workspace to your gopath
  docker.image(dockerImage).inside {
    sh cargoCommand
  }
}

/*
 ******************************* COMMON *******************************
 This a section for common utilities being called from the runSteps method in com.concur.Commands
 */

public getStageName(yml, args, stepName) {
  switch(stepName) {
    case 'cargo':
      return "rust: cargo: ${args?.command ?: "install}"
  }
}

/*
  ******************************* REQUIRED TESTING *************************************
  This area is for testing your workflow, and is a required part of workflow files.
  All tests must pass in order for your workflow to be merged into the master branch.
 */

def tests(yml) {

  println testHelper.header("Testing rust.groovy...")

  // Method test
  boolean passed = true
  try {

  } catch (e) {
    passed = false
    testHelper.fail("""|Errors with rust.groovy
                       |----------------------------
                       |$e""".stripMargin())
  } finally {
    if (passed) {
      println testHelper.success("Testing for rust.groovy passed")
      env.passedTests = (env.passedTests.toInteger() + 1)
    } else {
      println testHelper.fail("rust.groovy Testing failed")
      env.failedTests = (env.failedTests.toInteger() + 1)
    }
  }
}
return this;
