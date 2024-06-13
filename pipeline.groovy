pipelineJob('pipeline') {
  definition {
    cps {
      script(
'''pipeline {
    agent {
        docker {
            image 'pipeline/jenkins'
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
        sh 'git clean -xffd\'
    }
}

        stage('git') {
            steps {
                echo 'Cloning the repository'
                git branch: 'master', credentialsId: 'github-credentials', url: 'https://github.com/SamarMatoussi/docker-spring-boot.git'
            }
        }
        stage('maven') {
    agent { docker 'maven:3.8.3-openjdk-8' }
    steps {
        script {
            dir('docker-spring-boot') {
                sh 'mvn clean install -DskipTests'                
            }
        }
    }
}
        stage('Build Image') {
            steps {
                script {
                    dockerImage = docker.build("${registry}:$BUILD_NUMBER")
                }
            }
        }
        stage('Deploy Image') {
            steps {
                script {
                    docker.withRegistry('', registryCredential) {
                        dockerImage.push()
                    }
                }
            }
        }
    }
}
''')
    }
  }
}
