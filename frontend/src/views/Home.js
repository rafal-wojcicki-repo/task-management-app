import React from 'react';
import { Container, Row, Col, Card, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import AuthService from '../services/auth.service';

const Home = () => {
  const currentUser = AuthService.getCurrentUser();

  return (
    <Container>
      <div className="home-banner text-center">
        <h1>Welcome to Task Manager</h1>
        <p className="lead">
          A simple and efficient way to manage your tasks and stay organized
        </p>
        {!currentUser && (
          <div className="mt-4">
            <Link to="/login">
              <Button variant="primary" className="me-3">Login</Button>
            </Link>
            <Link to="/register">
              <Button variant="outline-primary">Register</Button>
            </Link>
          </div>
        )}
      </div>

      <Row className="mt-5">
        <Col md={4} className="mb-4">
          <Card>
            <Card.Body>
              <Card.Title>Organize Tasks</Card.Title>
              <Card.Text>
                Create, update, and organize your tasks with different priorities and statuses.
              </Card.Text>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4} className="mb-4">
          <Card>
            <Card.Body>
              <Card.Title>Track Progress</Card.Title>
              <Card.Text>
                Monitor your progress and keep track of completed, in-progress, and pending tasks.
              </Card.Text>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4} className="mb-4">
          <Card>
            <Card.Body>
              <Card.Title>Secure Access</Card.Title>
              <Card.Text>
                Your tasks are secure with our authentication system. Only you can access your tasks.
              </Card.Text>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {currentUser && (
        <div className="text-center mt-4 mb-5">
          <Link to="/tasks">
            <Button variant="success" size="lg">Go to My Tasks</Button>
          </Link>
        </div>
      )}
    </Container>
  );
};

export default Home;