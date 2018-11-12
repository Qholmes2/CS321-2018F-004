import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public abstract class Wizard {

	@FunctionalInterface
	public static interface Action {
		public void run() throws java.lang.Exception;
	}

	protected abstract static class WizardModule {
		protected String listName;
		protected BufferedReader stdin;
		protected PrintStream stdout;
		protected GameObjectInterface obj;
		protected String playerName;

		public WizardModule(BufferedReader stdin, PrintStream stdout, GameObjectInterface obj, String playerName) {
			this.stdin = stdin;
			this.stdout = stdout;
			this.obj = obj;
			this.playerName = playerName;
		}

		public abstract void run() throws java.lang.Exception;

		public abstract String getListName();
	}

	public static class TextMenu {
		private String name;
		private int count = 0;
		private ArrayList<String> names = new ArrayList<>();
		private ArrayList<Action> actions = new ArrayList<>();
		private BufferedReader stdin;
		private PrintStream stdout;

		public TextMenu(String name, BufferedReader stdin, PrintStream stdout) {
			this.name = name;
			this.stdin = stdin;
			this.stdout = stdout;
		}

		public TextMenu add(String name, Action action) {
			names.add(name);
			actions.add(action);
			count++;
			return this;
		}

		public void display() throws IOException {
			while (true) {
				printHeader();
				printOptions();
				stdout.print("> ");
				String line = stdin.readLine().trim();
				int option;
				try {
					option = Integer.parseInt(line);
				} catch (Exception e) {
					stdout.println("Enter a number please...");
					continue;
				}
				if (option <= 0 || option > count + 1) {
					stdout.println("That was not one of the options...");
					continue;
				} else if (option == count + 1)
					break;
				try {
					actions.get(option - 1).run();
				} catch (Exception e) {
					stdout.println("There was a problem casting this spell");
				}
			}
		}

		private void printHeader() {
			for (int i = 0; i < this.name.length(); i++)
				stdout.print("_");
			stdout.println();
			stdout.println(this.name);
		}

		private void printOptions() {
			stdout.println();
			int i = 0;
			for (i = 0; i < count; i++) {
				stdout.printf("%" + ((count + "").length()) + "d. %s\n", i + 1, names.get(i));
			}
			stdout.println(i + 1 + ". Leave");
		}
	}

	private BufferedReader stdin;
	private GameObjectInterface obj;
	private String playerName;
	private PrintStream stdout;
	private TextMenu mainMenu;

	public Wizard(BufferedReader stdin, PrintStream stdout, GameObjectInterface obj, String playerName,
			String wizardName) throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		this.stdin = stdin;
		this.stdout = stdout;
		this.obj = obj;
		this.playerName = playerName;

		mainMenu = new Wizard.TextMenu(wizardName, stdin, stdout);
	}

	protected PrintStream out() {
		return this.stdout;
	}

	public Wizard addModules(Class<?>... classes) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		for (Class<?> c : classes) {
			Constructor<?> con = c.getConstructor(BufferedReader.class, PrintStream.class, GameObjectInterface.class,
					String.class);
			Wizard.WizardModule m = (Wizard.WizardModule) con.newInstance(stdin, stdout, obj, playerName);
			mainMenu.add(m.getListName(), m::run);
		}
		return this;
	}

	public void enter() throws IOException {
		if (mainMenu.actions.size() == 0) {
			stdout.println("Sorry, currently there are no spells this wizard can perform.");
			leave();
			return;
		}
		mainMenu.display();
		leave();
	}

	protected abstract void leave();

	public static class SimpleWizard extends Wizard {

		public SimpleWizard(BufferedReader stdin, PrintStream stdout, GameObjectInterface obj, String playerName,
				String wizardName) throws NoSuchMethodException, SecurityException, InstantiationException,
				IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			super(stdin, stdout, obj, playerName, wizardName);
		}

		@Override
		protected void leave() {
		}

	}

	public static class BlockingWizard extends Wizard {
		private StringBuilder bufferedString = new StringBuilder();
		private PrintStream bufferedStream;

		public BlockingWizard(BufferedReader stdin, PrintStream stdout, GameObjectInterface obj, String playerName,
				String wizardName) throws NoSuchMethodException, SecurityException, InstantiationException,
				IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			super(stdin, stdout, obj, playerName, wizardName);
			bufferedStream = new PrintStream(new OutputStream() {
				@Override
				public void write(int arg0) throws IOException {
					bufferedString.append((char) arg0);
				}
			});
		}

		@Override
		public void enter() throws IOException {
			System.setOut(bufferedStream);
			super.enter();
		}

		@Override
		protected void leave() {
			// This isn't a perfect solution...
			if (bufferedString.length() > 0) {
				out().println("Here were the messages you missed while in the wizard:");
				out().println(bufferedString.toString());
				bufferedString.setLength(0);
			}
			System.setOut(out());
		}
	}
}
