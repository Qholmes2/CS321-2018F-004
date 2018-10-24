import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 *
 * @author Kevin
 */
public interface GameCoreInterface {
    
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
	 * @param recovery List of recovery questions and answers, ordered q1,a1,q2,a2,q3,a3
	 * @return an enumeration representing the creation status.
	 */
	public Responses createAccountAndJoinGame(String name, String password, ArrayList<String> recovery);

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
     * Delete a player's account.
     *
	 * @param name Name of the player to be deleted
	 * @return Player that was just deleted.
	 */
	public Player deleteAccount(String name);

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
	public String getAnswer(String name, int num);
	
	/**
	 * Resets passwords.
	 * 
	 * @param name Name of player getting password reset
	 * @param password New password to be saved
	 */
	public Responses resetPassword(String name, String pass);

}
