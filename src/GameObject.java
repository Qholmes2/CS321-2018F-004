


import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Kevin
 */
public class GameObject extends UnicastRemoteObject implements GameObjectInterface {
    private final GameCore core;
    
    /**
     * Creates a new GameObject.  Namely, creates the map for the rooms in the game,
     *  and establishes a new, empty, player list.
     * @throws RemoteException 
     */
	public GameObject(String playerAccountsLocation) throws Exception {
        super();
        
		core = new GameCore(playerAccountsLocation);
    }

    /**
     * Links an asynchronous event message connection to a player.
     * @param playerName Player to link the reply socket with.
     * @param writer PrintWriter to use for asynchronous messages.
     * @return true if player is found, false otherwise.
     */
    public boolean setReplyWriter(String playerName, PrintWriter writer) {
        Player player = core.findPlayer(playerName);
        if(player != null && writer != null) {
            player.setReplyWriter(writer);
            return true;
        }
        return false;
    }    

	/**
	 * Used to create a hash encrypted in SHA256 for use in encrypting passwords
	 * 
	 * @param toHash
	 * @return SHA256 encrypted hash value, or "ERROR" If encryption method fails.
	 */
	public String hash(String toHash) {
		try {
			byte[] encodedhash = MessageDigest.getInstance("SHA-256").digest(toHash.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : encodedhash)
				sb.append(String.format("%02X", b));
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
		}
		return "ERROR";
	}

    /**
     * Allows a player to join the game.  If a player with the same name (case-insensitive)
     *  is already in the game, then this returns false.  Otherwise, adds a new player of 
     *  that name to the game.  The next step is non-coordinated, waiting for the player
     *  to open a socket for message events not initiated by the player (ie. other player actions)
     * @param name
     * @return true is player is added, false if player name is already registered to someone else
     * @throws RemoteException 
     */
    @Override
	public boolean joinGame(String name, String password) throws RemoteException {
		// Request join to the core and return the results back to the remotely calling
		// method.
		password = hash(password);
		if (!password.equals("ERROR"))
			return (core.joinGame(name, password) != null);
		return false; // Password is invalid due to failure of hash function
	}

	/**
	 * Allows a player to create an account. If the player name already exists this
	 * returns the corresponding enum. If the players name is of an invalid format
	 * this returns that corresponding emum. Otherwise this returns success and
	 * calls joinGame.
	 * 
	 * @param name
	 * @param password
	 * @return an enumeration representing the creation status, or null if password
	 *         failed to be encrypted in hash function.
	 * @throws RemoteException
	 */
	@Override
	public Responses createAccountAndJoinGame(String name, String password) throws RemoteException {
		password = hash(password);
		if (password.equals("ERROR"))
			return Responses.UNKNOWN_FAILURE;
		return core.createAccountAndJoinGame(name, password);
	}

    /**
     * Returns a look at the area of the specified player.
     * @param playerName Player Name
     * @return String representation of the current area the player is in.
     * @throws RemoteException 
     */
    @Override
    public String look(String playerName) throws RemoteException {
        return core.look(playerName);
    }        
     
    /**
     * Turns the player left.
     * @param name Player Name
     * @return String message of the player turning left.
     * @throws RemoteException 
     */
    @Override
    public String left(String name) throws RemoteException {
        return core.left(name);
    }
       
    /**
     * Turns the player right.
     * @param name Player Name
     * @return String message of the player turning right.
     * @throws RemoteException 
     */
    @Override
    public String right(String name) throws RemoteException {
        return core.right(name);
    }    
       
    /**
     * Says "message" to everyone in the current area.
     * @param name Name of the player to speak
     * @param message Message to speak
     * @return Message showing success.
     * @throws RemoteException 
     */
    @Override
    public String say(String name, String message) throws RemoteException {
        return core.say(name, message);
    }
      
    /**
     * Attempts to walk forward < distance > times.  If unable to make it all the way,
     *  a message will be returned.  Will display LOOK on any partial success.
     * @param name Name of the player to move
     * @param distance Number of rooms to move forward through.
     * @return Message showing success.
     * @throws RemoteException 
     */
    @Override
    public String move(String name, int distance) throws RemoteException {
        return core.move(name, distance);
    }
      
    /**
     * Attempts to pick up an object < target >. Will return a message on any success or failure.
     * @param name Name of the player to move
     * @param target The case-insensitive name of the object to pickup.
     * @return Message showing success.
     * @throws RemoteException 
     */    
    @Override
    public String pickup(String name, String target) throws RemoteException {
        return core.pickup(name, target);
    }    
    
    /**
     * Returns a string representation of all objects you are carrying.
     * @param name Name of the player to move
     * @return Message showing success.
     * @throws RemoteException 
     */    
    @Override
    public String inventory(String name) throws RemoteException {
        return core.inventory(name);
    }    
    
     /**
     * Leaves the game.
     * @param name Name of the player to leave
     * @throws RemoteException 
     */    
    @Override
    public void leave(String name) throws RemoteException {
        Player player = core.leave(name);
        if(player != null) {
            player.getReplyWriter().close();
        }
    }    
	
	/**
	 * Delete a player's account.
	 * 
	 * @param name Name of the player to be deleted
	 * @throws RemoteException
	 */
	@Override
	public void deleteAccount(String name) throws RemoteException {
		Player player = core.deleteAccount(name);
		if (player != null) {
			player.getReplyWriter().close();
		}
	}

	/**
	 * Adds a player to your friends list
	 * 
	 * @param name
	 * @param friend
	 * @return responseType
	 * @throws RemoteException
	 */
	@Override
	public Responses addFriend(String name, String friend) throws RemoteException {
		return core.addFriend(name, friend);
	}

	/**
	 * Removes a player from your friends list
	 * 
	 * @param name
	 * @param ex
	 * @return responseType
	 * @throws RemoteException
	 */
	@Override
	public Responses removeFriend(String name, String ex) throws RemoteException {
		return core.removeFriend(name, ex);
	}
}
