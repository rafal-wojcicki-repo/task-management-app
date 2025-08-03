import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Form, Button, Card, Alert } from 'react-bootstrap';
import TaskService from '../services/task.service';

const TaskForm = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEditMode = !!id;

  const [task, setTask] = useState({
    title: '',
    description: '',
    status: 'TODO',
    priority: 'MEDIUM',
    dueDate: ''
  });
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [validated, setValidated] = useState(false);

  useEffect(() => {
    if (isEditMode) {
      setLoading(true);
      TaskService.getTaskById(id)
        .then(response => {
          const taskData = response.data;
          // Format the date for the input field (YYYY-MM-DD)
          if (taskData.dueDate) {
            const dueDate = new Date(taskData.dueDate);
            taskData.dueDate = dueDate.toISOString().split('T')[0];
          }
          setTask(taskData);
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
    }
  }, [id, isEditMode]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setTask({ ...task, [name]: value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const form = e.currentTarget;
    
    if (form.checkValidity() === false) {
      e.stopPropagation();
      setValidated(true);
      return;
    }

    setLoading(true);
    
    // Format the task data for API
    const taskData = { ...task };
    if (taskData.dueDate) {
      taskData.dueDate = new Date(taskData.dueDate).toISOString();
    }

    const saveTask = isEditMode
      ? TaskService.updateTask(id, taskData)
      : TaskService.createTask(taskData);

    saveTask
      .then(() => {
        navigate('/tasks');
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

  if (loading && isEditMode) {
    return <Container className="mt-4"><p>Loading task data...</p></Container>;
  }

  return (
    <Container className="mt-4">
      <Card className="form-container">
        <Card.Body>
          <h2 className="text-center mb-4">{isEditMode ? 'Edit Task' : 'Create New Task'}</h2>
          
          {error && (
            <Alert variant="danger">{error}</Alert>
          )}

          <Form noValidate validated={validated} onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
              <Form.Label column>Title</Form.Label>
              <Form.Control
                type="text"
                name="title"
                value={task.title}
                onChange={handleInputChange}
                required
                maxLength={100}
              />
              <Form.Control.Feedback type="invalid">
                Please provide a title.
              </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label column>Description</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                name="description"
                value={task.description || ''}
                onChange={handleInputChange}
                maxLength={500}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label column>Status</Form.Label>
              <Form.Select
                name="status"
                value={task.status}
                onChange={handleInputChange}
                required
              >
                <option value="TODO">To Do</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="DONE">Done</option>
              </Form.Select>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label column>Priority</Form.Label>
              <Form.Select
                name="priority"
                value={task.priority}
                onChange={handleInputChange}
                required
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </Form.Select>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label column>Due Date</Form.Label>
              <Form.Control
                type="date"
                name="dueDate"
                value={task.dueDate || ''}
                onChange={handleInputChange}
              />
            </Form.Group>

            <div className="d-flex justify-content-between mt-4">
              <Button variant="secondary" onClick={() => navigate('/tasks')}>
                Cancel
              </Button>
              <Button variant="primary" type="submit" disabled={loading}>
                {loading ? 'Saving...' : (isEditMode ? 'Update Task' : 'Create Task')}
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default TaskForm;