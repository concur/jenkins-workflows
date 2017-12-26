# Build

## Overview

Run various generic build tools including [Mage](1).

### Mage

#### Tools Section

| Argument            | Constraint | Type     | Default | Description                                                     |
|---------------------|------------|----------|---------|-----------------------------------------------------------------|
| **buildImage**      | Required   | String   |         | Public (or already pulled) Docker image to run the commands in. |
| **target**          | Optional   | String   |         | The mage target to run.                                         |
| **mageFileDir**     | Optional   | String   |         | The directory containing your magefile.                         |

## Available Methods

[1]: https://github.com/magefile/mage
