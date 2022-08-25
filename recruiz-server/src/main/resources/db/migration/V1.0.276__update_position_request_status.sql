drop procedure IF EXISTS change_position_request_pending_status;
drop procedure IF EXISTS change_position_request_inprocess_status;
drop procedure IF EXISTS change_position_request_rejected_status;
drop procedure IF EXISTS change_position_request_closed_status;
drop procedure IF EXISTS change_position_request_onhold_status;
drop procedure IF EXISTS change_position_request_removed_status;

DELIMITER $$
CREATE PROCEDURE change_position_request_pending_status()
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_request'
             AND table_schema = DATABASE()
             AND column_name = 'status' ) THEN 
             
      update `position_request` set status = 'Pending' where status = 'pending';
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE change_position_request_inprocess_status()
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_request'
             AND table_schema = DATABASE()
             AND column_name = 'status' ) THEN 
             
      update `position_request` set status = 'InProcess' where status = 'in-process' OR status = 'in process';
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE change_position_request_rejected_status()
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_request'
             AND table_schema = DATABASE()
             AND column_name = 'status' ) THEN 
             
      update `position_request` set status = 'Rejected' where status = 'rejected' OR status = 'reject';
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE change_position_request_closed_status()
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_request'
             AND table_schema = DATABASE()
             AND column_name = 'status' ) THEN 
             
      update `position_request` set status = 'Closed' where status = 'closed' OR status = 'close';
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE change_position_request_onhold_status()
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_request'
             AND table_schema = DATABASE()
             AND column_name = 'status' ) THEN 
             
      update `position_request` set status = 'OnHold' where status = 'on-hold' OR status = 'on hold';
      
    END IF;
    
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE change_position_request_removed_status()
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
                    WHERE table_name = 'position_request'
             AND table_schema = DATABASE()
             AND column_name = 'status' ) THEN 
             
      update `position_request` set status = 'Removed' where status = 'remove' OR status = 'removed';
      
    END IF;
    
END $$
DELIMITER ;

call change_position_request_pending_status();
call change_position_request_inprocess_status();
call change_position_request_rejected_status();
call change_position_request_closed_status();
call change_position_request_onhold_status();
call change_position_request_removed_status();