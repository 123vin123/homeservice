import { useState, useEffect } from 'react'
import { Login } from './Login'
import { Register } from './Register'
import { Dashboard } from './Dashboard'
import { ProviderDashboard } from './ProviderDashboard'
import { AdminDashboard } from './AdminDashboard'
import { API_BASE_URL } from './config'

function App() {
  const [currentView, setCurrentView] = useState<'login' | 'register'>('login')
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'))
  const [role, setRole] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(!!token)

  const handleLogout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setRole(null);
  }

  const fetchUserRole = async (jwt: string) => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/users/me`, {
        headers: { 'Authorization': `Bearer ${jwt}` }
      });
      if (response.ok) {
        const data = await response.json();
        setRole(data.role);
      } else {
        handleLogout();
      }
    } catch (err) {
      console.error(err);
      handleLogout();
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    if (token) {
      fetchUserRole(token);
    }
  }, [token])

  const handleLoginSuccess = (newToken: string) => {
    localStorage.setItem('token', newToken);
    setToken(newToken);
    setIsLoading(true);
    fetchUserRole(newToken);
  }

  if (isLoading) {
    return <div style={{ height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', color: 'white' }}>Loading...</div>;
  }

  if (token && role) {
    if (role === 'PROVIDER') {
      return <ProviderDashboard token={token} onLogout={handleLogout} />;
    }
    if (role === 'ADMIN') {
      return <AdminDashboard token={token} onLogout={handleLogout} />;
    }
    return <Dashboard token={token} onLogout={handleLogout} />
  }

  return (
    <>
      {currentView === 'login' ? (
        <Login onSwitchToRegister={() => setCurrentView('register')} onLoginSuccess={handleLoginSuccess} />
      ) : (
        <Register onSwitchToLogin={() => setCurrentView('login')} />
      )}
    </>
  )
}

export default App
