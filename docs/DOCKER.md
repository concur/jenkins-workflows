# Docker

## Overview

Manage Docker images within your pipeline.

## Tools Section

| Argument            | Constraint  | Type            | Default          | Description                                                                      |
|---------------------|-------------|-----------------|------------------|----------------------------------------------------------------------------------|
| dockerfile          | Optional    | String          |                  | Path to the Dockerfile.                                                          |
| buildArgs           | Optional    | String/Map/List |                  | Anything passed as `--build-arg`                                                 |
| imageName           | Optional    | String          | `${org}/${repo}"`| What to name the built image.                                                    |
| imageTag            | Optional    | String          | `buildVersion`   | What to tag the image as after it is built.                                      |
| contextPath         | Optional    | String          | `'.'`            | Context path for the Docker build same as the `.` at the end of `docker build`.  |
| uri                 | Optional    | String          | `env.DOCKER_URI` | URI for the Docker registry, default is currently Artifactory.                   |
| additionalTags      | Optional    | List            |                  | Any additional tags to push to the Docker registry.                              |
| credentials         | Optional    | Map             |                  | Key/Value pair of the credentials to use when running the playbook.              |

### Tools Example

```yaml
tools:
  docker:
    image: examples/golang-tools:0.1.0
    promoteLatest:
      develop: true
    skipLatestCommit:
      develop: true
      master: false
 ```

## Available Methods

### Build

> Build a Docker image

| Argument    | Constraint  | Type            | Default                               | Description                                                                         |
|-------------|-------------|-----------------|---------------------------------------|-------------------------------------------------------------------------------------|
| buildArgs   | Optional    | String/Map/List |                                       | Anything passed as `--build-arg`.                                                   |
| dockerfile  | Optional    | String          |                                       | Relative path to the Dockerfile.                                                    |
| imageName   | Optional    | String          | `${org}/${repo}"`                     | What to name the built image.                                                       |
| imageTag    | Optional    | String          | `buildVersion`                        | What to tag the image as after it is built.                                         |
| contextPath | Optional    | String          | `'.'`                                 | Context path for the Docker build same as the `.` at the end of `docker build`.     |
| vcsUrl      | Optional    | String          | `https://github.concur.com/org/repo`  | Link to the GitHub enterprise repository for the build, can be used as a build arg. |

### Build Examples

```yaml
branches:
  feature:
    steps:
      - docker:
        # Simple
        - build: # Defaults to pushing image to Artifactory
        # Advanced
        - build:
            dockerfile: production.dockerfile
            buildArgs:
              http_proxy: "{{ http_proxy }}"
              no_proxy: "{{ no_proxy }}"
              VCS_URL: "{{ vcs_url }}"
            credentials:
              description: Quay robot user
  ```

### Push

> Push a newly created image to a Docker registry.

| Argument            | Constraint  | Type            | Default          | Description                                                                      |
|---------------------|-------------|-----------------|------------------|----------------------------------------------------------------------------------|
| imageName           | Optional    | String          | `${org}/${repo}"`| What to name the built image.                                                    |
| imageTag            | Optional    | String          | `buildVersion`   | What to tag the image as after it is built.                                      |
| uri                 | Optional    | String          | `env.DOCKER_URI` | URI for the Docker registry, default is currently Artifactory.                   |
| additionalTags      | Optional    | List            |                  | Any additional tags to push to the Docker registry.                              |
| credentials         | Optional    | Map             |                  | Key/Value pair of the credentials to use when running the playbook.              |

### Push Examples

```yaml
branches:
  feature:
    steps:
      - docker:
        # Simple
        - push: # Defaults to pushing image to Artifactory
        # Advanced
        - push:
            dockerEndpoint: "{{ QUAY_URI }}"
            credentials:
              description: Quay robot user
  ```

## Full Pipeline Example

```yaml
pipelines:
  general:
    debug: true
    version:
      type: semver
      base: 1.0.0
  tools:
    buildhub:
      workflows:
        branch: "2.2.0"
    github:
      patterns:
        feature: .+
    docker:
      buildArgs:
        CommitSHA: "{{ git_commit }}"
        BuildVersion: "{{ build_version }}"
      credentials:
        description: quay.io robot
      imageName: concur/example-docker-image
      uri: quay.io
  branches:
    feature:
      steps:
        - docker:
          - build:
              command: install
          - push:
          - push:
              uri: quay.io
```


## Additional Resources

* [Promotion - Artifactory](https://wiki.concur.com/confluence/display/DA/Promotion+-+Artifactory)
* [Docker build documentation](https://docs.docker.com/engine/reference/commandline/build/)
* [Docker push documentation](https://docs.docker.com/engine/reference/commandline/push/)
