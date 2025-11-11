export default function authHeader() {
  const userStr = localStorage.getItem('user');
  if (!userStr) {
    return {};
  }

  try {
    const user = JSON.parse(userStr);
    if (user && user.accessToken) {
      return { Authorization: 'Bearer ' + user.accessToken };
    }
  } catch (error) {
    console.error('Error parsing user from localStorage:', error);
    localStorage.removeItem('user');
  }

  return {};
}