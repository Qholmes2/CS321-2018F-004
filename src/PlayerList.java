
import java.util.Iterator;
import java.util.HashMap;

/**
 *
 * @author Kevin
 */
public class PlayerList implements Iterable<Player> {
	private final HashMap<String, Player> playerList;

	@Override
	public Iterator<Player> iterator() {
		Iterator<Player> iter = this.playerList.values().iterator();
		return iter;
	}

	public PlayerList() {
		this.playerList = new HashMap<>();
	}

	public void addPlayer(Player player) {
		this.playerList.put(player.getName(), player);
	}

	public Player findPlayer(String name) {
		return playerList.get(name);
	}

	public void removePlayer(String name) {
		this.playerList.remove(name);
	}

	public int size() {
		return this.playerList.size();
	}
}
