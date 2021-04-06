package fitlibrary.eg;

import java.util.HashMap;
import java.util.Map;

import fitlibrary.traverse.DomainAdapter;

public abstract class DomainAdapterWithVariables implements DomainAdapter {
	protected Map variables = new HashMap();

	public Object findVariable(String s) {
		Variable var = (Variable) variables.get(s);
		if (var == null) {
			var = new Variable(s);
			variables.put(s,var);
		}
		return var;
	}
}
