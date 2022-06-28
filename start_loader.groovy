pipeline {
    agent any

    environment {
        server1="extroot@10.250.226.10"
        server2="extroot@10.250.226.11"
        server3="extroot@10.250.226.12"
        server4="extroot@10.250.226.13"
    }

    stages {

        stage('Get Artifacts') {

            steps {
                sh "rm -rf ./*"
                git credentialsId: 'e74ec59e-762a-4076-894c-56461d461af4', url: 'https://gitlab.com/vlad24011/load.git'
                // sh "wget https://apache-mirror.rbc.ru/pub/apache//jmeter/binaries/apache-jmeter-5.3.zip -O ./apache-jmeter-5.3.zip"
                sh "unzip ./apache-jmeter-5.3.zip -d ./"
                // sh "wget https://github.com/NovatecConsulting/JMeter-InfluxDB-Writer/releases/download/v-1.2/JMeter-InfluxDB-Writer-plugin-1.2.jar -O ./apache-jmeter-5.3/lib/ext/JMeter-InfluxDB-Writer-plugin-1.2.jar"
                // sh "wget https://jmeter-plugins.org/files/packages/jpgc-casutg-2.9.zip"
                // sh "unzip jpgc-casutg-2.9.zip -d ./apache-jmeter-5.3/"
                sh "chmod 0755 ./apache-jmeter-5.3/bin/jmeter.sh"

                sh "zip -r load.zip apache-jmeter-5.3 scripts/36"
            }
        }

        stage('Copy artifacts') {

            steps {
                parallel(
                        "Load1": {
                            sh "scp load.zip ${server1}:~"
                        },
                        "Load2": {
                            sh "scp load.zip ${server2}:~"
                        },
                        "Load3": {
                            sh "scp load.zip ${server3}:~"
                        },
                        "Load4": {
                            sh "scp load.zip ${server4}:~"
                        }
                )
            }
        }



        stage('Prepare loaders') {
            steps {
                parallel(
                        "Load1": {
                            sh "ssh  ${server1} \"rm -rf ./load && mkdir load && mkdir load/results && unzip load.zip -d load \" "
                        },
                        "Load2": {
                            sh "ssh  ${server2} \"rm -rf ./load && mkdir load && mkdir load/results && unzip load.zip -d load \" "
                        },
                        "Load3": {
                            sh "ssh  ${server3} \"rm -rf ./load && mkdir load && mkdir load/results && unzip load.zip -d load \" "
                        },
                        "Load4": {
                            sh "ssh  ${server4} \"rm -rf ./load && mkdir load && mkdir load/results && unzip load.zip -d load \" "
                        }
                )
            }
        }
        stage('Run test') {

            steps {
                parallel(
                        "Load1": {
                            sh "ssh  ${server1} \"export _JAVA_OPTIONS='-Xms3072m -Xmx5120m' && cd load && ./apache-jmeter-5.3/bin/jmeter.sh -n -t ~/load/scripts/36/${PERF_TEST}.jmx -l ~/load/results/${PERF_TEST}/${BUILD_NUMBER}.jtl\" "
                        },
                        "Load2": {
                            sh "ssh  ${server2} \"export _JAVA_OPTIONS='-Xms3072m -Xmx5120m' && cd load && ./apache-jmeter-5.3/bin/jmeter.sh -n -t ~/load/scripts/36/${PERF_TEST}.jmx -l ~/load/results/${PERF_TEST}/${BUILD_NUMBER}.jtl\" "
                        },
                        "Load3": {
                            sh "ssh  ${server3} \"export _JAVA_OPTIONS='-Xms3072m -Xmx5120m' && cd load && ./apache-jmeter-5.3/bin/jmeter.sh -n -t ~/load/scripts/36/${PERF_TEST}.jmx -l ~/load/results/${PERF_TEST}/${BUILD_NUMBER}.jtl\" "
                        },
                        "Load4": {
                            sh "ssh  ${server4} \"export _JAVA_OPTIONS='-Xms3072m -Xmx5120m' && cd load && ./apache-jmeter-5.3/bin/jmeter.sh -n -t ~/load/scripts/36/${PERF_TEST}.jmx -l ~/load/results/${PERF_TEST}/${BUILD_NUMBER}.jtl\" "
                        }
                )
            }
        }

    }
}