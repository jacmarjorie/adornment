package uk.ac.ox.cs.adornment;

public class Variable {
	
	private String name;
	private String value;
	
	public void setName(String name) {
		this.value = "";
		this.name = name;
	}
	
	public void setValue(String value) {
		this.name = "";
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public Boolean isConstant() {
		if(value.length() > 0) {
			return true;
		}
		return false;
	}
	
	public String toString() {
		if(this.isConstant()) {
			return value;
		}
		return name;
	}
	
	public Boolean isEqual(Variable variable) {
		if(this.name == variable.name || this.value == variable.value) {
			return true;
		}
		return false;
	}

}
