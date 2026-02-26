-- Script to fix existing appraiser users with incorrect role names
-- Run this to update existing appraiser users

USE actpro;

-- Check current roles
SELECT '==== Current User Roles ====' AS '';
SELECT u.userName, r.name as role_name 
FROM user u 
JOIN users_roles ur ON u.id = ur.users_ID
JOIN role r ON ur.roles_ID = r.id
WHERE r.name LIKE '%appraiser%' OR r.name LIKE '%APPRAISER%';

-- Update lowercase 'appraiser' role to uppercase 'APPRAISER'
UPDATE role 
SET name = 'APPRAISER' 
WHERE name = 'appraiser';

-- Verify the change
SELECT '==== Updated User Roles ====' AS '';
SELECT u.userName, r.name as role_name 
FROM user u 
JOIN users_roles ur ON u.id = ur.users_ID
JOIN role r ON ur.roles_ID = r.id
WHERE r.name LIKE '%APPRAISER%';

SELECT 'Fix complete - appraiser roles updated to APPRAISER' AS Status;