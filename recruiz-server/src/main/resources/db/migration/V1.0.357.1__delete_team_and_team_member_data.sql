SET FOREIGN_KEY_CHECKS=0; 

DELETE FROM team_member where id > 0; 

DELETE FROM team where id > 0; 

SET FOREIGN_KEY_CHECKS=1; 