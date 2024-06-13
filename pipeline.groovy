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
                git branch: 'master', credentialsId: 'github-credentials', url: 'https://github.com/SamarMatoussi/docker-spring-boot.git'
            }
        }
        stage('MVN CLEAN'){
            steps {
                sh 'export PATH=$PATH:/opt/maven/bin && mvn clean install'
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
}''')
    }
  }
}
