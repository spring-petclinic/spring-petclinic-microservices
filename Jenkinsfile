pipeline {
    // Chạy pipeline trên agent có nhãn 'build-agent'
    agent any

    stages {
        // Giai đoạn 1: Xác định các dịch vụ bị ảnh hưởng bởi thay đổi
        stage('Determine changed services') {
            steps {
                script {
                    // Lấy danh sách file thay đổi trong PR so với nhánh target (thường là main)
                    def changedFiles = sh(returnStdout: true, script: "git diff --name-only origin/${env.CHANGE_TARGET}...HEAD").trim().split('\n')
                    
                    // Xác định các dịch vụ bị ảnh hưởng (các thư mục bắt đầu bằng 'spring-petclinic-')
                    def affectedServices = changedFiles.collect { file ->
                        if (file.startsWith('spring-petclinic-')) {
                            return file.split('/')[0]
                        }
                    }.unique() // Loại bỏ trùng lặp
                    
                    // Lưu danh sách dịch vụ bị ảnh hưởng vào biến môi trường
                    env.AFFECTED_SERVICES = affectedServices.join(',')
                }
            }
        }

        // Giai đoạn 2: Kiểm thử các dịch vụ bị ảnh hưởng
        stage('Test affected services') {
            steps {
                script {
                    if (env.AFFECTED_SERVICES) {
                        // Chạy lệnh Maven để test các dịch vụ bị ảnh hưởng
                        sh "mvn -pl ${env.AFFECTED_SERVICES} test"
                        // Upload kết quả kiểm thử (JUnit reports)
                        junit '**/target/surefire-reports/*.xml'
                    } else {
                        echo 'Không có dịch vụ nào bị ảnh hưởng, bỏ qua giai đoạn test'
                    }
                }
            }
        }

        // Giai đoạn 3: Build các dịch vụ bị ảnh hưởng
        stage('Build affected services') {
            steps {
                script {
                    if (env.AFFECTED_SERVICES) {
                        // Build các dịch vụ bị ảnh hưởng, bỏ qua test vì đã chạy ở giai đoạn trước
                        sh "mvn -pl ${env.AFFECTED_SERVICES} package -DskipTests"
                    } else {
                        echo 'Không có dịch vụ nào bị ảnh hưởng, bỏ qua giai đoạn build'
                    }
                }
            }
        }
    }
}