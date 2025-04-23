import { getFirestore, doc, getDoc } from 'firebase/firestore';
import { app } from '../firebase';

const db = getFirestore(app);

export const isUserAuthorized = async (email: string): Promise<boolean> => {
  try {
    const userDoc = await getDoc(doc(db, 'authorizedUsers', email));
    return userDoc.exists();
  } catch (error) {
    console.error('Error checking user authorization:', error);
    return false;
  }
}; 