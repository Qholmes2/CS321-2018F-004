import java.io.BufferedReader;
import java.io.PrintStream;

public class AccountEditWizardModules {
	protected static class ChangePasswordModule extends Wizard.WizardModule {

		public ChangePasswordModule(BufferedReader stdin, PrintStream stdout, GameObjectInterface obj,
				String playerName) {
			super(stdin, stdout, obj, playerName);
			this.listName = "Change Password";
		}

		public String getListName() {
			return this.listName;
		}

		public void run() throws java.lang.Exception {
			stdout.print("Re-enter username: ");
			String user = stdin.readLine().trim();
			stdout.print("Re-enter password: ");
			String pass = new String(System.console().readPassword()).trim(); // task 221 hides password
			Responses resp = this.obj.verifyPassword(user, pass);
			if (resp != Responses.SUCCESS) {
				switch (resp) {
				case NOT_FOUND:
				case BAD_PASSWORD:
					stdout.println("Bad account information");
					break;
				case INTERNAL_SERVER_ERROR:
					stdout.println("Sorry, there was a problem server-side");
				default:
					stdout.println("Unknown server behavior");
				}
				return;
			}
			stdout.print("Enter new password: ");
			String newPass = new String(System.console().readPassword()).trim(); // task 221 hides password
			resp = this.obj.resetPassword(user, newPass);
			switch (resp) {
			case NOT_FOUND:
			case INTERNAL_SERVER_ERROR:
				stdout.println("Sorry, there was a problem server-side");
				break;
			case SUCCESS:
				stdout.println("Password changed.");
				break;
			default:
				stdout.println("Unknown server behavior");
			}
		}
	}
}
