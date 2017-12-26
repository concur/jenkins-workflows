# GitHub

## Overview

Manipulate pull requests, releases in GitHub within your pipeline.

## Available Methods

### createPullRequest

| Argument    | Constraint | Type   | Default                                             | Description                                       |
|-------------|------------|--------|-----------------------------------------------------|---------------------------------------------------|
| fromBranch  | Optional   | String | `env.BRANCH_NAME`                                   | The branch the pull request is coming from.       |
| toBranch    | Optional   | String | `master`                                            | Target branch for the pull request to merge into. |
| title       | Optional   | String | `Merge {{ from_branch }} into {{ target_branch }}`  | Title of pull request                             |
| summary     | Optional   | String | `Created by Buildhub run {{ build_url }}.`          | Summary description for pull request              |
| org         | Optional   | String | `Inferred from SCM settings`                        | Github organization                               |
| repo        | Optional   | String | `Inferred from SCM settings`                        | Github repository                                 |

### createPullRequest Examples

```yaml
branches:
  feature:
    steps:
      - pullRequest:
        # Simple
        - create:
        # Advanced
        - create:
            toBranch: develop
            title: "Pull {{ current_branch }} to {{ target_branch }}"
 ```

### Additional Resources

* [Github documentation](https://help.github.com/articles/about-pull-requests/)
