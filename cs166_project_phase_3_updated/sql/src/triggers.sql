DROP TRIGGER IF EXISTS orderNumberTrigger ON Orders;
DROP TRIGGER IF EXISTS updateNumberTrigger ON ProductUpdates;
DROP TRIGGER IF EXISTS productSupplyNumberTrigger ON ProductSupplyRequests;

DROP FUNCTION IF EXISTS orderNumberFunc();
DROP FUNCTION IF EXISTS updateNumberFunc();
DROP FUNCTION IF EXISTS productSupplyNumber();

CREATE OR REPLACE LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION orderNumberFunc()
RETURNS "trigger" AS
$BODY$
DECLARE
	numRows INTEGER;
BEGIN
	SELECT COUNT(*) INTO numRows FROM Orders;
	NEW.orderNumber := numRows + 1;
	RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER orderNumberTrigger BEFORE INSERT
ON Orders FOR EACH ROW
EXECUTE PROCEDURE orderNumberFunc();

CREATE OR REPLACE LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION updateNumberFunc()
RETURNS "trigger" AS 
$BODY$
DECLARE
	numRows INTEGER;
BEGIN
	SELECT COUNT(*) INTO numRows FROM ProductUpdates;
	NEW.updateNumber := numRows + 1;
	RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER updateNumberTrigger BEFORE INSERT
ON ProductUpdates FOR EACH ROW
EXECUTE PROCEDURE updateNumberFunc();

CREATE OR REPLACE LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION productSupplyNumber()
RETURNS "trigger" AS
$BODY$
DECLARE
        numRows INTEGER;
BEGIN
        SELECT COUNT(*) INTO numRows FROM ProductSupplyRequests;
        NEW.requestNumber := numRows + 1;
        RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER ProductSupplyNumberTrigger BEFORE INSERT
ON ProductSupplyRequests FOR EACH ROW
EXECUTE PROCEDURE productSupplyNumber();
