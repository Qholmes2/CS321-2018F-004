import java.io.BufferedReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class AccountEditWizard extends Wizard.BlockingWizard {

	private ArrayList<Class<?>> moduleClasses = new ArrayList<>();
	{
		moduleClasses.add(AccountEditWizardModules.ChangePasswordModule.class);
		moduleClasses.add(AccountEditWizardModules.RecoveryQuestionsModule.class);
	}

	public AccountEditWizard(BufferedReader stdin, PrintStream stdout, GameObjectInterface obj, String playerName)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		super(stdin, stdout, obj, playerName, "Account Edit Wizard");
		addModules(moduleClasses.toArray(new Class<?>[0]));
	}

}
