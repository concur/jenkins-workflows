public work(Map yml, Map args) {
  println "yml :: $yml"
  println "args :: $args"
}

/*
 * Allow the Workflow execution to determine how to name each stage.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'work':
      if (args?.name) {
        return "example: work: ${args.name}"
      } else {
        return 'example: work'
      }
  }
}

return this;
