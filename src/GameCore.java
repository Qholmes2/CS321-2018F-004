
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 *
 * @author Kevin
 */
public class GameCore implements GameCoreInterface {
    private final PlayerList playerList;
    private final Map map;

	// Acounts and Login
	private final PlayerAccountManager accountManager;
	private final Object loginLock = new Object();
	private final Object createAccountLock = new Object();
	private Logger playerLogger = Logger.getLogger("connections");
	private FriendsManager friendsManager;
	private final Object friendsLock = new Object();

	/**
	 * Creates a new GameCoreObject. Namely, creates the map for the rooms in the
	 * game, and establishes a new, empty, player list.
	 * 
	 * This is the main core that both the RMI and non-RMI based servers will
	 * interface with.
	 * 
	 * @throws Exception
	 * 
	 */
    public GameCore(String playerAccountsLocation) throws Exception {

        // Generate the game map.
        map = new Map();
        
        playerList = new PlayerList();
        
        initConnectionsLogger();
        
        accountManager = new PlayerAccountManager(playerAccountsLocation);
        
        friendsManager = loadFriendsManager();
        		
        Thread objectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Random rand = new Random();
                Room room;
                String object;
                String[] objects = {"Flower", "Textbook", "Phone", "Newspaper"};
                while(true) {
                    try {
                        Thread.sleep(rand.nextInt(60000));
                        object = objects[rand.nextInt(objects.length)];
                        room = map.randomRoom();
                        room.addObject(object);
                        
                        GameCore.this.broadcast(room, "You see a student rush past and drop a " + object + " on the ground.");

                    } catch (InterruptedException ex) {
                        Logger.getLogger(GameObject.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        objectThread.setDaemon(true);
        objectThread.start();
    }
    
    /**
     * Broadcasts a message to all other players in the same room as player.
     * @param player Player initiating the action.
     * @param message Message to broadcast.
     */   
    @Override
    public void broadcast(Player player, String message) {
        for(Player otherPlayer : this.playerList) {
            if(otherPlayer != player && otherPlayer.getCurrentRoom() == player.getCurrentRoom()) {
                otherPlayer.getReplyWriter().println(message);
            }
        }
    }
  
    /**
     * Broadcasts a message to all players in the specified room.
     * @param room Room to broadcast the message to.
     * @param message Message to broadcast.
     */   
    @Override
    public void broadcast(Room room, String message) {
        for(Player player : this.playerList) {
            if(player.getCurrentRoom() == room.getId()) {
                player.getReplyWriter().println(message);
            }
        }
    }
    
    /**
     * Returns the player with the given name or null if no such player.
     * @param name Name of the player to find.
     * @return Player found or null if none.
     */
    @Override
    public Player findPlayer(String name) {
        for(Player player : this.playerList) {
            if(player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }
    
    /**
     * Allows a player to join the game.  If a player with the same name (case-insensitive)
     *  is already in the game, then this returns false.  Otherwise, adds a new player of 
     *  that name to the game.  The next step is non-coordinated, waiting for the player
     *  to open a socket for message events not initiated by the player (ie. other player actions)
     * @param name
     * @param password password hash for corresponding account.
     * @return Player is player is added, null if player name is already registered to someone else
     */
    @Override
	public Player joinGame(String name, String password) {
		synchronized (loginLock) {
			// Check to see if the player of that name is already in game.
			Player player = this.playerList.findPlayer(name);
			if (player != null)
				return null;
			PlayerAccountManager.AccountResponse resp = accountManager.getAccount(name, password);
			if (!resp.success())
				return null;
			player = resp.player;
			this.playerList.addPlayer(player);

			this.broadcast(player, player.getName() + " has arrived.");
			connectionLog(true, player.getName());
			return player;
		}
	}

	/**
	 * Allows a player to create an account. If the player name already exists this
	 * returns the corresponding enum. If the players name is of an invalid format
	 * this returns that corresponding emum. Otherwise this returns success and
	 * calls joinGame.
	 * 
	 * @param name
	 * @param password
	 * @return an enumeration representing the creation status.
	 */
	@Override

	public synchronized Responses createAccountAndJoinGame(String name, String password) {
		synchronized (createAccountLock) {
			PlayerAccountManager.AccountResponse resp = accountManager.createNewAccount(name, password);
			if (!resp.success())
				return resp.error;
			if (joinGame(name, password) != null)
				return Responses.SUCCESS;
			return Responses.UNKNOWN_FAILURE;
		}
	}

	    /**
     * Returns a look at the area of the specified player.
     * @param playerName Player Name
     * @return String representation of the current area the player is in.
     */
    @Override
    public String look(String playerName) {
        Player player = playerList.findPlayer(playerName);

        if(player != null) {        
            // Find the room the player is in.
            Room room = this.map.findRoom(player.getCurrentRoom());

            // Send a message to all other players in the room that this player is looking around.
            this.broadcast(player, player.getName() + " takes a look around.");

            // Return a string representation of the room state.
            return room.toString(this.playerList, player);
        }
        // No such player exists
        else {
            return null;
        }
    }        
   
    /**
     * Turns the player left.
     * @param name Player Name
     * @return String message of the player turning left.
     */
    @Override
    public String left(String name) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            // Compel the player to turn left 90 degrees.
            player.turnLeft();
            
            // Send a message to every other player in the room that the player has turned left.
            this.broadcast(player, player.getName() + " turns to the left.");
            
            // Return a string back to the calling function with an update.
            return "You turn to the left to face " + player.getCurrentDirection();
        }
        else {
            return null;
        }
    }
    
    /**
     * Turns the player right.
     * @param name Player Name
     * @return String message of the player turning right.
     */
    @Override
    public String right(String name) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            // Compel the player to turn left 90 degrees.
            player.turnRight();
            
            // Send a message to every other player in the room that the player has turned right.
            this.broadcast(player, player.getName() + " turns to the right.");
            
            // Return a string back to the calling function with an update.
            return "You turn to the right to face " + player.getCurrentDirection();
        }
        else {
            return null;
        }
    }    
    
    /**
     * Says "message" to everyone in the current area.
     * @param name Name of the player to speak
     * @param message Message to speak
     * @return Message showing success.
     */
    @Override
    public String say(String name, String message) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            this.broadcast(player, player.getName() + " says, \"" + message + "\"");
            return "You say, \"" + message + "\"";
        }
        else {
            return null;
        }
    }  
    
    /**
     * Attempts to walk forward < distance > times.  If unable to make it all the way,
     *  a message will be returned.  Will display LOOK on any partial success.
     * @param name Name of the player to move
     * @param distance Number of rooms to move forward through.
     * @return Message showing success.
     */
    public String move(String name, int distance) {
        Player player = this.playerList.findPlayer(name);
        if(player == null || distance <= 0) {
            return null;
        }
        Room room;
        while(distance-- != 0) {
            room = map.findRoom(player.getCurrentRoom());
            if(room.canExit(player.getDirection())) {
                this.broadcast(player, player.getName() + " has walked off to the " + player.getCurrentDirection());
                player.getReplyWriter().println(room.exitMessage(player.getDirection()));
                player.setCurrentRoom(room.getLink(player.getDirection()));
                this.broadcast(player, player.getName() + " just walked into the area.");
                player.getReplyWriter().println(this.map.findRoom(player.getCurrentRoom()).toString(playerList, player));
            }
            else {
                player.getReplyWriter().println(room.exitMessage(player.getDirection()));
                return "You grumble a little and stop moving.";
            }
        }
        return "You stop moving and begin to stand around again.";
    }
    
    /**
     * Attempts to pick up an object < target >. Will return a message on any success or failure.
     * @param name Name of the player to move
     * @param target The case-insensitive name of the object to pickup.
     * @return Message showing success. 
     */    
    public String pickup(String name, String target) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            Room room = map.findRoom(player.getCurrentRoom());
            String object = room.removeObject(target);
            if(object != null) {
                player.addObjectToInventory(object);
                this.broadcast(player, player.getName() + " bends over to pick up a " + target + " that was on the ground.");
                return "You bend over and pick up a " + target + ".";
            }
            else {
                this.broadcast(player, player.getName() + " bends over to pick up something, but doesn't seem to find what they were looking for.");
                return "You look around for a " + target + ", but can't find one.";
            }
        }
        else {
            return null;
        }
    }       
    
    /**
     * Returns a string representation of all objects you are carrying.
     * @param name Name of the player to move
     * @return Message showing success.
     */    
    @Override
    public String inventory(String name) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            this.broadcast(player, "You see " + player.getName() + " looking through their pockets.");
            return "You look through your pockets and see" + player.viewInventory();
        }
        else {
            return null;
        }
    }    

     /**
     * Leaves the game.
     * @param name Name of the player to leave
     * @return Player that was just removed.
     */    
    @Override
    public Player leave(String name) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            this.broadcast(player, "You see " + player.getName() + " heading off to class.");
            this.playerList.removePlayer(name);
            connectionLog(false, player.getName());
            this.accountManager.forceUpdateData(player);
            return player;
        }
        return null;
    }       

	/**
	 * Logs player connections
	 * 
	 * @param connecting true if they're connecting, false if they are disconnecting
	 * @param name
	 */
	private void connectionLog(boolean connecting, String name) {
		playerLogger.info(String.format("(%s) logged %s", name, connecting ? "in" : "out"));
		for (Handler h : playerLogger.getHandlers())
			h.flush();
	}

	/**
	 * Creates the logger outside of the constructor. Uses RFC3339 timestamps
	 * 
	 * @throws IOException
	 */
	private void initConnectionsLogger() throws IOException {
		File f = new File("connections.log");
		if (!f.exists())
			f.createNewFile();
		FileOutputStream out = new FileOutputStream(f, true);
		StreamHandler handle = new StreamHandler(out, new SimpleFormatter() {
			private final SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

			@Override
			public String format(LogRecord log) {
				StringBuilder sb = new StringBuilder();
				sb.append("[").append(rfc3339.format(new Date(log.getMillis()))).append("] ");
				sb.append("[").append(log.getLoggerName()).append("] ");
				sb.append("[").append(log.getLevel()).append("] ");
				sb.append(log.getMessage()).append("\r\n");
				Throwable e = log.getThrown();
				if (e != null)
					for (StackTraceElement el : e.getStackTrace())
						sb.append(el.toString()).append("\r\n");
				return sb.toString();
			}
		});
		playerLogger.setUseParentHandlers(false);
		playerLogger.addHandler(handle);
		playerLogger.info("Player connections logger has started");
		handle.flush();
	}

	/**
	 * Delete a player's account.
	 * 
	 * @param name Name of the player to be deleted
	 * @return Player that was just deleted.
	 */
	@Override
	public Player deleteAccount(String name) {
		Player player = this.playerList.findPlayer(name);
		if (player != null) {
			this.broadcast(player, "You hear that " + player.getName() + " has dropped out of school.");
			this.playerList.removePlayer(name);
			this.accountManager.deleteAccount(player.getName());
			synchronized (friendsLock) {
				this.friendsManager.purge(name);
			}
			return player;
		}
		return null; // No such player was found.
	}

	/**
	 * Adds a player to the friend list if the player exists and isn't on the friend
	 * list already
	 * 
	 * @param name
	 * @param friend
	 * @return responseType
	 */
	@Override
	public Responses addFriend(String name, String friend) {
		synchronized (friendsLock) {
			if (!this.accountManager.accountExists(name))
				return Responses.INTERNAL_SERVER_ERROR;
			if (!this.accountManager.accountExists(friend))
				return Responses.NOT_FOUND;
			return this.friendsManager.addFriend(name, friend);
		}
	}

	/**
	 * Removes a player from the friend list
	 * 
	 * @param name
	 * @param ex
	 * @return reponseType
	 */
	@Override
	public Responses removeFriend(String name, String ex) {
		synchronized (friendsLock) {
			if (!this.accountManager.accountExists(name))
				return Responses.INTERNAL_SERVER_ERROR;
			return this.friendsManager.removeFriend(name, ex);
		}
	}

	private FriendsManager loadFriendsManager() {
		return new FriendsManager();
	}
	
	/**
	 * Returns a message showing all online friends
	 * 
	 * @param Player name
	 * @return Message showing online friends
	 */
	@Override
	public String viewOnlineFriends(String name) {

		String message = "Your friends that are currently online: \n"; // This is the first part of the message

		// get list of friends from FriendsManager
		HashSet<String> flist = this.friendsManager.getMyAdded().get(name.toLowerCase());
		if (flist == null) {
			message += "You don't have any.\n";
			return message;
		}

		// find online friends using flit and findPlayer from playerList
		for (String str : flist) {
			Player p;
			if ((p = this.playerList.findPlayer(str)) != null)
				message += "  " + p.getName() + "\n";
		}
		return message;
	}
}
