import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, message, Space } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import createApi from '../api';

function Groups({ apiBaseUrl }) {
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingGroup, setEditingGroup] = useState(null);
  const [pagination, setPagination] = useState({ current: 1, pageSize: 10, total: 0 });

  const [form] = Form.useForm();
  const api = createApi(apiBaseUrl);

  useEffect(() => {
    fetchGroups();
  }, [pagination.current, pagination.pageSize]);

  const fetchGroups = async () => {
    setLoading(true);
    try {
      const response = await api.get('/scim/v2/Groups', {
        params: {
          startIndex: (pagination.current - 1) * pagination.pageSize + 1,
          count: pagination.pageSize,
        },
      });
      setGroups(response.data.Resources);
      setPagination((prev) => ({ ...prev, total: response.data.totalResults }));
    } catch (error) {
      message.error('Failed to fetch groups');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingGroup(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (group) => {
    setEditingGroup(group);
    form.setFieldsValue({
      displayName: group.displayName,
    });
    setModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      await api.delete(`/scim/v2/Groups/${id}`);
      message.success('Group deleted successfully');
      fetchGroups();
    } catch (error) {
      message.error('Failed to delete group');
    }
  };

  const handleSubmit = async (values) => {
    try {
      const groupData = {
        schemas: ['urn:ietf:params:scim:schemas:core:2.0:Group'],
        displayName: values.displayName,
      };

      if (editingGroup) {
        await api.put(`/scim/v2/Groups/${editingGroup.id}`, groupData);
        message.success('Group updated successfully');
      } else {
        await api.post('/scim/v2/Groups', groupData);
        message.success('Group created successfully');
      }
      setModalVisible(false);
      fetchGroups();
    } catch (error) {
      message.error(error.response?.data?.detail || 'Operation failed');
    }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: 'Name', dataIndex: 'displayName' },
    {
      title: 'Members',
      dataIndex: 'members',
      width: 100,
      render: (members) => (members ? members.length : 0),
    },
    {
      title: 'Actions',
      width: 120,
      render: (_, record) => (
        <Space>
          <Button icon={<EditOutlined />} size="small" onClick={() => handleEdit(record)} />
          <Button icon={<DeleteOutlined />} size="small" danger onClick={() => handleDelete(record.id)} />
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
          Add Group
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={groups}
        loading={loading}
        rowKey="id"
        pagination={{
          ...pagination,
          showSizeChanger: true,
          showTotal: (total) => `Total ${total} groups`,
          onChange: (page, pageSize) => setPagination({ ...pagination, current: page, pageSize }),
        }}
      />

      <Modal
        title={editingGroup ? 'Edit Group' : 'Create Group'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={() => form.submit()}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="displayName" label="Group Name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default Groups;
