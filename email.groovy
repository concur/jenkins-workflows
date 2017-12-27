import com.concur.*;

concurPipeline  = new Commands()
concurUtil      = new Util()

public send(yml, options) {
  def emailData = yml.tools?.email ?: [:]

  switch (options) {
    case Map:
      emailData = emailData.plus(options)
      break
    case String:
      emailData.put('body', options)
      break
    default:
      error("The format provided for email options is not supported.")
      break
  }

  assert emailData.to : "No email recipient provided."
  assert (emailData.subject || emailData.body) : "No email subject or body provided."
  if (!emailData.from) {emailData.put('from', 'buildhub@concur.com')}

  concurPipeline.debugPrint('Workflows :: email :: send', emailData)

  emailData.body    = emailData.body    ? concurUtil.mustacheReplaceAll(emailData.body) : '.'
  emailData.subject = emailData.subject ? concurUtil.mustacheReplaceAll(emailData.subject) : 'No Subject'
  
  mail(emailData)
}

/*
 ******************************* COMMON *******************************
 This a section for common utilities being called from the runSteps method in com.concur.Commands
 */

public getStageName(Map yml, Map args, String stepName) {
  switch(stepName) {
    case 'send':
      def emailTo = args?.to ?: yml.tools?.email?.to
      return emailTo ? "email: send: ${emailTo}" : 'email: send'
  }
}

return this;
