import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import createApi from '../api';

const { Title } = Typography;

function Login({ onLogin, apiBaseUrl }) {
  const [loading, setLoading] = useState(false);
  const api = createApi(apiBaseUrl);

  const onFinish = async (values) => {
    setLoading(true);
    try {
      const response = await api.post('/login', {
        username: values.username,
        password: values.password,
      });

      if (response.data.token) {
        message.success('Login successful');
        onLogin(response.data.token);
      }
    } catch (error) {
      message.error(error.response?.data?.error || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        backgroundColor: '#f0f2f5',
      }}
    >
      <Card style={{ width: 400 }}>
        <Title level={2} style={{ textAlign: 'center', marginBottom: '30px' }}>
          IAM-DK Admin
        </Title>
        <Form
          name="login"
          onFinish={onFinish}
          autoComplete="off"
          layout="vertical"
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: 'Please input your username' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="Username"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: 'Please input your password' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="Password"
              size="large"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              size="large"
              loading={loading}
              block
            >
              Log In
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}

export default Login;
