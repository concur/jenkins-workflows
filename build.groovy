#!/usr/bin/env groovy
import com.concur.*;

concurUtil = new com.concur.Util()

// Execute any build using a magefile
// See [Official Docs](https://magefile.org) for more about Magefiles.
// Main repo available at [github.com/magefile/mage](https://github.com/magefile/mage).
public mage(Map yml, Map args) {
  def buildImage  = args?.buildImage  ?: yml.tools?.mage?.buildImage
  def target      = args?.target      ?: yml.tools?.mage?.target
  def mageFileDir = args?.mageFileDir ?: yml.tools?.mage?.mageFileDir ?: '.'

  def cmd = "mage"

  if (target) {
    cmd = "$cmd $target"
  }

  dir(mageFileDir) {
    docker.image(buildImage).inside {
      sh concurUtil.mustacheReplaceAll(cmd)
    }
  }
}

return this;
