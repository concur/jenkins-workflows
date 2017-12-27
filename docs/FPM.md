# FPM

## Overview

FPM is a tool for creating OS packages such as RPM or DEB in many ways. This workflow will allow you to create a custom package within your pipeline.

> Output file is a combination of name-version.type, leaving everything default in this repo would result in the following package name: `workflows-2.2.0.09234781.rpm` for example.

## Tools Section

These can be specified in the tools.ansible section or in an individual step

| Argument      | Constraint  | Type   | Default                  | Description                                                                                     |
|---------------|-------------|--------|--------------------------|-------------------------------------------------------------------------------------------------|
| buildImage    | Optional    | String |                          | Docker image that has FPM installed.                                                            |
| sourceType    | Optional    | String | `dir`                    | Type of source to create the package from, see documentation linked below. Equivalent to `-s`.  |
| targetTypes   | Optional    | List   | `['rpm']`                | Output package types, see documentation linked below. Equivalent to `-t`.                       |
| name          | Optional    | String | `GitHub repository name` | The base name of the package without version or extension.                                      |
| version       | Optional    | String | `{{ build_version }}`    | What to version the package as.                                                                 |
| sourceDir     | Optional    | Map    |                          | What directory to change to for your sources. Equivalent to `-C`.                               |
| dependencies  | Optional    | List   |                          | Optional list of dependencies to add to the package manifest. Equivalent to `-d`.               |
| extraArgs     | Optional    | String | `2`                      | Any additional arguments to the FPM command not covered by this workflow.                       |

## Available Methods

### package

> Execute the FPM command to create installable packages.

#### Playbook Examples

```yaml
branches:
  feature:
    steps:
      - fpm:
          # Simple
          - package: # looks at only what is under tools.ansible
          # Advanced
          - package:
              targetTypes:
                - deb
                - rpm
              dependencies: # -e DOCKER_IMAGE=1.0.4.2234234
                - docker-ce
 ```

## Full pipeline example

```yaml
pipelines:
  tools:
    artifactory:
      publishPattern: "publish/"
      repos:
        sandbox: util-sandbox-local
        staging: util-staging-local
        release: util-release-local
      repoPath: "jenkins-util/concur-example/linux/"
    fpm:
      targetTypes:
        - deb
        - rpm
      sourceDir: sources
      name: concur-example
    github:
      patterns:
        master: master
        feature: .+
  branches:
    feature:
      steps:
        - custom: # This should be your build process
          - build:
        - fpm:
          - package:
[...]
```

## Additional Resources

* [FPM ReadTheDocs](http://fpm.readthedocs.io/en/latest/)
* [FPM Wiki](https://github.com/jordansissel/fpm/wiki)
