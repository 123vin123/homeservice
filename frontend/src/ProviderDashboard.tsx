import React, { useState, useEffect } from 'react';
import './index.css';
import { API_BASE_URL } from './config';

interface ProviderDashboardProps {
  onLogout: () => void;
  token: string;
}

export const ProviderDashboard: React.FC<ProviderDashboardProps> = ({ onLogout, token }) => {
  const [activeTab, setActiveTab] = useState<'SERVICES' | 'AVAILABILITY' | 'LOCATION' | 'PROFILE' | 'BOOKINGS'>('SERVICES');

  // -- Services State --
  const [myProfile, setMyProfile] = useState<any>(null);
  const [allServices, setAllServices] = useState<any[]>([]);
  const [selectedServiceId, setSelectedServiceId] = useState('');
  const [customPrice, setCustomPrice] = useState('');
  const [estimatedDuration, setEstimatedDuration] = useState('');
  const [isAddingService, setIsAddingService] = useState(false);

  // -- Availability State --
  const [availabilities, setAvailabilities] = useState<any[]>([]);
  const [dayOfWeek, setDayOfWeek] = useState('1');
  const [startTime, setStartTime] = useState('08:00');
  const [endTime, setEndTime] = useState('18:00');
  const [isAddingAvailability, setIsAddingAvailability] = useState(false);

  // -- Location State --
  const [serviceAreas, setServiceAreas] = useState<any[]>([]);
  const [lat, setLat] = useState('');
  const [lng, setLng] = useState('');
  const [radius, setRadius] = useState('50');
  const [isAddingLocation, setIsAddingLocation] = useState(false);

  // -- Profile State --
  const [, setUserProfile] = useState<any>(null);
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [bio, setBio] = useState('');
  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);

  // -- Bookings State --
  const [bookings, setBookings] = useState<any[]>([]);

  // -- Toast State --
  const [toast, setToast] = useState<{ message: string, type: 'success' | 'error' } | null>(null);

  // -- Modal State --
  const [confirmModal, setConfirmModal] = useState<{ isOpen: boolean, message: string, onConfirm: () => void } | null>(null);

  const requestConfirm = (message: string, onConfirm: () => void) => {
    setConfirmModal({ isOpen: true, message, onConfirm });
  };

  const showToast = (message: string, type: 'success' | 'error') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const handleAuthError = (res: Response) => {
    if (res.status === 401 || res.status === 403) {
      onLogout();
      throw new Error('Session expired. Please log in again.');
    }
    return res;
  };

  const fetchMyProfile = () => {
    fetch(`${API_BASE_URL}/api/providers/me`, { headers: { 'Authorization': `Bearer ${token}` } })
      .then(handleAuthError)
      .then(res => res.json())
      .then(data => {
        setMyProfile(data);
        setBio(data.bio || '');
      })
      .catch(console.error);
  };

  const fetchUserProfile = () => {
    fetch(`${API_BASE_URL}/api/users/me`, { headers: { 'Authorization': `Bearer ${token}` } })
      .then(handleAuthError)
      .then(res => res.json())
      .then(data => {
        setUserProfile(data);
        setFirstName(data.firstName || '');
        setLastName(data.lastName || '');
        setEmail(data.email || '');
        setPhone(data.phone || '');
      })
      .catch(console.error);
  };

  const fetchAllServices = () => {
    fetch(`${API_BASE_URL}/api/services`)
      .then(res => res.json())
      .then(setAllServices)
      .catch(console.error);
  };

  const fetchAvailabilities = () => {
    if (myProfile?.id) {
      fetch(`${API_BASE_URL}/api/providers/${myProfile.id}/availability`, { headers: { 'Authorization': `Bearer ${token}` } })
        .then(handleAuthError)
        .then(res => res.json())
        .then(data => setAvailabilities(data.weeklyAvailability || []))
        .catch(console.error);
    }
  };

  const fetchServiceAreas = () => {
    fetch(`${API_BASE_URL}/api/location/service-areas`, { headers: { 'Authorization': `Bearer ${token}` } })
      .then(handleAuthError)
      .then(res => res.json())
      .then(setServiceAreas)
      .catch(console.error);
  };

  useEffect(() => {
    fetchMyProfile();
    fetchAllServices();
  }, [token]);

  useEffect(() => {
    if (activeTab === 'AVAILABILITY') fetchAvailabilities();
    if (activeTab === 'LOCATION') fetchServiceAreas();
    if (activeTab === 'PROFILE') fetchUserProfile();
    if (activeTab === 'BOOKINGS') fetchBookings();
  }, [activeTab, myProfile?.id]);

  // ---- Service Handlers ----
  const handleAddService = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedServiceId) return;
    setIsAddingService(true);
    let url = `${API_BASE_URL}/api/providers/me/services/${selectedServiceId}?customPrice=${customPrice}`;
    if (estimatedDuration) {
      url += `&estimatedDurationMinutes=${estimatedDuration}`;
    }

    try {
      const response = await fetch(url, { method: 'POST', headers: { 'Authorization': `Bearer ${token}` } });
      handleAuthError(response);
      if (!response.ok) {
        const errData = await response.json().catch(() => ({}));
        throw new Error(errData.message || 'Failed to add service');
      }
      showToast('Service added successfully!', 'success');
      fetchMyProfile();
      setSelectedServiceId('');
      setCustomPrice('');
      setEstimatedDuration('');
    } catch (err: any) {
      const msg = err.message || 'Error adding service';
      if (msg.includes('APPROVED')) {
        showToast('Please contact admin for approval', 'error');
      } else {
        showToast(msg, 'error');
      }
    } finally {
      setIsAddingService(false);
    }
  };

  const handleRemoveService = (serviceId: string) => {
    requestConfirm('Are you sure you want to remove this service?', async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/api/providers/me/services/${serviceId}`, {
          method: 'DELETE', headers: { 'Authorization': `Bearer ${token}` }
        });
        handleAuthError(response);
        showToast('Service removed successfully!', 'success');
        fetchMyProfile();
      } catch (err) { showToast('Error removing service', 'error'); }
    });
  };

  // ---- Availability Handlers ----
  const handleAddAvailability = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsAddingAvailability(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/providers/me/availability`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({
          dayOfWeek: parseInt(dayOfWeek),
          startTime: startTime + ':00',
          endTime: endTime + ':00',
          isActive: true
        })
      });
      handleAuthError(response);
      if (!response.ok) throw new Error('Failed to add availability');
      showToast('Working hours added successfully!', 'success');
      fetchAvailabilities();
    } catch (err) {
      showToast('Error adding working hours', 'error');
    } finally {
      setIsAddingAvailability(false);
    }
  };

  const handleRemoveAvailability = (id: string) => {
    requestConfirm('Are you sure you want to remove this working hour block?', async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/api/providers/me/availability/${id}`, {
          method: 'DELETE', headers: { 'Authorization': `Bearer ${token}` }
        });
        handleAuthError(response);
        showToast('Working hours removed successfully!', 'success');
        fetchAvailabilities();
      } catch (err) { showToast('Error removing working hours', 'error'); }
    });
  };

  // ---- Location Handlers ----
  const handleAddLocation = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsAddingLocation(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/location/service-areas`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({
          latitude: parseFloat(lat),
          longitude: parseFloat(lng),
          radiusKm: parseFloat(radius)
        })
      });
      handleAuthError(response);
      if (!response.ok) throw new Error('Failed to add service area');
      showToast('Service area added successfully!', 'success');
      fetchServiceAreas();
      setLat('');
      setLng('');
      setRadius('50');
    } catch (err) {
      showToast('Error adding service area', 'error');
    } finally {
      setIsAddingLocation(false);
    }
  };

  const handleRemoveLocation = (id: string) => {
    requestConfirm('Are you sure you want to remove this service area?', async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/api/location/service-areas/${id}`, {
          method: 'DELETE', headers: { 'Authorization': `Bearer ${token}` }
        });
        handleAuthError(response);
        showToast('Service area removed successfully!', 'success');
        fetchServiceAreas();
      } catch (err) { showToast('Error removing service area', 'error'); }
    });
  };

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsUpdatingProfile(true);
    try {
      const userRes = await fetch(`${API_BASE_URL}/api/users/me`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({ firstName, lastName, email, phone })
      });
      handleAuthError(userRes);
      if (!userRes.ok) throw new Error('Failed to update user profile');

      const providerRes = await fetch(`${API_BASE_URL}/api/providers/me`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({ bio })
      });
      handleAuthError(providerRes);
      if (!providerRes.ok) throw new Error('Failed to update provider profile');

      showToast('Profile updated successfully!', 'success');
      fetchUserProfile();
      fetchMyProfile();
    } catch (err) {
      showToast('Error updating profile', 'error');
    } finally {
      setIsUpdatingProfile(false);
    }
  };

  // ---- Bookings Handlers ----
  const fetchBookings = () => {
    fetch(`${API_BASE_URL}/api/bookings/provider`, { headers: { 'Authorization': `Bearer ${token}` } })
      .then(res => {
        if (res.ok) return res.json();
        throw new Error();
      })
      .then(setBookings)
      .catch(console.error);
  };

  const updateBookingStatus = (bookingId: string, status: string) => {
    fetch(`${API_BASE_URL}/api/bookings/${bookingId}/status`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ status })
    })
      .then(res => {
        if (!res.ok) throw new Error();
        showToast(`Booking marked as ${status}`, 'success');
        fetchBookings();
      })
      .catch(() => showToast('Failed to update booking status', 'error'));
  };

  const availableToAdd = allServices.filter(s => {
    if (!myProfile || !myProfile.services) return true;
    return !myProfile.services.some((mySvc: any) => mySvc.serviceId === s.id);
  });

  const getDayName = (day: number) => {
    const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
    return days[day] || 'Unknown';
  };

  return (
    <div className="dashboard-layout">
      <nav className="navbar">
        <div className="navbar-brand">
          <img src="/logo.jpg" alt="HouseEase" />
          <h1>HouseEase</h1>
          <span style={{ fontSize: '14px', background: '#3b82f6', color: 'white', padding: '4px 8px', borderRadius: '4px', verticalAlign: 'middle', marginLeft: '4px', fontWeight: 600 }}>Provider Portal</span>
        </div>
        <div className="navbar-actions">
          <button className="btn-ghost" onClick={onLogout}>Sign Out</button>
        </div>
      </nav>

      <section className="detail-section" style={{ paddingTop: '40px' }}>

        {/* TAB NAVIGATION */}
        <div className="category-tabs" style={{ marginBottom: '30px' }}>
          <button
            className={`category-tab ${activeTab === 'BOOKINGS' ? 'active' : ''}`}
            onClick={() => setActiveTab('BOOKINGS')}
          >
            📅 Bookings
          </button>
          <button
            className={`category-tab ${activeTab === 'SERVICES' ? 'active' : ''}`}
            onClick={() => setActiveTab('SERVICES')}
          >
            🛠️ My Services
          </button>
          <button
            className={`category-tab ${activeTab === 'AVAILABILITY' ? 'active' : ''}`}
            onClick={() => setActiveTab('AVAILABILITY')}
          >
            ⏰ Working Hours
          </button>
          <button
            className={`category-tab ${activeTab === 'LOCATION' ? 'active' : ''}`}
            onClick={() => setActiveTab('LOCATION')}
          >
            📍 Service Area
          </button>
          <button
            className={`category-tab ${activeTab === 'PROFILE' ? 'active' : ''}`}
            onClick={() => setActiveTab('PROFILE')}
          >
            👤 Profile Settings
          </button>
        </div>

        {activeTab === 'BOOKINGS' && (
          <div className="detail-hero-card">
            <div className="detail-header-left" style={{ marginBottom: '24px' }}>
              <h2>My Bookings</h2>
              <p>Manage your upcoming appointments and update their status.</p>
            </div>

            {bookings.length === 0 ? (
              <div className="empty-state">
                <div className="empty-state-icon">📅</div>
                <h3>No bookings yet</h3>
                <p>You don't have any appointments scheduled right now.</p>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                {bookings.map(b => (
                  <div key={b.id} style={{ background: 'var(--bg-secondary)', padding: '24px', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <h3 style={{ margin: '0 0 8px 0' }}>{b.serviceName}</h3>
                      <p style={{ margin: '0 0 4px 0', color: 'var(--text-secondary)' }}>
                        <strong>Customer:</strong> {b.customerName} ({b.customerPhone})
                      </p>
                      <p style={{ margin: '0 0 4px 0', color: 'var(--text-secondary)' }}>
                        <strong>Location:</strong> {b.addressLine}, {b.city}
                      </p>
                      <p style={{ margin: '0 0 4px 0', color: 'var(--text-secondary)' }}>
                        <strong>Scheduled:</strong> {new Date(b.scheduledTime).toLocaleString()}
                      </p>
                      <p style={{ margin: '0', color: 'var(--text-secondary)' }}>
                        <strong>Price:</strong> ₹{b.totalPrice}
                      </p>
                    </div>
                    <div style={{ textAlign: 'right', display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '12px' }}>
                      <span style={{ display: 'inline-block', padding: '6px 12px', borderRadius: '20px', fontSize: '0.875rem', fontWeight: 600, background: b.status === 'PENDING' ? '#fef3c7' : b.status === 'CONFIRMED' ? '#dbeafe' : b.status === 'IN_PROGRESS' ? '#e0e7ff' : b.status === 'COMPLETED' ? '#d1fae5' : '#fee2e2', color: b.status === 'PENDING' ? '#b45309' : b.status === 'CONFIRMED' ? '#1e40af' : b.status === 'IN_PROGRESS' ? '#3730a3' : b.status === 'COMPLETED' ? '#065f46' : '#991b1b' }}>
                        {b.status === 'IN_PROGRESS' ? 'IN PROGRESS' : b.status}
                      </span>

                      {b.status === 'PENDING' && (
                        <div style={{ display: 'flex', gap: '8px' }}>
                          <button onClick={() => updateBookingStatus(b.id, 'CONFIRMED')} style={{ padding: '8px 12px', borderRadius: '6px', border: 'none', background: '#3b82f6', color: 'white', cursor: 'pointer', fontWeight: 500 }}>Accept</button>
                          <button onClick={() => updateBookingStatus(b.id, 'CANCELLED')} style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #ef4444', background: 'transparent', color: '#ef4444', cursor: 'pointer', fontWeight: 500 }}>Decline</button>
                        </div>
                      )}

                      {b.status === 'CONFIRMED' && (
                        <div style={{ display: 'flex', gap: '8px' }}>
                          <button onClick={() => updateBookingStatus(b.id, 'IN_PROGRESS')} style={{ padding: '8px 12px', borderRadius: '6px', border: 'none', background: '#6366f1', color: 'white', cursor: 'pointer', fontWeight: 500 }}>Start Job</button>
                          <button onClick={() => updateBookingStatus(b.id, 'CANCELLED')} style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #ef4444', background: 'transparent', color: '#ef4444', cursor: 'pointer', fontWeight: 500 }}>Cancel</button>
                        </div>
                      )}

                      {b.status === 'IN_PROGRESS' && (
                        <div style={{ display: 'flex', gap: '8px' }}>
                          <button onClick={() => updateBookingStatus(b.id, 'COMPLETED')} style={{ padding: '8px 12px', borderRadius: '6px', border: 'none', background: '#10b981', color: 'white', cursor: 'pointer', fontWeight: 500 }}>Mark Completed</button>
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {activeTab === 'SERVICES' && (
          <>
            <div className="detail-hero-card" style={{ marginBottom: '40px' }}>
              <div className="detail-header-left" style={{ marginBottom: '24px' }}>
                <h2>Manage Your Services</h2>
                <p>Add the services you offer and set your custom prices. Customers will see these when searching for help.</p>
              </div>

              <form className="search-form" onSubmit={handleAddService} style={{ background: 'var(--bg-secondary)', padding: '20px', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', gridTemplateColumns: '1fr 1fr 1fr auto' }}>
                <div className="form-group">
                  <label>Select a Service</label>
                  <select value={selectedServiceId} onChange={e => setSelectedServiceId(e.target.value)} required>
                    <option value="">-- Choose a service --</option>
                    {availableToAdd.map(s => (
                      <option key={s.id} value={s.id}>{s.name}</option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label>Your Price (₹)</label>
                  <input type="number" min="1" step="0.01" placeholder="Enter your price" value={customPrice} onChange={e => setCustomPrice(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label>Estimated Time (Minutes) <span style={{ color: '#9ca3af', fontWeight: 'normal' }}>(Optional)</span></label>
                  <input type="number" min="15" step="15" placeholder="e.g. 45" value={estimatedDuration} onChange={e => setEstimatedDuration(e.target.value)} />
                </div>
                <button type="submit" className="btn-search" disabled={isAddingService || !selectedServiceId}>
                  {isAddingService ? 'Adding...' : '➕ Add Service'}
                </button>
              </form>
            </div>

            <div className="providers-section">
              <h3>Services You Offer ({myProfile?.services?.length || 0})</h3>
              {myProfile && myProfile.services && myProfile.services.length > 0 ? (
                <div className="providers-grid">
                  {myProfile.services.map((svc: any) => (
                    <div key={svc.serviceId} className="provider-card">
                      <div className="provider-info">
                        <h4>{svc.serviceName}</h4>
                        <p className="provider-meta" style={{ marginTop: '5px' }}>Service added</p>
                      </div>
                      <div className="provider-right">
                        <div className="provider-price">₹{svc.customPrice}</div>
                        {svc.estimatedDurationMinutes && (
                          <div style={{ fontSize: '12px', color: '#9ca3af', marginTop: '4px' }}>⏱️ {svc.estimatedDurationMinutes} mins</div>
                        )}
                        <button onClick={() => handleRemoveService(svc.serviceId)} style={{ background: 'none', border: '1px solid #ef4444', color: '#ef4444', padding: '8px 16px', borderRadius: '6px', cursor: 'pointer' }}>Remove</button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="empty-state">
                  <div className="empty-state-icon">🛠️</div>
                  <h3>No services added yet</h3>
                  <p>Add your first service above so customers can start booking you!</p>
                </div>
              )}
            </div>
          </>
        )}

        {activeTab === 'AVAILABILITY' && (
          <>
            <div className="detail-hero-card" style={{ marginBottom: '40px' }}>
              <div className="detail-header-left" style={{ marginBottom: '24px' }}>
                <h2>Working Hours</h2>
                <p>Define what days and times you are available to be booked by customers.</p>
              </div>

              <form className="search-form" onSubmit={handleAddAvailability} style={{ background: 'var(--bg-secondary)', padding: '24px', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', gridTemplateColumns: '1fr 1fr 1fr auto' }}>
                <div className="form-group">
                  <label>Day of Week</label>
                  <select value={dayOfWeek} onChange={e => setDayOfWeek(e.target.value)} required>
                    <option value="1">Monday</option>
                    <option value="2">Tuesday</option>
                    <option value="3">Wednesday</option>
                    <option value="4">Thursday</option>
                    <option value="5">Friday</option>
                    <option value="6">Saturday</option>
                    <option value="7">Sunday</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Start Time</label>
                  <input type="time" value={startTime} onChange={e => setStartTime(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label>End Time</label>
                  <input type="time" value={endTime} onChange={e => setEndTime(e.target.value)} required />
                </div>
                <button type="submit" className="btn-search" disabled={isAddingAvailability}>
                  {isAddingAvailability ? 'Adding...' : '➕ Add Hours'}
                </button>
              </form>
            </div>

            <div className="providers-section">
              <h3>Your Standard Availability ({availabilities.length})</h3>
              {availabilities.length > 0 ? (
                <div className="providers-grid">
                  {availabilities.map((avail: any) => (
                    <div key={avail.id} className="provider-card">
                      <div className="provider-info">
                        <h4>{getDayName(avail.dayOfWeek)}</h4>
                        <p className="provider-meta" style={{ marginTop: '5px' }}>{avail.startTime.slice(0, 5)} to {avail.endTime.slice(0, 5)}</p>
                      </div>
                      <div className="provider-right">
                        <button onClick={() => handleRemoveAvailability(avail.id)} style={{ background: 'none', border: '1px solid #ef4444', color: '#ef4444', padding: '8px 16px', borderRadius: '6px', cursor: 'pointer' }}>Remove</button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="empty-state">
                  <div className="empty-state-icon">⏰</div>
                  <h3>No availability set</h3>
                  <p>You cannot be booked until you add your working hours!</p>
                </div>
              )}
            </div>
          </>
        )}

        {activeTab === 'LOCATION' && (
          <>
            <div className="detail-hero-card" style={{ marginBottom: '40px' }}>
              <div className="detail-header-left" style={{ marginBottom: '24px' }}>
                <h2>Service Area</h2>
                <p>Define the geographical area where you offer your services.</p>
              </div>

              {serviceAreas.length === 0 ? (
                <form className="search-form" onSubmit={handleAddLocation} style={{ background: 'var(--bg-secondary)', padding: '24px', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', gridTemplateColumns: '1fr 1fr 1fr auto' }}>
                  <div className="form-group">
                    <label>Latitude</label>
                    <input type="number" step="any" placeholder="e.g. 40.7128" value={lat} onChange={e => setLat(e.target.value)} required />
                  </div>
                  <div className="form-group">
                    <label>Longitude</label>
                    <input type="number" step="any" placeholder="e.g. -74.0060" value={lng} onChange={e => setLng(e.target.value)} required />
                  </div>
                  <div className="form-group">
                    <label>Radius (km)</label>
                    <input type="number" step="1" value={radius} onChange={e => setRadius(e.target.value)} required />
                  </div>
                  <button type="submit" className="btn-search" disabled={isAddingLocation}>
                    {isAddingLocation ? 'Adding...' : '➕ Add Area'}
                  </button>
                </form>
              ) : (
                <div style={{ background: 'var(--bg-secondary)', padding: '24px', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', textAlign: 'center' }}>
                  <h4 style={{ color: 'var(--text-secondary)' }}>You have already defined your service area.</h4>
                  <p style={{ fontSize: '14px', color: 'var(--text-dim)', marginTop: '8px' }}>Remove your existing area below to add a new one.</p>
                </div>
              )}
            </div>

            <div className="providers-section">
              <h3>Your Covered Areas ({serviceAreas.length})</h3>
              {serviceAreas.length > 0 ? (
                <div className="providers-grid">
                  {serviceAreas.map((area: any) => (
                    <div key={area.id} className="provider-card">
                      <div className="provider-info">
                        <h4>Coordinates</h4>
                        <p className="provider-meta" style={{ marginTop: '5px' }}>Lat: {area.latitude}, Lng: {area.longitude}</p>
                      </div>
                      <div className="provider-right">
                        <div className="provider-price" style={{ fontSize: '18px' }}>{area.radiusKm} km</div>
                        <button onClick={() => handleRemoveLocation(area.id)} style={{ background: 'none', border: '1px solid #ef4444', color: '#ef4444', padding: '8px 16px', borderRadius: '6px', cursor: 'pointer' }}>Remove</button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="empty-state">
                  <div className="empty-state-icon">📍</div>
                  <h3>No service areas set</h3>
                  <p>You cannot be found in searches until you define a service radius!</p>
                </div>
              )}
            </div>
          </>
        )}

        {activeTab === 'PROFILE' && (
          <>
            <div className="detail-hero-card" style={{ marginBottom: '40px' }}>
              <div className="detail-header-left" style={{ marginBottom: '24px' }}>
                <h2>Profile Settings</h2>
                <p>Update your personal details and public bio.</p>
              </div>

              <form className="search-form" onSubmit={handleUpdateProfile} style={{ background: 'var(--bg-secondary)', padding: '24px', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', display: 'flex', flexDirection: 'column', gap: '20px' }}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                  <div className="form-group" style={{ marginBottom: 0 }}>
                    <label>First Name</label>
                    <input type="text" value={firstName} onChange={e => setFirstName(e.target.value)} required />
                  </div>
                  <div className="form-group" style={{ marginBottom: 0 }}>
                    <label>Last Name</label>
                    <input type="text" value={lastName} onChange={e => setLastName(e.target.value)} required />
                  </div>
                  <div className="form-group" style={{ marginBottom: 0 }}>
                    <label>Email Address</label>
                    <input type="email" value={email} onChange={e => setEmail(e.target.value)} required />
                  </div>
                  <div className="form-group" style={{ marginBottom: 0 }}>
                    <label>Phone Number</label>
                    <input type="text" value={phone} onChange={e => setPhone(e.target.value)} required />
                  </div>
                </div>

                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label>Public Bio</label>
                  <textarea
                    value={bio}
                    onChange={e => setBio(e.target.value)}
                    style={{ width: '100%', padding: '14px 16px', borderRadius: 'var(--radius-md)', border: '1px solid var(--border)', background: 'var(--bg-secondary)', color: 'var(--text-primary)', fontSize: '15px', fontFamily: 'inherit', minHeight: '100px' }}
                    required
                  />
                </div>

                <div style={{ alignSelf: 'flex-end' }}>
                  <button type="submit" className="btn-search" disabled={isUpdatingProfile}>
                    {isUpdatingProfile ? 'Saving...' : '💾 Save Changes'}
                  </button>
                </div>
              </form>
            </div>
          </>
        )}

      </section>

      {toast && (
        <div className={`toast-message toast-${toast.type}`}>
          {toast.message}
        </div>
      )}

      {confirmModal?.isOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Confirm Action</h3>
            <p>{confirmModal.message}</p>
            <div className="modal-actions">
              <button className="btn-cancel" onClick={() => setConfirmModal(null)}>Cancel</button>
              <button className="btn-danger" onClick={() => {
                confirmModal.onConfirm();
                setConfirmModal(null);
              }}>Yes, Remove</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
