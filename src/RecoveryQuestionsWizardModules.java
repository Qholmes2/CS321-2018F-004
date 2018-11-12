import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.ArrayList;

public class RecoveryQuestionsWizardModules {
	protected static class AddQuestionModule extends Wizard.WizardModule {

		public AddQuestionModule(BufferedReader stdin, PrintStream stdout, GameObjectInterface obj, String playerName) {
			super(stdin, stdout, obj, playerName);
			this.listName = "Add Recovery Question";
		}

		@Override
		public void run() throws Exception {
			String question;
			String answer;
			Boolean test;
			stdout.println("Please enter the recovery question you would like to add");
			stdout.print(">");
			question = stdin.readLine().trim();
			if (question.length() < 4) {
				test = true;
				while (test) {
					stdout.println("Please submit a question that is at least 4 characters");
					stdout.print(">");
					question = stdin.readLine().trim();
					if (question.length() >= 4) {
						test = false;
					}
				}
			}
			stdout.println("Please enter the answer");
			stdout.print("> ");
			answer = new String(System.console().readPassword()).trim().toLowerCase(); // task 221 hides password
			if (answer.length() < 4) {
				test = true;
				while (test) {
					stdout.println("Please submit an answer that is at least 4 characters");
					stdout.print(">");
					answer = stdin.readLine().trim();
					if (answer.length() >= 4) {
						test = false;
					}
				}
			}
			this.obj.addQuestion(this.playerName, question, answer);
		}

		@Override
		public String getListName() {
			return this.listName;
		}

	}

	protected static class RemoveQuestionModule extends Wizard.WizardModule {

		public RemoveQuestionModule(BufferedReader stdin, PrintStream stdout, GameObjectInterface obj,
				String playerName) {
			super(stdin, stdout, obj, playerName);
			this.listName = "Remove Recovery Question";
		}

		@Override
		public void run() throws Exception {
			ArrayList<String> questions = new ArrayList<String>();
			int count;
			Integer num;
			String input;
			boolean test;

			questions.add(this.obj.getQuestion(this.playerName, 0));
			if (questions.get(0) == null) {
				stdout.println("You have no recovery questions set");
			} else {
				count = 0;
				do {
					stdout.println((++count) + " " + questions.get(count - 1));
					questions.add(this.obj.getQuestion(this.playerName, count));
				} while (questions.get(count) != null);
				stdout.println("Please select the number of the question you wish to remove");
				stdout.print(">");
				input = stdin.readLine().trim();
				num = Integer.parseInt(input);
				if (num == null || num > questions.size() || num < 0) {
					test = true;
					while (test) {
						stdout.println("Your input was either not a number or was not an option listed");
						stdout.println("Please enter a valid input, -1 if you want to quit");
						input = stdin.readLine().trim();
						num = Integer.parseInt(input);
						if (!(num == null && num > questions.size() && num < -1))
							test = false;
						if (num != null && num == -1)
							return;
					}
				}
				this.obj.removeQuestion(this.playerName, num - 1);
				stdout.println("Question " + num + " has been removed");
			}
		}

		@Override
		public String getListName() {
			return this.listName;
		}

	}

}
