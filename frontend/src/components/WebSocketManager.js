class WebSocketManager {
    constructor() {
        this.ws = null;
        this.listeners = [];
        this.errorListeners = [];
        this.messageQueue = []; // Add a queue for messages
        this.isConnected = false; // Track connection state
    }

    connect(url) {
        if (!this.ws || this.ws.readyState === WebSocket.CLOSED) {
            console.log("Attempting to connect to WebSocket server...");
            this.ws = new WebSocket(url);

            this.ws.onopen = () => {
                console.log("Connected to WebSocket server");
                this.isConnected = true;

                // Process any queued messages
                while (this.messageQueue.length > 0) {
                    const message = this.messageQueue.shift();
                    this.sendImmediately(message);
                }
            };

            this.ws.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);
                    this.listeners.forEach((listener) => listener(data));
                } catch (error) {
                    console.error("Error parsing message:", error);
                }
            };

            this.ws.onerror = (error) => {
                console.error("WebSocket error:", error);
                this.isConnected = false;
                this.errorListeners.forEach((listener) => listener(error));
            };

            this.ws.onclose = () => {
                console.log("WebSocket connection closed");
                this.isConnected = false;
                // Attempt to reconnect
                setTimeout(() => this.connect(url), 3000);
            };
        }
    }

    sendImmediately(message) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            const messageString = JSON.stringify(message);
            this.ws.send(messageString);
            return true;
        }
        return false;
    }

    send(message) {
        if (this.isConnected && this.sendImmediately(message)) {
            return true;
        } else {
            this.messageQueue.push(message);
            return false;
        }
    }

    addListener(listener) {
        this.listeners.push(listener);
    }

    removeListener(listener) {
        this.listeners = this.listeners.filter((l) => l !== listener);
    }
}

const webSocketManager = new WebSocketManager();
export default webSocketManager;
