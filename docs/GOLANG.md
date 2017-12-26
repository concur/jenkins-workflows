# Golang

## Overview

Build and test Golang code in your pipeline.

## Tools Section

| Argument        | Constraint  | Type        | Default                               | Description                                                         |
|-----------------|-------------|-------------|---------------------------------------|---------------------------------------------------------------------|
| buildImage      | Required    | String      |                                       | A Docker image containing necessary Golang tools.                   |
| goPath          | Optional    | String      | `/go/src/<host>/<org>/<repo>`         | The path inside the Docker container to mount the code.             |
| outFile         | Optional    | String      |                                       | Where to output for `go build` command, equivalent to `-o`.         |
| env             | Optional    | String/Map  |                                       | Any environment variables to a command such as `GOARCH` or `GOOS`.  |
| additionalArgs  | Optional    | String/List |                                       | Any additional arguments to a command such as `-v` for verbose.     |
| testBinary      | Optional    | String      | `go test`                             | An alternative to `go test` if necessary.                           |
| testResultsPath | Optional    | String      | `test_results`                        | The folder containg test results for JUnit.                         |
| gatherJunit     | Optional    | Boolean     | `false`                               | Whether or not to attempt gathering JUnit results.                  |
| junitPattern    | Optional    | String      | `${resultsPath}/*.xml`                | Pattern for JUnit style test results for the JUnit plugin.          |

```yaml
tools:
  golang:
    buildImage: rew-examples/golang-tools:0.1.0
    goPath: /go/src/github.concur.com/rew-examples/math-api-golang
    env:
      CGO_ENABLED: 0
      GOOS: linux
      GOARCH: amd64
 ```

## Available Methods

### Glide

> Commands for the `glide` dependency manager

| Argument        | Constraint  | Type        | Default                               | Description                                                     |
|-----------------|-------------|-------------|---------------------------------------|-----------------------------------------------------------------|
| buildImage      | Required    | String      |                                       | A Docker image containing necessary Golang tools.               |
| goPath          | Optional    | String      | `/go/src/<host>/<org>/<repo>`         | The path inside the Docker container to mount the code.         |
| additionalArgs  | Optional    | String/List |                                       | Any additional arguments to a command such as `-v` for verbose. |
| command         | Optional    | String      | `ensure`                              | The godep subcommand to run.                                    |

### Glide Examples

```yaml
branches:
  feature:
    steps:
      - golang:
          # Simple, will pull from tools section as defaults
          - glide:
          # Providing arguments
          - glide: install
 ```

### Godep

> Commands for the `godep` dependency manager

| Argument        | Constraint  | Type        | Default                               | Description                                                     |
|-----------------|-------------|-------------|---------------------------------------|-----------------------------------------------------------------|
| buildImage      | Required    | String      |                                       | A Docker image containing necessary Golang tools.               |
| goPath          | Optional    | String      | `/go/src/<host>/<org>/<repo>`         | The path inside the Docker container to mount the code.         |
| additionalArgs  | Optional    | String/List |                                       | Any additional arguments to a command such as `-v` for verbose. |
| command         | Optional    | String      | `ensure`                              | The godep subcommand to run.                                    |

### Godep Examples

```yaml
branches:
  feature:
    steps:
      - golang:
          # Simple, will pull from tools section as defaults
          - godep:
          # Providing arguments
          - godep:
              command: update
 ```

### Build

> Run a customizable `go build`

| Argument        | Constraint  | Type        | Default                               | Description                                                         |
|-----------------|-------------|-------------|---------------------------------------|---------------------------------------------------------------------|
| buildImage      | Required    | String      |                                       | A Docker image containing necessary Golang tools.                   |
| goPath          | Optional    | String      | `/go/src/<host>/<org>/<repo>`         | The path inside the Docker container to mount the code.             |
| outFile         | Optional    | String      |                                       | Where to output for `go build` command, equivalent to `-o`.         |
| env             | Optional    | String/Map  |                                       | Any environment variables to a command such as `GOARCH` or `GOOS`.  |
| additionalArgs  | Optional    | String/List |                                       | Any additional arguments to a command such as `-v` for verbose.     |

### Build Examples

```yaml
branches:
  feature:
    steps:
      - golang:
          # Simple, will pull from tools section as defaults
          - build:
          # Providing arguments
          - build:
              outFile: publish/go-example-binary
              env:
                CGO_ENABLED: 0
                GOOS: linux
                GOARCH: amd64
 ```

### Test

> Run tests on your Golang code before building.

| Argument        | Constraint  | Type        | Default                               | Description                                                     |
|-----------------|-------------|-------------|---------------------------------------|-----------------------------------------------------------------|
| buildImage      | Required    | String      |                                       | A Docker image containing necessary Golang tools.               |
| goPath          | Optional    | String      | `/go/src/<host>/<org>/<repo>`         | The path inside the Docker container to mount the code.         |
| additionalArgs  | Optional    | String/List |                                       | Any additional arguments to a command such as `-v` for verbose. |
| binary          | Optional    | String      | `go test`                             | An alternative to `go test` if necessary.                       |
| resultsPath     | Optional    | String      | `test_results`                        | The folder containg test results for JUnit.                     |
| gatherJunit     | Optional    | Boolean     | `false`                               | Whether or not to attempt gathering JUnit results.              |
| junitPattern    | Optional    | String      | `${resultsPath}/*.xml`                | Pattern for JUnit style test results for the JUnit plugin.      |

### Test Examples

```yaml
branches:
  feature:
    steps:
      - golang:
          # Simple, will pull from tools section as defaults
          - test:
          # Providing arguments
          - test:
              binary: ginkgo
              gatherJunit: true
```

## Full pipeline example

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
    golang:
      buildImage: "{{ quay_uri }}/examples/golang-example-tools"
      gatherJunit: true
      junitPattern: test_results/*.xml
  branches:
    feature:
      steps:
        - golang:
          - glide:
              command: install
          - test:
              binary: ginkgo
              additionalArgs: "$(glide novendor)"
          - build:
              outFile: publish/math-api-helm
              env:
                CGO_ENABLED: 0
                GOOS: linux
                GOARCH: amd64
```

### Additional Resources

* [Official Golang site](https://golang.org)
