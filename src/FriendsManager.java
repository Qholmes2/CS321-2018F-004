import java.util.HashMap;
import java.util.HashSet;

public class FriendsManager {
	private HashMap<String, HashSet<String>> myAdded; // Everyone I have added
	private HashMap<String, HashSet<String>> addedMe; // Everyone thats added me

	/**
	 * Default constructor for first server run
	 */
	public FriendsManager() {
		this.myAdded = new HashMap<>();
		this.addedMe = new HashMap<>();
	}

	/**
	 * Constructor for unmarshalling
	 * 
	 * @param myAdded
	 * @param addedMe
	 */
	public FriendsManager(HashMap<String, HashSet<String>> myAdded, HashMap<String, HashSet<String>> addedMe) {
		this.myAdded = myAdded;
		this.addedMe = addedMe;
	}

	/**
	 * Add a friend to your friends list
	 * 
	 * @param player
	 * @param friend
	 * @return added - if false you already have this friend
	 */
	public Responses addFriend(String player, String friend) {
		player = player.toLowerCase();
		friend = friend.toLowerCase();
		if (friend.equals(player))
			return Responses.SILLY;
		if (!add(myAdded, player, friend))
			return Responses.EXISTS;
		add(addedMe, friend, player);
		return Responses.SUCCESS;
	}

	/**
	 * Removes a player from your friends list
	 * 
	 * @param player
	 * @param friend
	 * @return removed - if false this person wasnt on your friends list
	 */
	public Responses removeFriend(String player, String friend) {
		friend = friend.toLowerCase();
		player = player.toLowerCase();
		if (player.equals(friend))
			return Responses.SILLY;
		if (!remove(myAdded, player, friend))
			return Responses.NOT_FOUND;
		remove(addedMe, friend, player);
		return Responses.SUCCESS;
	}

	/**
	 * Removes a player from the friends manager
	 * 
	 * @param player
	 */
	public void purge(String player) {
		player = player.toLowerCase();
		HashSet<String> friends;
		if ((friends = myAdded.get(player)) != null)
			friends.clear();
		myAdded.remove(player);

		HashSet<String> temp = addedMe.get(player);
		if (temp == null)
			return;
		for (String removeFrom : temp)
			remove(myAdded, removeFrom, player);
		temp.clear();
		addedMe.remove(player);
	}

	private boolean add(HashMap<String, HashSet<String>> addTo, String a, String b) {
		HashSet<String> temp = null;
		if (!addTo.containsKey(a)) {
			temp = new HashSet<>();
			addTo.put(a, temp);
		} else
			temp = addTo.get(a);
		if (temp.contains(b))
			return false;
		temp.add(b);
		return true;
	}

	private boolean remove(HashMap<String, HashSet<String>> removeFrom, String a, String b) {
		return removeFrom.get(a).remove(b);
	}

	/**
	 * For marshalling
	 * 
	 * @return
	 */
	public HashMap<String, HashSet<String>> getMyAdded() {
		return myAdded;
	}

	/**
	 * For marshalling
	 * 
	 * @return
	 */
	public HashMap<String, HashSet<String>> getAddedMe() {
		return addedMe;
	}
}
