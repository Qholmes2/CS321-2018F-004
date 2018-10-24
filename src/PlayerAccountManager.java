import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerAccountManager {
	private File accountFolder;
	private HashSet<String> playerIds;
	private Logger logger;

	public static class AccountResponse {
		public Player player = null;
		public Responses error = null;

		private AccountResponse(Player p) {
			this.player = p;
		}

		private AccountResponse(Responses error) {
			this.error = error;
		}

		public boolean success() {
			return error == null;
		}
	}

	public PlayerAccountManager(String folderName) throws Exception {
		accountFolder = new File(folderName);
		if (accountFolder.exists() && !accountFolder.isDirectory())
			throw new Exception();
		if (!accountFolder.exists())
			accountFolder.mkdir();
		playerIds = new HashSet<>();
		for (File playerAccF : accountFolder.listFiles())
			if (playerAccF.isDirectory())
				playerIds.add(playerAccF.getName());
		System.out.printf("Found %d player accounts\n", playerIds.size());
		logger = Logger.getLogger(PlayerAccountManager.class.getName());
	}

	/**
	 * @param recovery List of recovery questions and answers, ordered q1,a1,q2,a2,q3,a3
	 */
	public synchronized AccountResponse createNewAccount(String username, String password, ArrayList<String> recovery) {
		String lower = username.toLowerCase();
		if (accountExists(lower))
			return new AccountResponse(Responses.USERNAME_TAKEN);
		if (!lower.matches("^[a-zA-Z 0-9]+$"))
			return new AccountResponse(Responses.BAD_USERNAME_FORMAT);
		File userDir = new File(accountFolder.getAbsolutePath() + "/" + lower);
		try {
			playerIds.add(lower);
			/*String[] recoveryArray = new String[recovery.size()];
			int count = 0;
			for(String i : recovery)
				recoveryArray[count++] = i;
			count = 0;*/
			Player p = new Player(username, recovery);
			userDir.mkdir();
			writePlayerDataFile(p);
			FileOutputStream passFile = new FileOutputStream(userDir.getAbsolutePath() + "/pass.txt");
			passFile.write(password.getBytes());
			passFile.close();
			return new AccountResponse(p);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
			playerIds.remove(lower);
			userDir.delete();
			return new AccountResponse(Responses.INTERNAL_SERVER_ERROR);
		}
	}

	private void writePlayerDataFile(Player p) throws Exception {
		File userDir = new File(accountFolder.getAbsolutePath() + "/" + p.getName().toLowerCase());
		FileOutputStream dataFile = new FileOutputStream(userDir.getAbsolutePath() + "/data.json");
		dataFile.write(JsonMarshaller.MARSHALLER.marshalIndent(p).getBytes());
		dataFile.close();
	}

	public void forceUpdateData(Player p) {
		try {
			writePlayerDataFile(p);
		} catch (Exception e) {
		}
	}

	public boolean deleteAccount(String username) {
		username = username.toLowerCase();
		if (!playerIds.contains(username))
			return false;
		File userDir = new File(accountFolder.getAbsolutePath() + "/" + username);
		for (File f : userDir.listFiles())
			f.delete();
		if (!userDir.delete())
			return false;
		playerIds.remove(username);
		return true;
	}

	public AccountResponse getAccount(String username, String password) {
		username = username.toLowerCase();
		if (!accountExists(username))
			return new AccountResponse(Responses.NOT_FOUND);
		File userData = new File(accountFolder.getAbsolutePath() + "/" + username + "/data.json");
		File passData = new File(accountFolder.getAbsolutePath() + "/" + username + "/pass.txt");
		if (!userData.exists() || !passData.exists())
			return new AccountResponse(Responses.INTERNAL_SERVER_ERROR);
		try {
			DataInputStream passReader = new DataInputStream(new FileInputStream(passData));
			byte[] buf = new byte[256];
			int read = passReader.read(buf, 0, 256);
			passReader.close();
			String filePass = new String(buf).substring(0, read);
			if (!password.equals(filePass))
				return new AccountResponse(Responses.BAD_PASSWORD);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
			return new AccountResponse(Responses.INTERNAL_SERVER_ERROR);
		}
		Player p;
		try {
			p = JsonMarshaller.MARSHALLER.unmarshalFile(userData.getAbsolutePath(), Player.class);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
			return new AccountResponse(Responses.INTERNAL_SERVER_ERROR);
		}
		return new AccountResponse(p);
	}

	public boolean accountExists(String username) {
		return playerIds.contains(username);
	}
	
	/**
	 * returns the player object based off of their name
	 * @param name Username of player to be returned
	 * @return player object 
	 */
	public AccountResponse getPlayer(String name) {
		Player player = null;
		name = name.toLowerCase();
		if(!playerIds.contains(name)) 
			return new AccountResponse(Responses.NOT_FOUND);
		File userData = new File(accountFolder.getAbsoluteFile() + "/" + name + "/data.json");
		if(!userData.exists())
			return new AccountResponse(Responses.NOT_FOUND);
		try {
			player = JsonMarshaller.MARSHALLER.unmarshalFile(userData.getAbsolutePath(), Player.class);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
			return new AccountResponse(Responses.INTERNAL_SERVER_ERROR);
		}
		if(player == null) {
			return new AccountResponse(Responses.UNKNOWN_FAILURE);
		}
		return new AccountResponse(player);
	}
	
	/**
	 * Resets the password of the give player
	 * @param p Player who's password is getting changed
	 * @param password Pre-hashed new password
	 * @return a response based on what happens
	 */
	public  Responses resetPassword(Player p, String password) {
		File userDir = new File(accountFolder.getAbsolutePath() + "/" + p.getName().toLowerCase());
		try {
			FileOutputStream passFile = new FileOutputStream(userDir.getAbsolutePath() + "/pass.txt");
			passFile.write(password.getBytes());
			passFile.close();
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, null, e);
			return Responses.INTERNAL_SERVER_ERROR;
		} catch (IOException e) {
			logger.log(Level.SEVERE, null, e);
			return Responses.INTERNAL_SERVER_ERROR;
		}
		return Responses.SUCCESS;
		
	}
}
