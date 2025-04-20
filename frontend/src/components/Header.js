import React from 'react';
import { Link } from 'react-router-dom';

function Header() {
  const [showInfo, setShowInfo] = React.useState(false);

  const togglePopup = () => {
    setShowInfo(!showInfo);
  };

  return (
    <div className="header">
      <div className="header-content">
        <Link to="/" style={{ textDecoration: 'none', display: 'flex', alignItems: 'center' }}>
          <img 
            src="/favicon.ico" 
            alt="Suduoku Logo" 
            className="header-logo" 
          />
          <h1 className="header-title">
            Su<span className="header-title-duo">duo</span>ku
          </h1>
          
        </Link>
        <span className="header-info" onClick={togglePopup}>i</span>
        {showInfo && (
          <div className="info-popup">
            <div className="popup-content">
              <span className="close-popup" onClick={togglePopup}>&times;</span>
              <h2 className="info-title">About Suduoku</h2>
              <p className="info-text">Suduoku is a real-time collaborative Sudoku game where multiple players can solve puzzles together.</p>
              <p className="info-text">For more information or to report any issues, check out the project on <a href="https://github.com/AndradeCJahb/suduoku-java" target="_blank" rel="noreferrer">GitHub</a>.</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Header;