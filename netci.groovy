// Import the utility functionality.
import jobs.generation.Utilities;

// Defines a the new of the repo, used elsewhere in the file
def project = GithubProject
def branch = GithubBranchName

// Generate the builds for commit and PRJob
[true, false].each { isPR ->
    ['Windows_NT', 'Ubuntu16.04', 'OSX10.12',].each { osName ->
        def newJobName = Utilities.getFullJobName(project, osName, isPR)

        def newJob = job(newJobName) {
            steps {
                if (osName == 'Windows_NT') {
                    batchFile("build.cmd -c -ci")
                }
                else {
                    shell("sudo HOME=\$WORKSPACE/tempHome ./build.sh -c --ci")
                }
            }
        }

        Utilities.setMachineAffinity(newJob, osName, 'latest-or-auto')
        Utilities.standardJobSetup(newJob, project, isPR, "*/${branch}")

        if (isPR) {
            Utilities.addGithubPRTriggerForBranch(newJob, branch, "${osName}")
        }
        else {
            Utilities.addGithubPushTrigger(newJob)
        }
    }
}