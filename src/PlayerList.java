
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Kevin
 */
public class PlayerList implements Iterable<Player> {
    private final LinkedList<Player> playerList;
    
    @Override
    public Iterator<Player> iterator() {
        Iterator<Player> iter = this.playerList.iterator();
        return iter;
    }    
    
    public PlayerList() {
        this.playerList = new LinkedList<>();
    }
    
    public void addPlayer(Player player) {
        this.playerList.add(player);
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
        if(player != null) {
            this.playerList.remove(player);
        }
    }
}
