def call(Map params = [:]) {
    // Orchestrator: run small steps in sequence so template is modular
    compileStep()
    packageStep()
    sonarStep()
    unitTestStep()
    publishStep()
    notifyStep()
}

def compileStep() {
    stage('Compile') {
        echo 'Compiling with Maven'
        sh 'mvn -B -DskipTests compile'
    }
}

def packageStep() {
    stage('Package') {
        echo 'Packaging artifact'
        sh 'mvn -B -DskipTests package'
    }
}

def sonarStep() {
    stage('Sonar') {
        if (env.SONAR_SERVER) {
            withSonarQubeEnv(env.SONAR_SERVER) {
                sh "mvn sonar:sonar -Dsonar.projectKey=${project?.artifact ?: 'app'} -Dsonar.projectName=${project?.artifact ?: 'app'}"
            }
        } else {
            echo 'Sonar not configured; skipping'
        }
    }
}

def unitTestStep() {
    stage('Unit Tests') {
        echo 'Running unit tests'
        sh 'mvn -B test'
        junit '**/target/surefire-reports/*.xml'
    }
}

def publishStep() {
    stage('Publish') {
        echo 'Publishing artifact (simulated)'
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: true
    }
}

def notifyStep() {
    stage('Notify') {
        echo "Build ${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    }
}
