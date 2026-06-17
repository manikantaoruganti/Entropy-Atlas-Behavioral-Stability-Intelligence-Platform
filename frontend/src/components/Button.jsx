import React from 'react';
import { Loader2 } from 'lucide-react';

const Button = ({
  children,
  className = '',
  variant = 'primary',
  size = 'md',
  loading = false,
  icon: Icon,
  disabled,
  ...props
}) => {
  return (
    <button
      className={`btn btn-${variant} btn-${size} ${className}`}
      disabled={loading || disabled}
      {...props}
    >
      {loading ? (
        <Loader2 className="animate-spin h-4 w-4" />
      ) : (
        Icon && <Icon className="h-4 w-4" />
      )}
      {children}
    </button>
  );
};

export default Button;
