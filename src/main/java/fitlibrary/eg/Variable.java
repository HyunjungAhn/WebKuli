package fitlibrary.eg;

public class Variable {
	private String name;
	private Object value;
	
	public Variable(String name) {
		this.name = name;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String toString() {
		return "Variable["+name+"]";
	}
}
