import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kevin
 */
public class GameServer {
// ----- RMI RELATED MEMBERS ------------
	// Remote object for RMI
	private GameObject remoteObject;

	// Members to control and run the asynchronous reply thread for the RMI
	// implementation.
	protected ServerSocket remoteListener;
	private Thread remoteOutputThread;

// ----- NON-RMI RELATED MEMBERS ------------    
	private Thread gameThread;

	/**
	 * Main game server class.
	 * 
	 * @param rmi
	 * @throws RemoteException
	 */
	public GameServer(String host) throws RemoteException {
		try {
			// Step 1: Create the remote listener thread. This thread is used
			// for asynchronous replies from the game for events the
			// client has not generated. (ie. other players talking)
			remoteOutputThread = new Thread(new ReplyRemote(host));
			remoteOutputThread.setDaemon(true);
			remoteOutputThread.start();
			// Step 2: Create the remote object and register it for RMI
			// a) Create the security manager.
			System.setSecurityManager(new SecurityManager());
			// b) Create the RMI remote object.
			remoteObject = new GameObject("players");
			// c) Bind the remote object to the rmi service (rmiregistry must be running)
			Naming.rebind("rmi://" + host + "/GameService", remoteObject);
			System.err.println("[RUN] Game Server is now running and accepting connections.");
		} catch (RemoteException re) {
			Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, re);
		} catch (MalformedURLException ex) {
			Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Exception e) {
			Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println(
					"[SHUTDOWN] .. This program requires one argument. Run as java -Djava.security.policy=game.policy GameServer hostname");
			System.exit(-1);
		}

		try {
			System.out.println("[STARTUP] Game GameServer Now Starting...");
			new GameServer(args[0]);
		} catch (RemoteException ex) {
			Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Inner class to handle remote message input to this program. - Runs as a
	 * separate thread. Interrupt it to kill it. - Spawns multiple threads, one for
	 * each remote connection.
	 */
	public class ReplyRemote implements Runnable {
		private String host;

		public ReplyRemote(String host) {
			this.host = host;
		}

		@Override
		public void run() {
			try {
				// This thread is interruptable, which will allow it to clean up before
				// being joined.
				System.err.println("[STARTUP] .. New socket opened on port 13500");
				remoteListener = new ServerSocket(13500);
				while (true) {
					try {
						final Socket remoteMessageSocket = remoteListener.accept();
						System.err.println("[SOCKET] .. Received new connection");

						// Anonymous to handle connections.
						Thread remoteConnectionThread = new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									// Get the socket input and output streams.
									BufferedReader remoteReader = new BufferedReader(
											new InputStreamReader(remoteMessageSocket.getInputStream()));
									PrintWriter remoteWriter = new PrintWriter(remoteMessageSocket.getOutputStream(),
											true);

									// Read message from the input buffer and acknowledge receipt.
									String playerName = remoteReader.readLine();
									System.err.println("[SOCKET] .. Connection for a player " + playerName);
									remoteWriter.println("ACK");

									// Attach this socket to the player managed by the remote object.
									if (GameServer.this.remoteObject.setReplyWriter(playerName,
											remoteWriter) == false) {
										System.err.println(
												"[ERROR] Non-registered player attempted connection.  Severing.");
										remoteMessageSocket.close();
									}

									System.err.println("[SOCKET] .. " + playerName + " has registered their socket.");
									// And give the player their first look at the area they are in.
									remoteWriter.println(GameServer.this.remoteObject.look(playerName));
								} catch (IOException ex) {
									Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
								}
							}
						});

						// Start the connection-specific socket thread.
						remoteConnectionThread.setDaemon(true);
						remoteConnectionThread.start();
					} catch (IOException ex) {
						Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			} catch (IOException ex) {
				Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
