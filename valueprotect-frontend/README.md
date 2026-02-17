# ValueProtect Frontend

React frontend application for ValueProtect appraisal management system.

## Prerequisites

- Node.js v16 or higher
- npm or yarn package manager
- ValueProtect Backend API running on port 8080

## Installation

```bash
npm install
```

## Running the Application

```bash
npm start
```

Application runs on `http://localhost:3000`

## Building for Production

```bash
npm run build
```

## Running Tests

```bash
npm test
```

## Environment Variables

Create a `.env` file in the project root:

```
REACT_APP_API_URL=http://localhost:8080/api
```

## Features

- User authentication with JWT tokens
- Role-based access (Lender/Appraiser)
- Appraisal request management
- Document upload and download
- Status-driven workflow system
