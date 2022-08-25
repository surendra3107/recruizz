DROP PROCEDURE IF EXISTS add_constraintFK_client_id_on_prospect;

DELIMITER $$
CREATE PROCEDURE add_constraintFK_client_id_on_prospect()
BEGIN
   IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'prospect' AND table_schema = DATABASE() AND column_name = 'client_id' ) THEN
           
     ALTER TABLE `prospect` ADD COLUMN `client_id` bigint(20) DEFAULT NULL,
     ADD CONSTRAINT FK_client_id_on_prospect FOREIGN KEY (client_id) REFERENCES client(id);

	UPDATE prospect INNER JOIN client ON prospect.companyName = client.clientName SET prospect.client_id = client.id;

   END IF;
   
END $$
DELIMITER ;

call add_constraintFK_client_id_on_prospect();