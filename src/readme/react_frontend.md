# ValueProtect React Frontend - MVP Documentation

## Table of Contents
1. [MVP Overview](#mvp-overview)
2. [Business Workflow](#business-workflow)
3. [Technical Setup](#technical-setup)
4. [Implementation Guide](#implementation-guide)
5. [API Integration](#api-integration)

---

## MVP Overview

### Purpose
ValueProtect manages appraisal requests between Lender Contacts and Appraisers through a status-driven workflow system.

### Key Stakeholders
- **Lender Contacts**: Create and monitor appraisal requests
- **Appraisers**: Accept requests, complete appraisals, and upload reports

### Prerequisites
- Node.js v16 or higher
- npm or yarn package manager
- ValueProtect Backend API running on port 8080

---

## Business Workflow

### Appraisal Status Lifecycle

```
raw → in-progress → uploaded → completed
```

| Status | Description | Triggered By |
|--------|-------------|--------------|
| `raw` | Initial state after lender creates request | Lender creates request |
| `in-progress` | Appraiser has accepted the request | Appraiser accepts |
| `uploaded` | Appraisal report has been submitted | Appraiser uploads document |
| `completed` | System review finished, lender notified | System automation |

### A. Lender Workflow

1. **Login**
   - Lender Contact authenticates into the system

2. **View Dashboard**
   - Displays all appraisal files for their company
   - Organized by status: New / In-Progress / Completed

3. **Create New Appraisal Request**
   - Click "Create new appraisal request" button
   - System creates request with initial status: `raw`
   - Request automatically assigned to default appraiser company

4. **Monitor Requests**
   - Track progress through status updates
   - Receive notification when appraisal is completed

### B. Appraiser Workflow

1. **Login**
   - Appraiser authenticates to view available requests

2. **View New Requests**
   - See all `raw` status requests
   - Requests visible to all employees in appraiser's company

3. **Accept Request**
   - Appraiser accepts a `raw` request
   - Status automatically changes to `in-progress`
   - Request moves to appraiser's personal "In-Progress" folder

4. **Complete Appraisal**
   - Perform property appraisal
   - Prepare appraisal report document

5. **Upload Report**
   - Submit completed appraisal document
   - Status automatically changes to `uploaded`

6. **System Finalization**
   - System performs automated review
   - Notification sent to original Lender Contact
   - Status updates to `completed`

### Visibility & Access Rules

#### For Appraisers
- **Raw Requests**: Visible to ALL employees of the appraiser company
- **In-Progress Requests**: Filtered to show ONLY requests accepted by the specific appraiser
- **Uploaded Requests**: Filtered to show ONLY requests uploaded by the specific appraiser

#### For Lenders
- **All Statuses**: Can view ALL appraisal requests created by their company

---

## Technical Setup

### 1. Create React Application

```bash
npx create-react-app valueprotect-frontend
cd valueprotect-frontend
```

### 2. Install Dependencies

```bash
npm install axios react-router-dom @mui/material @emotion/react @emotion/styled
npm install @mui/icons-material
```

### 3. Project Structure

```
valueprotect-frontend/
├── public/
├── src/
│   ├── components/
│   │   ├── auth/
│   │   │   ├── LoginPage.js
│   │   │   └── ProtectedRoute.js
│   │   ├── lender/
│   │   │   ├── LenderDashboard.js
│   │   │   ├── CreateAppraisalForm.js
│   │   │   └── AppraisalList.js
│   │   ├── appraiser/
│   │   │   ├── AppraiserDashboard.js
│   │   │   ├── AcceptRequestButton.js
│   │   │   └── UploadDocumentForm.js
│   │   └── common/
│   │       ├── StatusBadge.js
│   │       ├── Navbar.js
│   │       └── AppraisalCard.js
│   ├── services/
│   │   ├── api.js
│   │   ├── authService.js
│   │   ├── appraisalService.js
│   │   └── employeeService.js
│   ├── context/
│   │   └── AuthContext.js
│   ├── utils/
│   │   ├── constants.js
│   │   └── helpers.js
│   ├── App.js
│   ├── App.css
│   └── index.js
├── .env
└── package.json
```

---

## Implementation Guide

### Required UI Components

#### 1. User Login Page
- Email/username input field
- Password input field
- Login button
- Error message display
- Role-based redirect after login

#### 2. Lender Dashboard
**Layout**: Tabbed interface with three sections

- **New Tab**
  - Display appraisals with status: `raw`
  - "Create New Appraisal Request" button

- **In-Progress Tab**
  - Display appraisals with status: `in-progress`
  - View assigned appraiser information

- **Completed Tab**
  - Display appraisals with status: `uploaded` and `completed`
  - Download report button

#### 3. Appraiser Dashboard
**Layout**: Tabbed interface with three sections

- **New Tab**
  - Display `raw` requests (company-wide visibility)
  - "Accept" button for each request

- **In-Progress Tab**
  - Display `in-progress` requests (filtered by current appraiser)
  - "Upload Document" button

- **Uploaded Tab**
  - Display `uploaded` requests (filtered by current appraiser)
  - View uploaded document

#### 4. New Appraisal Request Form
**Fields**:
- Property address
- Property type
- Appraisal type
- Due date
- Additional notes
- Submit button

#### 5. Upload Document Form
**Fields**:
- File upload input (PDF/DOC)
- Preview uploaded file
- Upload button
- Cancel button

---

## API Integration

### Base API Configuration

Create `src/services/api.js`:
```javascript
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: Add JWT token to all requests
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: Handle authentication errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

### Authentication Service

Create `src/services/authService.js`:
```javascript
import apiClient from './api';

export const authService = {
  // Login user
  login: async (credentials) => {
    const response = await apiClient.post('/auth/login', credentials);
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
    }
    return response.data;
  },

  // Logout user
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
  },

  // Get current user from local storage
  getCurrentUser: () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  },

  // Get user role
  getUserRole: () => {
    const user = authService.getCurrentUser();
    return user?.role || null;
  }
};
```

### Appraisal Service

Create `src/services/appraisalService.js`:
```javascript
import apiClient from './api';

export const appraisalService = {
  // Create new appraisal request (Lender)
  createAppraisal: (appraisalData) => 
    apiClient.post('/appraisals', appraisalData),

  // Get all appraisals for current user's company
  getCompanyAppraisals: () => 
    apiClient.get('/appraisals/company'),

  // Get appraisals filtered by status
  getAppraisalsByStatus: (status) => 
    apiClient.get(`/appraisals?status=${status}`),

  // Get appraisals assigned to current appraiser
  getMyAppraisals: () => 
    apiClient.get('/appraisals/my-appraisals'),

  // Get single appraisal details
  getAppraisalById: (appraisalId) => 
    apiClient.get(`/appraisals/${appraisalId}`),

  // Accept appraisal request (Appraiser)
  acceptAppraisal: (appraisalId) => 
    apiClient.put(`/appraisals/${appraisalId}/accept`),

  // Upload appraisal document (Appraiser)
  uploadDocument: (appraisalId, formData) => 
    apiClient.post(`/appraisals/${appraisalId}/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),

  // Download appraisal document
  downloadDocument: (appraisalId) => 
    apiClient.get(`/appraisals/${appraisalId}/download`, {
      responseType: 'blob'
    })
};
```

### Constants

Create `src/utils/constants.js`:
```javascript
export const APPRAISAL_STATUS = {
  RAW: 'raw',
  IN_PROGRESS: 'in-progress',
  UPLOADED: 'uploaded',
  COMPLETED: 'completed'
};

export const USER_ROLES = {
  LENDER: 'LENDER',
  APPRAISER: 'APPRAISER'
};

export const STATUS_LABELS = {
  [APPRAISAL_STATUS.RAW]: 'New',
  [APPRAISAL_STATUS.IN_PROGRESS]: 'In Progress',
  [APPRAISAL_STATUS.UPLOADED]: 'Uploaded',
  [APPRAISAL_STATUS.COMPLETED]: 'Completed'
};

export const STATUS_COLORS = {
  [APPRAISAL_STATUS.RAW]: '#2196F3',
  [APPRAISAL_STATUS.IN_PROGRESS]: '#FF9800',
  [APPRAISAL_STATUS.UPLOADED]: '#9C27B0',
  [APPRAISAL_STATUS.COMPLETED]: '#4CAF50'
};
```

### Protected Routes

Create `src/components/auth/ProtectedRoute.js`:
```javascript
import { Navigate } from 'react-router-dom';
import { authService } from '../../services/authService';

const ProtectedRoute = ({ children, requiredRole }) => {
  const isAuthenticated = authService.isAuthenticated();
  const userRole = authService.getUserRole();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && userRole !== requiredRole) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

export default ProtectedRoute;
```

### App Routes

Update `src/App.js`:
```javascript
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './components/auth/LoginPage';
import ProtectedRoute from './components/auth/ProtectedRoute';
import LenderDashboard from './components/lender/LenderDashboard';
import AppraiserDashboard from './components/appraiser/AppraiserDashboard';
import { USER_ROLES } from './utils/constants';

function App() {
  return (
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
      </Routes>
    </Router>
  );
}

export default App;
```

---

## Environment Configuration

Create `.env` file in the project root:
```
REACT_APP_API_URL=http://localhost:8080/api
```

---

## Development Workflow

### Running the Application
```bash
npm start
```
Application runs on `http://localhost:3000`

### Building for Production
```bash
npm run build
```

### Running Tests
```bash
npm test
```

---

## Additional Resources

- [React Documentation](https://react.dev/)
- [Axios Documentation](https://axios-http.com/)
- [React Router Documentation](https://reactrouter.com/)
- [Material-UI Documentation](https://mui.com/)

---

## Next Steps

1. Implement LoginPage component with role-based authentication
2. Build LenderDashboard with tabbed interface
3. Build AppraiserDashboard with tabbed interface
4. Create CreateAppraisalForm component
5. Create UploadDocumentForm component
6. Implement StatusBadge component for visual status indicators
7. Add notification system for status changes
8. Implement file upload/download functionality
9. Add form validation
10. Implement error handling and loading states
