# Fpm

## Overview

> Use the FPM tool to create packages for various systems.

## Tools Section

| Name         | Required   | Type   | Default               | Description                                                                         |
|:-------------|:-----------|:-------|:----------------------|:------------------------------------------------------------------------------------|
| buildImage   | Required   | String |                       | Docker image containing the FPM tools as well as any other requirements.            |
| sourceType   |            | String | `dir`                 | Refer to [Sources documentation](http://fpm.readthedocs.io/en/latest/sources.html). |
| version      |            | String | `{{ build_version }}` | Version number to use for the resulting package, eqivalent to                       |
| name         |            | String | `<repo>`              | The name of the output package, format will be <name>-<version>.<target>            |
| sourceDir    |            | String |                       | When using the dir sourceType this is the directory that will get packaged.         |
| extraArgs    |            | String |                       | Any extra arguments to the FPM command.                                             |
| dependencies |            | List   |                       | A list of dependencies that are required by your output package.                    |
| targetTypes  |            | List   | `['rpm']`             | Formats to create with the command.                                                 |

## Available Methods

### package

> Create application packages for various systems with one tool.

| Name         | Required   | Type   | Default               | Description                                                                         |
|:-------------|:-----------|:-------|:----------------------|:------------------------------------------------------------------------------------|
| buildImage   | Required   | String |                       | Docker image containing the FPM tools as well as any other requirements.            |
| sourceType   |            | String | `dir`                 | Refer to [Sources documentation](http://fpm.readthedocs.io/en/latest/sources.html). |
| version      |            | String | `{{ build_version }}` | Version number to use for the resulting package, eqivalent to                       |
| name         |            | String | `<repo>`              | The name of the output package, format will be <name>-<version>.<target>            |
| sourceDir    |            | String |                       | When using the dir sourceType this is the directory that will get packaged.         |
| extraArgs    |            | String |                       | Any extra arguments to the FPM command.                                             |
| dependencies |            | List   |                       | A list of dependencies that are required by your output package.                    |
| targetTypes  |            | List   | `['rpm']`             | Formats to create with the command.                                                 |

### package Example

```yaml
branches:
  feature:
    steps:
    - ansible:
      - playbook:
      - playbook:
          extraVars:
            DOCKER_IMAGE: '{{ DOCKER_IMAGE_TAG }}'
          limit: qa
          playbook: scripts/ansible/example-playbook.yml
```

## Full Example Pipeline

```yaml
pipelines:
  branches:
    feature:
      steps:
      - fpm:
        - package:
            targetTypes:
            - deb
            - rpm
      - artifactory:
        - publish:
  tools:
    branches:
      patterns:
        develop: develop
        feature: .+
        master: master
```

## Additional Resources

* [FPM](https://github.com/jordansissel/fpm)