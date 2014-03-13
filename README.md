Server-Client
=============
Перед началом работы нужно создать в базе данных таблицу 
CREATE  TABLE `test`.`history` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `date` TIMESTAMP NULL ,
  `message` VARCHAR(45) NULL ,
  PRIMARY KEY (`id`) );
