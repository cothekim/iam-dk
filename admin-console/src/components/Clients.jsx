import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, Switch, message, Space, Tag, Popconfirm } from 'antd';
import { PlusOutlined, KeyOutlined, DeleteOutlined } from '@ant-design/icons';
import createApi from '../api';

const { TextArea } = Input;

function Clients({ apiBaseUrl }) {
  const [clients, setClients] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [secretModalVisible, setSecretModalVisible] = useState(false);
  const [newSecret, setNewSecret] = useState('');
  const [editingClient, setEditingClient] = useState(null);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });

  const [form] = Form.useForm();
  const api = createApi(apiBaseUrl);

  useEffect(() => {
    fetchClients();
  }, [pagination.current, pagination.pageSize]);

  const fetchClients = async () => {
    setLoading(true);
    try {
      const response = await api.get('/clients', {
        params: {
          page: pagination.current - 1,
          size: pagination.pageSize,
        },
      });
      setClients(response.data.content);
      setPagination((prev) => ({ ...prev, total: response.data.totalElements }));
    } catch (error) {
      message.error('Failed to fetch clients');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingClient(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (client) => {
    setEditingClient(client);
    form.setFieldsValue({
      clientId: client.clientId,
      name: client.name,
      description: client.description,
      redirectUris: client.redirectUris.join('\n'),
      grantTypes: client.grantTypes,
      scopes: client.scopes,
      enabled: client.enabled,
    });
    setModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      await api.delete(`/clients/${id}`);
      message.success('Client deleted successfully');
      fetchClients();
    } catch (error) {
      message.error('Failed to delete client');
    }
  };

  const handleSubmit = async (values) => {
    try {
      const clientData = {
        ...values,
        redirectUris: values.redirectUris.split('\n').filter((uri) => uri.trim()),
        grantTypes: values.grantTypes || [],
        scopes: values.scopes || [],
      };

      if (editingClient) {
        await api.put(`/clients/${editingClient.id}`, clientData);
        message.success('Client updated successfully');
      } else {
        clientData.clientSecret = values.clientSecret || generateSecret();
        await api.post('/clients', clientData);
        message.success('Client created successfully');
      }
      setModalVisible(false);
      fetchClients();
    } catch (error) {
      message.error(error.response?.data?.message || 'Operation failed');
    }
  };

  const handleRegenerateSecret = async (client) => {
    try {
      const response = await api.post(`/clients/${client.id}/regenerate-secret`);
      setNewSecret('New secret generated! Update your configuration.');
      setSecretModalVisible(true);
      message.success('Secret regenerated successfully');
      fetchClients();
    } catch (error) {
      message.error('Failed to regenerate secret');
    }
  };

  const generateSecret = () => {
    return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: 'Client ID', dataIndex: 'clientId' },
    { title: 'Name', dataIndex: 'name' },
    {
      title: 'Grant Types',
      dataIndex: 'grantTypes',
      width: 150,
      render: (types) => types?.map((t) => <Tag key={t}>{t}</Tag>) || '-',
    },
    {
      title: 'Enabled',
      dataIndex: 'enabled',
      width: 80,
      render: (enabled) => <Tag color={enabled ? 'green' : 'red'}>{enabled ? 'Yes' : 'No'}</Tag>,
    },
    {
      title: 'Actions',
      width: 140,
      render: (_, record) => (
        <Space>
          <Button size="small" onClick={() => handleEdit(record)}>
            Edit
          </Button>
          <Button icon={<KeyOutlined />} size="small" onClick={() => handleRegenerateSecret(record)} />
          <Popconfirm
            title="Are you sure you want to delete this client?"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button icon={<DeleteOutlined />} size="small" danger />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          Add OAuth Client
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={clients}
        loading={loading}
        rowKey="id"
        pagination={{
          ...pagination,
          showSizeChanger: true,
          showTotal: (total) => `Total ${total} clients`,
          onChange: (page, pageSize) => setPagination({ ...pagination, current: page, pageSize }),
        }}
      />

      <Modal
        title={editingClient ? 'Edit OAuth Client' : 'Create OAuth Client'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="clientId" label="Client ID" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="clientSecret" label="Client Secret" rules={[{ required: !editingClient }]}>
            <Input.Password />
          </Form.Item>
          <Form.Item name="name" label="Name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input />
          </Form.Item>
          <Form.Item name="redirectUris" label="Redirect URIs (one per line)" rules={[{ required: true }]}>
            <TextArea rows={3} placeholder="http://localhost:3000/auth/callback" />
          </Form.Item>
          <Form.Item
            name="grantTypes"
            label="Grant Types"
            rules={[{ required: true }]}
          >
            <Select mode="multiple" placeholder="Select grant types">
              <Select.Option value="authorization_code">Authorization Code</Select.Option>
              <Select.Option value="client_credentials">Client Credentials</Select.Option>
              <Select.Option value="refresh_token">Refresh Token</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="scopes" label="Scopes" rules={[{ required: true }]}>
            <Select mode="tags" placeholder="Add scopes">
              <Select.Option value="openid">openid</Select.Option>
              <Select.Option value="profile">profile</Select.Option>
              <Select.Option value="email">email</Select.Option>
              <Select.Option value="read">read</Select.Option>
              <Select.Option value="write">write</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="enabled" label="Enabled" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="Secret Regenerated"
        open={secretModalVisible}
        onCancel={() => setSecretModalVisible(false)}
        onOk={() => setSecretModalVisible(false)}
      >
        <p>{newSecret}</p>
      </Modal>
    </div>
  );
}

export default Clients;
