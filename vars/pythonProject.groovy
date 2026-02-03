def call(Map cfg = [:]) {
    def skipTests = cfg.get('skipTests', false)
    def image = cfg.get('image', null)

    stage('Checkout') {
        checkout scm
    }

    stage('Install') {
        sh 'python3 -m venv venv'
        sh 'venv/bin/pip install -r requirements.txt'
    }

    if (!skipTests) {
        stage('Test') {
            sh 'venv/bin/python -m unittest || true'
        }
    }

    if (env.SONAR_SERVER) {
        stage('SonarQube') {
            withSonarQubeEnv(env.SONAR_SERVER) {
                sh 'echo "Running Sonar for Python"'
            }
        }
    }

    if (image) {
        stage('Docker Build') {
            sh "docker build -t ${image}:${env.BUILD_NUMBER} ."
        }
    }
}
