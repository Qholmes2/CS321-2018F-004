import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FriendsManager {
	private HashMap<String, HashSet<String>> myAdded; // Everyone I have added
	private HashMap<String, HashSet<String>> addedMe; // Everyone thats added me
	private File saveFile;

	/**
	 * Default constructor for first server run
	 */
	public FriendsManager() {
		this.myAdded = new HashMap<>();
		this.addedMe = new HashMap<>();
	}

	public static FriendsManager Create(File f) throws Exception {
		FriendsManager res = null;
		if (f.exists()) {
			res = JsonMarshaller.MARSHALLER.unmarshalFile(f.getAbsolutePath(), FriendsManager.class);
			res.saveFile = f;
		} else {
			res = new FriendsManager();
			f.createNewFile();
			res.saveFile = f;
			res.writeFileSink();
		}
		res.saveFile = f;
		return res;
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
		writeFile();
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
		writeFile();
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
		writeFile();
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
		writeFile();
		return true;
	}

	private boolean remove(HashMap<String, HashSet<String>> removeFrom, String a, String b) {
		boolean res = removeFrom.get(a).remove(b);
		if (res)
			writeFile();
		return res;
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

	private void writeFile() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				FriendsManager.this.writeFileSink();
			}
		}).start();
	}

	private synchronized void writeFileSink() {
		try {
			String dat = JsonMarshaller.MARSHALLER.marshalIndent(this);
			FileOutputStream out = new FileOutputStream(this.saveFile);
			out.write(dat.getBytes());
			out.close();
		} catch (Exception e) {
			Logger.getLogger(FriendsManager.class.getName()).log(Level.SEVERE, "Error saving friend lists", e);
		}
	}

}
