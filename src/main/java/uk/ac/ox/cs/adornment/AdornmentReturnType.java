package uk.ac.ox.cs.adornment;

import java.util.List;

/**
 * Adorn atom return type
 * @author jac
 *
 */
public class AdornmentReturnType {
	
	protected List<Rule> program;
	protected List<AdornedAtom> failedAdorn;
	protected List<List<Atom>> failedSips;
	protected List<AdornedAtom> completed;
	protected Boolean success = false;
	
	public AdornmentReturnType(List<Rule> program, List<AdornedAtom> failedAdorn, List<List<Atom>> failedSips, List<AdornedAtom> completed) {
		this.program = program;
		if(!this.program.isEmpty()) {
			this.success = true;
		}
		this.failedAdorn = failedAdorn;
		this.failedSips = failedSips;
		this.completed = completed;
	}

}
