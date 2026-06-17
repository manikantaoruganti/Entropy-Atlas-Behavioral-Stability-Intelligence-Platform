import React from 'react';

const Card = ({ children, className = '', title, description, animate = true, delay = 0 }) => {
  const animationClass = animate ? 'animate-slide-up' : '';
  const style = animate && delay ? { animationDelay: `${delay}s` } : {};

  return (
    <div className={`glass-card ${animationClass} ${className}`} style={style}>
      {(title || description) && (
        <div className="glass-card-header">
          <div>
            {title && <h3 className="glass-card-title">{title}</h3>}
            {description && <p className="text-xs text-text-muted mt-1">{description}</p>}
          </div>
        </div>
      )}
      <div className="glass-card-body">
        {children}
      </div>
    </div>
  );
};

export default Card;
