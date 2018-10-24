
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Kevin
 */
public class PlayerList implements Iterable<Player> {
    private final LinkedList<Player> playerList;
    private final HashMap<String, Calendar> playerCheckIn;
    
    @Override
    public Iterator<Player> iterator() {
        Iterator<Player> iter = this.playerList.iterator();
        return iter;
    }    
    
    public PlayerList() {
        this.playerList = new LinkedList<>();
        playerCheckIn = new HashMap<>();
    }
    
    public void addPlayer(Player player) {
        this.playerList.add(player);
        playerCheckIn.put(player.getName(), Calendar.getInstance());
    }
    
    public Player findPlayer(String name) {
        for(Player player : this.playerList) {
            if(player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }
    
    public void removePlayer(String name) {
        Player player = findPlayer(name);
        playerCheckIn.remove(name);
        if(player != null) {
            this.playerList.remove(player);
        }
    }

    public void heartbeat(String name) {
        playerCheckIn.put(name, Calendar.getInstance());
    }
    
    public List<String> getExpiredPlayers() {
        List<String> expliredPlayers = new ArrayList<>();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.MINUTE, -1);
        
        playerCheckIn.forEach( (k, v) -> {
            if(v.before(yesterday))
                expliredPlayers.add(k);
        });
        
        return expliredPlayers;
    }
}
