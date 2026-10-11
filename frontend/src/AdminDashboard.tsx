import React, { useState, useEffect } from 'react';
import './index.css';
import { API_BASE_URL } from './config';

interface AdminDashboardProps {
  onLogout: () => void;
  token: string;
}

export const AdminDashboard: React.FC<AdminDashboardProps> = ({ onLogout, token }) => {
  const [activeTab, setActiveTab] = useState<'SERVICES' | 'PROVIDERS'>('SERVICES');

  // -- Services State --
  const [categories, setCategories] = useState<any[]>([]);
  const [services, setServices] = useState<any[]>([]);

  // -- Providers State --
  const [providers, setProviders] = useState<any[]>([]);

  // Form State
  const [categoryId, setCategoryId] = useState('');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [durationMinutes, setDurationMinutes] = useState('60');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Category Form State
  const [newCatName, setNewCatName] = useState('');
  const [newCatDesc, setNewCatDesc] = useState('');
  const [isCatSubmitting, setIsCatSubmitting] = useState(false);

  const [isCategoryFormOpen, setIsCategoryFormOpen] = useState(false);
  const [isCategoryListOpen, setIsCategoryListOpen] = useState(true);
  const [isServiceFormOpen, setIsServiceFormOpen] = useState(false);
  const [isServiceListOpen, setIsServiceListOpen] = useState(true);
  const [isProviderListOpen, setIsProviderListOpen] = useState(true);

  const [toastMessage, setToastMessage] = useState<{ text: string, type: 'success' | 'error' } | null>(null);

  const showToast = (text: string, type: 'success' | 'error') => {
    setToastMessage({ text, type });
    setTimeout(() => setToastMessage(null), 4000);
  };
  const fetchCategories = () => {
    fetch(`${API_BASE_URL}/api/categories`)
      .then(res => res.json())
      .then(setCategories)
      .catch(console.error);
  };

  const fetchServices = () => {
    fetch(`${API_BASE_URL}/api/services`)
      .then(res => res.json())
      .then(setServices)
      .catch(console.error);
  };

  const fetchProviders = () => {
    fetch(`${API_BASE_URL}/api/providers/all`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
      .then(res => res.json())
      .then(setProviders)
      .catch(console.error);
  };

  useEffect(() => {
    fetchCategories();
    fetchServices();
    fetchProviders();
  }, [token]);

  const handleAddCategory = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsCatSubmitting(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/admin/categories`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          name: newCatName,
          description: newCatDesc
        })
      });
      if (!response.ok) throw new Error('Failed to create category');
      setNewCatName('');
      setNewCatDesc('');
      fetchCategories();
      showToast('Category created successfully!', 'success');
    } catch (err) {
      showToast('Error creating category', 'error');
    } finally {
      setIsCatSubmitting(false);
    }
  };

  const handleDeleteCategory = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this category? All related services might be affected.')) return;
    try {
      const response = await fetch(`${API_BASE_URL}/api/admin/categories/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (!response.ok) throw new Error('Failed to delete category');
      fetchCategories();
      showToast('Category deleted successfully!', 'success');
    } catch (err) {
      showToast('Error deleting category', 'error');
    }
  };

  const handleAddService = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      const response = await fetch(`${API_BASE_URL}/api/admin/services`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          categoryId,
          name,
          description,
          durationMinutes: parseInt(durationMinutes),
          supportsHomeService: true
        })
      });

      if (!response.ok) throw new Error('Failed to create service');

      // Reset form
      setName('');
      setDescription('');
      fetchServices();
      showToast('Service created successfully!', 'success');
    } catch (err) {
      showToast('Error creating service', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteService = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this global service?')) return;
    try {
      await fetch(`${API_BASE_URL}/api/admin/services/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      fetchServices();
      showToast('Service deleted successfully!', 'success');
    } catch (err) {
      showToast('Error deleting service', 'error');
    }
  };

  const handleUpdateProviderStatus = async (id: string, newStatus: string) => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/providers/${id}/status`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status: newStatus })
      });
      if (!response.ok) throw new Error('Failed to update status');
      fetchProviders();
      showToast('Provider status updated!', 'success');
    } catch (err) {
      showToast('Error updating provider status', 'error');
    }
  };

  return (
    <div className="dashboard-layout">
      {toastMessage && (
        <div style={{
          position: 'fixed',
          top: '20px',
          right: '20px',
          padding: '16px 24px',
          borderRadius: '8px',
          color: '#fff',
          backgroundColor: toastMessage.type === 'success' ? '#22c55e' : '#ef4444',
          zIndex: 9999,
          boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          fontWeight: 500
        }}>
          {toastMessage.type === 'success' ? '✅' : '❌'} {toastMessage.text}
        </div>
      )}
      <nav className="navbar">
        <div className="navbar-brand">
          <img src="/logo.jpg" alt="HouseEase" />
          <h1>HouseEase</h1>
          <span style={{ fontSize: '14px', background: '#3b82f6', color: 'white', padding: '4px 8px', borderRadius: '4px', verticalAlign: 'middle', marginLeft: '4px', fontWeight: 600 }}>Admin Portal</span>
        </div>
        <div className="navbar-actions">
          <button className="btn-ghost" onClick={onLogout}>Sign Out</button>
        </div>
      </nav>

      <section className="detail-section" style={{ paddingTop: '40px' }}>
        <div className="category-tabs" style={{ marginBottom: '30px' }}>
          <button
            className={`category-tab ${activeTab === 'SERVICES' ? 'active' : ''}`}
            onClick={() => setActiveTab('SERVICES')}
          >
            📋 Manage Catalog
          </button>
          <button
            className={`category-tab ${activeTab === 'PROVIDERS' ? 'active' : ''}`}
            onClick={() => setActiveTab('PROVIDERS')}
          >
            👥 Manage Providers
          </button>
        </div>

        {activeTab === 'SERVICES' && (
          <>
            <div className="detail-hero-card" style={{ marginBottom: isCategoryFormOpen ? '0' : '20px', cursor: 'pointer', borderRadius: isCategoryFormOpen ? 'var(--radius-lg) var(--radius-lg) 0 0' : 'var(--radius-lg)' }} onClick={() => setIsCategoryFormOpen(!isCategoryFormOpen)}>
              <div className="detail-header-left" style={{ marginBottom: '0' }}>
                <h2>{isCategoryFormOpen ? '▼' : '▶'} Add Category</h2>
                <p style={{ marginTop: '8px' }}>Create service categories to organize global services.</p>
              </div>
            </div>

            {isCategoryFormOpen && (
              <div style={{ padding: '24px', background: 'var(--bg-secondary)', border: '1px solid var(--border)', borderTop: 'none', borderRadius: '0 0 var(--radius-lg) var(--radius-lg)', marginBottom: '20px' }}>
                <form className="search-form" onSubmit={handleAddCategory} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '0' }}>
                  <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                    <label>Category Name</label>
                    <input type="text" placeholder="e.g. Cleaning" value={newCatName} onChange={e => setNewCatName(e.target.value)} required />
                  </div>
                  <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                    <label>Description</label>
                    <input type="text" placeholder="What is this category for?" value={newCatDesc} onChange={e => setNewCatDesc(e.target.value)} required />
                  </div>
                  <button type="submit" className="btn-search" disabled={isCatSubmitting} style={{ gridColumn: '1 / -1', marginTop: '16px' }}>
                    {isCatSubmitting ? 'Creating...' : '➕ Create Category'}
                  </button>
                </form>
              </div>
            )}

            <div className="detail-hero-card" style={{ marginBottom: isCategoryListOpen ? '0' : '40px', cursor: 'pointer', borderRadius: isCategoryListOpen ? 'var(--radius-lg) var(--radius-lg) 0 0' : 'var(--radius-lg)' }} onClick={() => setIsCategoryListOpen(!isCategoryListOpen)}>
              <div className="detail-header-left" style={{ marginBottom: '0' }}>
                <h2>{isCategoryListOpen ? '▼' : '▶'} Categories ({categories.length})</h2>
              </div>
            </div>

            {isCategoryListOpen && (
              <div style={{ padding: '24px', background: 'var(--bg-secondary)', border: '1px solid var(--border)', borderTop: 'none', borderRadius: '0 0 var(--radius-lg) var(--radius-lg)', marginBottom: '40px' }}>
                <div className="providers-grid">
                  {categories.map(c => (
                    <div key={c.id} className="provider-card">
                      <div className="provider-info">
                        <h4>{c.name}</h4>
                        <p className="provider-meta" style={{ marginTop: '5px' }}>{c.description}</p>
                      </div>
                      <div className="provider-right" style={{ textAlign: 'right' }}>
                        <button
                          onClick={() => handleDeleteCategory(c.id)}
                          style={{ background: 'none', border: '1px solid #ef4444', color: '#ef4444', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer', fontSize: '12px' }}
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div className="detail-hero-card" style={{ marginBottom: isServiceFormOpen ? '0' : '20px', cursor: 'pointer', borderRadius: isServiceFormOpen ? 'var(--radius-lg) var(--radius-lg) 0 0' : 'var(--radius-lg)' }} onClick={() => setIsServiceFormOpen(!isServiceFormOpen)}>
              <div className="detail-header-left" style={{ marginBottom: '0' }}>
                <h2>{isServiceFormOpen ? '▼' : '▶'} Add Global Service</h2>
                <p style={{ marginTop: '8px' }}>Create official services that providers can add to their profiles.</p>
              </div>
            </div>

            {isServiceFormOpen && (
              <div style={{ padding: '24px', background: 'var(--bg-secondary)', border: '1px solid var(--border)', borderTop: 'none', borderRadius: '0 0 var(--radius-lg) var(--radius-lg)', marginBottom: '20px' }}>
                <form className="search-form" onSubmit={handleAddService} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '0' }}>
                  <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                    <label>Category</label>
                    <select value={categoryId} onChange={e => setCategoryId(e.target.value)} required>
                      <option value="">-- Select Category --</option>
                      {categories.map(c => (
                        <option key={c.id} value={c.id}>{c.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                    <label>Service Name</label>
                    <input type="text" placeholder="e.g. Deep House Cleaning" value={name} onChange={e => setName(e.target.value)} required />
                  </div>
                  <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                    <label>Description</label>
                    <input type="text" placeholder="What does this service include?" value={description} onChange={e => setDescription(e.target.value)} required />
                  </div>
                  <div className="form-group">
                    <label>Duration (Minutes)</label>
                    <input type="number" min="15" step="15" value={durationMinutes} onChange={e => setDurationMinutes(e.target.value)} required />
                  </div>

                  <button type="submit" className="btn-search" disabled={isSubmitting} style={{ gridColumn: '1 / -1', marginTop: '16px' }}>
                    {isSubmitting ? 'Creating...' : '➕ Create Global Service'}
                  </button>
                </form>
              </div>
            )}

            <div className="detail-hero-card" style={{ marginBottom: isServiceListOpen ? '0' : '40px', cursor: 'pointer', borderRadius: isServiceListOpen ? 'var(--radius-lg) var(--radius-lg) 0 0' : 'var(--radius-lg)' }} onClick={() => setIsServiceListOpen(!isServiceListOpen)}>
              <div className="detail-header-left" style={{ marginBottom: '0' }}>
                <h2>{isServiceListOpen ? '▼' : '▶'} Global Services Catalog ({services.length})</h2>
              </div>
            </div>

            {isServiceListOpen && (
              <div style={{ padding: '24px', background: 'var(--bg-secondary)', border: '1px solid var(--border)', borderTop: 'none', borderRadius: '0 0 var(--radius-lg) var(--radius-lg)', marginBottom: '40px' }}>
                <div className="providers-grid">
                  {services.map(s => (
                    <div key={s.id} className="provider-card">
                      <div className="provider-info">
                        <h4>{s.name}</h4>
                        <p className="provider-meta" style={{ marginTop: '5px' }}>{s.description}</p>
                      </div>
                      <div className="provider-right" style={{ textAlign: 'right' }}>
                        <div style={{ fontSize: '12px', color: '#9ca3af', marginBottom: '8px' }}>{s.durationMinutes} mins</div>
                        <button
                          onClick={() => handleDeleteService(s.id)}
                          style={{ background: 'none', border: '1px solid #ef4444', color: '#ef4444', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer', fontSize: '12px' }}
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </>
        )}

        {activeTab === 'PROVIDERS' && (
          <>
            <div className="detail-hero-card" style={{ marginBottom: isProviderListOpen ? '0' : '40px', cursor: 'pointer', borderRadius: isProviderListOpen ? 'var(--radius-lg) var(--radius-lg) 0 0' : 'var(--radius-lg)' }} onClick={() => setIsProviderListOpen(!isProviderListOpen)}>
              <div className="detail-header-left" style={{ marginBottom: '0' }}>
                <h2>{isProviderListOpen ? '▼' : '▶'} Provider Management ({providers.length})</h2>
                <p style={{ marginTop: '8px' }}>Review and approve or suspend provider accounts across the platform.</p>
              </div>
            </div>

            {isProviderListOpen && (
              <div style={{ padding: '24px', background: 'var(--bg-secondary)', border: '1px solid var(--border)', borderTop: 'none', borderRadius: '0 0 var(--radius-lg) var(--radius-lg)', marginBottom: '40px' }}>
                <div className="providers-grid">
                  {providers.map(p => (
                    <div key={p.id} className="provider-card">
                      <div className="provider-info">
                        <h4>{p.firstName} {p.lastName}</h4>
                        <p className="provider-meta" style={{ marginTop: '5px' }}>{p.bio || 'No bio provided'}</p>

                        <div style={{ marginTop: '10px', display: 'flex', gap: '8px', alignItems: 'center' }}>
                          <span style={{
                            fontSize: '12px',
                            padding: '2px 8px',
                            borderRadius: '12px',
                            background: p.status === 'APPROVED' ? 'rgba(34, 197, 94, 0.2)' :
                              p.status === 'PENDING' ? 'rgba(234, 179, 8, 0.2)' : 'rgba(239, 68, 68, 0.2)',
                            color: p.status === 'APPROVED' ? '#4ade80' :
                              p.status === 'PENDING' ? '#facc15' : '#f87171'
                          }}>
                            {p.status}
                          </span>
                        </div>
                      </div>
                      <div className="provider-right" style={{ textAlign: 'right', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        <select
                          value={p.status}
                          onChange={(e) => handleUpdateProviderStatus(p.id, e.target.value)}
                          style={{
                            background: 'var(--bg-secondary)',
                            color: 'var(--text-primary)',
                            border: '1px solid var(--border)',
                            padding: '6px',
                            borderRadius: '4px',
                            fontSize: '12px',
                            cursor: 'pointer'
                          }}
                        >
                          <option value="PENDING">Pending</option>
                          <option value="APPROVED">Approved</option>
                          <option value="SUSPENDED">Suspended</option>
                        </select>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </>
        )}
      </section>
    </div>
  );
};
