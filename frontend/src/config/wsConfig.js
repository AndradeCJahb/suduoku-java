/**
 * WebSocket configuration
 * Supports both local development and production environments
 */

const isDevelopment = true;

export const WS_URL = isDevelopment
  ? "ws://192.168.4.24:8080/ws"
  : "wss://suduoku-java.onrender.com/ws";
