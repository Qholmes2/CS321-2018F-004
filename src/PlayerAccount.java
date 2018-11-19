import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerAccount implements IAccount {
	private String password;
	private long accountAge;
	private ArrayList<RecoveryQuestion> recoveryQuestions;
	private String name;

	@JsonCreator
	private PlayerAccount() {
	}

	public PlayerAccount(String name, String password) {
		this.name = name;
		this.password = hash(password);
		this.accountAge = System.currentTimeMillis();
		this.recoveryQuestions = new ArrayList<>();
	}

	@SuppressWarnings("unused")
	private void setName(@JsonProperty("name") String name) {
		this.name = name;
	}

	@SuppressWarnings("unused")
	private void setPassword(@JsonProperty("password") String password) {
		this.password = password;
	}

	@JsonProperty("name")
	public String getName() {
		return this.name;
	}

	@JsonProperty("password")
	private String getPassword() {
		return this.password;
	}

	@JsonProperty("recoveryQuestions")
	private ArrayList<RecoveryQuestion> getRecoveryQuestions() {
		return this.recoveryQuestions;
	}

	@JsonProperty("accountAge")
	private long getAccountAge() {
		return this.accountAge;
	}

	@SuppressWarnings("unused")
	private void setAccountAge(@JsonProperty("accountAge") long accountAge) {
		this.accountAge = accountAge;
	}

	@SuppressWarnings("unused")
	private void setRecoveryQuestions(
			@JsonProperty("recoveryQuestions") ArrayList<RecoveryQuestion> recoveryQuestions) {
		this.recoveryQuestions = recoveryQuestions;
	}

	/**
	 * Guaranteed success
	 * 
	 * @param name
	 * @return accountAge
	 */
	public DataResponse<Long> getAccountAge(String name) {
		return new DataResponse<Long>(this.accountAge);
	}

	/**
	 * Possible Responses:<br>
	 * SUCCESS<br>
	 * FAILURE<br>
	 * 
	 * @param name
	 * @param pass
	 * @return goodPassword
	 */
	public Responses verifyPassword(String name, String pass) {
		if (this.password.equals(hash(pass)))
			return Responses.SUCCESS;
		return Responses.FAILURE;
	}

	/**
	 * Possible Responses:<br>
	 * FAILURE - bad question length<br>
	 * BAD_PASSWORD - need answer<br>
	 * SUCCESS<br>
	 * 
	 * @param name
	 * @param question
	 * @param answer
	 * @return addStatus
	 */
	public Responses addRecoveryQuestion(String name, String question, String answer) {
		question = question.trim();
		if (question.length() < 4)
			return Responses.FAILURE;
		if (answer.trim().length() == 0)
			return Responses.BAD_PASSWORD;
		recoveryQuestions.add(new RecoveryQuestion(question, hash(answer)));
		return Responses.SUCCESS;
	}

	/**
	 * Possible Responses:<br>
	 * FAILURE<br>
	 * SUCCESS<br>
	 * 
	 * @param name
	 * @param answers
	 * @return verified
	 */
	public Responses verifyAnswers(String name, ArrayList<String> answers) {
		if (answers.size() != recoveryQuestions.size())
			return Responses.FAILURE;
		for (int i = 0; i < answers.size(); i++)
			if (!hash(answers.get(i)).equals(recoveryQuestions.get(i).answer))
				return Responses.FAILURE;
		return Responses.SUCCESS;
	}

	/**
	 * Possible Responses:<br>
	 * SUCCESS<br>
	 * 
	 * @param name
	 * @param password
	 * @return changeStatus
	 */
	public Responses changePassword(String name, String password) {
		this.password = hash(password);
		return Responses.SUCCESS;
	}

	/**
	 * Is guaranteed to return the questions
	 * 
	 * @param name
	 * @return questions
	 */
	@JsonIgnore
	public DataResponse<ArrayList<String>> getQuestions(String name) {
		ArrayList<String> res = new ArrayList<>(recoveryQuestions.size());
		for (RecoveryQuestion r : recoveryQuestions)
			res.add(r.question);
		return new DataResponse<ArrayList<String>>(res);
	}

	/**
	 * Possible Responses:<br>
	 * FAILURE<br>
	 * SUCCESS<br>
	 * 
	 * @param name
	 * @param num
	 * @return removeStatus
	 */
	public Responses removeQuestion(String name, int num) {
		if (num < 0 || num >= recoveryQuestions.size())
			return Responses.FAILURE;
		recoveryQuestions.remove(num);
		return Responses.SUCCESS;
	}

	private static class RecoveryQuestion {
		private String question;
		private String answer;

		@JsonCreator
		private RecoveryQuestion() {
		}

		private RecoveryQuestion(String question, String answer) {
			this.question = question;
			this.answer = answer;
		}

		@JsonProperty("question")
		private String getQuestion() {
			return question;
		}

		@SuppressWarnings("unused")
		private void setQuestion(@JsonProperty("question") String question) {
			this.question = question;
		}

		@JsonProperty("answer")
		private String getAnswer() {
			return answer;
		}

		@SuppressWarnings("unused")
		private void setAnswer(@JsonProperty("answer") String answer) {
			this.answer = answer;
		}

	}

	/**
	 * Used to create a hash encrypted in SHA256 for use in encrypting passwords
	 * 
	 * @param toHash
	 * @return SHA256 encrypted hash value, or "ERROR" If encryption method fails.
	 */
	private String hash(String toHash) {
		try {
			byte[] encodedhash = MessageDigest.getInstance("SHA-256").digest(toHash.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : encodedhash)
				sb.append(String.format("%02X", b));
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
		}
		return "ERROR";
	}

}
