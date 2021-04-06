package fitnesse.responders.concurrent;

public class ConTestDefinitionException extends Exception {

	private static final long serialVersionUID = 1L;

	public String getMessage() {
		return "CONTEST_PROP variable definition not found!\ndefine like: !define CONTEST_PROP {%PATH%/KingProperties}\n"; 
	}
}
