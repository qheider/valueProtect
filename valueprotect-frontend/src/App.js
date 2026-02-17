import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import { AuthProvider } from './context/AuthContext';
import LoginPage from './components/auth/LoginPage';
import ProtectedRoute from './components/auth/ProtectedRoute';
import LenderDashboard from './components/lender/LenderDashboard';
import AppraiserDashboard from './components/appraiser/AppraiserDashboard';
import { USER_ROLES } from './utils/constants';
import './App.css';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            
            <Route 
              path="/lender/dashboard" 
              element={
                <ProtectedRoute requiredRole={USER_ROLES.LENDER}>
                  <LenderDashboard />
                </ProtectedRoute>
              } 
            />
            
            <Route 
              path="/appraiser/dashboard" 
              element={
                <ProtectedRoute requiredRole={USER_ROLES.APPRAISER}>
                  <AppraiserDashboard />
                </ProtectedRoute>
              } 
            />
            
            <Route path="/" element={<Navigate to="/login" replace />} />
            
            <Route 
              path="/unauthorized" 
              element={
                <div style={{ textAlign: 'center', marginTop: '50px' }}>
                  <h1>Unauthorized</h1>
                  <p>You don't have permission to access this page.</p>
                </div>
              } 
            />
            
            <Route 
              path="*" 
              element={
                <div style={{ textAlign: 'center', marginTop: '50px' }}>
                  <h1>404 - Page Not Found</h1>
                  <p>The page you're looking for doesn't exist.</p>
                </div>
              } 
            />
          </Routes>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
