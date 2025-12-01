pipeline {
    agent any
    
    environment {
        RELEASE_NAME = 'bank-system'
        UMBRELLA_CHART_PATH = 'bank-umbrella'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }
        
        stage('Build All Services') {
            parallel {
                stage('Build accounts-service') {
                    steps {
                        dir('accounts-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build cash-service') {
                    steps {
                        dir('cash-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build transfer-service') {
                    steps {
                        dir('transfer-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build exchange-service') {
                    steps {
                        dir('exchange-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build exchange-generator') {
                    steps {
                        dir('exchange-generator') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build notification-service') {
                    steps {
                        dir('notification-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Build front-ui') {
                    steps {
                        dir('front-ui') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }
        
        stage('Test All Services') {
            parallel {
                stage('Test accounts-service') {
                    steps {
                        dir('accounts-service') {
                            sh 'mvn test'
                        }
                    }
                }
                stage('Test cash-service') {
                    steps {
                        dir('cash-service') {
                            sh 'mvn test'
                        }
                    }
                }
                stage('Test transfer-service') {
                    steps {
                        dir('transfer-service') {
                            sh 'mvn test'
                        }
                    }
                }
                stage('Test exchange-service') {
                    steps {
                        dir('exchange-service') {
                            sh 'mvn test'
                        }
                    }
                }
                stage('Test exchange-generator') {
                    steps {
                        dir('exchange-generator') {
                            sh 'mvn test'
                        }
                    }
                }
                stage('Test notification-service') {
                    steps {
                        dir('notification-service') {
                            sh 'mvn test'
                        }
                    }
                }
                stage('Test front-ui') {
                    steps {
                        dir('front-ui') {
                            sh 'mvn test'
                        }
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Build Docker Images') {
            parallel {
                stage('Docker: accounts-service') {
                    steps {
                        script {
                            docker.build("bank-app/accounts-service:${BUILD_NUMBER}", "./accounts-service")
                        }
                    }
                }
                stage('Docker: cash-service') {
                    steps {
                        script {
                            docker.build("bank-app/cash-service:${BUILD_NUMBER}", "./cash-service")
                        }
                    }
                }
                stage('Docker: transfer-service') {
                    steps {
                        script {
                            docker.build("bank-app/transfer-service:${BUILD_NUMBER}", "./transfer-service")
                        }
                    }
                }
                stage('Docker: exchange-service') {
                    steps {
                        script {
                            docker.build("bank-app/exchange-service:${BUILD_NUMBER}", "./exchange-service")
                        }
                    }
                }
                stage('Docker: exchange-generator') {
                    steps {
                        script {
                            docker.build("bank-app/exchange-generator:${BUILD_NUMBER}", "./exchange-generator")
                        }
                    }
                }
                stage('Docker: notification-service') {
                    steps {
                        script {
                            docker.build("bank-app/notification-service:${BUILD_NUMBER}", "./notification-service")
                        }
                    }
                }
                stage('Docker: front-ui') {
                    steps {
                        script {
                            docker.build("bank-app/front-ui:${BUILD_NUMBER}", "./front-ui")
                        }
                    }
                }
            }
        }
        
        stage('Helm Lint') {
            steps {
                echo 'Linting umbrella Helm chart...'
                sh "helm lint ${UMBRELLA_CHART_PATH}"
                sh "helm dependency update ${UMBRELLA_CHART_PATH}"
            }
        }
        
        stage('Deploy to Test') {
            steps {
                echo 'Deploying entire application to test namespace...'
                sh """
                    helm upgrade --install ${RELEASE_NAME} ${UMBRELLA_CHART_PATH} \\
                        --namespace test \\
                        --create-namespace \\
                        --set accounts-service.image.tag=${BUILD_NUMBER} \\
                        --set cash-service.image.tag=${BUILD_NUMBER} \\
                        --set transfer-service.image.tag=${BUILD_NUMBER} \\
                        --set exchange-service.image.tag=${BUILD_NUMBER} \\
                        --set exchange-generator.image.tag=${BUILD_NUMBER} \\
                        --set notification-service.image.tag=${BUILD_NUMBER} \\
                        --set front-ui.image.tag=${BUILD_NUMBER} \\
                        --wait \\
                        --timeout 10m
                """
            }
        }
        
        stage('Helm Test (Test)') {
            steps {
                echo 'Running Helm tests in test namespace...'
                sh "helm test ${RELEASE_NAME} --namespace test"
            }
        }
        
        stage('Deploy to Prod') {
            when {
                branch 'main'
            }
            steps {
                echo 'Deploying entire application to production namespace...'
                input message: 'Deploy entire application to production?', ok: 'Deploy'
                sh """
                    helm upgrade --install ${RELEASE_NAME} ${UMBRELLA_CHART_PATH} \\
                        --namespace prod \\
                        --create-namespace \\
                        --set accounts-service.image.tag=${BUILD_NUMBER} \\
                        --set cash-service.image.tag=${BUILD_NUMBER} \\
                        --set transfer-service.image.tag=${BUILD_NUMBER} \\
                        --set exchange-service.image.tag=${BUILD_NUMBER} \\
                        --set exchange-generator.image.tag=${BUILD_NUMBER} \\
                        --set notification-service.image.tag=${BUILD_NUMBER} \\
                        --set front-ui.image.tag=${BUILD_NUMBER} \\
                        --wait \\
                        --timeout 10m
                """
            }
        }
        
        stage('Helm Test (Prod)') {
            when {
                branch 'main'
            }
            steps {
                echo 'Running Helm tests in prod namespace...'
                sh "helm test ${RELEASE_NAME} --namespace prod"
            }
        }
    }
    
    post {
        success {
            echo 'Umbrella pipeline completed successfully!'
            echo "All services deployed with build number: ${BUILD_NUMBER}"
        }
        failure {
            echo 'Umbrella pipeline failed!'
        }
        always {
            cleanWs()
        }
    }
}
