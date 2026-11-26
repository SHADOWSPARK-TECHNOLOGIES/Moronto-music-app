import { initializeApp } from 'firebase/app';
import {
  GoogleAuthProvider,
  type User,
  createUserWithEmailAndPassword,
  getAuth,
  reload,
  sendEmailVerification,
  sendPasswordResetEmail,
  signInWithEmailAndPassword,
  signInWithPopup,
  signOut,
  updateProfile,
} from 'firebase/auth';
import {
  Timestamp,
  collection,
  doc,
  getDoc,
  getDocFromServer,
  getDocs,
  getFirestore,
  query,
  serverTimestamp,
  setDoc,
  updateDoc,
  where,
} from 'firebase/firestore';
import firebaseConfig from '../firebase-applet-config.json';
import type { AppLanguage, MorontoThemeId } from './model/musicoletModels';

export const FIREBASE_PROJECT_ID = firebaseConfig.projectId;
export const FIREBASE_DATABASE_ID = firebaseConfig.firestoreDatabaseId;

export const app = initializeApp(firebaseConfig);
export const db = getFirestore(app, firebaseConfig.firestoreDatabaseId);
export const auth = getAuth(app);

export enum OperationType {
  CREATE = 'create',
  UPDATE = 'update',
  DELETE = 'delete',
  LIST = 'list',
  GET = 'get',
  WRITE = 'write',
}

export interface FirestoreErrorInfo {
  error: string;
  operationType: OperationType;
  path: string | null;
  authInfo: {
    userId?: string | null;
    email?: string | null;
    emailVerified?: boolean | null;
    isAnonymous?: boolean | null;
    tenantId?: string | null;
    providerInfo?: {
      providerId?: string | null;
      email?: string | null;
    }[];
  };
}

export function handleFirestoreError(
  error: unknown,
  operationType: OperationType,
  path: string | null,
): never {
  const errInfo: FirestoreErrorInfo = {
    error: error instanceof Error ? error.message : String(error),
    authInfo: {
      userId: auth.currentUser?.uid,
      email: auth.currentUser?.email,
      emailVerified: auth.currentUser?.emailVerified,
      isAnonymous: auth.currentUser?.isAnonymous,
      tenantId: auth.currentUser?.tenantId,
      providerInfo:
        auth.currentUser?.providerData?.map((provider) => ({
          providerId: provider.providerId,
          email: provider.email,
        })) ?? [],
    },
    operationType,
    path,
  };
  console.error('Firestore Error: ', JSON.stringify(errInfo));
  throw new Error(JSON.stringify(errInfo));
}

async function testConnection(): Promise<void> {
  try {
    await getDocFromServer(doc(db, 'test', 'connection'));
  } catch (error) {
    if (error instanceof Error && error.message.includes('the client is offline')) {
      console.error('Please check your Firebase configuration.');
    }
  }
}

void testConnection();

export type SupportDeviceType = 'android' | 'iphone' | 'windows-mac' | 'other';

export interface SupportRequestRecord {
  id: string;
  userId: string;
  userEmail: string;
  displayName: string;
  deviceType: SupportDeviceType;
  topic: string;
  message: string;
  status: 'open' | 'resolved';
  createdAtMs: number;
}

export function mapFirebaseAuthError(error: unknown): string {
  const code =
    typeof error === 'object' && error !== null && 'code' in error
      ? String((error as { code?: unknown }).code)
      : '';

  switch (code) {
    case 'auth/operation-not-allowed':
      return `Email/Password login is not enabled yet in Firebase Console for project ${FIREBASE_PROJECT_ID} (Authentication -> Sign-in method -> Email/Password). Please use Google Sign-In or ask the project owner to enable Email/Password.`;
    case 'auth/email-already-in-use':
      return 'An account already exists with this email address. Switch to Sign In instead.';
    case 'auth/invalid-email':
      return 'Enter a valid email address.';
    case 'auth/weak-password':
      return 'Password must be at least 6 characters long.';
    case 'auth/invalid-credential':
    case 'auth/wrong-password':
    case 'auth/user-not-found':
      return 'Incorrect email or password. Double-check your details or reset your password.';
    case 'auth/popup-closed-by-user':
      return 'Google sign-in popup was closed before finishing.';
    case 'auth/popup-blocked':
      return 'Browser blocked the sign-in popup. Please allow popups for this site and try again.';
    case 'auth/too-many-requests':
      return 'Too many attempts. Wait a moment and try again.';
    case 'auth/network-request-failed':
      return 'Network connection required to sign in or create your Moronto Music account.';
    default:
      return error instanceof Error
        ? error.message
        : 'Authentication failed. Please try again.';
  }
}

const googleProvider = new GoogleAuthProvider();
googleProvider.setCustomParameters({ prompt: 'select_account' });

export async function signInWithGoogleAccount(): Promise<User> {
  const cred = await signInWithPopup(auth, googleProvider);
  return cred.user;
}

export async function createEmailAccount(input: {
  displayName: string;
  email: string;
  password: string;
}): Promise<User> {
  const cleanName = input.displayName.trim();
  const cleanEmail = input.email.trim();
  const cred = await createUserWithEmailAndPassword(auth, cleanEmail, input.password);
  if (cleanName) {
    await updateProfile(cred.user, { displayName: cleanName.slice(0, 80) });
  }
  try {
    await sendEmailVerification(cred.user);
  } catch {
    // Do not block account creation if verification dispatch is throttled
  }
  return cred.user;
}

export async function signInWithEmailAccount(
  email: string,
  password: string,
): Promise<User> {
  const cred = await signInWithEmailAndPassword(auth, email.trim(), password);
  return cred.user;
}

export async function sendAccountPasswordReset(email: string): Promise<void> {
  await sendPasswordResetEmail(auth, email.trim());
}

export async function resendVerificationEmailForCurrentUser(): Promise<void> {
  if (!auth.currentUser) {
    throw new Error('Sign in first to resend your verification email.');
  }
  await sendEmailVerification(auth.currentUser);
}

export async function reloadCurrentUser(): Promise<User | null> {
  if (!auth.currentUser) return null;
  await reload(auth.currentUser);
  return auth.currentUser;
}

export async function signOutAccount(): Promise<void> {
  await signOut(auth);
}

function resolveProviderId(user: User): 'google.com' | 'password' | 'other' {
  const first = user.providerData[0]?.providerId;
  if (first === 'google.com') return 'google.com';
  if (first === 'password') return 'password';
  return 'other';
}

export async function syncPrivateUserProfile(prefs: {
  language: AppLanguage;
  themeId: MorontoThemeId;
}): Promise<void> {
  const user = auth.currentUser;
  if (!user || !user.emailVerified || !user.email) return;

  const profilePath = `users/${user.uid}/private/profile`;
  const ref = doc(db, 'users', user.uid, 'private', 'profile');
  const displayName = (user.displayName?.trim() || user.email.split('@')[0] || 'Listener').slice(
    0,
    80,
  );

  let snapExists = false;
  try {
    const snap = await getDoc(ref);
    snapExists = snap.exists();
  } catch (error) {
    handleFirestoreError(error, OperationType.GET, profilePath);
  }

  if (!snapExists) {
    try {
      await setDoc(ref, {
        uid: user.uid,
        displayName,
        email: user.email.slice(0, 160),
        providerId: resolveProviderId(user),
        language: prefs.language,
        themeId: prefs.themeId,
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
      });
    } catch (error) {
      handleFirestoreError(error, OperationType.CREATE, profilePath);
    }
    return;
  }

  try {
    await updateDoc(ref, {
      displayName,
      language: prefs.language,
      themeId: prefs.themeId,
      updatedAt: serverTimestamp(),
    });
  } catch (error) {
    handleFirestoreError(error, OperationType.UPDATE, profilePath);
  }
}

export async function createSupportRequestInFirestore(input: {
  topic: string;
  message: string;
  deviceType: SupportDeviceType;
}): Promise<SupportRequestRecord> {
  const user = auth.currentUser;
  if (!user) {
    throw new Error('Sign in with your Moronto Music account to contact support.');
  }
  if (!user.emailVerified) {
    throw new Error(
      'Verify your email address first (check your inbox or tap Resend Verification) to save support tickets.',
    );
  }
  const requestId = `req_${user.uid.slice(0, 12)}_${Date.now()}`;
  const path = `support_requests/${requestId}`;
  const displayName = (user.displayName?.trim() || user.email?.split('@')[0] || 'Listener').slice(
    0,
    80,
  );
  const userEmail = (user.email ?? '').slice(0, 160);
  const topic = input.topic.trim().slice(0, 120);
  const message = input.message.trim().slice(0, 1000);

  try {
    await setDoc(doc(db, 'support_requests', requestId), {
      userId: user.uid,
      userEmail,
      displayName,
      deviceType: input.deviceType,
      topic,
      message,
      status: 'open',
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    });
  } catch (error) {
    handleFirestoreError(error, OperationType.CREATE, path);
  }

  return {
    id: requestId,
    userId: user.uid,
    userEmail,
    displayName,
    deviceType: input.deviceType,
    topic,
    message,
    status: 'open',
    createdAtMs: Date.now(),
  };
}

export async function listUserSupportRequests(): Promise<SupportRequestRecord[]> {
  const user = auth.currentUser;
  if (!user || !user.emailVerified) return [];

  const path = 'support_requests';
  try {
    const q = query(
      collection(db, 'support_requests'),
      where('userId', '==', user.uid),
    );
    const snap = await getDocs(q);
    const items: SupportRequestRecord[] = [];
    snap.forEach((d) => {
      const data = d.data();
      const createdAtRaw = data.createdAt;
      const createdAtMs =
        createdAtRaw instanceof Timestamp
          ? createdAtRaw.toMillis()
          : Date.now();
      items.push({
        id: d.id,
        userId: String(data.userId ?? user.uid),
        userEmail: String(data.userEmail ?? ''),
        displayName: String(data.displayName ?? ''),
        deviceType: (data.deviceType as SupportDeviceType) ?? 'other',
        topic: String(data.topic ?? ''),
        message: String(data.message ?? ''),
        status: data.status === 'resolved' ? 'resolved' : 'open',
        createdAtMs,
      });
    });
    return items.sort((a, b) => b.createdAtMs - a.createdAtMs);
  } catch (error) {
    handleFirestoreError(error, OperationType.LIST, path);
  }
}
