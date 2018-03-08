package uk.ac.ox.cs.adornment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Atom {
	
	protected String name;
	protected List<Variable> variables;
	protected Boolean edb = false;
	
	public Atom(String name, String variables) {
		this.name = name;
		setVariables(variables);
	}
	
	public Atom(String name, List<Variable >variables) {
		this.name = name;
		this.variables = variables;
	}
	
	public String getName() {
		return name;
	}
	
	public void setVariables(String variables) {
		this.variables = new ArrayList<>();
		String[] vars = variables.split(",");
		Arrays.asList(vars).forEach(variable -> {
			Variable tmp = new Variable();
			tmp.setName(variable);
			this.variables.add(tmp);
		});
	}
	
	public Boolean isEDB() {
		return edb;
	}
	
	public void setEDB(Boolean edb) {
		this.edb = edb;
	}
	
	public void setConstantValue(int position, String value) {
		this.variables.get(position).setValue(value);
	}
	
	public List<Variable> getVariables() {
		return variables;
	}
	
	public Boolean isEqual(Atom atom) {
		if(atom.name.equals(this.name)) {
			return true;
		}
		return false;
	}
	
	public String toString() {
		StringBuilder atom = new StringBuilder();
		atom.append(name);
		atom.append("(");
		variables.forEach(variable -> {
			atom.append(variable);
			atom.append(",");
		});
		atom.setLength(atom.length() - 1);
		atom.append(")");
		return atom.toString();
	}
}
