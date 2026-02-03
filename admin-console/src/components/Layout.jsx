import React, { useState } from 'react';
import { Layout, Menu, Button } from 'antd';
import {
  UserOutlined,
  TeamOutlined,
  AppstoreOutlined,
  CloudUploadOutlined,
  LogoutOutlined,
} from '@ant-design/icons';

const { Header, Content, Sider } = Layout;

function LayoutComponent({ children, onLogout }) {
  const [collapsed, setCollapsed] = useState(false);

  const menuItems = [
    {
      key: '/users',
      icon: <UserOutlined />,
      label: 'Users',
    },
    {
      key: '/groups',
      icon: <TeamOutlined />,
      label: 'Groups',
    },
    {
      key: '/clients',
      icon: <AppstoreOutlined />,
      label: 'OAuth Clients',
    },
    {
      key: '/provisioning',
      icon: <CloudUploadOutlined />,
      label: 'Provisioning',
    },
  ];

  const handleMenuClick = ({ key }) => {
    window.location.href = key;
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} onCollapse={setCollapsed}>
        <div
          style={{
            height: '32px',
            margin: '16px',
            color: 'white',
            fontSize: collapsed ? '16px' : '20px',
            fontWeight: 'bold',
            textAlign: 'center',
          }}
        >
          {collapsed ? 'IAM' : 'IAM-DK'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[window.location.pathname]}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            padding: '0 20px',
            background: '#fff',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            boxShadow: '0 1px 4px rgba(0,21,41,0.08)',
          }}
        >
          <h2 style={{ margin: 0 }}>IAM-DK Admin Console</h2>
          <Button
            type="primary"
            danger
            icon={<LogoutOutlined />}
            onClick={onLogout}
          >
            Logout
          </Button>
        </Header>
        <Content style={{ margin: '20px' }}>
          <div
            style={{
              padding: '24px',
              background: '#fff',
              borderRadius: '4px',
            }}
          >
            {children}
          </div>
        </Content>
      </Layout>
    </Layout>
  );
}

export default LayoutComponent;
