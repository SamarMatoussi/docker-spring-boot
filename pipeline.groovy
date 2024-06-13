pipelineJob('pipeline') {
  definition {
    cps {
      script(
'''pipeline {
    agent {
        docker {
            image 'maven:3.8.3-openjdk-8'
            args '-u 0:0 -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }
    environment {
        registry = "samarmatoussi/jcasc"
        registryCredential = 'dockerHub'
        dockerImage = ""
    }
    stages {
        stage('Clean workspace') {
            steps {
                sh 'git clean -xffd'
            }
        }
        stage('Clone repository') {
            steps {
                echo 'Cloning the repository'
                git branch: 'master', credentialsId: 'github-credentials', url: 'https://github.com/SamarMatoussi/docker-spring-boot.git\'
            }
        }
        stage('Maven Build') {
            steps {
                dir('docker-spring-boot') {
                    sh 'mvn clean install -DskipTests'
                }
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    dockerImage = docker.build("${registry}:$BUILD_NUMBER")
                }
            }
        }
        stage('Deploy Docker Image') {
            steps {
                script {
                    docker.withRegistry('', registryCredential) {
                        dockerImage.push()
                    }
                }
            }
        }
    }
    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
''')
    }
  }
}
