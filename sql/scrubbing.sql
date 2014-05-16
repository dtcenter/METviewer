DROP PROCEDURE IF EXISTS delete_from_table;

DELIMITER $$
-- PROCEDURE pLog
--   outputs log message
-- Params:
--   sTitle - title
--   sMsg - log message
DROP PROCEDURE IF EXISTS pLog $$
CREATE PROCEDURE pLog(IN sTitle VARCHAR(255), IN sMsg VARCHAR(255))
  BEGIN
    DECLARE strSQL VARCHAR(512);
    SET strSQL = CONCAT('SELECT ''', sMsg, ''' AS ''', sTitle, '''');
    CALL pExecuteImmediate(strSQL);
  END $$


-- PROCEDURE pExecuteImmediate
--   executes dynamic SQL statement
-- Params:
--   tSQLStmt - SQL statement to be executed
DROP PROCEDURE IF EXISTS pExecuteImmediate $$
CREATE PROCEDURE pExecuteImmediate(IN tSQLStmt TEXT)
  BEGIN
    SET @executeImmediateSQL = tSQLStmt;
    PREPARE executeImmediateSTML FROM @executeImmediateSQL;
    EXECUTE executeImmediateSTML;
    DEALLOCATE PREPARE executeImmediateSTML;
  END $$


-- PROCEDURE delete_from_table
--   deletes or estimates deletion of the field values based on the thresholds
-- Params:
--   table_name - table name
--   field_name - field name
--   database_name - database schema name
--   info_only - determines if deletion should be actual or only estimated
DROP PROCEDURE IF EXISTS delete_from_table $$
CREATE PROCEDURE delete_from_table(IN table_name VARCHAR(255), IN field_name VARCHAR(255), IN database_name VARCHAR(255), IN info_only BOOL)
  BEGIN
    SET @strLogBefore =0;
    SET @strLogAfter =0;
    SET @strLogForDeletion =0;
    SET @column_count =0;

    -- check if the table has the column with the name 'field_name'
    SET @qry = CONCAT('SELECT count(*) INTO  @column_count FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND column_name = ? AND TABLE_SCHEMA = ?');
    PREPARE stmt1 FROM @qry;
    SET @t = table_name;
    SET @f = field_name;
    SET @d = database_name;
    EXECUTE stmt1 USING @t, @f, @d;

    IF @column_count > 0
    THEN
      -- the column is present
      IF (info_only = TRUE) THEN
        -- count number of records for deletion and print it
        SET @qry = CONCAT('SELECT count(*) INTO @strLogForDeletion FROM ', table_name, ' WHERE ', field_name, '>= ? AND ', field_name, '<= ?');
        PREPARE stmt2 FROM @qry;
        EXECUTE stmt2 USING @min_value, @max_value;
        CALL pLog( CONCAT( 'From table: ',table_name), CONCAT('would be deleted: ', @strLogForDeletion, ' records'));
        DEALLOCATE PREPARE stmt2;
      ELSE
        -- count all records before deletion
        SET @qry = CONCAT('SELECT count(*) INTO @strLogBefore FROM ', table_name);
        PREPARE stmt2 FROM @qry;
        EXECUTE stmt2;
        DEALLOCATE PREPARE stmt2;

        -- perform deletion
        SET @qry = CONCAT('DELETE FROM  ', table_name, ' WHERE ', field_name, '>= ? AND ', field_name, '<= ?');
        PREPARE stmt3 FROM @qry;
        EXECUTE stmt3 USING @min_value, @max_value;
        DEALLOCATE PREPARE stmt3;

        -- count all records after deletion
        SET @qry = CONCAT('SELECT count(*) INTO @strLogAfter FROM ', table_name);
        PREPARE stmt4 FROM @qry;
        EXECUTE stmt4;
        DEALLOCATE PREPARE stmt4;

        -- print number of deleted records
        CALL pLog(CONCAT( 'From table ', table_name), CONCAT('deleted: ', @strLogBefore - @strLogAfter, ' records'));
      END IF;


    END IF;
    DEALLOCATE PREPARE stmt1;

  END $$

-- PROCEDURE delete_from_all_tables
--   gets all tables from the database and prepare them for the next step

DROP PROCEDURE IF EXISTS delete_from_all_tables $$
CREATE PROCEDURE delete_from_all_tables()
  BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE _tbl_name VARCHAR (255);
    DECLARE info_only BOOL;
    DECLARE field_name VARCHAR (255);
    DECLARE database_name VARCHAR (255);
    -- store tables names in cursor variable
    DECLARE cur CURSOR FOR SELECT TABLE_NAME FROM (SELECT DISTINCT TABLE_NAME   FROM INFORMATION_SCHEMA.COLUMNS where TABLE_SCHEMA = '_database_name_') as all_tables;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    SET info_only = _info_only_;
    SET field_name  = '_field_name_';
    SET database_name  = '_database_name_';


    OPEN cur;

    read_loop: LOOP
      FETCH cur INTO _tbl_name;
      IF done THEN
        LEAVE read_loop;
      END IF;
      CALL delete_from_table(_tbl_name, field_name, database_name, info_only);
    END LOOP;

    CLOSE cur;
END $$

DELIMITER ';'
CALL delete_from_all_tables();

DROP PROCEDURE IF EXISTS delete_from_all_tables;
DROP PROCEDURE IF EXISTS delete_from_table;
















