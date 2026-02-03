import React, { useState, useEffect } from 'react';
import { Table, Button, Upload, message, Tag, Space, Modal, Typography } from 'antd';
import { UploadOutlined, CloudUploadOutlined, DownloadOutlined } from '@ant-design/icons';
import createApi from '../api';

const { Dragger } = Upload;
const { Text, Paragraph } = Typography;

function Provisioning({ apiBaseUrl }) {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [dryRun, setDryRun] = useState(true);
  const [selectedFile, setSelectedFile] = useState(null);

  const api = createApi(apiBaseUrl);

  useEffect(() => {
    fetchJobs();
  }, []);

  const fetchJobs = async () => {
    setLoading(true);
    try {
      const response = await api.get('/provisioning/jobs');
      setJobs(response.data);
    } catch (error) {
      message.error('Failed to fetch jobs');
    } finally {
      setLoading(false);
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      message.warning('Please select a CSV file first');
      return;
    }

    setUploading(true);
    try {
      // Create job first
      const jobResponse = await api.post('/provisioning/jobs', {
        jobName: 'CSV Import - ' + new Date().toISOString(),
        sourceLocation: selectedFile.name,
        triggeredBy: 'admin',
      });

      // Upload and execute
      const formData = new FormData();
      formData.append('file', selectedFile);
      formData.append('dryRun', dryRun);

      await api.post(`/provisioning/jobs/${jobResponse.data.id}/execute`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      message.success(dryRun ? 'Dry run completed' : 'Provisioning completed');
      fetchJobs();
      setSelectedFile(null);
    } catch (error) {
      message.error(error.response?.data?.message || 'Provisioning failed');
    } finally {
      setUploading(false);
    }
  };

  const downloadTemplate = () => {
    const csvContent =
      'loginName,email,firstName,lastName,active\njohn.doe,john@example.com,John,Doe,true\njane.smith,jane@example.com,Jane,Smith,false';
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'users_template.csv';
    a.click();
    window.URL.revokeObjectURL(url);
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: 'Job Name', dataIndex: 'jobName' },
    {
      title: 'Status',
      dataIndex: 'status',
      width: 120,
      render: (status) => {
        const colors = {
          PENDING: 'default',
          RUNNING: 'processing',
          COMPLETED: 'success',
          FAILED: 'error',
        };
        return <Tag color={colors[status]}>{status}</Tag>;
      },
    },
    {
      title: 'Processed',
      dataIndex: 'totalProcessed',
      width: 90,
    },
    {
      title: 'Created',
      dataIndex: 'createdCount',
      width: 80,
      render: (count) => <Text type="success">{count}</Text>,
    },
    {
      title: 'Updated',
      dataIndex: 'updatedCount',
      width: 80,
      render: (count) => <Text type="warning">{count}</Text>,
    },
    {
      title: 'Failed',
      dataIndex: 'failedCount',
      width: 80,
      render: (count) => <Text type="danger">{count}</Text>,
    },
    { title: 'Type', dataIndex: 'sourceType', width: 80 },
    { title: 'Dry Run', dataIndex: 'dryRun', width: 90 },
    { title: 'Created At', dataIndex: 'createdAt', width: 160 },
    {
      title: 'Triggered By',
      dataIndex: 'triggeredBy',
      width: 120,
    },
  ];

  return (
    <div>
      <Space direction="vertical" style={{ width: '100%' }} size="large">
        <div>
          <Button icon={<DownloadOutlined />} onClick={downloadTemplate} style={{ marginBottom: 16 }}>
            Download CSV Template
          </Button>

          <div
            style={{
              padding: '20px',
              border: '1px solid #d9d9d9',
              borderRadius: '4px',
              backgroundColor: '#fafafa',
              marginBottom: 16,
            }}
          >
            <Paragraph>
              <Text strong>Required Fields:</Text> loginName, email, firstName, lastName
            </Paragraph>
            <Paragraph>
              <Text strong>Optional Fields:</Text> active (true/false)
            </Paragraph>
            <Paragraph>
              <Text strong>Max Rows:</Text> 5,000
            </Paragraph>
          </div>

          <Dragger
            accept=".csv"
            beforeUpload={(file) => {
              setSelectedFile(file);
              return false;
            }}
            onRemove={() => setSelectedFile(null)}
            fileList={selectedFile ? [selectedFile] : []}
            maxCount={1}
          >
            <p className="ant-upload-drag-icon">
              <CloudUploadOutlined style={{ fontSize: '48px', color: '#1890ff' }} />
            </p>
            <p className="ant-upload-text">Click or drag CSV file to upload</p>
            <p className="ant-upload-hint">Supports CSV format only</p>
          </Dragger>

          {selectedFile && (
            <div style={{ marginTop: 16 }}>
              <Space>
                <Button type="primary" icon={<UploadOutlined />} onClick={handleUpload} loading={uploading}>
                  Execute Import
                </Button>
                <Button onClick={() => setSelectedFile(null)}>Cancel</Button>
              </Space>
              <div style={{ marginTop: 8 }}>
                <label>
                  <input type="checkbox" checked={dryRun} onChange={(e) => setDryRun(e.target.checked)} />
                  {' '}Dry run (preview changes only)
                </label>
              </div>
            </div>
          )}
        </div>

        <div>
          <h3>Provisioning History</h3>
          <Table
            columns={columns}
            dataSource={jobs}
            loading={loading}
            rowKey="id"
            pagination={{ pageSize: 10 }}
            scroll={{ x: 1200 }}
          />
        </div>
      </Space>
    </div>
  );
}

export default Provisioning;
