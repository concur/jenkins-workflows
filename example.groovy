import com.concur.*;

public work(Map yml, Map args) {
  println "yml :: $yml"
  println "args :: $args"
}

/*
 * Set the name of the stage dynamically.
 */
public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'work':
      return args?.name ? "example: work: ${args.name}" : 'example: work'
  }
}

return this;
