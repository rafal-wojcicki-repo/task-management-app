# Task Management Application - Frontend

This is the frontend part of the Task Management Application, built with React.

## Features

- User authentication (login/register)
- Task management (create, read, update, delete)
- Task filtering and sorting
- Task status tracking
- User profile with task statistics
- Responsive design

## Technologies Used

- React 18
- React Router 6
- React Bootstrap
- Axios for API communication
- JWT for authentication

## Prerequisites

- Node.js (v14 or higher)
- npm (v6 or higher)

## Setup and Installation

1. Make sure the backend server is running (see backend README)

2. Install dependencies:
   ```
   npm install
   ```

3. Start the development server:
   ```
   npm start
   ```

4. The application will be available at `http://localhost:3000`

## Building for Production

To create a production build:

```
npm run build
```

This will create a `build` folder with optimized production files.

## Project Structure

- `public/` - Static files
- `src/` - Source code
  - `components/` - Reusable UI components
  - `services/` - API services
  - `views/` - Page components
  - `App.js` - Main application component
  - `index.js` - Application entry point

## API Integration

The frontend communicates with the backend API at `http://localhost:8080/api`. The proxy is configured in `package.json`.

## Authentication

The application uses JWT tokens for authentication. Tokens are stored in localStorage and included in API requests via the Authorization header.

## Available Scripts

- `npm start` - Starts the development server
- `npm test` - Runs tests
- `npm run build` - Builds the app for production
- `npm run eject` - Ejects from Create React App

## License

This project is licensed under the MIT License.