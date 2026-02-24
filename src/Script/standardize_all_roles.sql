-- Comprehensive script to standardize all role names to uppercase format
-- This ensures consistency across the system

USE actpro;

SELECT '==== BEFORE: All Current Roles ====' AS '';
SELECT id, name as current_role_name FROM role ORDER BY name;

-- Update all role names to proper uppercase format
UPDATE role SET name = 'ADMIN' WHERE name IN ('admin', 'Admin', 'ROLE_ADMIN');
UPDATE role SET name = 'USER' WHERE name IN ('user', 'User', 'ROLE_USER');
UPDATE role SET name = 'EMPLOYEE' WHERE name IN ('employee', 'Employee', 'ROLE_EMPLOYEE');
UPDATE role SET name = 'APPRAISER' WHERE name IN ('appraiser', 'Appraiser', 'ROLE_APPRAISER');
UPDATE role SET name = 'LENDER' WHERE name IN ('lender', 'Lender', 'ROLE_LENDER');

SELECT '==== AFTER: Standardized Roles ====' AS '';
SELECT id, name as standardized_role_name FROM role ORDER BY name;

-- Show user-role mappings for verification
SELECT '==== User-Role Mappings ====' AS '';
SELECT u.userName, r.name as role_name, u.email
FROM user u 
JOIN users_roles ur ON u.id = ur.users_ID
JOIN role r ON ur.roles_ID = r.id
ORDER BY u.userName, r.name;

SELECT 'Role standardization complete!' AS Status;