import React, { useState } from 'react';
import { 
    Box, 
    Button, 
    TextField, 
    Typography, 
    Paper, 
    Alert
} from '@mui/material';
import { notificationService } from '../services/api';

interface NotificationFormProps {
    onSuccess?: () => void;
}

const NotificationForm: React.FC<NotificationFormProps> = ({ onSuccess }) => {
    const [title, setTitle] = useState('');
    const [body, setBody] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        setSuccess(null);

        try {
            const response = await notificationService.sendNotification(
                title,
                body,
                'all_users' // Global topic for all users
            );

            setSuccess('Notification sent successfully to all users!');
            setTitle('');
            setBody('');
            if (onSuccess) onSuccess();
        } catch (err) {
            setError('Failed to send notification. Please try again.');
            console.error('Error sending notification:', err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Paper elevation={3} sx={{ p: 3, maxWidth: 600, mx: 'auto', mt: 4 }}>
            <Typography variant="h5" gutterBottom>
                Send Notification to All Users
            </Typography>
            
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

                    <Button
                        type="submit"
                        variant="contained"
                        color="primary"
                        fullWidth
                        disabled={loading}
                    >
                        {loading ? 'Sending...' : 'Send to All Users'}
                    </Button>
                </Box>
            </form>
        </Paper>
    );
};

export default NotificationForm; 