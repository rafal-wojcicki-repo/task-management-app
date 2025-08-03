import React from 'react';
import { Container, Row, Col } from 'react-bootstrap';

const Footer = () => {
  return (
    <footer className="bg-dark text-light py-4 mt-auto">
      <Container>
        <Row>
          <Col md={6} className="text-center text-md-start">
            <h5>Task Management App</h5>
            <p className="small">Organize your tasks efficiently</p>
          </Col>
          <Col md={6} className="text-center text-md-end">
            <p className="small">
              &copy; {new Date().getFullYear()} Task Manager. All rights reserved.
            </p>
          </Col>
        </Row>
      </Container>
    </footer>
  );
};

export default Footer;