package suduoku;

import jakarta.websocket.Session;

import java.util.Random;

public class Player {
    private static final String[] adjectives = {
        "Brave", "Clever", "Happy", "Kind", "Quick", "Witty", "Bright", "Calm", "Bold", "Sharp",
        "Gentle", "Loyal", "Strong", "Wise", "Fierce", "Noble", "Friendly", "Quiet", "Swift", "Charming",
        "Graceful", "Fearless", "Mighty", "Playful", "Cheerful", "Daring", "Elegant", "Generous", "Humble", "Jolly",
        "Lively", "Patient", "Proud", "Sincere", "Thoughtful", "Vibrant", "Zesty", "Adventurous", "Ambitious", "Courageous",
        "Diligent", "Energetic", "Faithful", "Gentle", "Harmonious", "Inventive", "Joyful", "Radiant", "Resilient", "Spirited"
    };

    private static final String[] nouns = {
        "Tiger", "Eagle", "Fox", "Bear", "Wolf", "Lion", "Hawk", "Shark", "Panda", "Falcon",
        "Otter", "Dolphin", "Cheetah", "Leopard", "Jaguar", "Panther", "Rabbit", "Deer", "Koala", "Penguin",
        "Turtle", "Crocodile", "Alligator", "Peacock", "Swan", "Raven", "Owl", "Parrot", "Lynx", "Seal",
        "Whale", "Octopus", "Crane", "Stork", "Hedgehog", "Badger", "Moose", "Buffalo", "Antelope", "Gazelle",
        "Kangaroo", "Wallaby", "Platypus", "Armadillo", "Sloth", "Chameleon", "Iguana", "Gecko", "Flamingo", "Toucan"
    };

    private Session session;
    private String name;
    private String color;
    private int currentPuzzleId;
    private int selectedRow;
    private int selectedCol;

    // Constructor
    public Player(Session session) {
        this.session = session;
        this.name = generateName();
        this.color = generateColor();
        this.currentPuzzleId = -1; // No puzzle assigned initially
        this.selectedRow = -1;       // No square selected initially
        this.selectedCol = -1;       // No square selected initially
    }

    private String generateName() {
        Random random = new Random(System.currentTimeMillis());
        int randomIndex = random.nextInt(adjectives.length);
        String adjective = adjectives[randomIndex];
        String noun = nouns[randomIndex];
        return adjective + noun + random.nextInt(100);
    }

    private String generateColor() {
        Random random = new Random(System.currentTimeMillis());
        String color = "#";
        for (int i = 1; i < 7; i++) {
            int randomHex = random.nextInt(16);
            color = color + Integer.toHexString(randomHex);
        }
        return color;
    }

    public Session getSession() {
        return session;
    }
    
    public void setSession(Session session) {
        this.session = session;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public Integer getCurrentPuzzleId() {
        return currentPuzzleId;
    }

    public void setCurrentPuzzleId(Integer currentPuzzleId) {
        this.currentPuzzleId = currentPuzzleId;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public void setSelectedRow(int selectedRow) {
        this.selectedRow = selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    public void setSelectedCol(int selectedCol) {
        this.selectedCol = selectedCol;
    }

    // Utility Methods
    public void clearSelection() {
        this.selectedRow = -1;
        this.selectedCol = -1;
    }

    @Override
    public String toString() {
        return "Player{" +
                "Session=" + session.getId() +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", currentPuzzleId=" + currentPuzzleId +
                ", selectedRow=" + selectedRow +
                ", selectedCol=" + selectedCol +
                '}';
    }
}