# Email

## Overview

Provides email messaging using the Concur pipelines.yml file.

## Functionality

All of the functionality of the Jenkins Mailer plugin is encapsulated in our implementation. You can pass all the same parameters that you would when using the plugin, except that you can define them in the pipelines file.

## Tools Section

In order to use the email functionality, simply add the necessary fields to the pipelines.yml file. The default settings are pulled from the tools section of the file, and can be overwritten in any email step that you define.

example

```yaml
tools:
  email:
    to: default_recipient@concur.com
    from: Jenkinsbuild@concur.com
    subject: default_subject
```

The email workflow requires at minimum that the 'to' field and either the 'subject' OR 'body' field is defined. If a subject is not defined, the email will get sent with the subject line, "No Subject". If the email body is not defined, the email will have a single period in the body.

## Examples

This workflow will take a string argument that will be used as the email's body:

```yaml
branches:
  features:
    steps:
      - email:
        # Simple
        - send: "Please visit {{ build_url }} for build results."
        # Advanced
        - send:
            to: special_recipient@concur.com
            subject: "Build number {{ build_number }} status"
            body: "Please visit {{ build_url }} for build results."
```

### Available Parameters

All parameters available for the [Jenkins Mailer plugin](https://github.com/jenkinsci/mailer-plugin) can be used in the parameterized syntax.
