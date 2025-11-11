import axios from 'axios';

// Determine API base URL with sensible runtime defaults:
// 1) Use REACT_APP_API_BASE_URL if provided at build time.
// 2) If running on localhost (Docker or dev), call backend directly on http://localhost:8080 to avoid nginx proxy flakiness.
// 3) Otherwise, use relative path so any reverse proxy can handle /api.
const RUNTIME_LOCALHOST = (typeof window !== 'undefined' && window.location && window.location.hostname === 'localhost');
const SELECTED_BASE = process.env.REACT_APP_API_BASE_URL || (RUNTIME_LOCALHOST ? 'http://localhost:8080' : '');
const API_BASE = (SELECTED_BASE || '').replace(/\/$/, '');
const API_URL = `${API_BASE}/api/auth/`;

class AuthService {
    login(username, password) {
        return axios
            .post(API_URL + 'signin', {
                username,
                password
            })
            .then(response => {
                if (response.data.accessToken) {
                    localStorage.setItem('user', JSON.stringify(response.data));
                }
                return response.data;
            })
            .catch(error => {
                console.error('Login error:', error);
                throw error;
            });
    }

    logout() {
        localStorage.removeItem('user');
    }

    register(username, email, password) {
        return axios.post(API_URL + 'signup', {
            username,
            email,
            password
        });
    }

    getCurrentUser() {
        return JSON.parse(localStorage.getItem('user'));
    }

    isAuthenticated() {
        const user = this.getCurrentUser();
        return !!user && !!user.accessToken;
    }
}

export default new AuthService();