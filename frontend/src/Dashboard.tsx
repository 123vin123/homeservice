import React, { useState, useEffect } from 'react';
import './index.css';
import { API_BASE_URL } from './config';

interface DashboardProps {
  onLogout: () => void;
  token: string;
}

const SERVICE_ICONS: Record<string, { icon: string; bg: string }> = {
  'Leak Repair': { icon: '🔧', bg: 'rgba(59, 130, 246, 0.12)' },
  'Light Fixture Install': { icon: '💡', bg: 'rgba(250, 204, 21, 0.12)' },
  'Deep House Cleaning': { icon: '🧹', bg: 'rgba(168, 85, 247, 0.12)' },
  'Furniture Assembly': { icon: '🪑', bg: 'rgba(251, 146, 60, 0.12)' },
  'TV Mounting': { icon: '📺', bg: 'rgba(45, 212, 191, 0.12)' },
  'AC Tune-Up': { icon: '❄️', bg: 'rgba(96, 165, 250, 0.12)' },
  'Drain Snaking': { icon: '🚿', bg: 'rgba(74, 222, 128, 0.12)' },
  'Panel Upgrade': { icon: '⚡', bg: 'rgba(252, 211, 77, 0.12)' },
};

const CATEGORY_ICONS: Record<string, string> = {
  'Plumbing': '🔧',
  'Electrical': '⚡',
  'Cleaning': '🧹',
  'Handyman': '🛠️',
  'HVAC': '❄️',
};

export const Dashboard: React.FC<DashboardProps> = ({ onLogout, token }) => {
  const [services, setServices] = useState<any[]>([]);
  const [selectedService, setSelectedService] = useState<any | null>(null);
  const [activeCategory, setActiveCategory] = useState<string>('All');
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [providers, setProviders] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [searchAttempted, setSearchAttempted] = useState(false);
  const [userAddressId, setUserAddressId] = useState<string>('');
  const [isBooking, setIsBooking] = useState(false);
  const [activeView, setActiveView] = useState<'HOME' | 'PROFILE' | 'BOOKINGS'>('HOME');

  // Bookings State
  const [bookings, setBookings] = useState<any[]>([]);
  const [bookingNotes, setBookingNotes] = useState<string>('');
  const [paymentMethod, setPaymentMethod] = useState<string>('CREDIT_CARD');
  const [transactionDetails, setTransactionDetails] = useState<any>(null);
  const [checkoutState, setCheckoutState] = useState<{ providerId: string; price: number } | null>(null);
  const [cardNumber, setCardNumber] = useState('');
  const [upiId, setUpiId] = useState('');

  // Profile State
  const [userProfile, setUserProfile] = useState<any>(null);
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);

  // Address State
  const [userAddress, setUserAddress] = useState<any>(null);
  const [label, setLabel] = useState('Home');
  const [addressLine, setAddressLine] = useState('');
  const [city, setCity] = useState('');
  const [lat, setLat] = useState('');
  const [lng, setLng] = useState('');
  const [isSavingAddress, setIsSavingAddress] = useState(false);
  const [toast, setToast] = useState<{ message: string, type: 'success' | 'error' } | null>(null);

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

  const fetchAddress = () => {
    fetch(`${API_BASE_URL}/api/location/addresses`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
      .then(handleAuthError)
      .then(res => res.json())
      .then(data => {
        if (data && data.length > 0) {
          setUserAddress(data[0]);
          setUserAddressId(data[0].id);
          setLabel(data[0].label || '');
          setAddressLine(data[0].addressLine || '');
          setCity(data[0].city || '');
          setLat(data[0].latitude || '');
          setLng(data[0].longitude || '');
        } else {
          setUserAddress(null);
          setUserAddressId('88888888-8888-8888-8888-888888888888');
        }
      })
      .catch(console.error);
  };

  useEffect(() => {
    fetch(`${API_BASE_URL}/api/services`)
      .then(res => res.json())
      .then(data => setServices(data))
      .catch(console.error);

    fetchAddress();
    fetchUserProfile();

    // Pre-load Razorpay script
    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.async = true;
    document.body.appendChild(script);
    
    return () => {
      document.body.removeChild(script);
    };
  }, [token]);

  useEffect(() => {
    if (activeView === 'BOOKINGS') {
      fetch(`${API_BASE_URL}/api/bookings/customer`, { headers: { 'Authorization': `Bearer ${token}` } })
        .then(handleAuthError)
        .then(res => res.json())
        .then(setBookings)
        .catch(console.error);
    }
  }, [token, activeView]);

  const categories = ['All', ...Array.from(new Set(services.map(s => s.categoryName).filter(Boolean)))];

  const filteredServices = activeCategory === 'All'
    ? services
    : services.filter(s => s.categoryName === activeCategory);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedService || !date || !time) return;
    setIsLoading(true);
    setSearchAttempted(true);
    setProviders([]);

    // Use local Date to correctly handle timezones before sending to the backend
    const localDate = new Date(`${date}T${time}:00`);
    const desiredTime = localDate.toISOString();

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/matching?addressId=${userAddressId}&serviceId=${selectedService.id}&desiredTime=${encodeURIComponent(desiredTime)}`,
        { headers: { 'Authorization': `Bearer ${token}` } }
      );
      handleAuthError(response);
      if (!response.ok) throw new Error('Search failed');
      const data = await response.json();
      setProviders(data);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleServiceClick = (service: any) => {
    setSelectedService(service);
    setProviders([]);
    setSearchAttempted(false);
    const today = new Date();
    const nextWeekday = new Date();
    const day = today.getDay();
    const daysUntilMonday = day === 0 ? 1 : day === 6 ? 2 : day >= 1 && day <= 4 ? 1 : (8 - day);
    nextWeekday.setDate(today.getDate() + daysUntilMonday);
    setDate(nextWeekday.toISOString().split('T')[0]);
    setTime('10:00');
  };

  const getIcon = (name: string) => SERVICE_ICONS[name] || { icon: '🏠', bg: 'rgba(45, 212, 191, 0.12)' };

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
      showToast('Profile details updated successfully!', 'success');
      fetchUserProfile();
    } catch (err) {
      showToast('Error updating profile', 'error');
    } finally {
      setIsUpdatingProfile(false);
    }
  };

  const handleSaveAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSavingAddress(true);
    try {
      const url = userAddress
        ? `${API_BASE_URL}/api/location/addresses/${userAddress.id}`
        : `${API_BASE_URL}/api/location/addresses`;
      const method = userAddress ? 'PUT' : 'POST';

      const res = await fetch(url, {
        method,
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({ label, addressLine, city, latitude: parseFloat(lat), longitude: parseFloat(lng) })
      });
      handleAuthError(res);
      if (!res.ok) throw new Error('Failed to save address');
      showToast('Address saved successfully!', 'success');
      fetchAddress();
    } catch (err) {
      showToast('Error saving address', 'error');
    } finally {
      setIsSavingAddress(false);
    }
  };

  const handleBookService = async (providerId: string) => {
    if (!selectedService || !date || !time) {
      showToast('Please select a date and time first', 'error');
      return;
    }

    if (!userAddressId || userAddressId === '88888888-8888-8888-8888-888888888888') {
      showToast('Please set your address in the Profile tab before booking', 'error');
      setActiveView('PROFILE');
      return;
    }

    setIsBooking(true);
    try {
      // Parse local date and time, then convert to UTC ISO string for backend
      const localDate = new Date(`${date}T${time}:00`);
      const scheduledTime = localDate.toISOString();

      const requestBody = {
        providerId: providerId,
        serviceId: selectedService.id,
        addressId: userAddressId,
        scheduledTime: scheduledTime,
        notes: bookingNotes,
        paymentMethod: paymentMethod
      };

      const res = await fetch(`${API_BASE_URL}/api/bookings`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
      });

      handleAuthError(res);
      if (!res.ok) {
        const errorBody = await res.json().catch(() => null);
        throw new Error(errorBody?.message || 'Failed to book service');
      }

      const data = await res.json();
      
      if (paymentMethod === 'RAZORPAY') {
        const options = {
          key: import.meta.env.VITE_RAZORPAY_KEY_ID || 'rzp_test_TiJpMMRLL7JNvr',
          amount: checkoutState?.price ? checkoutState.price * 100 : 0,
          currency: 'INR',
          name: 'HouseEase',
          description: 'Service Booking',
          order_id: data.transactionId, 
          handler: async function (response: any) {
            try {
              const verifyRes = await fetch(`${API_BASE_URL}/api/payments/verify`, {
                method: 'POST',
                headers: {
                  'Authorization': `Bearer ${token}`,
                  'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                  bookingId: data.id,
                  razorpayOrderId: response.razorpay_order_id,
                  razorpayPaymentId: response.razorpay_payment_id,
                  razorpaySignature: response.razorpay_signature
                })
              });
              if (verifyRes.ok) {
                // Update transaction details with success
                setTransactionDetails({...data, transactionId: `${response.razorpay_order_id}|${response.razorpay_payment_id}`, status: 'CONFIRMED'});
                showToast('Payment successful! Booking confirmed.', 'success');
              } else {
                showToast('Payment verification failed.', 'error');
              }
            } catch (err) {
              showToast('Error verifying payment.', 'error');
            }
          },
          prefill: {
            name: `${userProfile?.firstName || ''} ${userProfile?.lastName || ''}`,
            email: userProfile?.email || '',
            contact: userProfile?.phone || ''
          },
          theme: {
            color: '#4f46e5'
          }
        };
        const rzp = new (window as any).Razorpay(options);
        rzp.on('payment.failed', function (response: any){
          showToast('Payment failed! ' + response.error.description, 'error');
        });
        rzp.open();
      } else {
        setTransactionDetails(data);
        showToast('Service booked successfully! Check your bookings tab.', 'success');
      }

      setProviders([]);
      setSelectedService(null);
      setBookingNotes('');
      setCheckoutState(null);

    } catch (err: any) {
      showToast(err.message || 'Error booking service. Please try again.', 'error');
    } finally {
      setIsBooking(false);
    }
  };

  return (
    <div className="dashboard-layout">
      {/* Navbar */}
      <nav className="navbar">
        <div className="navbar-brand">
          <img src="/logo.jpg" alt="HouseEase" />
          <h1>HouseEase</h1>
        </div>
        <div className="navbar-actions">
          <button className={`btn-ghost ${activeView === 'HOME' ? 'active' : ''}`} onClick={() => setActiveView('HOME')}>
            🏠 Home
          </button>
          <button className={`btn-ghost ${activeView === 'BOOKINGS' ? 'active' : ''}`} onClick={() => setActiveView('BOOKINGS')}>
            📅 My Bookings
          </button>
          <button className={`btn-ghost ${activeView === 'PROFILE' ? 'active' : ''}`} onClick={() => setActiveView('PROFILE')}>
            👤 Profile
          </button>
          <button className="btn-ghost" onClick={onLogout}>Sign Out</button>
        </div>
      </nav>

      {activeView === 'BOOKINGS' && (
        <section className="detail-section" style={{ paddingTop: '40px' }}>
          <div className="detail-hero-card" style={{ marginBottom: '40px' }}>
            <div className="detail-header-left" style={{ marginBottom: '24px' }}>
              <h2>My Bookings</h2>
              <p>View your upcoming and past service appointments.</p>
            </div>

            {bookings.length === 0 ? (
              <div className="empty-state">
                <div className="empty-state-icon">📅</div>
                <h3>No bookings yet</h3>
                <p>Find a service provider and book an appointment!</p>
                <button className="btn-primary" onClick={() => setActiveView('HOME')} style={{ marginTop: '20px' }}>
                  Browse Services
                </button>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                {bookings.map(b => (
                  <div key={b.id} style={{ background: 'var(--bg-secondary)', padding: '24px', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <h3 style={{ margin: '0 0 8px 0' }}>{b.serviceName}</h3>
                      <p style={{ margin: '0 0 4px 0', color: 'var(--text-secondary)' }}>
                        <strong>Provider:</strong> {b.providerName}
                      </p>
                      <p style={{ margin: '0 0 4px 0', color: 'var(--text-secondary)' }}>
                        <strong>Scheduled:</strong> {new Date(b.scheduledTime).toLocaleString()}
                      </p>
                      <p style={{ margin: '0', color: 'var(--text-secondary)' }}>
                        <strong>Price:</strong> ₹{b.totalPrice}
                      </p>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <span style={{ display: 'inline-block', padding: '6px 12px', borderRadius: '20px', fontSize: '0.875rem', fontWeight: 600, background: b.status === 'PENDING' ? '#fef3c7' : b.status === 'CONFIRMED' ? '#dbeafe' : b.status === 'IN_PROGRESS' ? '#e0e7ff' : b.status === 'COMPLETED' ? '#d1fae5' : '#fee2e2', color: b.status === 'PENDING' ? '#b45309' : b.status === 'CONFIRMED' ? '#1e40af' : b.status === 'IN_PROGRESS' ? '#3730a3' : b.status === 'COMPLETED' ? '#065f46' : '#991b1b' }}>
                        {b.status === 'IN_PROGRESS' ? 'IN PROGRESS' : b.status}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>
      )}

      {activeView === 'PROFILE' && (
        <section className="detail-section" style={{ paddingTop: '40px' }}>

          <div className="detail-hero-card" style={{ marginBottom: '40px' }}>
            <div className="detail-header-left" style={{ marginBottom: '24px' }}>
              <h2>Personal Details</h2>
              <p>Update your name, email, and phone number.</p>
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
              <div style={{ alignSelf: 'flex-end' }}>
                <button type="submit" className="btn-search" disabled={isUpdatingProfile}>
                  {isUpdatingProfile ? 'Saving...' : '💾 Save Details'}
                </button>
              </div>
            </form>
          </div>

          <div className="detail-hero-card">
            <div className="detail-header-left" style={{ marginBottom: '24px' }}>
              <h2>My Address</h2>
              <p>Set your home location so we can match you with nearby service providers.</p>
            </div>

            <form className="search-form" onSubmit={handleSaveAddress} style={{ background: 'var(--bg-secondary)', padding: '24px', borderRadius: 'var(--radius-lg)', border: '1px solid var(--border)', display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label>Label (e.g., Home)</label>
                  <input type="text" value={label} onChange={e => setLabel(e.target.value)} required />
                </div>
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label>City</label>
                  <input type="text" value={city} onChange={e => setCity(e.target.value)} required />
                </div>
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label>Latitude</label>
                  <input type="number" step="any" value={lat} onChange={e => setLat(e.target.value)} required />
                </div>
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label>Longitude</label>
                  <input type="number" step="any" value={lng} onChange={e => setLng(e.target.value)} required />
                </div>
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label>Full Address Line</label>
                <input type="text" value={addressLine} onChange={e => setAddressLine(e.target.value)} required />
              </div>

              <div style={{ alignSelf: 'flex-end' }}>
                <button type="submit" className="btn-search" disabled={isSavingAddress}>
                  {isSavingAddress ? 'Saving...' : '💾 Save Address'}
                </button>
              </div>
            </form>
          </div>
        </section>
      )}

      {activeView === 'HOME' && (!selectedService ? (
        <>
          {/* Hero */}
          <section className="hero">
            <div className="hero-greeting">Welcome to HouseEase</div>
            <h2>
              Find Trusted Pros for<br />
              <span className="gradient-text">Any Home Service</span>
            </h2>
            <p>
              Browse our handpicked service providers, compare prices, and book instantly. Quality work, guaranteed.
            </p>
          </section>

          {/* Category Tabs + Service Grid */}
          <section className="category-section">
            <div className="category-tabs">
              {categories.map(cat => (
                <button
                  key={cat}
                  className={`category-tab ${activeCategory === cat ? 'active' : ''}`}
                  onClick={() => setActiveCategory(cat)}
                >
                  {cat !== 'All' && CATEGORY_ICONS[cat] ? `${CATEGORY_ICONS[cat]} ` : ''}{cat}
                </button>
              ))}
            </div>

            <div className="services-grid">
              {filteredServices.map(s => {
                const { icon, bg } = getIcon(s.name);
                return (
                  <div key={s.id} className="service-card" onClick={() => handleServiceClick(s)}>
                    <div className="service-card-icon" style={{ background: bg }}>
                      {icon}
                    </div>
                    <div className="service-card-header">
                      <h3>{s.name}</h3>
                    </div>
                    <p className="service-card-desc">{s.description}</p>
                    <div className="service-card-meta">
                      <span>⏱️ {s.durationMinutes} min</span>
                      {s.categoryName && <span>📁 {s.categoryName}</span>}
                    </div>
                  </div>
                );
              })}
            </div>
          </section>
        </>
      ) : (
        <section className="detail-section" style={{ paddingTop: '40px' }}>
          <button className="back-btn" onClick={() => setSelectedService(null)}>
            ← Back to Services
          </button>

          {/* Service Detail Card */}
          <div className="detail-hero-card">
            <div className="detail-header">
              <div className="detail-header-left">
                <h2>{getIcon(selectedService.name).icon} {selectedService.name}</h2>
                <p>{selectedService.description}</p>
              </div>
            </div>

            <form className="search-form" onSubmit={handleSearch}>
              <div className="form-group" style={{ gridColumn: '1 / -1', display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
                <div style={{ flex: 1, minWidth: '150px' }}>
                  <label>Preferred Date</label>
                  <input type="date" value={date} onChange={e => setDate(e.target.value)} required style={{ width: '100%' }} />
                </div>
                <div style={{ flex: 1, minWidth: '150px' }}>
                  <label>Preferred Time</label>
                  <input type="time" value={time} onChange={e => setTime(e.target.value)} required style={{ width: '100%' }} />
                </div>
                <div style={{ flex: 2, minWidth: '300px' }}>
                  <label>Booking Notes (Optional)</label>
                  <input type="text" placeholder="e.g. Please ring the doorbell" value={bookingNotes} onChange={e => setBookingNotes(e.target.value)} style={{ width: '100%' }} />
                </div>
              </div>
              <div style={{ gridColumn: '1 / -1', display: 'flex', justifyContent: 'flex-end', marginTop: '16px' }}>
                <button type="submit" className="btn-search" disabled={isLoading} style={{ padding: '12px 32px', minWidth: '240px', fontSize: '1.1rem' }}>
                  {isLoading ? 'Searching...' : '🔍 Find Providers'}
                </button>
              </div>
            </form>
          </div>

          {/* Provider Results */}
          {providers.length > 0 && (
            <div className="providers-section">
              <h3>Available Providers ({providers.length})</h3>
              <div className="providers-grid">
                {providers.map((p, i) => (
                  <div key={p.providerId} className="provider-card" style={{ animation: `slideUp 0.4s ${i * 0.08}s both` }}>
                    <div className="provider-info">
                      <h4>{p.firstName} {p.lastName}</h4>
                      <p className="provider-meta">
                        <span className="rating">★ {p.averageRating}</span> · {p.totalReviews} reviews · {p.distanceKm} km away
                      </p>
                    </div>
                    <div className="provider-right">
                      <div className="provider-price">₹{p.price}</div>
                      <button
                        className="btn-book"
                        onClick={() => setCheckoutState({ providerId: p.providerId, price: p.price })}
                        disabled={isBooking}
                      >
                        {isBooking ? 'Booking...' : 'Book Now'}
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {providers.length === 0 && searchAttempted && !isLoading && (
            <div className="empty-state">
              <div className="empty-state-icon">🔍</div>
              <h3>No providers available</h3>
              <p>
                No providers found for <strong>{selectedService.name}</strong> at the selected time.
                Try a <strong>weekday (Mon–Fri)</strong> between <strong>9 AM – 5 PM</strong>.
              </p>
            </div>
          )}
        </section>
      ))}

      {/* Checkout Modal */}
      {checkoutState && !transactionDetails && (
        <div className="modal-overlay" style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 9999 }}>
          <div className="modal-content" style={{ background: 'var(--bg-card)', padding: '32px', borderRadius: 'var(--radius-lg)', maxWidth: '500px', width: '90%', border: '1px solid var(--border)', boxShadow: '0 10px 25px rgba(0,0,0,0.5)' }}>
            <h2 style={{ marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              Secure Checkout
            </h2>
            
            <div style={{ marginBottom: '24px' }}>
              <label style={{ display: 'block', marginBottom: '8px', color: 'var(--text-muted)' }}>Select Payment Method</label>
              <select 
                value={paymentMethod} 
                onChange={e => setPaymentMethod(e.target.value)} 
                style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid var(--border)', background: 'var(--bg-secondary)', color: 'var(--text-color)', fontSize: '1rem' }}
              >
                <option value="CREDIT_CARD">Credit Card (Simulated)</option>
                <option value="UPI">UPI (Simulated)</option>
                <option value="RAZORPAY">RazorPay (Real Test Mode)</option>
                <option value="WALLET">Wallet (Simulated)</option>
              </select>
            </div>

            <div style={{ marginBottom: '20px' }}>
              <p style={{ color: 'var(--text-muted)' }}>You are paying <strong>₹{checkoutState.price}</strong> via <strong>{paymentMethod.replace('_', ' ')}</strong></p>
            </div>

            {paymentMethod === 'CREDIT_CARD' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginBottom: '24px' }}>
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label>Card Number</label>
                  <input type="text" placeholder="XXXX XXXX XXXX XXXX" value={cardNumber} onChange={e => setCardNumber(e.target.value)} />
                </div>
                <div style={{ display: 'flex', gap: '16px' }}>
                  <div className="form-group" style={{ flex: 1, marginBottom: 0 }}>
                    <label>Expiry (MM/YY)</label>
                    <input type="text" placeholder="12/26" />
                  </div>
                  <div className="form-group" style={{ flex: 1, marginBottom: 0 }}>
                    <label>CVV</label>
                    <input type="password" placeholder="***" maxLength={4} />
                  </div>
                </div>
              </div>
            )}

            {paymentMethod === 'UPI' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginBottom: '24px' }}>
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label>Your UPI ID (VPA)</label>
                  <input type="text" placeholder="e.g. 9876543210@ybl" value={upiId} onChange={e => setUpiId(e.target.value)} />
                </div>
              </div>
            )}
            
            {paymentMethod === 'RAZORPAY' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginBottom: '24px' }}>
                <p style={{ color: 'var(--text-muted)' }}>You will be securely redirected to RazorPay to complete this transaction.</p>
              </div>
            )}
            
            {paymentMethod === 'WALLET' && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginBottom: '24px' }}>
                <p style={{ color: 'var(--text-muted)' }}>Amount will be deducted from your HouseEase Wallet.</p>
              </div>
            )}

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '16px' }}>
              <button className="btn-secondary" onClick={() => setCheckoutState(null)}>
                Cancel
              </button>
              <button className="btn-primary" onClick={() => {
                handleBookService(checkoutState.providerId);
              }}>
                {isBooking ? 'Processing...' : `Pay ₹${checkoutState.price}`}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Transaction Details Modal */}
      {transactionDetails && (
        <div className="modal-overlay" style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 9999 }}>
          <div className="modal-content" style={{ background: 'var(--bg-card)', padding: '32px', borderRadius: 'var(--radius-lg)', maxWidth: '500px', width: '90%', border: '1px solid var(--border)', boxShadow: '0 10px 25px rgba(0,0,0,0.5)' }}>
            <h2 style={{ marginBottom: '24px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{ color: 'var(--success-color)' }}>✅</span> Transaction Successful
            </h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', background: 'var(--bg-secondary)', padding: '20px', borderRadius: 'var(--radius-md)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-muted)' }}>Booking ID:</span>
                <strong>{transactionDetails.id?.substring(0, 8)}...</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-muted)' }}>Service:</span>
                <strong>{transactionDetails.serviceName}</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-muted)' }}>Provider:</span>
                <strong>{transactionDetails.providerName}</strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-muted)' }}>Payment Method:</span>
                <span style={{ background: 'var(--primary-color)', padding: '2px 8px', borderRadius: '12px', fontSize: '0.85rem' }}>
                  {transactionDetails.paymentMethod}
                </span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-muted)' }}>Transaction ID / Ref:</span>
                <strong style={{ fontFamily: 'monospace', wordBreak: 'break-all', textAlign: 'right', maxWidth: '200px' }}>
                  {transactionDetails.transactionId || 'PENDING'}
                </strong>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid var(--border)', paddingTop: '16px', marginTop: '8px' }}>
                <span style={{ color: 'var(--text-muted)' }}>Total Amount:</span>
                <strong style={{ fontSize: '1.2rem', color: 'var(--primary-color)' }}>₹{transactionDetails.totalPrice}</strong>
              </div>
            </div>
            
            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '24px' }}>
              <button className="btn-primary" onClick={() => { setTransactionDetails(null); setActiveView('BOOKINGS'); }}>
                View My Bookings
              </button>
            </div>
          </div>
        </div>
      )}

      {toast && (
        <div className={`toast-message toast-${toast.type}`}>
          {toast.message}
        </div>
      )}
    </div>
  );
};
