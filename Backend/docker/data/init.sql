CREATE DATABASE IF NOT EXISTS first_aid_participant_bd;
CREATE DATABASE IF NOT EXISTS first_aid_training_bd;

GRANT ALL PRIVILEGES ON first_aid_participant_bd.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON first_aid_training_bd.* TO 'user'@'%';
FLUSH PRIVILEGES;