def call(Map cfg = [:]) {
    def skipTests = cfg.get('skipTests', false)
    def image = cfg.get('image', null)

    stage('Checkout') {
        checkout scm
    }

    stage('Build') {
        sh "mvn -B ${skipTests ? '-DskipTests' : ''} package"
    }

    if (env.SONAR_SERVER) {
        stage('SonarQube') {
            withSonarQubeEnv(env.SONAR_SERVER) {
                sh 'mvn sonar:sonar'
            }
        }
    }

    if (image) {
        stage('Docker Build') {
            sh "docker build -t ${image}:${env.BUILD_NUMBER} ."
        }
    }
}
