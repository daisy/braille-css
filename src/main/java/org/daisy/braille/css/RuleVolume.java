package org.daisy.braille.css;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.PrettyOutput;
import cz.vutbr.web.csskit.AbstractRuleBlock;
import cz.vutbr.web.csskit.OutputUtil;

public class RuleVolume extends AbstractRuleBlock<Declaration> implements PrettyOutput {
	
	private static final Set<String> validPseudoIdents = new HashSet<String>(Arrays.asList("first", "last"));
	private static final Set<String> validPseudoFuncNames = new HashSet<String>(Arrays.asList("nth", "nth-last"));
	
	protected String pseudo = null;
	
	protected RuleVolume(String pseudo, String pseudoFuncArg) {
		super();
		if (pseudo != null) {
			pseudo = pseudo.toLowerCase();
			if (validPseudoIdents.contains(pseudo))
				this.pseudo = pseudo;
			else if (validPseudoFuncNames.contains(pseudo) && pseudoFuncArg != null)
				this.pseudo = pseudo + "(" + pseudoFuncArg + ")";
		}
	}
	
	public String getPseudo() {
		return pseudo;
	}
	
	public String toString(int depth) {
		StringBuilder sb = new StringBuilder();
		sb.append("@volume");
		if (pseudo != null)
			sb.append(":").append(pseudo);
		sb.append(" ");
		sb.append(OutputUtil.RULE_OPENING);
		sb = OutputUtil.appendList(sb, list, OutputUtil.EMPTY_DELIM, depth + 1);
		sb.append(OutputUtil.RULE_CLOSING);
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return this.toString(0);
	}
}
