import React from 'react';
import { Link } from 'react-router-dom';

function Header() {
  return (
    <div className="header">
      <div className="header-content">
        <Link to="/" style={{ textDecoration: 'none', display: 'flex', alignItems: 'center' }}>
          <img 
            src="/favicon.ico" 
            alt="Suduoku Logo" 
            className="header-logo" 
          />
          <h1 className="header-title">Suduoku</h1>
        </Link>
      </div>
    </div>
  );
}

export default Header;