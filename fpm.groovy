concurGit   = new com.concur.Git()
concurUtil  = new com.concur.Util()
concurGit   = new com.concur.Git()

public 'package'(yml, args) {
  def gitData       = concurGit.getGitData()
  def buildImage    = args?.buildImage    ?: yml.tools?.fpm?.buildImage
  def sourceType    = args?.sourceType    ?: yml.tools?.fpm?.sourceType   ?: 'dir'
  def targetTypes   = args?.targetTypes   ?: yml.tools?.fpm?.targetTypes  ?: ['rpm']
  def version       = args?.version       ?: yml.tools?.fpm?.version      ?: "{{ build_version }}"
  def packageName   = args?.name          ?: yml.tools?.fpm?.name         ?: gitData.repo
  def sourceDir     = args?.sourceDir     ?: yml.tools?.fpm?.sourceDir
  def dependencies  = args?.dependencies  ?: yml.tools?.fpm?.dependencies
  def extraArgs     = args?.extraArgs     ?: yml.tools?.fpm?.extraArgs

  assert sourceType   : "Workflows :: fpm :: package :: No [sourceType] provided in [tools.fpm] or as a parameter to the fpm.package step."
  assert targetTypes  : "Workflows :: fpm :: package :: No [targetTypes] provided in [tools.fpm] or as a parameter to the fpm.package step."
  assert sourceDir    : "Workflows :: fpm :: package :: No [sourceDir] provided in [tools.fpm] or as a parameter to the fpm.package step."
  assert buildImage   : "Workflows :: fpm :: package :: No [buildImage] provided in [tools.fpm] or as a parameter to the fpm.package step."

  assert (targetTypes instanceof List) : """Workflows :: fpm :: package :: Target types must be a list of targets.
                                            |Example:
                                            |tools:
                                            |  fpm:
                                            |    targetTypes:
                                            |      - rpm
                                            |      - deb
                                            |--------------
                                            |Or:
                                            |[...]
                                            |feature:
                                            |  steps:
                                            |    - fpm:
                                            |      - package:
                                            |          targetTypes:
                                            |            - rpm
                                            |            - deb""".stripMargin()
  if (dependencies) {
    assert (dependencies instanceof List) : """Workflows :: fpm :: package :: Dependencies must be provided as a list.
                                            |Example:
                                            |tools:
                                            |  fpm:
                                            |    dependencies:
                                            |      - rpm
                                            |      - deb
                                            |--------------
                                            |Or:
                                            |[...]
                                            |feature:
                                            |  steps:
                                            |    - fpm:
                                            |      - package:
                                            |          dependencies:
                                            |            - dependency1
                                            |            - dependency2""".stripMargin()
  }

  buildImage = concurUtil.mustacheReplaceAll(buildImage)

  def fpmFlags = "-s ${sourceType}"

  if (sourceType == 'dir') {
    fpmFlags = "${fpmFlags} -C ${sourceDir}"
  }

  if (packageName) {
    fpmFlags = "${fpmFlags} -n ${packageName}"
  }

  if (version) {
    fpmFlags = "${fpmFlags} -v ${version}"
  }

  if (dependencies) {
    def joinedDependencies = dependencies.collect{ "-d ${it}" }.join(' ')
    fpmFlags = "${fpmFlags} ${joinedDependencies}"
  }

  if (extraArgs) {
    fpmFlags = "${fpmFlags} ${extraArgs}"
  }

  fpmFlags = concurUtil.mustacheReplaceAll(fpmFlags, ['build_version': concurGit.getVersion()])

  docker.image(buildImage).inside {
    targetTypes.each {
      sh "fpm -t ${it} ${fpmFlags}"
    }
  }
}

/*
 ******************************* COMMON *******************************
 This a section for common utilities being called from the runSteps method in com.concur.Commands
 */

public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'package':
      def fpmCommand = args?.sourceType ?: yml.tools?.fpm?.sourceType
      return fpmCommand ? "fpm: package: ${fpmCommand}" : 'fpm: package'
  }
}

return this;
