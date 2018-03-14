#!/usr/bin/env groovy
import com.concur.*;

workflowDoc = '''
title: Documentation
overview: Includes workflows for running various language independent documentation generator tools.
additional_resources:
  - name: Mkdocs
    url: http://www.mkdocs.org
tools:
  - type: String
    name: buildImage
    section: mage
    description: Docker image that has Mage installed.
  - type: String
    name: target
    section: mage
    description: The mage target to execute.
  - type: String
    name: mageFileDir
    section: mage
    default: '.'
    description: The directory containing your magefile.
full_example: |
  pipelines:
    tools:
      mkdocs:
        buildImage: "quay.io/example/mkdocs"
      branches:
        patterns:
          feature: .+
    branches:
      feature:
        steps:
          - documentation:
            - mkdocs:
'''

concurUtil = new com.concur.Util()

/*
description: Generate documentation using mkdocs
parameters:
  - type: String
    name: buildImage
    description: Docker image that has mkdocs installed.
  - type: String
    name: command
    default: build
    description: Which mkdocs command to use, serve will not work, supported commands are build and gh-deploy.
  - type: List
    name: extraArgs
    description: A list of extra arguments to append to the command.
example: |
  branches:
    feature:
      steps:
        - build:
            # Simple
            - mkdocs:
            # Advanced
            - mkdocs:
                command: gh-deploy
 */
public mkdocs(Map yml, Map args) {
  String buildImage = args?.buildImage  ?: yml.tools?.mkdocs?.buildImage
  String command    = args?.command     ?: yml.tools?.mkdocs?.command     ?: 'build'
  List extraArgs    = args?.extraArgs   ?: yml.tools?.mkdocs?.extraArgs   ?: []

  assert buildImage : 'Workflows :: documentation :: mkdocs :: No [buildImage] provided in [tools.mkdocs] or as a parameter to the documentation.mkdocs step.'

  def unsupportedCommands = ['serve']

  assert !(command in unsupportedCommands) : "Workflows :: documentation :: mkdocs :: ${unsupportedCommands.join(', ')} are unsupported commands for mkdocs"

  def cmd = "mkdocs $command"

  if (extraArgs) {
    extraArgs.each { arg ->
      cmd = "$cmd $arg"
    }
  }

  docker.image(buildImage).inside {
    sh concurUtil.mustacheReplaceAll(cmd)
  }
}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'mkdocs':
      String command = args?.command ?: yml.tools?.mkdocs?.command ?: 'build'
      return "documentation: mkdocs: ${command}"
  }
}

public tests(Map yml, Map args) {
  String workflowName = 'documentation'
  println "Testing $workflowName"
}

return this;
