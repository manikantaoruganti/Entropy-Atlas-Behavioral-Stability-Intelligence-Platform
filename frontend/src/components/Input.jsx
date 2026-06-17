import React from 'react';

const Input = ({ label, error, className = '', ...props }) => {
  return (
    <div className="flex flex-col gap-1">
      {label && (
        <label htmlFor={props.id || props.name} className="label">
          {label}
        </label>
      )}
      <input
        className={`input ${error ? 'border-accent-red' : ''} ${className}`}
        {...props}
      />
      {error && <p className="text-accent-red text-xs mt-1">{error}</p>}
    </div>
  );
};

export default Input;
