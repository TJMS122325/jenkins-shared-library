def call(Map cfg = [:]) {

    // --------------------
    // Config defaults
    // --------------------
    def skipTests     = cfg.get('skipTests', false)
    def image         = cfg.get('image', null)
    def mavenArgs     = cfg.get('mavenArgs', '')
    def dockerPush    = cfg.get('dockerPush', false)
    def dockerRegistry= cfg.get('dockerRegistry', '')
    def appVersion    = cfg.get('version', env.BUILD_NUMBER)

    // --------------------
    // Build
    // --------------------
    stage('Build') {
        sh "mvn -B clean package ${skipTests ? '-DskipTests' : ''} ${mavenArgs}"
    }

    // --------------------
    // Unit Tests
    // --------------------
    if (!skipTests) {
        stage('Unit Tests') {
            sh 'mvn -B test'
            junit '**/target/surefire-reports/*.xml'
        }
    }

    // --------------------
    // SonarQube
    // --------------------
    if (env.SONAR_SERVER) {
        stage('SonarQube Analysis') {
            withSonarQubeEnv(env.SONAR_SERVER) {
                sh 'mvn sonar:sonar'
            }
        }
    } else {
        echo 'SonarQube not configured, skipping'
    }

    // --------------------
    // Docker Build
    // --------------------
    if (image) {
        stage('Docker Build') {
            sh "docker build -t ${image}:${appVersion} ."
        }

        // --------------------
        // Docker Push (Optional)
        // --------------------
        if (dockerPush && dockerRegistry) {
            stage('Docker Push') {
                sh """
                  docker tag ${image}:${appVersion} ${dockerRegistry}/${image}:${appVersion}
                  docker push ${dockerRegistry}/${image}:${appVersion}
                """
            }
        }
    }

    // --------------------
    // Archive Artifacts
    // --------------------
    stage('Archive Artifacts') {
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
    }

    // --------------------
    // Notify
    // --------------------
    stage('Notify') {
        echo "Build ${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    }
}
