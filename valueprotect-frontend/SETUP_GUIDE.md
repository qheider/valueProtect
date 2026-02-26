# ValueProtect React Frontend - Setup Guide

## Project Structure Created

```
valueprotect-frontend/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── auth/
│   │   │   ├── LoginPage.js
│   │   │   └── ProtectedRoute.js
│   │   ├── lender/
│   │   │   ├── LenderDashboard.js
│   │   │   ├── AppraisalList.js
│   │   │   └── CreateAppraisalForm.js
│   │   ├── appraiser/
│   │   │   ├── AppraiserDashboard.js
│   │   │   ├── AppraiserAppraisalList.js
│   │   │   └── UploadDocumentForm.js
│   │   └── common/
│   │       ├── Navbar.js
│   │       ├── StatusBadge.js
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
│   ├── App.test.js
│   ├── index.js
│   └── index.css
├── .env
├── .gitignore
├── package.json
└── README.md
```

## Installation Steps
install nodeJS

### 1. Navigate to the Frontend Directory

```bash
cd valueprotect-frontend
```

### 2. Install Dependencies

```bash
npm install
```

This will install all required packages:
- React 18.2.0
- React Router DOM 6.20.1
- Material-UI 5.14.20
- Axios 1.6.2
- And all other dependencies

### 3. Configure Environment Variables

The `.env` file is already created with:
```
REACT_APP_API_URL=http://localhost:8080/api
```

If your backend runs on a different port or URL, update this value.

### 4. Ensure Backend is Running

Make sure the ValueProtect backend API is running on port 8080 before starting the frontend.

### 5. Start the Development Server

```bash
npm start
```

The application will open automatically at `http://localhost:3000`

## Features Implemented

### Authentication
- ✅ Login page with username/password
- ✅ JWT token management
- ✅ Role-based routing (Lender/Appraiser)
- ✅ Protected routes
- ✅ Auto-redirect on authentication
- ✅ Logout functionality

### Lender Dashboard
- ✅ Tabbed interface (Draft/In Review/Completed)
- ✅ View all appraisals by status
- ✅ Create new appraisal requests
- ✅ Property information form
- ✅ View appraisal details
- ✅ Document access

### Appraiser Dashboard
- ✅ Tabbed interface (New/In Progress/Completed)
- ✅ View new requests (company-wide)
- ✅ Accept appraisal requests
- ✅ Upload documents
- ✅ Track completed work
- ✅ Status transitions

### Common Components
- ✅ Navigation bar with user info
- ✅ Status badges with colors
- ✅ Appraisal cards with property details
- ✅ Responsive Material-UI design
- ✅ Loading states
- ✅ Error handling

## API Integration

All API calls are configured to use the backend endpoints:

- **Authentication**: `/api/auth/login`
- **Appraisals**: `/api/appraisals`
- **Documents**: `/api/appraisals/{id}/documents`
- **Employees**: `/api/employees`

The API client automatically:
- Adds JWT token to requests
- Handles authentication errors
- Redirects to login on 401 errors

## Testing

Run tests with:
```bash
npm test
```

## Building for Production

Create a production build:
```bash
npm run build
```

This creates an optimized build in the `build/` folder.

## Usage Guide

### For Lenders

1. **Login** with lender credentials
2. **Dashboard** shows three tabs:
   - **Draft**: New appraisals you created
   - **In Review**: Appraisals accepted by appraisers
   - **Completed**: Finished appraisals with documents
3. **Create Appraisal**: Click "Create New Appraisal" button
   - Fill in property details (address, APN, type)
   - Set appraisal information (value, purpose, dates)
   - Submit to create draft appraisal

### For Appraisers

1. **Login** with appraiser credentials
2. **Dashboard** shows three tabs:
   - **New**: Available requests to accept
   - **In Progress**: Your accepted requests
   - **Completed**: Your finished appraisals
3. **Accept Request**: Click "Accept Request" on new appraisals
4. **Upload Document**: Click "Upload Document" on in-progress items
   - Select document type
   - Choose file (PDF, Word, Excel, images)
   - Submit to complete appraisal

## Troubleshooting

### Cannot connect to API
- Verify backend is running on port 8080
- Check `.env` file has correct API URL
- Check browser console for CORS errors

### Login fails
- Verify credentials are correct
- Check backend authentication endpoint
- Look for error messages in browser console

### White screen after login
- Check browser console for errors
- Verify user has correct role assigned
- Check network tab for failed API calls

## Next Steps for Enhancement

1. Add document viewer for uploaded files
2. Implement real-time notifications
3. Add search and filter functionality
4. Create detailed appraisal view page
5. Add user profile management
6. Implement bulk operations
7. Add export functionality
8. Enhance error handling with retry logic
9. Add loading skeletons
10. Implement offline support

## Support

For issues or questions, refer to:
- Backend API documentation
- React documentation: https://react.dev/
- Material-UI documentation: https://mui.com/
