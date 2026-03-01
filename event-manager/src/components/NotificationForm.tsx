import React, { useState } from 'react';
import {
    Box,
    Button,
    TextField,
    Paper,
    Alert,
    FormControl,
    FormHelperText,
    InputLabel,
    MenuItem,
    Select,
    SelectChangeEvent,
} from '@mui/material';
import { notificationService } from '../services/notificationService';

interface NotificationFormProps {
    onSuccess?: () => void;
}

type NotificationTarget = 'GENERAL' | 'APP_ENV_DEBUG' | 'APP_ENV_PROD' | 'APP_VERSION' | 'CUSTOM';

const NotificationForm: React.FC<NotificationFormProps> = ({ onSuccess }) => {
    const [title, setTitle] = useState('');
    const [body, setBody] = useState('');
    const [target, setTarget] = useState<NotificationTarget>('GENERAL');
    const [version, setVersion] = useState('');
    const [customTopic, setCustomTopic] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    const normalizeTopicSegment = (raw: string) => {
        const trimmed = raw.trim();
        if (!trimmed) {
            return '';
        }
        return trimmed
            .replace(/\./g, '_')
            .replace(/[^A-Za-z0-9_-]/g, '_');
    };

    const resolveTopic = () => {
        if (target === 'APP_VERSION') {
            const normalized = normalizeTopicSegment(version);
            return normalized ? `APP_VERSION_${normalized}` : '';
        }
        if (target === 'CUSTOM') {
            return customTopic.trim().toUpperCase();
        }
        return target;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccess(null);

        try {
            const topic = resolveTopic();
            if (!topic) {
                throw new Error(
                    target === 'APP_VERSION'
                        ? 'Enter a version to target (for example: 8.0).'
                        : 'Enter a custom topic.'
                );
            }

            await notificationService.sendNotification(
                title,
                body,
                topic
            );

            setSuccess(`Notification sent successfully to topic "${topic}".`);
            setTitle('');
            setBody('');
            if (onSuccess) onSuccess();
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to send notification. Please try again.');
            console.error('Error sending notification:', err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Paper elevation={3} sx={{ p: 3, maxWidth: 600, mx: 'auto', mt: 4 }}>
            {error && (
                <Alert severity="error" sx={{ mb: 2 }}>
                    {error}
                </Alert>
            )}
            
            {success && (
                <Alert severity="success" sx={{ mb: 2 }}>
                    {success}
                </Alert>
            )}

            <form onSubmit={handleSubmit}>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <TextField
                        fullWidth
                        label="Title"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        required
                    />
                    
                    <TextField
                        fullWidth
                        label="Message"
                        value={body}
                        onChange={(e) => setBody(e.target.value)}
                        multiline
                        rows={4}
                        required
                    />

                    <FormControl fullWidth>
                        <InputLabel id="notification-target-label">Target</InputLabel>
                        <Select
                            labelId="notification-target-label"
                            label="Target"
                            value={target}
                            onChange={(e: SelectChangeEvent<NotificationTarget>) =>
                                setTarget(e.target.value as NotificationTarget)
                            }
                        >
                            <MenuItem value="GENERAL">All users (GENERAL)</MenuItem>
                            <MenuItem value="APP_ENV_DEBUG">Debug builds (APP_ENV_DEBUG)</MenuItem>
                            <MenuItem value="APP_ENV_PROD">Production builds (APP_ENV_PROD)</MenuItem>
                            <MenuItem value="APP_VERSION">Specific app version</MenuItem>
                            <MenuItem value="CUSTOM">Custom topic</MenuItem>
                        </Select>
                        <FormHelperText>
                            Clients subscribe to GENERAL + environment + version topics.
                        </FormHelperText>
                    </FormControl>

                    {target === 'APP_VERSION' && (
                        <TextField
                            fullWidth
                            label="App Version"
                            value={version}
                            onChange={(e) => setVersion(e.target.value)}
                            placeholder="8.0"
                            required
                            helperText={`Will send to topic: ${
                                resolveTopic() || 'APP_VERSION_<version>'
                            }`}
                        />
                    )}

                    {target === 'CUSTOM' && (
                        <TextField
                            fullWidth
                            label="Custom Topic"
                            value={customTopic}
                            onChange={(e) => setCustomTopic(e.target.value)}
                            placeholder="APP_VERSION_8_0"
                            required
                            helperText="Use this for advanced targeting."
                        />
                    )}

                    <Button
                        type="submit"
                        variant="contained"
                        color="primary"
                        fullWidth
                        disabled={loading}
                    >
                        {loading ? 'Sending...' : 'Send Notification'}
                    </Button>
                </Box>
            </form>
        </Paper>
    );
};

export default NotificationForm; 
