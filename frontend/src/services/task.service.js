import axios from 'axios';
import authHeader from './auth-header';

const API_URL = '/api/tasks/';

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
    return axios.post(API_URL, task, { headers: authHeader() });
  }

  updateTask(id, task) {
    return axios.put(API_URL + id, task, { headers: authHeader() });
  }

  deleteTask(id) {
    return axios.delete(API_URL + id, { headers: authHeader() });
  }

  updateTaskStatus(id, status) {
    return axios.patch(
      API_URL + id + '/status', 
      { status }, 
      { headers: authHeader() }
    );
  }
}

export default new TaskService();