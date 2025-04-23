import { initializeApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';

const firebaseConfig = {
  apiKey: process.env.REACT_APP_FIREBASE_API_KEY,
  authDomain: "district37-toastmasters.firebaseapp.com",
  projectId: "district37-toastmasters",
  storageBucket: "district37-toastmasters.firebasestorage.app",
  messagingSenderId: "117850780335",
  appId: "1:117850780335:web:7171bdb759ddc8b3d8da5a",
  measurementId: "G-4JTXWV37E1"
}

export const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
export const googleProvider = new GoogleAuthProvider(); 