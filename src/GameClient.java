

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

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.*;


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
    private String lastCommand;
    /** 
     * Main class for running the game client.
     */
    public GameClient(String host) {
        this.runGame = true;
        boolean nameSat = false;

        showIntroduction();

        showCommand();


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
                    System.out.println("Please enter a name for your player.");
                    System.out.print("> ");
                    this.playerName = keyboardInput.readLine();
                    System.out.println("Welcome, " + this.playerName + ". Are you sure you want to use this name?");
                    System.out.print("(Y/N) >");
                    if(keyboardInput.readLine().equalsIgnoreCase("Y")) {
                        // Attempt to join the server
                        if(remoteGameInterface.joinGame(this.playerName) == false) {
                            System.out.println("I'm sorry, " + this.playerName + ", but someone else is already logged in with your name. Please pick another.");
                        }
                        else {
                            nameSat = true;
                        }
                    }
                } catch (IOException ex) {
                    System.err.println("[CRITICAL ERROR] Error at reading any input properly.  Terminating the client now.");
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
        String command = tokens.remove(0).toUpperCase();

        try {

            switch(command) {
                boolean success = true; //Trips whenever the user enters a bad command. Used so redo doesnt redo bad commands atm
                case "LOOK":
                    System.out.println(remoteGameInterface.look(this.playerName));   
                    break;
                case "SAY":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to say something in order to SAY.");
                        success = false;
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
                        System.err.println("You need to provide a direction to move.");
                        success = false;
                    } else {
                        Direction dir = Direction.toValue(tokens.remove(0));
                        if(dir!=null) {
                            System.out.println(remoteGameInterface.move(this.playerName, dir));
                        }                    
                    }
                    break;
                case "PICKUP":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide an object to pickup.");
                        success = false;
                    }
                    else {
                        System.out.println(remoteGameInterface.pickup(this.playerName, tokens.remove(0)));
                    }
                    break;
                case "PICKUPALL":
                    System.out.println(remoteGameInterface.pickupAll(this.playerName));
                    break;
                case "WHITEBOARD":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide an argument to the WHITEBOARD command.");
                    }
                    else {
                        switch(tokens.remove(0).toUpperCase()) {
                            case "ERASE":
                                System.out.println(remoteGameInterface.whiteboardErase(this.playerName));
                                break;
                            case "READ":
                                System.out.println(remoteGameInterface.whiteboardRead(this.playerName));
                                break;
                            case "WRITE":
                                if (tokens.isEmpty()) { 
                                    System.err.println("You need to provide an argument to the WHITEBOARD WRITE command");
                                }
                                else {
                                    System.out.println(remoteGameInterface.whiteboardWrite(this.playerName, tokens.remove(0)));
                                }
                                break;
                            default:
                                System.err.println("Invalid argument provided to WHITEBOARD command.");
                                break;
                        }
                    }
                    break;
                case "INVENTORY":
                    System.out.println(remoteGameInterface.inventory(this.playerName));
                    break;                                                            
                case "QUIT":
                    remoteGameInterface.leave(this.playerName);
                    runListener = false;
                    break;
                case "HELP":
                    showCommand();
                    break;
                case "REDO":
                    if(lastCommand==null){
                        System.out.println("No command to redo.");
                        break;
                    }
                    parseInput(lastCommand);
                    break;
                case "ADDCOMMAND":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide a custom command to add.");
                    } else {
                        String customCommand = tokens.remove(0).toUpperCase();
                        addCustomCommand(customCommand);
                    }
                    break;
                case "REMOVECOMMAND":
                    removeCustomCommand();
                    break;
                case "CUSTOMHELP":
                    showCustomCommands();
                    break;
                default:
                    //If command does not match with any, see if it is custom command
                    if (!executeCustomCommand(command, tokens)) {
                        System.out.println("Invalid Command, Enter \"help\" to get help");
                    }
                    break;
            }
            if(!input.equals("REDO")&&success) {
                this.lastCommand = input;
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

    /*If no parameter has been given for showCommand, pass in null to showCommand.
     *This will cause showCommand to print every commands available in game
     */
    private void showCommand()
    {
        showCommand(null);
    }

    //Shows every command available in game
    private void showCommand(String commandToShow)
    {
        try {
            File commandFile = new File("./help.xml");
            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(commandFile);

            document.getDocumentElement().normalize();
            NodeList xmlCommands = document.getElementsByTagName("help");

            String description;
            Element xmlElement;

            System.out.println("The game allows you to use the following commands:");

            //Get every commands from xml file and print them
            for (int i = 0; i < xmlCommands.getLength(); i++) {
                xmlElement = (Element) xmlCommands.item(i);

                description = xmlElement.getElementsByTagName("description").item(0).getTextContent();

                //If the commmand does not have description yet, do not show it.
                if ( !description.equals("") ){
                    System.out.println(description);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Shows the introduction of the game
    private void showIntroduction()
    {
        try {
            File commandFile = new File("./help.xml");
            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(commandFile);

            document.getDocumentElement().normalize();
            NodeList xmlCommands = document.getElementsByTagName("introduction");

            String description;
            Element xmlElement;

            xmlElement = (Element) xmlCommands.item(0);
            description = xmlElement.getElementsByTagName("description").item(0).getTextContent();
            System.out.println(description);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addCustomCommand(String customCommandName)
    {
        InputStreamReader keyboardReader = new InputStreamReader(System.in);
        BufferedReader keyboardInput = new BufferedReader(keyboardReader);
        String commandToExecute;

        try {
            File customCommandFile = new File("./CommandShortcut.xml");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document;
            Element root;

            //If no xml file CommandShortcut exists, create one
            if (!customCommandFile.exists())
            {
                document = dBuilder.newDocument();

                root = document.createElement("CustomCommand");
                document.appendChild(root);
            //Else use already existing xml file CommandShortcut
            } else {
                document = dBuilder.parse(customCommandFile);

                NodeList rootElements = document.getElementsByTagName("CustomCommand");
                root = (Element) rootElements.item(0);
            }

            NodeList customCommands = document.getElementsByTagName("Command");

            Element cCommand;

            //Get every custom commands to check if user is trying to use already existing custom command name
            for (int i = 0; i < customCommands.getLength(); i++) {
                cCommand = (Element) customCommands.item(i);

                if (customCommandName.equals(cCommand.getAttribute("name"))) {
                    System.out.println("There already exists a custom command with a name " + customCommandName);
                    return;
                }
            }

            //Get command to bind to custom command
            System.out.println("Enter the command you wish to bind to " + customCommandName + ":");
            commandToExecute = keyboardInput.readLine();

            //Add elements to xml file
            Element command = document.createElement("Command");
            root.appendChild(command);

            Attr attr = document.createAttribute("name");
            attr.setValue(customCommandName);
            command.setAttributeNode(attr);

            Element bindCommand = document.createElement("CommandToExecute");
            bindCommand.appendChild(document.createTextNode(commandToExecute));
            command.appendChild(bindCommand);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File("./CommandShortcut.xml"));

            //Add indentation to xml file for readability
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(domSource, streamResult);

            System.out.println("Command " + customCommandName + " has been added.");

        } catch (ParserConfigurationException | TransformerException | SAXException | IOException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean executeCustomCommand(String commandName, ArrayList<String> parameters) {
        try {
            File customCommandFile = new File("./CommandShortcut.xml");

            //If no custom command exists yet, return false
            if (!customCommandFile.exists())
            {
                return false;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(customCommandFile);

            NodeList customCommands = document.getElementsByTagName("Command");

            Element cCommand;

            //Check if custom command with commandName exists
            for (int i = 0; i < customCommands.getLength(); i++) {
                cCommand = (Element) customCommands.item(i);

                //If there is a custom command, use command binded to perform next aciton
                if (commandName.equals(cCommand.getAttribute("name").toUpperCase())) {
                    String bindCommand = cCommand.getElementsByTagName("CommandToExecute").item(0).getTextContent();
                    for (int j = 0; j < parameters.size(); j++)
                    {
                        bindCommand += " " + parameters.get(j).toUpperCase();
                    }

                    parseInput(bindCommand);
                    return true;
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    private void removeCustomCommand() {
        try {
            File customCommandFile = new File("./CommandShortcut.xml");

            if (!showCustomCommands()) {
                return;
            }

            InputStreamReader keyboardReader = new InputStreamReader(System.in);
            BufferedReader keyboardInput = new BufferedReader(keyboardReader);
            String commandName;

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(customCommandFile);

            NodeList customCommands = document.getElementsByTagName("Command");

            Element cCommand;

            System.out.println("\nEnter the custom command to delete:");
            commandName = keyboardInput.readLine().toUpperCase();

            //Check if custom command with commandName exists
            for (int i = 0; i < customCommands.getLength(); i++) {
                cCommand = (Element) customCommands.item(i);

                //If there is a custom command, remove that command
                if (commandName.equals(cCommand.getAttribute("name").toUpperCase())) {
                    cCommand.getParentNode().removeChild(cCommand);
                    System.out.println("Custom command " + commandName + " has been deleted");
                    break;
                }

                if (i == (customCommands.getLength() - 1)) {
                    System.out.println("No custom command " + commandName + " was found");
                }
            }

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File("./CommandShortcut.xml"));

            transformer.transform(domSource, streamResult);

        } catch (ParserConfigurationException | SAXException | IOException | TransformerException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean showCustomCommands()
    {
        try {
            File customCommandFile = new File("./CommandShortcut.xml");

            //If no custom command exists yet, return false
            if (!customCommandFile.exists())
            {
                System.out.println("There is no custom command configured");
                return false;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(customCommandFile);

            NodeList customCommands = document.getElementsByTagName("Command");

            if (customCommands.getLength() == 0)
            {
                System.out.println("There is no custom command configured");
                return false;
            }

            System.out.println("Here are the list of custom commands currently available:");

            Element cCommand;

            //Show every custom commands
            for (int i = 0; i < customCommands.getLength(); i++) {
                cCommand = (Element) customCommands.item(i);

                System.out.println(cCommand.getAttribute("name").toUpperCase() + " - " + cCommand.getElementsByTagName("CommandToExecute").item(0).getTextContent());
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
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
