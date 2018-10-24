
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
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
    private LinkedList<String> currentInventory;
    private String name;
    private int currentRoom;
    private Direction currentDirection;
    private PrintWriter replyWriter = null;
    private DataOutputStream outputWriter = null;
    @JsonProperty("recovery")
    private ArrayList<String> recovery;
    
    
	public Player(@JsonProperty("name") String name, @JsonProperty("recovery") ArrayList<String> recovery) {
        this.currentRoom = 1;
        this.currentDirection = Direction.NORTH;
        this.name = name;
        this.recovery = recovery;
        this.currentInventory = new LinkedList<>();
    }

	@JsonProperty("direction")
	public void setDirection(Direction d) {
		this.currentDirection = d;
	}
    
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
    
    public String getName() {
        return name;
    }
    
    @JsonProperty("recovery")
    public void setRecovery(ArrayList<String> recovery) {
    	this.recovery = recovery;
    }

	@JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }
	
	public String getQuestion(int num) {
		if(this.recovery.size() >= num * 2)
			return this.recovery.get(num * 2);
		return null;
	}
	
	public String getAnswer(int num) {
		if(this.recovery.size() >= (num * 2) + 1)
			return this.recovery.get((num * 2) + 1);
		return null;
	}

    public LinkedList<String> getCurrentInventory() {
        return currentInventory;
    }

	@JsonProperty("currentInventory")
    public void setCurrentInventory(LinkedList<String> currentInventory) {
        this.currentInventory = currentInventory;
    }
    
    public void addObjectToInventory(String object) {
        this.currentInventory.add(object);
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
    
    
    
    public String viewInventory() {
        String result = "";
        if(this.currentInventory.isEmpty() == true) {
            return "nothing.";
        }
        else {
            for(String obj : this.currentInventory) {
                result += " " + obj;
            }
            result += ".";
        }
        return result;
    }

    @Override
    public String toString() {
        return "Player " + this.name + ": " + currentDirection.toString();
    }
}
