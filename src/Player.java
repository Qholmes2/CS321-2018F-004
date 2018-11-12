
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Kevin
 */
@JsonIgnoreProperties({ "replyWriter", "outputWriter" })
public class Player {
    public LinkedList<Item> currentInventory;
    private String name;
    private int currentRoom;
    private Direction currentDirection;
    private PrintWriter replyWriter = null;
    private DataOutputStream outputWriter = null;
    private double money;
    private DataInputStream inputWriter = null;
    private boolean inTrade = false;
    private boolean tradeRequested = false;
    private String tradePartner = "";
    private String lastPlayer = "";
    private boolean hasChallenge = false;
    private boolean inBattle = false;
    private String challenger = " ";
    private String option = "";
    private String challengerOption = "";
    private boolean hasOption = false;
    @JsonProperty("recovery")
    private ArrayList<String> recovery; //stored question, answer, question,...

    
    
	public Player(@JsonProperty("name") String name) {
        this.currentRoom = 1;
        this.currentDirection = Direction.NORTH;
        this.name = name;
        this.currentInventory = new LinkedList<>();
        this.money = 0;
        this.recovery = new ArrayList<String>();
    }

    private HashSet<Player> ignoredPlayers = new HashSet<Player>();
    // missed Messages - not yet in uses
    private HashSet<Message> missedMessages = new HashSet<Message>();

    //Feature 409 WordFilter

    /**
     * Prints a chat statemnet from another player.
     * @param playerName - name of player making the statement
     * @param action - whether the player is saying or whispering the statement
     * @param message - the statement the other player is making
     */
    public void say(String playerName, String action, String message) {
        String filteredMessage = filterMessage(message);
        String statement = playerName + " " + action + " " + "\"" + filteredMessage + "\"" + ".";
        getReplyWriter().println(statement);
    }

    //Collection of words to be filtered from game chat
    private HashSet<String> filteredWords = new HashSet<String>();

    /**
     * Sets the words filtered from this player's chat.
     * @param newFilteredWords - collection of words to be filtered from this player's chat
     */
    public void setFilteredWords(HashSet<String> newFilteredWords) {
        for(String word : newFilteredWords) {
            filteredWords.add(word.toLowerCase());
        }
    }

    public HashSet<String> getFilteredWords() {
        return filteredWords;
    }

    /**
     * Adds a new word to the list of words filtered from this player's chat.
     * @param wordToAdd - word to be added to the filter list.
     * @return - whether the word was successfully added.
     */
    public boolean addFilteredWord(String wordToAdd) {
        boolean ret = filteredWords.add(wordToAdd);
        return ret;
    }

    /**
     * Checks whether this player is actively filtering a given word from chat.
     * @param word - word to check
     * @return - whether the givin word is being filtered.
     */

    public boolean isFiltering(String word) {
        return filteredWords.contains(word);
    }

    /**
     * Parses a message, replacing filtered words with "[BLEEEEP]"
     * @param message - message being filtered.
     * @return - new filtered message.
     */

    public String filterMessage(String message) {
        String newMessage = "";
        String bleep = "[BLEEEEP]";

        for(String word : message.split("\\s+")) {
            boolean match = false;

            if(filteredWords.contains(word.toLowerCase())) {
                newMessage += bleep + " ";
            } else {
                newMessage += word + " ";
            }
        }

        newMessage = newMessage.substring(0, (newMessage.length()-1));

        return newMessage;
    }

    public void printMessage(Player speaker, String message, String action) {
        String newMessage = filterMessage(message);
        this.getReplyWriter().println(speaker.getName() + " " + action + " \"" + newMessage + "\"");
    }

    /**
     * Adds a player's reference to set ignoredPlayers.
     * @param playerToIgnore
     * @return - whether player reference was successfully added to set ignorePlayer.
     */
    public boolean ignorePlayer(Player playerToIgnore) {
        // if(!ignoredPlayers.contains(playerToIgnore)){
        //     System.out.println(playerToIgnore.name + " has been ignored.");
        return ignoredPlayers.add(playerToIgnore);
        // } else {
        //     System.out.println(playerToIgnore.name + " is already being ignored.");
        //     return false;
        // }
    }

    //Feature 408. Unignore Player.
    /**
     *
     * Removes a given player form the set ignoredPlayers
     * @param playerToUnIgnore - player to remove from set
     * @return - whether the player reference was successfully removed
     *
     */
    public boolean unIgnorePlayer(Player playerToUnIgnore) {
        if(ignoredPlayers.contains(playerToUnIgnore)){
            return ignoredPlayers.remove(playerToUnIgnore);
        }else {
            return false;
        }
    }

    /**
     * Checks a given other player to see if they're on the THIS player's ignore list.
     * @param otherPlayer - other player this player may or may not be ignoring.
     * @return - whether the other player is being ignored by this player.
     */
    public boolean isIgnoring(Player otherPlayer) {
        return ignoredPlayers.contains(otherPlayer);
    }

    /**
     * Access the list of players this player is ignoring.
     * @return - Returns a String of all player names this player is ignoring
     */
	@JsonIgnore
    public String getIgnoredPlayersList() {
        StringBuilder ignoredPlayersList = new StringBuilder();
        ignoredPlayersList.append("\nIgnored Players: ");
        if(ignoredPlayers.isEmpty()) { ignoredPlayersList.append(" Your ignore list is empty.\n"); }
        else {
            int count = 1;
            for(Player ignored : ignoredPlayers) {
                ignoredPlayersList.append(ignored.name);
                if(count == ignoredPlayers.size()) {
                    ignoredPlayersList.append(".\n");
                } else {
                    count++;
                    ignoredPlayersList.append(", ");
                }
            }
        }
        return ignoredPlayersList.toString();
    }

    /**
     *
     * @param sentMessage - the Message being sent to this player.
     * @return - whether or not the sent message was successfully added to the set of received messages.
     */

    public boolean receiveMessage(Message sentMessage) {
        boolean received = false;

        //put ignore detection here

        /*if(recievedMessages.add(sentMessage)) {

            sentMessage.SetReceived();
            received = true;
        }*/

        // if recipient is offline, detect here

        return received;
    }

    /**
     *
     * @param textOfMessage - The actual input message from the user.
     * @param intendedRecipient - A reference to the user the message is being sent to.
     * @return - Whether the recipient successfully received the sent message.
     */
    public boolean sendMessage(String textOfMessage, Player intendedRecipient) {

        Message newMessage = new Message(textOfMessage, this, intendedRecipient);
        return intendedRecipient.receiveMessage(newMessage);

    }

    /**Reeds changes end here**/

    public void turnLeft() {
        switch(this.currentDirection.toString()) {
            case "North":
                this.currentDirection = Direction.WEST;
                break;
            case "South":
                this.currentDirection = Direction.EAST;
                break;
            case "East":
                this.currentDirection = Direction.NORTH;
                break;
            case "West":
                this.currentDirection = Direction.SOUTH;
                break;                
        }
    }
    
    public void turnRight() {
        switch(this.currentDirection.toString()) {
            case "North":
                this.currentDirection = Direction.EAST;
                break;
            case "South":
                this.currentDirection = Direction.WEST;
                break;
            case "East":
                this.currentDirection = Direction.SOUTH;
                break;
            case "West":
                this.currentDirection = Direction.NORTH;
                break;                
        }
    }

    public String getLastPlayer() {
        return lastPlayer;
    }

    public void setLastPlayer(String lastPlayer) {
        this.lastPlayer = lastPlayer;
    }

    public void setInBattle(boolean battle){
	inBattle = battle;
    }

    public boolean getInBattle(){
	return inBattle;
    } 

    @JsonProperty("name")
    public String getName() {
        return name;
    }
    
    @JsonProperty("recovery")
    public void setRecovery(@JsonProperty("recovery") ArrayList<String> recovery) {
    	this.setMoney(5);
    	this.recovery = recovery;
    }

    @JsonProperty("recovery")
    public ArrayList<String> getRecovery() {
    	return this.recovery;
    }
    public void setName(String name) {
        this.name = name;
    }
	
	public String getQuestion(int num) {
		String q = null;
		try {
			q = this.recovery.get(num * 2);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return q;
	}
	
	public void addQuestion(String question, String answer) {
		this.recovery.add(question);
		this.recovery.add(answer);
		this.setRecovery(this.recovery);
	}
	
	public void removeQuestion(int num) {
		this.recovery.remove(num * 2);
		this.money = num * 2;
		this.recovery.remove(num * 2); //second one removes the answer
	}
	
	public String getAnswer(int num) {
		String q = null;
		try {
			q = this.recovery.get((num * 2) + 1);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return q;
	}

    public LinkedList<Item> getCurrentInventory() {
        return currentInventory;
    }

	@JsonProperty("currentInventory")
    public void setCurrentInventory(LinkedList<Item> currentInventory) {
        this.currentInventory = currentInventory;
    }
    
    public void addObjectToInventory(Item object) {
        this.currentInventory.add(object);
    }

    //removes the first instance
    public Item removeObjectFromInventory(String target) {
        for(Item o : this.currentInventory) {
                String nameToRemove = o.getName();
            if(nameToRemove.equalsIgnoreCase(target)) {
		Item temp = o;
                this.currentInventory.remove(o);
                return temp;
            }
        }
        return null;
    }

    public void sortCurrentInventory(String modes) {
	switch(modes) {
		case "ni":
			Collections.sort(currentInventory, new ItemNameComparator());
			break;
		case "nd":
			Collections.sort(currentInventory, new ItemNameComparator());
			Collections.reverse(currentInventory);
			break;
		case "wi":
			Collections.sort(currentInventory, new ItemWeightComparator());
			break;
		case "wd":
			Collections.sort(currentInventory, new ItemWeightComparator());
			Collections.reverse(currentInventory);
			break;
		case "pi":
			Collections.sort(currentInventory, new ItemPriceComparator());
			break;
		case "pd":
			Collections.sort(currentInventory, new ItemWeightComparator());
			Collections.reverse(currentInventory);
			break;
		default:
			System.out.println("Please enter in valid input or use the correct format (n/w/p) -> (i/d)");
	}
    }

    public boolean hasTradeRequest(){
        return tradeRequested;
    }

    public void setTradeRequest(boolean val){
        tradeRequested = val;
    }

    public boolean isInTrade(){
        return inTrade;
    }
    public void setInTrade(boolean val){
        inTrade = val;
    }

    public String getTradePartner(){
        return tradePartner;
    }
    public void setTradePartner(String s){
        tradePartner = s;
    }

    public void setReplyWriter(PrintWriter writer) {
        this.replyWriter = writer;
    }
    
    public PrintWriter getReplyWriter() {
        return this.replyWriter;
    }
    
    public void setOutputWriter(DataOutputStream writer) {
        this.outputWriter = writer;
    }
    
    public DataOutputStream getOutputWriter() {
        return this.outputWriter;
    }
    
    public int getCurrentRoom() {
        return this.currentRoom;
    }
    
	@JsonProperty("currentRoom")
    public void setCurrentRoom(int room) {
        this.currentRoom = room;
    }
    
	@JsonIgnore
    public String getCurrentDirection() {
        return this.currentDirection.name();
    }
    
    public Direction getDirection() {
        return this.currentDirection;
    }

    public String getOption(){
        return this.option;
    }

    public void setOption(String option){
        this.option = option;
    }

    public String getChallengerOption(){
        return this.challengerOption;
    }

    public void setChallengerOption(String challengerOption){
        this.challengerOption = challengerOption;
    }
    
    public double getMoney() {
        return this.money;
    }

    public void setMoney(double m){
        this.money = m;
    }

    public String getChallenger(){
        return challenger;
    }

    public void setChallenger(String name){
        challenger = name;
    }

    public boolean getHasChallenge(){
        return hasChallenge;
    }

    public void setHasChallenge(boolean challenged){
        hasChallenge = challenged;
    }
    
    /**
     * Allows the caller to add/take money in user's wallet.
     * 
     * @author Team 4: Alaqeel
     * @param m Amount to be added/taken.
     */
    public void changeMoney(double m){
        this.money += m;
    }
    
    public String viewInventory() {
        String result = "";
        if(this.currentInventory.isEmpty() == true) {
            return " nothing.";
        }
        else {
            for(Item obj : this.currentInventory) {
                result += " " + obj.name;
		result += (" (" + obj.weight + ") ");
            }
            result += ".";
        }
        return result;
    }

    @Override
    public String toString() {
        return "Player " + this.name + ": " + currentDirection.toString();
    }

    private static class ItemNameComparator implements Comparator<Item> {
	@Override
	public int compare(Item ItemA, Item ItemB) {
		return ItemA.getName().compareTo(ItemB.getName());
	}
    }

    private static class ItemWeightComparator implements Comparator<Item> {
	@Override
	public int compare(Item ItemA, Item ItemB) {
		if(ItemA.getWeight() > ItemB.getWeight()) {
			return 1;
		}
		else if(ItemA.getWeight() < ItemB.getWeight()) {
			return -1;
		}
		else {
			return 0;
		}
	}
    }

    private static class ItemPriceComparator implements Comparator<Item> {
	@Override
	public int compare(Item ItemA, Item ItemB) {
		if(ItemA.getPrice() > ItemB.getPrice()) {
			return 1;
		}
		else if(ItemA.getPrice() < ItemB.getPrice()) {
			return -1;
		}
		else {
			return 0;
		}
	}
    }
}
