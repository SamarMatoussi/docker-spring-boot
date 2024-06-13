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
        stage('git') {
            steps {
                echo 'Cloning the repository'
                git branch: 'master', credentialsId: 'github-credentials', url: 'https://github.com/SamarMatoussi/JCasC.git'
                sh 'docker run hello-world'
            }
        }
        stage('MVN CLEAN'){
            steps {
            bat 'mvn clean'
            }
        }
        stage('unit_test') {
            steps {
                sh 'echo "Start the unit test"'
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
     stage('Deploy to Production') {
            steps {
                script {
                    docker.withRegistry('', registryCredential) {
                        dockerImage.pull()
                        docker.image("${registry}:$BUILD_NUMBER").run()
                    }
                }
            }
        }    
    }
}''')
    }
  }
}
