import React, { useState, useEffect } from 'react';
import { 
  Box, 
  Typography, 
  Container, 
  TextField, 
  Button, 
  List, 
  ListItem, 
  ListItemText, 
  IconButton,
  Paper
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import { getFirestore, collection, setDoc, deleteDoc, doc, getDocs } from 'firebase/firestore';
import { db } from '../firebase';

interface AuthorizedUser {
  id: string;
  email: string;
}

const Admin: React.FC = () => {
  const [email, setEmail] = useState('');
  const [authorizedUsers, setAuthorizedUsers] = useState<AuthorizedUser[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchAuthorizedUsers = async () => {
    try {
      const querySnapshot = await getDocs(collection(db, 'authorizedUsers'));
      const users = querySnapshot.docs.map(doc => ({
        id: doc.id,
        email: doc.id
      }));
      setAuthorizedUsers(users);
    } catch (error) {
      console.error('Error fetching authorized users:', error);
    }
  };

  useEffect(() => {
    fetchAuthorizedUsers();
  }, []);

  const handleAddUser = async () => {
    if (!email) return;

    try {
      setLoading(true);
      await setDoc(doc(db, 'authorizedUsers', email), {
        addedAt: new Date().toISOString()
      });
      setEmail('');
      await fetchAuthorizedUsers();
    } catch (error) {
      console.error('Error adding authorized user:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveUser = async (email: string) => {
    try {
      setLoading(true);
      await deleteDoc(doc(db, 'authorizedUsers', email));
      await fetchAuthorizedUsers();
    } catch (error) {
      console.error('Error removing authorized user:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="md">
      <Box sx={{ mt: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Manage Authorized Users
        </Typography>
        
        <Paper sx={{ p: 3, mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            Add New User
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
            <TextField
              fullWidth
              label="Email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={loading}
            />
            <Button
              variant="contained"
              onClick={handleAddUser}
              disabled={loading || !email}
            >
              Add User
            </Button>
          </Box>
        </Paper>

        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Authorized Users
          </Typography>
          <List>
            {authorizedUsers.map((user) => (
              <ListItem
                key={user.id}
                secondaryAction={
                  <IconButton
                    edge="end"
                    aria-label="delete"
                    onClick={() => handleRemoveUser(user.email)}
                    disabled={loading}
                  >
                    <DeleteIcon />
                  </IconButton>
                }
              >
                <ListItemText primary={user.email} />
              </ListItem>
            ))}
          </List>
        </Paper>
      </Box>
    </Container>
  );
};

export default Admin; 