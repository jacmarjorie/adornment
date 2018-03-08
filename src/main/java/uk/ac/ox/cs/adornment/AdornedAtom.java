package uk.ac.ox.cs.adornment;

import java.util.List;
import java.util.stream.Collectors;

public class AdornedAtom extends Atom{
	
	protected String adornment;
	
	public AdornedAtom(String name, String variables) {
		super(name, variables);
	}
	
	public AdornedAtom(String name, List<Variable> variables) {
		super(name, variables);
	}

	public void setAdornment(String adornment) throws Exception {
		if(adornment.length() != variables.size()) {
			throw new Exception("Invalid adornment");
		}
		for(int i = 0; i < adornment.length(); i++) {
			// make sure that constants are adorned as bound
			if(variables.get(i).isConstant()) {
				char[] tmp = adornment.toCharArray();
				tmp[i] = 'b';
				adornment = String.valueOf(tmp);
			}
		}
		this.adornment = adornment;
	}
	
	public String getAdornment() {
		return adornment;
	}
	
	public Boolean isEqual(AdornedAtom atom) {
		if(atom.name.equals(this.name) && atom.getAdornment().equals(this.adornment)) {
			return true;
		}
		return false;
	}
	
	public Boolean isContained(List<AdornedAtom> atoms) {
		List<Atom> match = atoms.stream().filter(atom -> atom.isEqual(this)).collect(Collectors.toList());
		if(match.size() > 0) {
			return true;
		}
		return false;
	}
	
	public Boolean isBound(Variable variable){
		List<String> vars = this.variables.stream().map(var -> var.getName()).collect(Collectors.toList());
		int varpos = vars.indexOf(variable.getName());
		if(varpos == -1) {
			return false;
		}
		if("b".equals(String.valueOf(this.adornment.charAt(varpos)))) {
			return true;
		};
		return false;
	}
	
	public String toString() {
		StringBuilder atom = new StringBuilder();
		atom.append(name);
		atom.append(":");
		atom.append(adornment);
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
