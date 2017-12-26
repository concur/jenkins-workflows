# Ansible

## Overview

Execute Ansible playbooks within a pipeline.

## Tools Section

These can be specified in the tools.ansible section or in an individual step

| Argument    | Constraint  | Type          | Default | Description                                                                   |
|-------------|-------------|---------------|---------|-------------------------------------------------------------------------------|
| credentials | Required    | Map           |         | Key/Value pair of the credentials to use when running the playbook.           |
| buildImage  | Required    | String        |         | Docker image that has Ansible installed.                                      |
| playbook    | Required    | String        |         | Path to the playbook file used during this run.                               |
| inventory   | Required    | String        |         | Path to an inventory file to run the playbook against.                        |
| extraVars   | Optional    | Map           |         | equivalent to `-e` or `--extra-vars` overwrite variables.                     |
| tags        | Optional    | String        |         | Run only specific tags during the playbook run.                               |
| skippedTags | Optional    | String        |         | Skip the specified tags during the playbook run.                              |
| verbosity   | Optional    | Number/String | `2`     | Levels of verbose output to have.                                             |
| extras      | Optional    | String        |         | Additional arguments to the `ansible-playbook` command.                       |
| limit       | Optional    | String        |         | Equivalent to `-l` or `--limit` only run against specific host groups.        |
| sudo        | Optional    | Boolean       | `false` | Equivalent to `-b` or `--become`.                                             |
| sudoUser    | Optional    | String        | `root`  | Equivalent to `--become-user`.                                                |
| forks       | Optional    | Number        | `10`    | Equivalent to `-f` or `--forks` specify number of parallel processes to use.  |

## Available Methods

### Playbook

> Execute an Ansible playbook
> Anything from the tools section can be specified as arguments to the step as well.

#### Playbook Examples

```yaml
branches:
  feature:
    steps:
      - ansible:
          # Simple
          - playbook: # looks at only what is under tools.ansible
          # Advanced
          - playbook:
              playbook: scripts/ansible/example-playbook.yml # will overwrite from tools.ansible.playbook
              extraVars: # -e DOCKER_IMAGE=1.0.4.2234234
                DOCKER_IMAGE: "{{ DOCKER_IMAGE_TAG }}"
              verbosity: 3 # equivalent to -vvv
              limit: qa # equivalent to -l qa
 ```

## Full pipeline example

```yaml
pipelines:
  tools:
    ansible:
      credentials:
        description: "SSH deploy credentials"
      buildImage: "{{ quay_uri }}/da-workflow/ansible-alpine:2.4.1.0"
      playbook: "ansible/playbooks/app_deploy.yml"
      inventory: "ansible/app_inventory.yml"
    github:
      patterns:
        master: master
        develop: develop
        feature: .+
  branches:
    feature:
      steps:
        - custom: # This should be your build process
          - buildPackage:
        - deploy:
          - to: awsqa
    master:
      steps:
        - deploy:
          - to: awsprod
  deployments:
    awsqa:
      steps:
        - ansible:
          - playbook:
              limit: awsqa
              verbosity: 3
    awsprod:
      steps:
        - custom:
          - deploy:

[...]
```

## Additional Resources

* [Ansible](https://docs.ansible.com/ansible/latest/playbooks.html)