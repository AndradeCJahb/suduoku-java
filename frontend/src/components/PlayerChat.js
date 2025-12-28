import React from "react";
import webSocketManager from "./WebSocketManager";
import "../css/PlayerChat.css";

function PlayerChat({
	chatMessages,
	chatInput,
	setChatInput,
	chatLogRef,
	clientInfo,
	players,
	puzzleId,
}) {
	const sendChatMessage = () => {
		if (chatInput.trim() !== "") {
			const message = {
				user: clientInfo.name,
				color: clientInfo.color,
				text: chatInput,
				puzzleId: puzzleId,
			};

			webSocketManager.send({ type: "sendChat", message });
			setChatInput("");
		}
	};

	return (
		<div className="chat-stack">
			<div className="chat-box">
				<div className="chat-log" ref={chatLogRef}>
					{chatMessages.map((msg, index) => (
						<div key={index}>
							<strong style={{ color: msg.color || "#000" }}>
								{msg.user}:
							</strong>
							<span className="message">{msg.message}</span>
							<span className="time">
								{new Date(msg.time).toLocaleTimeString([], {
									hour: "2-digit",
									minute: "2-digit",
									hour12: true,
								})}
							</span>
						</div>
					))}
				</div>

				<div className="chatInput">
					<input
						type="text"
						value={chatInput}
						onChange={(e) => setChatInput(e.target.value)}
						onKeyDown={(e) => {
							if (e.key === "Enter") {
								sendChatMessage();
							}
						}}
						placeholder="Type to chat"
					/>
				</div>
			</div>

			<div className="players-section">
				<div className="clientInfo">
					<span>You are:</span>
					<span style={{ color: clientInfo.color }}> {clientInfo.name}</span>
				</div>

				<h3 className="playerHeader">Connected Players:</h3>

				<div className="playerList">
					<ul>
						{players.map((player, index) => (
							<li key={index} style={{ color: player.color }}>
								{player.name}
							</li>
						))}
					</ul>
				</div>
			</div>
		</div>
	);
}

export default PlayerChat;
