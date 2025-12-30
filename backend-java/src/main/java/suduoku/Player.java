package suduoku;

import jakarta.websocket.Session;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

import static suduoku.Constants.playerNameNouns;
import static suduoku.Constants.playerNameAdjectives;

@Getter
@Setter
public class Player {
    private Session session;
    private final String name;
    private final String color;
    private int currentPuzzleId;
    private int selectedRow;
    private int selectedCol;

    public Player(Session session) {
        this.session = session;
        this.name = generateName();
        this.color = generateColor();
        this.currentPuzzleId = -1;
        this.selectedRow = -1;
        this.selectedCol = -1;
    }

    private String generateName() {
        Random random = new Random(System.currentTimeMillis());
        int randomIndex = random.nextInt(playerNameAdjectives.length);
        String adjective = playerNameAdjectives[randomIndex];
        String noun = playerNameNouns[randomIndex];
        return adjective + noun + random.nextInt(100);
    }

    private String generateColor() {
        Random random = new Random(System.currentTimeMillis());
        StringBuilder color = new StringBuilder("#");
        for (int i = 1; i < 7; i++) {
            int randomHex = random.nextInt(16);
            color.append(Integer.toHexString(randomHex));
        }
        return color.toString();
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