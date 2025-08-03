import React, { useState } from 'react';
import { Form, Button, Card, Alert } from 'react-bootstrap';
import AuthService from '../services/auth.service';

const Register = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [successful, setSuccessful] = useState(false);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const validateForm = () => {
    if (!username || !email || !password || !confirmPassword) {
      setMessage('All fields are required!');
      return false;
    }

    if (password !== confirmPassword) {
      setMessage('Passwords do not match!');
      return false;
    }

    if (password.length < 6) {
      setMessage('Password must be at least 6 characters!');
      return false;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setMessage('Please enter a valid email address!');
      return false;
    }

    return true;
  };

  const handleRegister = (e) => {
    e.preventDefault();
    setMessage('');
    setSuccessful(false);
    setLoading(true);

    if (validateForm()) {
      AuthService.register(username, email, password)
        .then(response => {
          setMessage(response.data.message);
          setSuccessful(true);
          setLoading(false);
        })
        .catch(error => {
          const resMessage =
            (error.response &&
              error.response.data &&
              error.response.data.message) ||
            error.message ||
            error.toString();

          setMessage(resMessage);
          setSuccessful(false);
          setLoading(false);
        });
    } else {
      setLoading(false);
    }
  };

  return (
    <div className="col-md-6 mx-auto">
      <Card className="auth-form">
        <Card.Body>
          <h2 className="text-center mb-4">Register</h2>
          
          {message && (
            <Alert variant={successful ? 'success' : 'danger'}>
              {message}
            </Alert>
          )}

          {!successful && (
            <Form onSubmit={handleRegister}>
              <Form.Group className="mb-3">
                <Form.Label column>Username</Form.Label>
                <Form.Control
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label column>Email</Form.Label>
                <Form.Control
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label column>Password</Form.Label>
                <Form.Control
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label column>Confirm Password</Form.Label>
                <Form.Control
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                />
              </Form.Group>

              <Button 
                variant="primary" 
                type="submit" 
                className="w-100 mt-3"
                disabled={loading}
              >
                {loading ? 'Loading...' : 'Register'}
              </Button>
            </Form>
          )}
          
          <div className="text-center mt-3">
            <p>
              Already have an account? <a href="/login">Login</a>
            </p>
          </div>
        </Card.Body>
      </Card>
    </div>
  );
};

export default Register;