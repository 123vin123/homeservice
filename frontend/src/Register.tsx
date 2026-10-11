import React, { useState } from 'react';
import './index.css';
import { API_BASE_URL } from './config';

interface RegisterProps {
  onSwitchToLogin: () => void;
}

export const Register: React.FC<RegisterProps> = ({ onSwitchToLogin }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phone, setPhone] = useState('');
  const [role, setRole] = useState('CUSTOMER');
  const [addressLine, setAddressLine] = useState('123 Main St');
  const [city, setCity] = useState('Metropolis');
  const [latitude, setLatitude] = useState('40.7128');
  const [longitude, setLongitude] = useState('-74.0060');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          email, password, firstName, lastName, phone, role,
          addressLine, city, latitude: parseFloat(latitude), longitude: parseFloat(longitude)
        }),
      });

      if (!response.ok) throw new Error('Registration failed. Email might already be in use.');

      const data = await response.json();
      setSuccessMsg('Registration Successful! Redirecting to login...');
      localStorage.setItem('token', data.token);
      setTimeout(() => {
        onSwitchToLogin();
      }, 1500);
    } catch (err: any) {
      setError(err.message || 'Something went wrong');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="login-container" style={{ maxWidth: '520px' }}>
        <div className="logo-container">
          <img src="/logo.jpg" alt="HouseEase" className="logo" style={{ height: '56px' }} />
        </div>
        <div className="login-header" style={{ marginBottom: '28px' }}>
          <h1>Create Account</h1>
          <p>Join HouseEase today</p>
        </div>

        <form onSubmit={handleSubmit}>
          {error && (
            <div style={{ color: '#f87171', fontSize: '14px', marginBottom: '16px', textAlign: 'center', background: 'rgba(248, 113, 113, 0.1)', border: '1px solid rgba(248, 113, 113, 0.2)', padding: '12px', borderRadius: '10px' }}>
              {error}
            </div>
          )}
          {successMsg && (
            <div style={{ color: '#4ade80', fontSize: '14px', marginBottom: '16px', textAlign: 'center', background: 'rgba(74, 222, 128, 0.1)', border: '1px solid rgba(74, 222, 128, 0.2)', padding: '12px', borderRadius: '10px' }}>
              ✨ {successMsg}
            </div>
          )}

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
            <div className="form-group">
              <label>First Name</label>
              <input type="text" value={firstName} onChange={(e) => setFirstName(e.target.value)} required />
            </div>
            <div className="form-group">
              <label>Last Name</label>
              <input type="text" value={lastName} onChange={(e) => setLastName(e.target.value)} required />
            </div>
          </div>

          <div className="form-group">
            <label>Email Address</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
            <div className="form-group">
              <label>Phone Number</label>
              <input type="tel" value={phone} onChange={(e) => setPhone(e.target.value)} />
            </div>
            <div className="form-group">
              <label>Account Type</label>
              <select value={role} onChange={(e) => setRole(e.target.value)}>
                <option value="CUSTOMER">Customer</option>
                <option value="PROVIDER">Provider</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
          </div>

          <div style={{ borderTop: '1px solid var(--border)', paddingTop: '20px', marginTop: '8px' }}>
            <label style={{ display: 'block', fontSize: '13px', fontWeight: 500, marginBottom: '14px', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>📍 Location Details</label>
            <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '14px', marginBottom: '14px' }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <input type="text" value={addressLine} onChange={(e) => setAddressLine(e.target.value)} placeholder="Address" required />
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <input type="text" value={city} onChange={(e) => setCity(e.target.value)} placeholder="City" required />
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <input type="number" step="0.0001" value={latitude} onChange={(e) => setLatitude(e.target.value)} placeholder="Latitude" required />
              </div>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <input type="number" step="0.0001" value={longitude} onChange={(e) => setLongitude(e.target.value)} placeholder="Longitude" required />
              </div>
            </div>
          </div>

          <button type="submit" className="btn-primary" disabled={isLoading} style={{ marginTop: '24px' }}>
            {isLoading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <div className="login-footer">
          Already have an account? <a href="#" onClick={(e) => { e.preventDefault(); onSwitchToLogin(); }}>Sign in</a>
        </div>
      </div>
    </div>
  );
};
