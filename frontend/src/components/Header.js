import React from "react";
import { Link } from "react-router-dom";
import InfoPopup from "./InfoPopup";
import "../css/Header.css";

function Header() {
	const [showInfo, setShowInfo] = React.useState(false);

	const togglePopup = () => {
		setShowInfo(!showInfo);
	};

	return (
		<div className="header">
			<div className="header-content">
				<Link
					to="/"
					style={{
						textDecoration: "none",
						display: "flex",
						alignItems: "center",
					}}
				>
					<img src="/favicon.ico" alt="Suduoku Logo" className="header-logo" />
					<h1 className="header-title">
						Su<span className="header-title-duo">duo</span>ku
					</h1>
				</Link>

				<span className="header-info" onClick={togglePopup}>
					i
				</span>

				<InfoPopup visible={showInfo} onClose={togglePopup} />
			</div>
		</div>
	);
}

export default Header;
