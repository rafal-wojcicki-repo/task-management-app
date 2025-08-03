import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import AuthService from '../services/auth.service';

const Header = ({ currentUser }) => {
  const navigate = useNavigate();

  const logOut = () => {
    AuthService.logout();
    navigate('/login');
    window.location.reload();
  };

  return (
    <Navbar bg="dark" variant="dark" expand="lg">
      <Container>
        <Navbar.Brand as={Link} to="/">Task Manager</Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/">Home</Nav.Link>
            {currentUser && (
              <>
                <Nav.Link as={Link} to="/tasks">My Tasks</Nav.Link>
                <Nav.Link as={Link} to="/tasks/new">Create Task</Nav.Link>
              </>
            )}
          </Nav>
          
          <Nav>
            {currentUser ? (
              <>
                <Nav.Link as={Link} to="/profile">
                  {currentUser.username}
                </Nav.Link>
                <Button variant="outline-light" onClick={logOut}>
                  Logout
                </Button>
              </>
            ) : (
              <>
                <Nav.Link as={Link} to="/login">Login</Nav.Link>
                <Nav.Link as={Link} to="/register">Register</Nav.Link>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Header;