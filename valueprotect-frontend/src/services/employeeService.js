import apiClient from './api';

export const employeeService = {
  // Get employee details
  getEmployeeById: (employeeId) => 
    apiClient.get(`/employees/${employeeId}`),

  // Get all employees for a company
  getCompanyEmployees: () => 
    apiClient.get('/employees'),

  // Get current employee details
  getCurrentEmployee: () => 
    apiClient.get('/employees/me')
};
