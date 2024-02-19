package ucmo.senior_project.domain;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import ucmo.senior_project.domain.gametypes.DebugGame;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Data
public class SuperGame {
    public static Class[] loadableGames = new Class[]
    {
        DebugGame.class,
        DebugGame.class,
        DebugGame.class,
        DebugGame.class,
        DebugGame.class,
        DebugGame.class,
        DebugGame.class,
    };

    public static HashMap<String, SuperGame> activeGames = new HashMap<>();

    private List<TempUser> users = new ArrayList<>();

    private SuperGame(String code, GameUser user) {
        this.code = code;
        this.gameMaster = new TempUser(this, user, this.code);
        this.users.add(this.gameMaster);
        activeGames.put(code, this);
    }

    private TempUser gameMaster;
    private Game currentGame = null;
    private String code;

    public void updateInput(TempUser user, JsonNode userInputs) {
        if (currentGame != null) {
            currentGame.updateInput(user, userInputs);
        }
        JsonNode adminNode = userInputs.get("admin");
        if(adminNode != null && user == gameMaster) {
            JsonNode gameState = adminNode.get("state");
            if(gameState != null) {
                switch (gameState.asText()) {
                    case "start":
                        this.beginNewGame();
                        break;
                }
            }
        }
    }

    public void beginNewGame() {
        int rnd = new Random().nextInt(loadableGames.length);
        try {
            this.currentGame = (Game)loadableGames[rnd].getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    public TempUser newTempUser(String name) {
        TempUser user = new TempUser(this, name, this.code);
        this.users.add(user);
        return user;
    }

    public static SuperGame fetchGame(String code) {
        return activeGames.get(code);
    }
    public static SuperGame setupGame(GameUser user) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return new SuperGame(generatedString, user);
    }
}
