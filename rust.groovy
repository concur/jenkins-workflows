concurPipeline  = new com.concur.Commands()
concurUtil      = new com.concur.Util()

public cargo(yml, args) {
  def dockerImage     = args?.buildImage      ?: yml.tools?.rust?.buildImage
  def additionalArgs  = args?.additionalArgs  ?: yml.tools?.cargo?.additionalArgs
  def command         = args?.command         ?: "build"

  assert dockerImage : "[buildImage] is needed in [tools.rust] or as a parameter to the test step."

  dockerImage = concurUtil.mustacheReplaceAll(dockerImage)

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
      def cargoCommand = args?.command ?: "install"
      return "rust: cargo: ${cargoCommand}"
  }
}

return this;
