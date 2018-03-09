package uk.ac.ox.cs.adornment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * Adornment Algorithm from web services
 *
 */
public class App 
{
	/**
	 * AdornAtom takes as input an adornment for an atom and generates a set of rules. The general idea is to start adorning rules with the goal in the head
	 * the adornment of body elements in the query rules will trigger recursive calls to adorn rules which have query body elements in the head, and so on.
	 * Eventually, all rules associated with the query rules will be adorned. 
	 * @param program, list of unadorned rules
	 * @param adornment, adorned atom
	 * @param failedAdorn, list of adorned atoms that are known to fail (or are forbidden based on web service access patterns
	 * @param failedSips, list of rules which the ordering of body elements is known to have failed (do we actually need this?)
	 * @param completed, list of adorned atoms for which all the rules with matching base predicate in the head have been adorned successfully
	 * @param inProgress, a list of adorned atoms that are not complete, but are in progress (necessary to track for recursive calls)
	 * @return AdornmentReturnType (List<Rule> program, List<AdornAtom> failedAdorn, List<AdornAtom> failedSips, List<AdornAtom> completed)
	 * @throws Exception when an adornment of an atom is invalid
	 */
	public static AdornmentReturnType adornAtom(List<Rule> program, AdornedAtom adornment, List<AdornedAtom> failedAdorn, List<List<Atom>> failedSips, List<AdornedAtom> completed, List<AdornedAtom> inProgress) throws Exception {
		
		List<Rule> adornedProgram = new ArrayList<>();
		
		if(failedAdorn.contains(adornment)) {
			return new AdornmentReturnType(adornedProgram, failedAdorn, failedSips, completed);
		}
		
		// for each rule with adornment predicate in the head
		Rule[] rules = program.stream().filter(rule->(rule.head.name==adornment.name)).toArray(Rule[]::new);
		for(int i = 0; i < rules.length; i++) {
			Boolean failed = true;
			inProgress.add(adornment);
			
			List<List<Atom>> sips = rules[i].getSips(failedSips);
			for(List<Atom> reorderedBody: sips) {
				
				// Set up the adorned head for this rule
				AdornedAtom adornedHead = new AdornedAtom(adornment.getName(), rules[i].head.variables);
				adornedHead.setAdornment(adornment.getAdornment());
				
				// adorn rule
				AdornmentReturnType rulereturn = adornRule(program, adornedHead, reorderedBody, failedAdorn, failedSips, completed, inProgress);
				
				// update failed adornments
				failedAdorn.addAll(rulereturn.failedAdorn);
				failedAdorn.stream().distinct().collect(Collectors.toList());
				failedSips.addAll(rulereturn.failedSips);
				failedSips.stream().distinct().collect(Collectors.toList());
				
				// break on a successful adornment
				if(rulereturn.success) {
					adornedProgram.addAll(rulereturn.program);
					completed = rulereturn.completed;
					failed = false;
					break;
				}
			}
			
			// fail when all sips are exhausted
			if(failed) {
				failedAdorn.add(adornment);
				inProgress.remove(adornment);
				adornedProgram.clear();
				return new AdornmentReturnType(adornedProgram, failedAdorn, failedSips, completed);
			}
		}
		
		completed.add(adornment);
		inProgress.remove(adornment);
		
		// TODO remove duplicate rules
		return new AdornmentReturnType(adornedProgram, failedAdorn, failedSips, completed);
		
	}
	
	/**
	 * Adorn rule generates the actual adornment of a rule based on the adornment of the head predicate, the presence of a constant, or the ordering of predicates
	 * in the body of the rule (SIP). Adorned atoms that have not been (or are not currently in) the adornAtom process will have a recursive call to adornedAtom
	 * to adorn rules with this predicate in the head. Failure of an adorned rule only means that a new Sip will be tried in adornAtom call.
	 * @param program, list of unadorned rules, used for the recursive call of adorned atom
	 * @param adornedHead, adorned head atom that will influence adornment of body predicates
	 * @param reorderedBody, rule with body reorder to reflect the sip in question
	 * @param failedAdorn, list of adorned atoms that are known to have failed or are forbidden based on web service access patterns
	 * @param failedSips, list of rules with body orderings that have produced failed adornments
	 * @param completed, list of adorned atoms that have been completed
	 * @param inProgress, list of adorned atoms that do not have all rule adornments generated
	 * @return AdornmentReturnType (List<Rule> program, List<AdornAtom> failedAdorn, List<AdornAtom> failedSips, List<AdornAtom> completed)
	 * @throws Exception when an adornment of an atom is invalid (due to unequal lengths)
	 */
	public static AdornmentReturnType adornRule(List<Rule> program, AdornedAtom adornedHead, List<Atom> reorderedBody, List<AdornedAtom> failedAdorn, List<List<Atom>> failedSips, List<AdornedAtom> completed, List<AdornedAtom> inProgress) throws Exception {
		
		List<Variable> seen = new ArrayList<>(); 
		List<Atom> adornedBody = new ArrayList<>();
		List<Rule> adornedProgram = new ArrayList<>();
		
		// generate an adornment for each body atom
		AdornedAtom adornedAtom;
		for(Atom bodyAtom: reorderedBody){
			
			StringBuilder adornment = new StringBuilder();
			bodyAtom.variables.forEach(variable->{
				
				if(variable.isConstant() || seen.contains(variable) || adornedHead.isBound(variable)){
					adornment.append("b");
				}else {
					adornment.append("f");
				}
				
				if(!seen.contains(variable) &&  !variable.isConstant()) {
					seen.add(variable);
				}
			});
			
			adornedAtom = new AdornedAtom(bodyAtom.name, StringUtils.join(bodyAtom.variables, ','));
			adornedAtom.setEDB(bodyAtom.isEDB());
			adornedAtom.setAdornment(adornment.toString());
		
			// fail this adornment if known to fail or has previously failed
			if(failedAdorn.contains(adornedAtom)) {
				failedSips.add(reorderedBody);
				adornedProgram.clear();
				return new AdornmentReturnType(adornedProgram, failedAdorn, failedSips, completed);
			}
			
			adornedBody.add(adornedAtom);
			
			// if atom is and IDB predicate, has not been completed, and is not in progress,
			//  then create new adorned rules based on the adornment of this predicate.
			if(!adornedAtom.isEDB() && !adornedAtom.isContained(completed) && !adornedAtom.isContained(inProgress)) {
				
				AdornmentReturnType adornreturn = adornAtom(program, adornedAtom, failedAdorn, failedSips, completed, inProgress);
				
				failedAdorn.addAll(adornreturn.failedAdorn);
				failedAdorn.stream().distinct().collect(Collectors.toList());
				failedSips.addAll(adornreturn.failedSips);
				failedSips.stream().distinct().collect(Collectors.toList());
				
				// report failure
				if(!adornreturn.success) {
					adornedProgram.clear();
					return new AdornmentReturnType(adornedProgram, failedAdorn, failedSips, completed);
				}
				
				completed = adornreturn.completed;
				
				// update program with rules from recursive call
				adornedProgram.addAll(adornreturn.program);
			}
			
		}
		
		// generate the newly adorned rule and add it to the program
		Rule adornedRule = new Rule(adornedHead, adornedBody);
		adornedProgram.add(adornedRule);

		return new AdornmentReturnType(adornedProgram, failedAdorn, failedSips, completed);
	}
	
    public static void main( String[] args ) throws Exception
    {
        System.out.println("Adornment algorithm");
        
        // Define the example program
        Atom ud = new Atom("Udirectory", "X");
        ud.setEDB(true);
        Atom pix = new Atom("ProfInfo", "X,Y");
        pix.setEDB(true);
        Atom piz = new Atom("ProfInfo", "Z,Y");
        piz.setEDB(true);
        Atom wwx = new Atom("WorksWith", "X,Y");
        wwx.setEDB(true);
        Atom wwz = new Atom("WorksWith", "Z,Y");
        wwz.setEDB(true);
        
        Atom t1 = new Atom("T1", "X");
        Atom t1joe = new Atom("T1", "X");
        t1joe.setConstantValue(0, "Joe");
        Atom t2xy = new Atom("T2", "X,Y");
        Atom t2xz = new Atom("T2", "X,Z");
        Atom t2zy = new Atom("T2", "Z,Y");
        Atom t3xy = new Atom("T3", "X,Y");
        Atom t3xz = new Atom("T3", "X,Z");
        Atom t3zy = new Atom("T3", "Z,Y");
        Atom t3yz = new Atom("T3", "Y,Z");
        Atom t3joey = new Atom("T3", "X,Y");
        t3joey.setConstantValue(0, "Joe");
        
        Atom qy = new Atom("Q", "Y");
        Atom qz = new Atom("Q", "Z");
        
        List<Rule> P = new ArrayList<>();
        P.add(new Rule(t1joe));
        P.add(new Rule(t1, ud));
        P.add(new Rule(t2xy, t1, pix));
        P.add(new Rule(t2xy, t2xz, pix));
        P.add(new Rule(t2xy, t3xz, pix));
        P.add(new Rule(t2zy, t3xz, piz));
        P.add(new Rule(t3xy, t1, wwx));
        P.add(new Rule(t3xy, t2xz, wwx));
        P.add(new Rule(t3xy, t3xz, wwx));
        P.add(new Rule(t3zy, t3xz, wwz));
        P.add(new Rule(qy, t3joey));
        P.add(new Rule(qz, qy, t3yz));
        
        System.out.println("This is the input program: ");
        P.forEach(rule->{
        		System.out.println(rule.toString());
        });
        System.out.println("");
        
        AdornedAtom adornment = new AdornedAtom("Q", "y");
        adornment.setAdornment("f");
        
        List<AdornedAtom> failedAdorn = new ArrayList<>();
        List<List<Atom>> failedSips = new ArrayList<>();
        List<AdornedAtom> completed = new ArrayList<>();
        List<AdornedAtom> inProgress = new ArrayList<>();
        
        AdornmentReturnType result = adornAtom(P, adornment, failedAdorn, failedSips, completed, inProgress);
        
        if(!result.success) {
        		System.out.println("Failed to adorn program.");
        }else {
            System.out.println("This is the final program: ");
            result.program.forEach(System.out::println);
        }
        
    }
}
