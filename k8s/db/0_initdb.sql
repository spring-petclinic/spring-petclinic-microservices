CREATE USER 'DBMon_Agent_User'@'%' IDENTIFIED BY 'AppDynamicsS3cur3';
GRANT SELECT,PROCESS,SHOW DATABASES ON *.* TO 'DBMon_Agent_User'@'%';
GRANT REPLICATION CLIENT ON *.* TO 'DBMon_Agent_User'@'%';
FLUSH privileges;
