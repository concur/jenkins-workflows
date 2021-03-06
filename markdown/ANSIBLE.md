# Ansible

## Overview

> Execute Ansible playbooks within a pipeline.

## Tools Section

| Name        | Required   | Type    | Default   | Description                                                                                 |
|:------------|:-----------|:--------|:----------|:--------------------------------------------------------------------------------------------|
| buildImage  | Required   | String  |           | Docker image that has Ansible installed.                                                    |
| playbook    | Required   | String  |           | Path to the playbook file used during this run.                                             |
| inventory   | Required   | String  |           | Path to an inventory file to run the playbook against.                                      |
| limit       | Optional   | String  |           | Equivalent to `-l` or `--limit` only run against specific host groups.                      |
| sudoUser    | Optional   | String  | `root`    | Equivalent to `--become-user`.                                                              |
| credentials | Optional   | Map     |           | Key/Value pair of the credentials to use when running the playbook.                         |
| extraVars   | Optional   | Map     |           | equivalent to `-e` or `--extra-vars` overwrite variables.                                   |
| tags        | Optional   | List    |           | Run only specific tags during the playbook run.                                             |
| skippedTags | Optional   | List    |           | Skip the specified tags during the playbook run.                                            |
| extras      | Optional   | List    |           | Additional arguments to the `ansible-playbook` command.                                     |
| sudo        | Optional   | Boolean | `False`   | Equivalent to `-b` or `--become`.                                                           |
| forks       | Optional   | int     | `10`      | Equivalent to `-f` or `--forks` specify number of parallel processes to use.                |
| verbosity   | Optional   | int     |           | Levels of verbose output to have. Example setting this to 2 would be the equivalent of -vv. |

## Available Methods

### playbook

> Execute an Ansible playbook.

| Name        | Required   | Type    | Default   | Description                                                                                 |
|:------------|:-----------|:--------|:----------|:--------------------------------------------------------------------------------------------|
| buildImage  | Required   | String  |           | Docker image that has Ansible installed.                                                    |
| playbook    | Required   | String  |           | Path to the playbook file used during this run.                                             |
| inventory   | Required   | String  |           | Path to an inventory file to run the playbook against.                                      |
| limit       | Optional   | String  |           | Equivalent to `-l` or `--limit` only run against specific host groups.                      |
| sudoUser    | Optional   | String  | `root`    | Equivalent to `--become-user`.                                                              |
| credentials | Optional   | Map     |           | Key/Value pair of the credentials to use when running the playbook.                         |
| extraVars   | Optional   | Map     |           | equivalent to `-e` or `--extra-vars` overwrite variables.                                   |
| tags        | Optional   | List    |           | Run only specific tags during the playbook run.                                             |
| skippedTags | Optional   | List    |           | Skip the specified tags during the playbook run.                                            |
| extras      | Optional   | List    |           | Additional arguments to the `ansible-playbook` command.                                     |
| sudo        | Optional   | Boolean | `False`   | Equivalent to `-b` or `--become`.                                                           |
| forks       | Optional   | int     | `10`      | Equivalent to `-f` or `--forks` specify number of parallel processes to use.                |
| verbosity   | Optional   | int     |           | Levels of verbose output to have. Example setting this to 2 would be the equivalent of -vv. |

### playbook Example

```yaml
branches:
  feature:
    steps:
      - ansible:
          # Simple
          - playbook:
          # Advanced
          - playbook:
              playbook: scripts/ansible/example-playbook.yml
              extraVars:
                DOCKER_IMAGE: "{{ DOCKER_IMAGE_TAG }}"
              limit: qa
```

## Full Example Pipeline

```yaml
pipelines:
  tools:
    ansible:
      credentials:
        description: "SSH deploy credentials"
      buildImage: "{{ quay_uri }}/da-workflow/ansible-alpine:2.4.1.0"
      playbook: "ansible/playbooks/app_deploy.yml"
      inventory: "ansible/app_inventory.yml"
    branches:
      patterns:
        master: master
        develop: develop
        feature: .+
  branches:
    feature:
      steps:
        - custom: # This should be your build process
          - buildPackage:
        - ansible:
          - playbook:
              limit: staging
    master:
      steps:
        - github:
          - createRelease:
        - ansible:
          - playbook:
              limit: production
```

## Additional Resources

* [Ansible](https://docs.ansible.com/ansible/latest/playbooks.html)
* [Docker Images](https://hub.docker.com/ansible/ansible/)