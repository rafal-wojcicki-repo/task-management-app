import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Button, Form, Dropdown } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import TaskService from '../services/task.service';

const TaskList = () => {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('all'); // all, todo, in-progress, done
  const [sortBy, setSortBy] = useState('dueDate'); // dueDate, priority, title
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchTasks();
  }, []);

  const fetchTasks = () => {
    setLoading(true);
    TaskService.getMyTasks()
      .then(response => {
        setTasks(response.data);
        setLoading(false);
      })
      .catch(error => {
        const message =
          (error.response &&
            error.response.data &&
            error.response.data.message) ||
          error.message ||
          error.toString();
        setError(message);
        setLoading(false);
      });
  };

  const handleStatusChange = (taskId, newStatus) => {
    TaskService.updateTaskStatus(taskId, newStatus)
      .then(() => {
        setTasks(tasks.map(task => 
          task.id === taskId ? { ...task, status: newStatus } : task
        ));
      })
      .catch(error => {
        console.error('Error updating task status:', error);
      });
  };

  const handleDelete = (taskId) => {
    if (window.confirm('Are you sure you want to delete this task?')) {
      TaskService.deleteTask(taskId)
        .then(() => {
          setTasks(tasks.filter(task => task.id !== taskId));
        })
        .catch(error => {
          console.error('Error deleting task:', error);
        });
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'TODO':
        return <Badge bg="secondary">To Do</Badge>;
      case 'IN_PROGRESS':
        return <Badge bg="primary">In Progress</Badge>;
      case 'DONE':
        return <Badge bg="success">Done</Badge>;
      default:
        return <Badge bg="secondary">{status}</Badge>;
    }
  };

  const getPriorityBadge = (priority) => {
    switch (priority) {
      case 'HIGH':
        return <Badge bg="danger">High</Badge>;
      case 'MEDIUM':
        return <Badge bg="warning">Medium</Badge>;
      case 'LOW':
        return <Badge bg="info">Low</Badge>;
      default:
        return <Badge bg="secondary">{priority}</Badge>;
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'No due date';
    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  const filteredTasks = tasks
    .filter(task => {
      if (filter === 'all') return true;
      return task.status === filter.toUpperCase();
    })
    .filter(task => {
      if (!searchTerm) return true;
      return (
        task.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (task.description && task.description.toLowerCase().includes(searchTerm.toLowerCase()))
      );
    });

  const sortedTasks = [...filteredTasks].sort((a, b) => {
    switch (sortBy) {
      case 'dueDate':
        if (!a.dueDate) return 1;
        if (!b.dueDate) return -1;
        return new Date(a.dueDate) - new Date(b.dueDate);
      case 'priority':
        const priorityOrder = { HIGH: 1, MEDIUM: 2, LOW: 3 };
        return priorityOrder[a.priority] - priorityOrder[b.priority];
      case 'title':
        return a.title.localeCompare(b.title);
      default:
        return 0;
    }
  });

  if (loading) {
    return <Container className="mt-4"><p>Loading tasks...</p></Container>;
  }

  if (error) {
    return <Container className="mt-4"><p className="text-danger">Error: {error}</p></Container>;
  }

  return (
    <Container className="mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>My Tasks</h2>
        <Link to="/tasks/new">
          <Button variant="primary">Create New Task</Button>
        </Link>
      </div>

      <Row className="mb-4">
        <Col md={4}>
          <Form.Group>
            <Form.Control
              type="text"
              placeholder="Search tasks..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </Form.Group>
        </Col>
        <Col md={4}>
          <Form.Select 
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
          >
            <option value="all">All Statuses</option>
            <option value="todo">To Do</option>
            <option value="in_progress">In Progress</option>
            <option value="done">Done</option>
          </Form.Select>
        </Col>
        <Col md={4}>
          <Form.Select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
          >
            <option value="dueDate">Sort by Due Date</option>
            <option value="priority">Sort by Priority</option>
            <option value="title">Sort by Title</option>
          </Form.Select>
        </Col>
      </Row>

      {sortedTasks.length === 0 ? (
        <div className="text-center my-5">
          <p>No tasks found. Create a new task to get started!</p>
          <Link to="/tasks/new">
            <Button variant="outline-primary">Create Task</Button>
          </Link>
        </div>
      ) : (
        <Row>
          {sortedTasks.map(task => (
            <Col md={6} lg={4} key={task.id} className="mb-4">
              <Card className={`task-card priority-${task.priority.toLowerCase()} status-${task.status.toLowerCase().replace('_', '-')}`}>
                <Card.Body>
                  <div className="d-flex justify-content-between align-items-start mb-2">
                    <Card.Title className="text-truncate-2">{task.title}</Card.Title>
                    <Dropdown>
                      <Dropdown.Toggle variant="light" size="sm" id={`dropdown-${task.id}`}>
                        <i className="bi bi-three-dots"></i>
                      </Dropdown.Toggle>
                      <Dropdown.Menu>
                        <Dropdown.Item as={Link} to={`/tasks/edit/${task.id}`}>Edit</Dropdown.Item>
                        <Dropdown.Item onClick={() => handleDelete(task.id)}>Delete</Dropdown.Item>
                      </Dropdown.Menu>
                    </Dropdown>
                  </div>
                  
                  <Card.Text className="text-truncate-3 mb-3">
                    {task.description || 'No description provided.'}
                  </Card.Text>
                  
                  <div className="d-flex justify-content-between mb-2">
                    <div>{getStatusBadge(task.status)}</div>
                    <div>{getPriorityBadge(task.priority)}</div>
                  </div>
                  
                  <div className="d-flex justify-content-between align-items-center">
                    <small className="text-muted">Due: {formatDate(task.dueDate)}</small>
                    
                    <Dropdown>
                      <Dropdown.Toggle variant="outline-secondary" size="sm">
                        Change Status
                      </Dropdown.Toggle>
                      <Dropdown.Menu>
                        <Dropdown.Item onClick={() => handleStatusChange(task.id, 'TODO')}>To Do</Dropdown.Item>
                        <Dropdown.Item onClick={() => handleStatusChange(task.id, 'IN_PROGRESS')}>In Progress</Dropdown.Item>
                        <Dropdown.Item onClick={() => handleStatusChange(task.id, 'DONE')}>Done</Dropdown.Item>
                      </Dropdown.Menu>
                    </Dropdown>
                  </div>
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </Container>
  );
};

export default TaskList;