# Jenkins Workflows

## v0.3.0

### Added

* golang.lint: Lint Golang code during a pipeline execution.
* golang.dep: Support for the official dependency manager for Golang.

### Fixes

* docker.push: Fixed issue causing wrong credential to be pulled.
* golang.getStageName: Fixed issue causing test workflow to fail due to type safety.

## v0.2.0

### Added

* Gradle workflow
* Slack workflow
* NodeJS workflow

### Updates

* git.commit: Checks for changes before attempting the commit, this didn't cause a failure before but is cleaner now.
* Add more asserts to workflows.

## v0.1.1

### Fixed

* github.createRelease: Fix issue where generated name/tag can get multiple `v`s for a prefix.

## 0.1.0

### Added

Please view the associated documentation on how to use these workflows, or visit [GitHub pages](https://concur.github.io/jenkins-workflows/) generated documentation.

* [Ansible](docs/ANSIBLE.md)
* [Build](docs/BUILD.md)
* [Docker](docs/DOCKER.md)
* [Email](docs/EMAIL.md)
* [Fpm](docs/FPM.md)
* [Git](docs/GIT.md)
* [Github](docs/GITHUB.md)
* [Golang](docs/GOLANG.md)
* [Python](docs/PYTHON.md)
* [Rust](docs/RUST.md)
