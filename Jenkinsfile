pipeline {
    agent any

    tools {
        maven 'Maven3'   // Must match the name configured in Jenkins Global Tool Configuration
    }

    environment {
        // these are the environment variables (Amir's machine) that will be used in the pipeline
       PATH = "C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"
       JAVA_HOME = 'C:\\Program Files\\Java\\jdk-21.0.12.1'  // Adjust to your actual JDK path


        SONARQUBE_SERVER = 'SonarQubeServer'
        SONAR_TOKEN = credentials('SONAR_TOKEN') //make sure a right token is selected
        DOCKERHUB_CREDENTIALS_ID = 'ca3e514b-32b9-4ac2-ab59-c733071649c5'
        DOCKERHUB_REPO = 'gregtish/bmi_3013_greg_task'
        DOCKER_IMAGE_TAG = 'latest'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/GrigoriiTishchuk/RTL_BMI_Solution_Instructions.git'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean install'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test'
            }
        }
// Check the SonarQube analysis stage for the correct project key and name
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${env.SONARQUBE_SERVER}") {
                    bat """
                        mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar ^
                        -Dsonar.projectKey=BMI_CL ^
                        -Dsonar.sources=src/main ^
                        -Dsonar.tests=src/test ^
                        -Dsonar.projectName=BMI_CL ^
                        -Dsonar.host.url=http://localhost:9000 ^
                        -Dsonar.java.binaries=target/classes ^
                        -Dsonar.token=%SONAR_TOKEN%
                    """
                }
            }
        }
        stage('Publish Coverage Report') {
            steps {
                jacoco execPattern: 'target/jacoco.exec'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def app = docker.build("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}")
                    env.BUILT_IMAGE = app.id  // Optional: store image ID for reuse
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKERHUB_CREDENTIALS_ID) {
                       docker.image("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}").push()
                    }
                }
            }
        }
    }


}
