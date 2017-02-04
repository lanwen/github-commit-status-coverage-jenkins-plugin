## Reports coverage as status

Based on jacoco and github plugin. Requires Jacoco publisher enabled to work


## Generated Job DSL example

```groovy
        publishers {
             jacocoCodeCoverage {
                 exclusionPattern('**/path/*.class,**/path2/sub/*.class')
             }
 
             gitHubCommitStatusSetter {
                 commitShaSource {
                     manuallyEnteredShaSource {
                         sha('$GITHUB_PR_HEAD_SHA')
                     }
                 }
                 contextSource {
                     manuallyEnteredCommitContextSource {
                         context('coverage/line')
                     }
                 }
 
                 reposSource {
                     anyDefinedRepositorySource()
                 }
 
                 statusBackrefSource {
                     buildRefBackrefSource()
                 }
 
                 statusResultSource {
                     coverageStatusResultSource {
                         baseJob('master_job')
                     }
                 }
             }
 
             mailer('some@mail.dot', false, true)
         }
```
