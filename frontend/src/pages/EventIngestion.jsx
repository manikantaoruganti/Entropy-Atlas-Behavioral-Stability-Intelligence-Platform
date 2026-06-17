import React, { useState } from 'react';
import { useIngestEvent } from '../api/hooks';
import Card from '../components/Card';
import Input from '../components/Input';
import Button from '../components/Button';
import { Send, Plus, Trash2, Database, Zap } from 'lucide-react';

const EventIngestion = () => {
  const { mutate: ingestEvent, isPending } = useIngestEvent();
  
  const [formData, setFormData] = useState({
    entityId: '',
    entityType: 'USER',
    timestamp: '',
    location: '',
    resource: '',
    action: '',
    latency: '',
    payloadSize: '',
  });

  const [metadataFields, setMetadataFields] = useState([{ key: '', value: '' }]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleMetadataChange = (index, field, value) => {
    const updated = [...metadataFields];
    updated[index][field] = value;
    setMetadataFields(updated);
  };

  const addMetadataField = () => {
    setMetadataFields([...metadataFields, { key: '', value: '' }]);
  };

  const removeMetadataField = (index) => {
    const updated = metadataFields.filter((_, i) => i !== index);
    setMetadataFields(updated);
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    // Map metadata list to record object
    const metadata = {};
    metadataFields.forEach(({ key, value }) => {
      if (key.trim()) {
        metadata[key.trim()] = value.trim();
      }
    });

    const payload = {
      ...formData,
      latency: formData.latency ? parseInt(formData.latency, 10) : null,
      payloadSize: formData.payloadSize ? parseInt(formData.payloadSize, 10) : null,
      timestamp: formData.timestamp ? new Date(formData.timestamp).toISOString() : null,
      metadata: Object.keys(metadata).length > 0 ? metadata : null,
    };

    // Clean up empty strings
    Object.keys(payload).forEach((key) => {
      if (payload[key] === '') {
        payload[key] = null;
      }
    });

    ingestEvent(payload, {
      onSuccess: () => {
        // Clear fields except entityType
        setFormData({
          entityId: '',
          entityType: 'USER',
          timestamp: '',
          location: '',
          resource: '',
          action: '',
          latency: '',
          payloadSize: '',
        });
        setMetadataFields([{ key: '', value: '' }]);
      },
    });
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-text-primary flex items-center gap-2">
            <Send className="w-6 h-6 text-accent-cyan animate-pulse" /> Telemetry Event Ingestion
          </h1>
          <p className="text-text-secondary mt-1">Directly inject real-time behavioral signals to simulate systems load, trigger drift anomalies, or test pipeline components.</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Card title="Primary Identity & Classification">
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-mono uppercase text-text-muted mb-1.5">Entity ID (Required)</label>
                <Input
                  name="entityId"
                  value={formData.entityId}
                  onChange={handleChange}
                  placeholder="e.g. user-7489, service-auth"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-mono uppercase text-text-muted mb-1.5">Entity Type (Required)</label>
                <select
                  name="entityType"
                  value={formData.entityType}
                  onChange={handleChange}
                  className="w-full bg-bg-surface border border-border-default hover:border-border-emphasis focus:border-accent-cyan rounded px-3 py-2 text-sm text-text-primary outline-none transition-colors"
                  required
                >
                  <option value="USER">USER</option>
                  <option value="SERVICE">SERVICE</option>
                  <option value="DEVICE">DEVICE</option>
                  <option value="PIPELINE">PIPELINE</option>
                  <option value="DATABASE">DATABASE</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-mono uppercase text-text-muted mb-1.5">Action Executed (Required)</label>
                <Input
                  name="action"
                  value={formData.action}
                  onChange={handleChange}
                  placeholder="e.g. LOGIN, READ, PROCESS, CHECKOUT"
                  required
                />
              </div>
            </div>
          </Card>

          <Card title="Signal Metadata & Execution context">
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-mono uppercase text-text-muted mb-1.5">Resource Path / Target</label>
                <Input
                  name="resource"
                  value={formData.resource}
                  onChange={handleChange}
                  placeholder="e.g. /api/v1/auth, db.users"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-mono uppercase text-text-muted mb-1.5">Latency (ms)</label>
                  <Input
                    type="number"
                    name="latency"
                    value={formData.latency}
                    onChange={handleChange}
                    placeholder="e.g. 150"
                    min="0"
                  />
                </div>
                <div>
                  <label className="block text-xs font-mono uppercase text-text-muted mb-1.5">Payload Size (bytes)</label>
                  <Input
                    type="number"
                    name="payloadSize"
                    value={formData.payloadSize}
                    onChange={handleChange}
                    placeholder="e.g. 1024"
                    min="0"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-mono uppercase text-text-muted mb-1.5">Location / Zone</label>
                  <Input
                    name="location"
                    value={formData.location}
                    onChange={handleChange}
                    placeholder="e.g. US-EAST-1, EU-WEST"
                  />
                </div>
                <div>
                  <label className="block text-xs font-mono uppercase text-text-muted mb-1.5">Timestamp (UTC)</label>
                  <Input
                    type="datetime-local"
                    name="timestamp"
                    value={formData.timestamp}
                    onChange={handleChange}
                  />
                </div>
              </div>
            </div>
          </Card>
        </div>

        <Card title="Additional Context (SHAP Dimensions)">
          <div className="space-y-4">
            <p className="text-xs text-text-muted font-sans">Define key-value metadata. These values feed straight into features extractor for stability, volatility, and drift calculations.</p>
            
            <div className="space-y-2">
              {metadataFields.map((field, index) => (
                <div key={index} className="flex items-center gap-4">
                  <div className="flex-1">
                    <Input
                      placeholder="Dimension key (e.g. browser, device_os)"
                      value={field.key}
                      onChange={(e) => handleMetadataChange(index, 'key', e.target.value)}
                    />
                  </div>
                  <div className="flex-1">
                    <Input
                      placeholder="Value"
                      value={field.value}
                      onChange={(e) => handleMetadataChange(index, 'value', e.target.value)}
                    />
                  </div>
                  <button
                    type="button"
                    onClick={() => removeMetadataField(index)}
                    className="p-2 hover:text-accent-red text-text-muted transition-colors"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              ))}
            </div>

            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={addMetadataField}
              className="flex items-center gap-1"
            >
              <Plus className="w-3 h-3" /> Add Metadata Tag
            </Button>
          </div>
        </Card>

        <div className="flex justify-end">
          <Button
            type="submit"
            disabled={isPending}
            className="flex items-center gap-2 px-6"
          >
            {isPending ? (
              <>
                <Zap className="animate-spin w-4 h-4 text-bg-deepest" />
                Ingesting...
              </>
            ) : (
              <>
                <Send className="w-4 h-4" />
                Transmit Event Signal
              </>
            )}
          </Button>
        </div>
      </form>
    </div>
  );
};

export default EventIngestion;
