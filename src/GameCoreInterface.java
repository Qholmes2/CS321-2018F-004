import java.util.ArrayList;

/**
 *
 * @author Kevin
 */
public interface GameCoreInterface {
	
	/**
	 * Makes the ghoul walk to an adjacent room
	 * @param g Ghoul that is being moved
	 * @param room Room the ghoul is currently in
	 */
	public void ghoulWander(Ghoul g,Room room);
	
	/**
	 * Pokes the ghoul in the current room
	 * @param playerName Player name
	 * @return String message of ghoul
	 */
	public String pokeGhoul(String playerName);
	

	/**
	 * Bribe the ghoul in the current room
	 * @param playerName Player name
	 * @param item item's name, which will be throw. 
	 * @return String message of ghoul
	 */
	public String bribeGhoul(String playerName,String item);

	
	//public String giveToGhoul(String object, String playerName);

    
    /**
     * Broadcasts a message to all other players in the same room as player.
     * @param player Player initiating the action.
     * @param message Message to broadcast.
     */   
    public void broadcast(Player player, String message);    
    
    /**
     * Broadcasts a message to all players in the specified room.
     * @param room Room to broadcast the message to.
     * @param message Message to broadcast.
     */   
    public void broadcast(Room room, String message);
    
    
    /**
     * Returns the player with the given name or null if no such player.
     * @param name Name of the player to find.
     * @return Player found or null if none.
     */
    public Player findPlayer(String name);
    
    /**
     * Allows a player to join the game.  If a player with the same name (case-insensitive)
     *  is already in the game, then this returns false.  Otherwise, adds a new player of 
     *  that name to the game.  The next step is non-coordinated, waiting for the player
     *  to open a socket for message events not initiated by the player (ie. other player actions)
     * @param name
     * @return Player that is added, null if player name is already registered to someone else
     */
    public Player joinGame(String name, String password);
    
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
	public Responses createAccountAndJoinGame(String name, String password);

    /**
     * Returns a look at the area of the specified player.
     * @param playerName Player Name
     * @return String representation of the current area the player is in.
     */
    public String look(String playerName);
    
    /**
     * Turns the player left.
     * @param name Player Name
     * @return String message of the player turning left.
     */
    public String left(String name);
    
    /**
     * Turns the player right.
     * @param name Player Name
     * @return String message of the player turning right.
     */
    public String right(String name);    
    
    /**
     * Says "message" to everyone in the current area.
     * @param name Name of the player to speak
     * @param message Message to speak
     * @return Message showing success.
     */
    public String say(String name, String message);

    /**
     * Generates list of all online players.
     * @return String of linked list PlayerList
     */
    public String showPlayers();

    /**
     * Whispers "message" to a specific player.
     * @param srcName Name of the player to speak
     * @param dstName Name of the player to receive
     * @param message Message to speak
     * @return Message showing success
     */
    public String whisper(String srcName, String dstName, String message);

    /**
     * Player ignores further messages from another Player
     * @param srcName Player making the ignore request
     * @param dstName Player to be ignored
     * @return Message showing success
     */
    public String ignorePlayer(String srcName, String dstName);

    /**
     * Player unIgnores further messages from another Player
     * @param srcName Player making the unIgnore request
     * @param dstName Player to be unIgnored
     * @return Message showing success
     */
    public String unIgnorePlayer(String srcName, String dstName);

    /**
     * Player displays the list of players that are being ignored
     * @param name Player who's list is being targeted
     * @return The list of players being ignored
     */
    public String getIgnoredPlayersList(String name);

    // Feature 404. Quick Reply
    /**
     * Reply "message" to last whisper.
     * @param srcName Name of the player to speak
     * @param message Message to speak
     * @return Message showing success
     */
    public String quickReply(String srcName, String message);
    // End 404.

    // Feature 410: Joke
    /**
     * Tells a joke to the room. Reads local "chat config" file
     * that keeps a list of jokes, one per line. The command
     * chooses a random joke.
     * @param filename the "chat config" file to read the joke from.
     * */
    public String joke(String filename);

    /**
     * Returns a string representation of all objects you are carrying.
     * @param name Name of the player to move
     * @return Message showing success.
     */    
    public String inventory(String name);
    
    /**
     * Leaves the game.
     * @param name Name of the player to leave
     * @return Player that was just removed.
     */
    public Player leave(String name);
   
    
    /**
     * Takes the player into venmo. The new and improved way to exchange money with other players.
     * 
     * @author Team 4: Alaqeel
     * @param name Name of the player enter the bank
     */    
    public String venmo(String name, ArrayList<String> tokens);
    
    /**
     * Returns a reference to a shop 
     * @param id
     * @return the shop or null
     */
    public String getShopStr(int id);
    
    /**
     * Allows player to sell an item to a shop, and increases their money
     * @author Team 4: King
     * @param name Name of the player
     * @param shopId The ID of the shop the player is selling an item to
     * @param item The item the player is selling (eventually will be an Item obj)
     */
    public double sellItem(String name, int shopId, String item);
    
    /**
     * 605B_buy_method
     * Allows player to sell an item to a shop, and increases their money
     * @author Team 4: Mistry
     * @param name Name of the player
     * @param shopId The ID of the shop the player is selling an item to
     * @param item The item the player is buying (eventually will be an Item obj)
     */
    public String buyItem(String name, int shopId, String item);
  
    /**
     * Returns a Shop's inventory as a formatted string
     * @param id The shop ID
     * @return A formatted string representing the Shop's inventory
     */
    public String getShopInv(int id);

    /**
     * Returns a string representation of the offer statement.
     * @param srcName Name of player making offer
     * @param dstName Name of player receiving offer
     * @param message Message item offered
     * @return Message showing offer
     * @throws RemoteException.
     */
    public String offer(String srcName, String dstName, String message); 

    /**
     * Prints message to player if request can processed, contacts other player about their request
     * @param requestingTrader Name of the player who has requested the trade
     * @param traderToRequest Name of the player whom the first player has requested to trade with
     */ 
    public void requestPlayer(String requestingTrader, String traderToRequest);

    /**
     * Return string representation of trade acceptance
     * @param acceptingTrader Name of the player who is accepting the trade
     * @param traderToAccept Name of the player who has requested a trade
     * @return Message of success or fail
     */ 
    public String playerResponse(String acceptingTrader, String traderToAccept);

    /**
     *Shouts "message" to everyone that is online
     *@param name Name of the player speaking
     *@param message Meesage to be spoken
     *@return Message showing success.
     */
    public String shout(String name, String message);

    /**
     * Delete a player's account.
     *
     * @param name Name of the player to be deleted
     * @return Player that was just deleted.
     */
    public Player deleteAccount(String name);
    
    /**
     * Player check in to ensure the client has not crashed. A client needs to 
     * call this method at least every hour or else it will be logged off.
     * @param name Name of client's player that is checking in.
     */
    public void heartbeatCheck(String name);
	
	/**
	 * Gets recovery question
	 * @param name User of recovery question 
	 * @param num Marks which question will be grabbed
	 * @return String of recovery question, null if user doesn't exist
	 */
	public String getQuestion(String name, int num);
	
	/**
	 * Gets recovery answer
	 * @param name User of recovery answer
	 * @param num Marks which answer will be grabbed
	 * @return String of recovery question, null if user doesn't exist
	 */
	public Boolean getAnswer(String name, int num, String answer);
	
	public Responses verifyPassword(String name, String password);

	/**
	 * Resets passwords.
	 * 
	 * @param name Name of player getting password reset
	 * @param password New password to be saved
	 */
	public Responses resetPassword(String name, String pass);

     /**
      * Challenge someone to R-P-S
      * @param challenger is the name of the player challenging to R-P-S
      * @param challenge is the name of the player being challenge
      * @return String message of the challenge
      */
    public String challenge(String challenger, String challengee);

    /**
      * Accept someones challenge to R-P-S
      * @param challenger is the name of the player challenging to R-P-S
      * @param challenge is the name of the player accepting
      * @return String message of the acceptence
      */
    public String accept(String challenger, String challengee);

    /**
     *
     * Reject someones challenge to a R-P-s
     * @param challenger is the name of the player challenging to R-P-S
     * @param challengee is the name of the player accepting 
     * @return String message of the rejection
     */
    public String reject(String challenger, String challengee);

    /**
      * Pick R-P-S
      * @param name is the name of player
      * @options is what the player pick R-P-S
      * @return String message of what they pick and who won
      */
    public String pickRPS(String name, String options);

    /**
      *
      * Teaches player how to play R-P-s
      * @param player is the name of the player to teach R-P-S
      * @return String message of the rejection
      */
     public String teach(String player);

	/**
	 * Adds a player to the friend list if the player exists and isn't on the friend
	 * list already
	 * 
	 * @param name
	 * @param friend
	 * @return responseType
	 */
	public Responses addFriend(String name, String friend);

	/**
	 * Removes a player from the friend list
	 * 
	 * @param name
	 * @param ex
	 * @return reponseType
	 */
	public Responses removeFriend(String name, String ex);
	
	/**
	 * returns a message showing all online friends
	 * 
	 * @param Player name
	 * @return Message showing online friends
	 */
	public String viewOnlineFriends(String name);
	
	//TODO
	public void addQuestion(String name, String question, String answer);
	
	public void removeQuestion(String name, int num);
}
