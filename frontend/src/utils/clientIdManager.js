/**
 * Utility functions for client ID management
 * Handles UUID generation and localStorage persistence
 */

/**
 * Generates a random UUID v4
 * @returns {string} Random UUID
 */
export function generateUUID() {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function (c) {
    var r = (Math.random() * 16) | 0,
      v = c === "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

/**
 * Gets or creates a client ID from localStorage
 * @returns {string} Persisted client ID
 */
export function getOrCreateClientId() {
  let clientId = localStorage.getItem("clientId");
  if (!clientId) {
    clientId = generateUUID();
    localStorage.setItem("clientId", clientId);
  }
  return clientId;
}
