import org.codehaus.groovy.runtime.GStringImpl;

concurPipeline  = new com.concur.Commands()
concurUtil      = new com.concur.Util()
concurGit       = new com.concur.Git()

public build(yml, args) {
  Map orgAndRepo      = concurGit.getGitData()
  String baseVersion  = yml.general?.version?.base  ?: "0.1.0"
  String buildVersion = concurGit.getVersion(baseVersion)
  String buildDate    = new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))

  String dockerfile = args?.dockerfile            ?: yml.tools?.docker?.dockerfile  ?: ""
  String imageName  = args?.imageName             ?: yml.tools?.docker?.imageName   ?: "${orgAndRepo.org}/${orgAndRepo.repo}"
  String imageTag   = args?.imageTag              ?: yml.tools?.docker?.imageTag    ?: buildVersion
  String context    = args?.contextPath           ?: yml.tools?.docker?.contextPath ?: '.'
  String vcsUrl     = args?.vcsUrl                ?: yml.tools?.github?.uri         ?: "https://github.concur.com/${orgAndRepo.org}/${orgAndRepo.repo}"
  Map buildArgs     = args?.buildArgs             ?: yml.tools?.docker?.buildArgs   ?: [:]

  String additionalArgs = ""

  if (dockerfile) {
    additionalArgs = "${additionalArgs} --file ${dockerfile}"
  }

  if (buildArgs) {
    additionalArgs = "${additionalArgs} ${buildArgs.collect { "--build-arg ${it.key}=${it.value}" }.join(' ')}"
  }

  additionalArgs = concurUtil.mustacheReplaceAll("${additionalArgs} ${context}", [
    'COMMIT_SHA'    : env.GIT_COMMIT,
    'VCS_URL'       : vcsUrl,
    'BUILD_DATE'    : buildDate
  ])

  String fullImageName = concurUtil.mustacheReplaceAll("${imageName}:${imageTag}")

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
  Map orgAndRepo        = concurGit.getGitData()
  String baseVersion    = yml.general?.version?.base ?: "0.1.0"
  String buildVersion   = concurGit.getVersion(baseVersion)
  String imageName      = args?.imageName      ?: yml.tools?.docker?.imageName      ?: "${orgAndRepo.org}/${orgAndRepo.repo}"
  String imageTag       = args?.imageTag       ?: yml.tools?.docker?.imageTag       ?: buildVersion
  String dockerEndpoint = args?.uri            ?: yml.tools?.docker?.uri            ?: env.DOCKER_URI
  List additionalTags   = args?.additionalTags ?: yml.tools?.docker?.additionalTags ?: []
  Map credentials       = args?.credentials    ?: yml.tools?.docker?.credentials    ?: [:]

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

  println "image not pushed"
  // bhDockerPublish {
  //   image         = fullImageName
  //   dockerUri     = dockerEndpoint
  //   credentialId  = dockerCredentialId
  //   tags          = additionalTags
  // }
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

return this;
