import React, { useState, useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Container } from 'react-bootstrap';

// Components
import Header from './components/Header';
import Footer from './components/Footer';

// Pages
import Home from './views/Home';
import Login from './views/Login';
import Register from './views/Register';
import TaskList from './views/TaskList';
import TaskForm from './views/TaskForm';
import Profile from './views/Profile';

// Services
import AuthService from './services/auth.service';

import './App.css';

const App = () => {
  const [currentUser, setCurrentUser] = useState(undefined);

  useEffect(() => {
    const user = AuthService.getCurrentUser();
    if (user) {
      setCurrentUser(user);
    }
  }, []);

  const PrivateRoute = ({ children }) => {
    return currentUser ? children : <Navigate to="/login" />;
  };

  return (
    <div className="d-flex flex-column min-vh-100">
      <Header currentUser={currentUser} />
      <Container className="flex-grow-1 py-3">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route 
            path="/tasks" 
            element={
              <PrivateRoute>
                <TaskList />
              </PrivateRoute>
            } 
          />
          <Route 
            path="/tasks/new" 
            element={
              <PrivateRoute>
                <TaskForm />
              </PrivateRoute>
            } 
          />
          <Route 
            path="/tasks/edit/:id" 
            element={
              <PrivateRoute>
                <TaskForm />
              </PrivateRoute>
            } 
          />
          <Route 
            path="/profile" 
            element={
              <PrivateRoute>
                <Profile />
              </PrivateRoute>
            } 
          />
        </Routes>
      </Container>
      <Footer />
    </div>
  );
};

export default App;