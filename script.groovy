pipeline {
    environment{
    REGISTRY = "gramira/projectdevops"
    VERSION = "${BUILD_NUMBER}"
    }
    
    agent any
    
    stages {
        
        stage ('checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/ewreerturk/case-for-me'
            }
        }
        
        stage ('docker build') {
            steps {
                sh "pwd"
                sh "ls -la"
                sh "who am i"
                sh "docker build -t ${REGISTRY}:${VERSION} ./myapp/app/python/."
            }
        }
        stage ('docker push'){
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USER')]) {
    // some block
                    sh "docker login -u ${env.DOCKER_USER} -p  ${env.DOCKER_PASSWORD}"
                    sh "docker push ${REGISTRY}:${VERSION}"
                }
            }
        }
        
        stage ('kubernetes deploy') {
            steps {
                withKubeConfig(caCertificate: '', clusterName: '', contextName: '', credentialsId: 'kubeconfig', restrictKubeConfigAccess: false, serverUrl: '') { 
                    sh "sed -i 's|${REGISTRY}:.*|${REGISTRY}:'${VERSION}'|' ./myapp/kubernetes/deployment.yaml"
                    sh "kubectl apply -f ./myapp/kubernetes/"
                }  
            }
        
        }
    }
}
