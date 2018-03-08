package uk.ac.ox.cs.adornment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Collections2;

public class Rule {
	
	protected Atom head;
	protected List<Atom> body;
	
	public Rule(Atom... atom) {
		this.head = atom[0];
		if(atom.length > 1) {
			this.body = Arrays.asList(atom).subList(1, atom.length);
		}else {
			this.body = new ArrayList<>();
		}
	}
	
	public Rule(Atom head, List<Atom> body) {
		this.head = head;
		this.body = body;
	}
	
	/**
	 * Return all permutations of body elements that are not in the failedSips set
	 * @param failedSips, list of body atom orderings that fail to produce successful adornments
	 * @return a list of rule bodies (list of atoms) that represent all nonfailing sips for a given rule body
	 */
	public List<List<Atom>> getSips(List<List<Atom>> failedSips) {
		List<List<Atom>> sips = Collections2.permutations(this.body).stream().collect(Collectors.toList());
		sips.remove(failedSips);
		
		// catch inferred atoms
		if(sips.isEmpty()) {
			sips.add(new ArrayList<>());
		}
		return sips;
	}
	
	public Boolean isEqual(Rule rule) {
		if(!rule.head.isEqual(this.head)) {
			return false;
		}
		for(int i = 0; i < rule.body.size(); i++) {
			if(!rule.body.get(i).isEqual(this.body.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	public Boolean isContained(List<Rule> program) {
		for(Rule rule: program){
			if(rule.isEqual(this)) {
				return true;
			}
		}
		return false;
	}
	
	public String toString() {
		StringBuilder rule = new StringBuilder();
		rule.append(this.head.toString());
		rule.append(" := ");
		this.body.forEach(body->{
			rule.append(body);
			rule.append(",");
		});
		rule.setLength(rule.length() - 1);
		return rule.toString();	
	}
}
