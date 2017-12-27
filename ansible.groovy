import com.concur.*;

concurArtUtil   = new ArtifactoryUtil()
concurPipeline  = new Commands()
concurUtil      = new Util()
concurGit       = new Git()

public playbook(yml, args) {
  String dockerImage  = args?.buildImage  ?: yml.tools?.ansible?.buildImage
  String playbook     = args?.playbook    ?: yml.tools?.ansible?.playbook
  String inventory    = args?.inventory   ?: yml.tools?.ansible?.inventory
  String limit        = args?.limit       ?: yml.tools?.ansible?.limit
  String sudoUser     = args?.sudoUser    ?: yml.tools?.ansible?.sudoUser     ?: 'root'
  Map credentialDef   = args?.credentials ?: yml.tools?.ansible?.credentials  ?: yml.defaults?.credentials
  Map extraVars       = args?.extraVars   ?: yml.tools?.ansible?.extraVars    ?: ""
  List tags           = args?.tags        ?: yml.tools?.ansible?.tags         ?: ""
  List skippedTags    = args?.skippedTags ?: yml.tools?.ansible?.skippedTags  ?: ""
  List extras         = args?.extras      ?: yml.tools?.ansible?.extras       ?: ""
  Boolean sudo        = args?.sudo        ?: yml.tools?.ansible?.sudo         ?: false
  int forks           = args?.forks       ?: yml.tools?.ansible?.forks        ?: 10
  int ymlVerbosity    = args?.verbosity   ?: yml.tools?.ansible?.verbosity    ?: ""

  def verbosity = 2
  if (ymlVerbosity) {
    verbosity = ymlVerbosity
  }

  concurPipeline.debugPrint('Workflows :: ansible :: playbook', [
    'dockerImage'   : dockerImage,
    'playbook'      : playbook,
    'inventory'     : inventory,
    'extras'        : extras,
    'verbosity'     : verbosity,
    'extraVars'     : extraVars,
    'tags'          : tags,
    'skippedTags'   : skippedTags,
    'limit'         : limit,
    'sudo'          : sudo,
    'sudoUser'      : sudoUser,
    'forks'         : forks
  ])

  assert dockerImage      : "Workflows :: ansible :: playbook :: [buildImage] not provided in [tools.ansible] or as a parameter to the ansible.playbook step."
  assert playbook         : "Workflows :: ansible :: playbook :: [playbook] not provided in [tools.ansible] or as a parameter to the ansible.playbook step."
  assert inventory        : "Workflows :: ansible :: playbook :: [inventory] not provided in [tools.ansible] or as a parameter to the ansible.playbook step."
  assert credentialDef    : "Workflows :: ansible :: playbook :: [credentials] not provided in [tools.ansible] or as a parameter to the ansible.playbook step."

  def credential = concurPipeline.getCredentialsWithCriteria(credentialDef)

  dockerImage = concurUtil.mustacheReplaceAll(dockerImage)

  if (extraVars) {
    def vars = extraVars.collect { "-e ${it.key}=\"${it.value}\"" }
    extras = "$extras ${vars.join(' ')}"
  }
  if (verbosity > 0) {
    extras = "$extras -${'v'*verbosity}"
  }

  if (extras) {
    extras = concurUtil.mustacheReplaceAll(extras)
  }

  docker.image(dockerImage).inside('-u 0:0') {
    if (!concurUtil.binAvailable('ansible')) {
      error("""Attempt to use ansible binary failed, please ensure the image [$dockerImage] contains an install of Ansible and that it is in the PATH.""")
    }
    def ansibleVersion = sh(returnStdout: true, script: 'ansible --version').split('\n')[0].split(' ')[1]
    println "Container has Ansible version [$ansibleVersion] installed."
    def ansiblePluginAvailable = new Commands().getPluginVersion('ansible')
    if (ansiblePluginAvailable) {
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
    } else {
      error("Ansible workflow currently only works with the Ansible plugin installed.")
    }
  }
}

/*
 ******************************* COMMON *******************************
 This a section for common utilities being called from the runSteps method in com.concur.Commands
 */

public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'playbook':
      def playbook = args?.playbook ?: yml.tools?.ansible?.playbook
      return "ansible: playbook: ${playbook}"
  }
}

return this;
