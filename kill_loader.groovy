pipeline {
    agent any
    
    environment {
        server1="ecom@10.189.24.170"
        server2="ecom@10.189.24.171"
        server3="ecom@10.189.24.172"
        server4="ecom@10.189.24.173"
    }
    
    stages {

        stage('Get Kill script') {
            
            steps {
                sh "rm -rf ./*"
                git credentialsId: 'Gitlab.com', url: 'https://gitlab.com/konstantin.schepkin/perf.git'
            }
        }

      stage('Copy script') {

            steps {
            parallel(
                        "Load1": {
                            sh "scp scripts/kill.sh ${server1}:~"
                        },
                        "Load2": {
                            sh "scp scripts/kill.sh ${server2}:~"
                        },
                        "Load3": {
                            sh "scp scripts/kill.sh ${server3}:~"
                        },
                        "Load4": {
                            sh "scp scripts/kill.sh ${server4}:~"
                        }
            )
        }
    }

      stage('Kill') {
            

            steps {
                parallel(
                        "Load1": {
                            sh "ssh  ${server1} \"chmod +x kill.sh && bash kill.sh\""
                        },
                        "Load2": {
                            sh "ssh  ${server2} \"chmod +x kill.sh && bash kill.sh\""
                        },
                        "Load3": {
                            sh "ssh  ${server3} \"chmod +x kill.sh && bash kill.sh\""
                        },
                        "Load4": {
                            sh "ssh  ${server4} \"chmod +x kill.sh && bash kill.sh\""
                        }
              ) 
        }
      }
  } 
}