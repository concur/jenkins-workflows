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
      return args?.name ? "example: work: ${args.name}" : 'example: work'
  }
}

return this;
