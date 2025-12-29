package suduoku;

public class Constants {
    public static final String DB_URL = "jdbc:sqlite:sudokugames.db";
    public static final String SCRAPER_PATH = "/app/sudoku_scraper/sudoku_scraper.py";

    public static final String[] playerNameAdjectives = {
            "Brave", "Clever", "Happy", "Kind", "Quick", "Witty", "Bright", "Calm", "Bold", "Sharp",
            "Gentle", "Loyal", "Strong", "Wise", "Fierce", "Noble", "Friendly", "Quiet", "Swift", "Charming",
            "Graceful", "Fearless", "Mighty", "Playful", "Cheerful", "Daring", "Elegant", "Generous", "Humble", "Jolly",
            "Lively", "Patient", "Proud", "Sincere", "Thoughtful", "Vibrant", "Zesty", "Adventurous", "Ambitious", "Courageous",
            "Diligent", "Energetic", "Faithful", "Gentle", "Harmonious", "Inventive", "Joyful", "Radiant", "Resilient", "Spirited"
    };

    public static final String[] playerNameNouns = {
            "Tiger", "Eagle", "Fox", "Bear", "Wolf", "Lion", "Hawk", "Shark", "Panda", "Falcon",
            "Otter", "Dolphin", "Cheetah", "Leopard", "Jaguar", "Panther", "Rabbit", "Deer", "Koala", "Penguin",
            "Turtle", "Crocodile", "Alligator", "Peacock", "Swan", "Raven", "Owl", "Parrot", "Lynx", "Seal",
            "Whale", "Octopus", "Crane", "Stork", "Hedgehog", "Badger", "Moose", "Buffalo", "Antelope", "Gazelle",
            "Kangaroo", "Wallaby", "Platypus", "Armadillo", "Sloth", "Chameleon", "Iguana", "Gecko", "Flamingo", "Toucan"
    };
}
