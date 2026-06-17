import React from 'react';

const Select = ({ label, options, error, className = '', ...props }) => {
  return (
    <div className="flex flex-col gap-1">
      {label && (
        <label htmlFor={props.id || props.name} className="label">
          {label}
        </label>
      )}
      <select
        className={`select ${error ? 'border-accent-red' : ''} ${className}`}
        {...props}
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {error && <p className="text-accent-red text-xs mt-1">{error}</p>}
    </div>
  );
};

export default Select;
