/**
 * WebSocket configuration
 * Supports both local development and production environments
 */

const isDevelopment = false;

export const WS_URL = isDevelopment
  ? "ws://localhost:8080/ws"
  : "wss://suduoku-java.onrender.com/ws";

export const logWebSocketConnection = () => {
  console.log(`Connecting to WebSocket server at ${WS_URL}`);
};
