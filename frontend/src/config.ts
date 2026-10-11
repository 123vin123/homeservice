const rawUrl: string = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').trim();

// Ensure the protocol is included if a domain like xxx.onrender.com is provided
export const API_BASE_URL: string = (rawUrl.startsWith('http://') || rawUrl.startsWith('https://'))
  ? rawUrl
  : `https://${rawUrl}`;
