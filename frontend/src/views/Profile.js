import React, { useState, useEffect } from 'react';
import { Container, Card, Row, Col, Badge } from 'react-bootstrap';
import AuthService from '../services/auth.service';
import TaskService from '../services/task.service';

const Profile = () => {
  const currentUser = AuthService.getCurrentUser();
  const [taskStats, setTaskStats] = useState({
    total: 0,
    todo: 0,
    inProgress: 0,
    done: 0
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (currentUser) {
      fetchTaskStats();
    }
  }, []);

  const fetchTaskStats = () => {
    TaskService.getMyTasks()
      .then(response => {
        const tasks = response.data;
        const stats = {
          total: tasks.length,
          todo: tasks.filter(task => task.status === 'TODO').length,
          inProgress: tasks.filter(task => task.status === 'IN_PROGRESS').length,
          done: tasks.filter(task => task.status === 'DONE').length
        };
        setTaskStats(stats);
        setLoading(false);
      })
      .catch(error => {
        console.error('Error fetching task stats:', error);
        setLoading(false);
      });
  };

  if (!currentUser) {
    return (
      <Container className="mt-4">
        <p>Please login to view your profile.</p>
      </Container>
    );
  }

  return (
    <Container className="mt-4">
      <h2 className="mb-4">My Profile</h2>
      
      <Row>
        <Col md={6}>
          <Card className="mb-4">
            <Card.Body>
              <Card.Title>User Information</Card.Title>
              <hr />
              <p><strong>Username:</strong> {currentUser.username}</p>
              <p><strong>Email:</strong> {currentUser.email}</p>
              <div>
                <strong>Roles:</strong>{' '}
                {currentUser.roles && currentUser.roles.map((role, index) => (
                  <Badge bg="primary" key={index} className="me-1">
                    {role}
                  </Badge>
                ))}
              </div>
            </Card.Body>
          </Card>
        </Col>
        
        <Col md={6}>
          <Card>
            <Card.Body>
              <Card.Title>Task Statistics</Card.Title>
              <hr />
              {loading ? (
                <p>Loading statistics...</p>
              ) : (
                <>
                  <p><strong>Total Tasks:</strong> {taskStats.total}</p>
                  <div className="d-flex justify-content-between mb-2">
                    <span>To Do:</span>
                    <Badge bg="secondary">{taskStats.todo}</Badge>
                  </div>
                  <div className="d-flex justify-content-between mb-2">
                    <span>In Progress:</span>
                    <Badge bg="primary">{taskStats.inProgress}</Badge>
                  </div>
                  <div className="d-flex justify-content-between mb-2">
                    <span>Completed:</span>
                    <Badge bg="success">{taskStats.done}</Badge>
                  </div>
                  
                  {taskStats.total > 0 && (
                    <div className="mt-3">
                      <div className="progress" style={{ height: '20px' }}>
                        <div 
                          className="progress-bar bg-secondary" 
                          role="progressbar" 
                          style={{ width: `${(taskStats.todo / taskStats.total) * 100}%` }}
                          aria-valuenow={(taskStats.todo / taskStats.total) * 100}
                          aria-valuemin="0" 
                          aria-valuemax="100"
                        >
                          {Math.round((taskStats.todo / taskStats.total) * 100)}%
                        </div>
                        <div 
                          className="progress-bar bg-primary" 
                          role="progressbar" 
                          style={{ width: `${(taskStats.inProgress / taskStats.total) * 100}%` }}
                          aria-valuenow={(taskStats.inProgress / taskStats.total) * 100}
                          aria-valuemin="0" 
                          aria-valuemax="100"
                        >
                          {Math.round((taskStats.inProgress / taskStats.total) * 100)}%
                        </div>
                        <div 
                          className="progress-bar bg-success" 
                          role="progressbar" 
                          style={{ width: `${(taskStats.done / taskStats.total) * 100}%` }}
                          aria-valuenow={(taskStats.done / taskStats.total) * 100}
                          aria-valuemin="0" 
                          aria-valuemax="100"
                        >
                          {Math.round((taskStats.done / taskStats.total) * 100)}%
                        </div>
                      </div>
                    </div>
                  )}
                </>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Profile;