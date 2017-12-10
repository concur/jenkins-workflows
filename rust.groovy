concurPipeline  = new com.concur.Commands()
concurUtil      = new com.concur.Util()

public cargo(yml, args) {
  def buildImage        = args?.buildImage      ?: yml.tools?.rust?.buildImage
  def additionalArgs    = args?.additionalArgs  ?: yml.tools?.rust?.additionalArgs
  def rustupComponents  = args?.components      ?: yml.tools?.rust?.components
  def toolchain         = args?.toolchain       ?: yml.tools?.rust?.toolchain
  def command           = args?.command         ?: "build"

  assert buildImage : "[buildImage] is needed in [tools.rust] or as a parameter to the test step."

  buildImage = concurUtil.mustacheReplaceAll(buildImage)

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
    'buildImage'      : buildImage,
    'command'         : command,
    'additionalArgs'  : additionalArgs,
    'cargoCommand'    : cargoCommand
  ])

  // -u 0:0 runs as root, -v mounts the current workspace to your gopath
  docker.image(buildImage).inside {
    /*
      RustUp is a tool for managing Rust and its components
     */
    if (rustupComponents) {
      assert (rustupComponents instanceof List) : """
      |To install additional rustup components please provide as a list.
      |Example:
      |----------------------------------------------------------------
      |pipelines:
      |  tools:
      |    rust:
      |      components:
      |        - rust-std-x86_64-unknown-linux-musl
      |        - rust-std-x86_64-apple-darwin""".stripMargin()
      rustupComponents.each {
        sh "rustup component add ${it}"
      }
    }
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
