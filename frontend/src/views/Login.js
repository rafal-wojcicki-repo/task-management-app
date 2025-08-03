import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Button, Card, Alert } from 'react-bootstrap';
import AuthService from '../services/auth.service';

const Login = () => {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const handleLogin = (e) => {
    e.preventDefault();
    setMessage('');
    setLoading(true);

    if (!username || !password) {
      setMessage('All fields are required!');
      setLoading(false);
      return;
    }

    AuthService.login(username, password)
      .then(() => {
        navigate('/tasks');
        window.location.reload();
      })
      .catch(error => {
        const resMessage =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();

        setLoading(false);
        setMessage(resMessage);
      });
  };

  return (
    <div className="col-md-6 mx-auto">
      <Card className="auth-form">
        <Card.Body>
          <h2 className="text-center mb-4">Login</h2>
          
          {message && (
            <Alert variant="danger">{message}</Alert>
          )}

          <Form onSubmit={handleLogin}>
            <Form.Group className="mb-3">
              <Form.Label column>Username</Form.Label>
              <Form.Control
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
              />
            </Form.Group>z

            <Form.Group className="mb-3">
              <Form.Label column>Password</Form.Label>
              <Form.Control
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </Form.Group>

            <Button 
              variant="primary" 
              type="submit" 
              className="w-100 mt-3"
              disabled={loading}
            >
              {loading ? 'Loading...' : 'Login'}
            </Button>
          </Form>
          
          <div className="text-center mt-3">
            <p>
              Don't have an account? <a href="/register">Register</a>
            </p>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
};

export default Login;