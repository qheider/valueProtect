SELECT e.id as 'employee id',e.firstName,e.lastName,e.company_id as 'emplyee company id',e.user_id as 'employee user_id' , 
u.id as 'id from user table', u.userName, r.name as 'role name', c.name as 'company name',c.id as 'company id', c.company_type
FROM employee e 
inner join user u on u.id=e.user_id
inner join company c on c.id= e.company_id
inner join users_roles ur on ur.users_ID=u.id
inner join role r on r.id=ur.roles_ID;