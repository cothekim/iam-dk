import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { ConfigProvider, theme } from 'antd';
import Login from './components/Login';
import Layout from './components/Layout';
import Users from './components/Users';
import Groups from './components/Groups';
import Clients from './components/Clients';
import Provisioning from './components/Provisioning';

// API base URL
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8081/api/admin';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    setIsAuthenticated(!!token);
    setLoading(false);
  }, []);

  const handleLogin = (token) => {
    localStorage.setItem('token', token);
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setIsAuthenticated(false);
  };

  if (loading) {
    return <div style={{ padding: '50px', textAlign: 'center' }}>Loading...</div>;
  }

  return (
    <ConfigProvider theme={{ algorithm: theme.defaultAlgorithm }}>
      <Router>
        <Routes>
          <Route
            path="/login"
            element={
              isAuthenticated ? (
                <Navigate to="/" />
              ) : (
                <Login onLogin={handleLogin} apiBaseUrl={API_BASE_URL} />
              )
            }
          />
          <Route
            path="/*"
            element={
              isAuthenticated ? (
                <Layout onLogout={handleLogout} apiBaseUrl={API_BASE_URL}>
                  <Routes>
                    <Route path="/" element={<Navigate to="/users" />} />
                    <Route path="/users" element={<Users apiBaseUrl={API_BASE_URL} />} />
                    <Route path="/groups" element={<Groups apiBaseUrl={API_BASE_URL} />} />
                    <Route path="/clients" element={<Clients apiBaseUrl={API_BASE_URL} />} />
                    <Route path="/provisioning" element={<Provisioning apiBaseUrl={API_BASE_URL} />} />
                    <Route path="*" element={<Navigate to="/users" />} />
                  </Routes>
                </Layout>
              ) : (
                <Navigate to="/login" />
              )
            }
          />
        </Routes>
      </Router>
    </ConfigProvider>
  );
}

export default App;
