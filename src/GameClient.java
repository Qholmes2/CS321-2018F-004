

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kevin
 */
public class GameClient {
    // Control flag for running the game.
    private boolean runGame;

    // Remote object for RMI server access
    protected GameObjectInterface remoteGameInterface;
    
    // Members for running the remote receive connection (for non-managed events)
    private boolean runListener;
    protected ServerSocket remoteListener;
    private Thread remoteOutputThread;  
    
    // Members related to the player in the game.
    protected String playerName;
    
    /** 
     * Main class for running the game client.
     */
    public GameClient(String host) {
        this.runGame = true;
        boolean nameSat = false;
        
        System.out.println("Welcome to the client for an RMI based online game.\n");
        System.out.println("This game allows you to connect to a server an walk around a virtual,");
        System.out.println(" text-based version of the George Mason University campus.\n");
        System.out.println("You will be asked to create a character momentarily.");
        System.out.println("When you do, you will join the game at the George Mason Clock, in the main quad.");
        System.out.println("You will be able to see if any other players are in the same area as well as what");
        System.out.println("objects are on the ground and what direction you are facing.\n");
        System.out.println("The game allows you to use the following commands:");
        System.out.println("  LOOK          - Shows you the area around you");
        System.out.println("  SAY message   - Says 'message' to any other players in the same area.");
        System.out.println("  LEFT          - Turns your player left 90 degrees.");
        System.out.println("  RIGHT         - Turns your player right 90 degrees.");
        System.out.println("  MOVE distance - Tries to walk forward <distance> times.");
        System.out.println("  PICKUP obect  - Tries to pick up an object in the same area.");
        System.out.println("  INVENTORY     - Shows you what objects you have collected.");
 		System.out.println("  FRIENDS       - Displays friend subcommand list.");
        System.out.println("  QUIT          - Quits the game.");
		System.out.println("  DELETE        - Deletes your character permanently.");
        System.out.println();
        

        // Set up for keyboard input for local commands.
        InputStreamReader keyboardReader = new InputStreamReader(System.in);
        BufferedReader keyboardInput = new BufferedReader(keyboardReader);
        String keyboardStatement;

        try {
            // Establish RMI connection with the server
            System.setSecurityManager(new SecurityManager());
            String strName = "rmi://"+host+"/GameService";
            remoteGameInterface = (GameObjectInterface) Naming.lookup(strName);

            // Start by remotely executing the joinGame method.  
            //   Lets the player choose a name and checks it with the server.  If the name is
            //    already taken or the user doesn't like their input, they can choose again.
            while(nameSat == false) {
				try {
					System.out.println("Logging in or creating account?");
					String mode;
					do {
						System.out.print("(L/C)> ");
						mode = keyboardInput.readLine().toUpperCase().trim();
					} while (!(mode.equals("L") || mode.equals("C")));
					System.out.print("Username: ");
					this.playerName = keyboardInput.readLine().trim();
					System.out.print("Password: ");
					String pass = keyboardInput.readLine();
					switch (mode) {
					case "L":
						nameSat = remoteGameInterface.joinGame(this.playerName, pass);
						if (!nameSat)
							System.out.println("Username and password combination invalid\n");
						break;
					case "C":
						Responses resp = remoteGameInterface.createAccountAndJoinGame(playerName, pass);
						switch (resp) {
						case BAD_USERNAME_FORMAT:
							System.out
									.println("This is a bad user name. Please use only spaces, numbers, and letters.");
							break;
						case USERNAME_TAKEN:
							System.out.println("Sorry but this username was already taken.");
							break;
						case UNKNOWN_FAILURE:
							System.out.println("The server experienced an unknown failure.");
							break;
						case SUCCESS:
							nameSat = true;
							break;
						default:
							System.out.println("Unknown server behavior");
							break;
						}
						if (!nameSat)
							System.out.println();

					}

				} catch (IOException ex) {
					System.err.println(
							"[CRITICAL ERROR] Error at reading any input properly.  Terminating the client now.");
					System.exit(-1);
				}
			}

            // Player has joined, now start up the remote socket.
            this.runListener = true;
            remoteOutputThread = new Thread(new GameClient.ReplyRemote(host));
            remoteOutputThread.setDaemon(true);
            remoteOutputThread.start();

            // Collect input for the game.
            while(runGame) {
                try {
                    keyboardStatement = keyboardInput.readLine();
                    parseInput(keyboardStatement);
                } catch (IOException ex) {
                    System.err.println("[CRITICAL ERROR] Error at reading any input properly.  Terminating the client now.");
                    System.exit(-1);
                }
            }                
        } catch (NotBoundException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch(RemoteException re) {
            System.err.println("[CRITICAL ERROR] There was a severe error with the RMI mechanism.");
            System.err.println("[CRITICAL ERROR] Code: " + re);
            System.exit(-1);
        }        
    }
    
    /** 
     * Simple method to parse the local input and remotely execute the RMI commands.
     * @param input 
     */
    private void parseInput(String input) {
        boolean reply;
        
        // First, tokenize the raw input.
        StringTokenizer commandTokens = new StringTokenizer(input);
        ArrayList<String> tokens = new ArrayList<>();
        while(commandTokens.hasMoreTokens() == true) {
            tokens.add(commandTokens.nextToken());
        }

        if(tokens.isEmpty()) {
            System.out.println("The keyboard input had no commands.");
            return;
        }
        
        String message = "";

        try {
            switch(tokens.remove(0).toUpperCase()) {

                case "LOOK":
                    System.out.println(remoteGameInterface.look(this.playerName));
                    break;
                case "LEFT":
                    System.out.println(remoteGameInterface.left(this.playerName));
                    break;
                case "RIGHT":
                    System.out.println(remoteGameInterface.right(this.playerName));
                    break;
                case "SAY":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to say something in order to SAY.");
                    }
                    else {
                        while(tokens.isEmpty() == false) {
                            message += tokens.remove(0);
                            if(tokens.isEmpty() == false) {
                                message += " ";
                            }
                        }                        
                        System.out.println(remoteGameInterface.say(this.playerName, message));
                    }
                    break;
                case "MOVE":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide a distance in order to move.");
                    }
                    else {
                        System.out.println(remoteGameInterface.move(this.playerName, Integer.parseInt(tokens.remove(0))));
                    }
                    break;
                case "PICKUP":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide an object to pickup.");
                    }
                    else {
                        System.out.println(remoteGameInterface.pickup(this.playerName, tokens.remove(0)));
                    }
                    break;
                case "INVENTORY":
                    System.out.println(remoteGameInterface.inventory(this.playerName));
                    break;                                                            
                case "QUIT":
                    remoteGameInterface.leave(this.playerName);
                    runListener = false;
                    break;
			case "DELETE":
				remoteGameInterface.deleteAccount(this.playerName);
				runListener = false;
				break;
            case "FRIENDS":
				String sub;
				if (tokens.isEmpty() || !((sub = tokens.remove(0).toUpperCase()).equals("ADD") || sub.equals("REMOVE")
						|| sub.equals("ONLINE"))) {
					System.out.println("\nProvide FRIENDS subcommand");
					System.out.println("  ADD name     - Adds a player to your friends list");
					System.out.println("  REMOVE name  - Removes a player from your friends list");
					System.out.println("  ONLINE       - lists your friends who are currently online");
					break;
				}
				if (tokens.isEmpty() && !sub.equals("ONLINE")) {
					System.out.println("Please provide a name for this command");
					break;
				} else if (tokens.isEmpty()) {
					System.out.println(remoteGameInterface.viewOnlineFriends(this.playerName));
					break;
				}
				String name = tokens.remove(0);
				while (!tokens.isEmpty())
					name += " " + tokens.remove(0);
				switch (sub) {
				case "ADD":
					switch (remoteGameInterface.addFriend(this.playerName, name)) {
					case INTERNAL_SERVER_ERROR:
						System.out.println("There was an internal server error with your account");
						break;
					case NOT_FOUND:
						System.out.println("The player you were trying to add could not be found");
						break;
					case EXISTS:
						System.out.println("You already have this friend added");
						break;
					case SILLY:
						System.out.println("Sorry, you cannot add yourself as a friend. Go make some");
						break;
					case SUCCESS:
						System.out.println(name + " was added to your friends list!");
						break;
					default:
						System.out.println("Unknown server behavior");
					}
					break;
				case "REMOVE":
					switch (remoteGameInterface.removeFriend(this.playerName, name)) {
					case INTERNAL_SERVER_ERROR:
						System.out.println("There was an internal server error with your account");
						break;
					case NOT_FOUND:
						System.out.println("This player was not on your friends list");
						break;
					case SILLY:
						System.out.println("Removing yourself? Who am I to judge");
						break;
					case SUCCESS:
						System.out.println(name + " was removed from your friends list");
						break;
					default:
						System.out.println("Unknown server behavior");
					}
					break;
				}
			}
        } catch (RemoteException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) {
		if(args.length < 1) {
			System.out.println("[SHUTDOWN] .. This program requires one argument. Run as java -Djava.security.policy=game.policy GameClient hostname");
			System.exit(-1);
		}
		
        System.out.println("[STARTUP] Game Client Now Starting...");
        new GameClient(args[0]);
    }

    /**
     * Inner class to handle remote message input to this program.  
     *  - Runs as a separate thread.  Interrupt it to kill it.
     *  - Spawns multiple threads, one for each remote connection.
     */
    public class ReplyRemote implements Runnable {
		private String host;
		
		public ReplyRemote(String host) {
			this.host = host;
		}
		
        @Override
        public void run() {
            // This thread is interruptable, which will allow it to clean up before
            
            // Attempt communcations with the server.
            try (Socket remoteMessageSocket = new Socket(host, 13500)) {
                
                // Get stream reader and writer. 
                //  Writer is only used once, to register this socket with a player.
                //  Otherwise, this is read only to receive non-locally generated event notifications.
                BufferedReader remoteReader = new BufferedReader(new InputStreamReader(remoteMessageSocket.getInputStream()));
                PrintWriter remoteWriter = new PrintWriter(remoteMessageSocket.getOutputStream(), true);
                
                // Register the socket with the player.
                remoteWriter.println(GameClient.this.playerName);
                remoteReader.readLine();

                // As long as this program is running, print all messages directly to output.
                String message;
                while(runListener == true) {
                    message = remoteReader.readLine();
                    if(message == null) {
                        System.err.println("The remote server has closed its connection!  Shutting down.");
                        System.exit(-1);
                    }
                    System.out.println(message);
                }                
            
                // Close the socket
                remoteMessageSocket.close();
            } catch(ConnectException ex) {
                System.err.println("[FAILURE] The connection has been refused.");
                System.err.println("          As this communication is critical, terminating the process.");
                System.exit(-1);
            } catch (IOException ex) {
                Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
    }    
    
}
