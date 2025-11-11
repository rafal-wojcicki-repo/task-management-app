import axios from 'axios';
import authHeader from './auth-header';

// Use relative path so nginx (prod) and CRA proxy (dev) can route to backend consistently
const API_URL = '/api/tasks/';

function jsonAuthHeaders() {
  return {
    'Content-Type': 'application/json',
    ...authHeader(),
  };
}

class TaskService {
  getAllTasks() {
    return axios.get(API_URL, { headers: authHeader() });
  }

  getMyTasks() {
    return axios.get(API_URL + 'my', { headers: authHeader() });
  }

  getTaskById(id) {
    return axios.get(API_URL + id, { headers: authHeader() });
  }

  createTask(task) {
    return axios.post(API_URL, task, { headers: jsonAuthHeaders() });
  }

  updateTask(id, task) {
    return axios.put(API_URL + id, task, { headers: jsonAuthHeaders() });
  }

  deleteTask(id) {
    return axios.delete(API_URL + id, { headers: authHeader() });
  }

  updateTaskStatus(id, status) {
    return axios.patch(
      API_URL + id + '/status',
      { status },
      { headers: jsonAuthHeaders() }
    );
  }
}

export default new TaskService();