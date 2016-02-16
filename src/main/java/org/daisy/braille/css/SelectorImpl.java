package org.daisy.braille.css;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.vutbr.web.css.CombinedSelector.Specificity;
import cz.vutbr.web.css.CombinedSelector.Specificity.Level;
import cz.vutbr.web.css.MatchCondition;
import cz.vutbr.web.csskit.OutputUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Element;

public class SelectorImpl extends cz.vutbr.web.csskit.SelectorImpl {
	
	@Override
	public boolean add(SelectorPart part) {
		if (part instanceof PseudoElement) {
			if (!(part instanceof PseudoElementImpl))
				throw new RuntimeException();
			if (size() > 0) {
				SelectorPart lastPart = get(size() - 1);
				if (lastPart instanceof PseudoElement) {
					if (!(lastPart instanceof PseudoElementImpl))
						throw new RuntimeException();
					return ((PseudoElementImpl)lastPart).add((PseudoElementImpl)part);
				}
			}
		} else if (part instanceof PseudoClass) {
			if (!(part instanceof PseudoClassImpl))
				throw new RuntimeException();
			if (size() > 0) {
				SelectorPart lastPart = get(size() - 1);
				if (lastPart instanceof PseudoElement) {
					if (!(lastPart instanceof PseudoElementImpl))
						throw new RuntimeException();
					return ((PseudoElementImpl)lastPart).add((PseudoClassImpl)part);
				}
			}
		}
		return super.add(part);
	}
	
	public static class PseudoClassImpl extends cz.vutbr.web.csskit.SelectorImpl.PseudoClassImpl {
		
		private final String name;
		private final String[] args;
		
		public PseudoClassImpl(String name, String... args) {
			super(name, args);
			this.name = name;
			this.args = args;
		}
		
		public boolean matchesPosition(int position, int elementCount) {
			if (name.equals("first-child")) {
				return position == 1;
			} else if (name.equals("last-child")) {
				return position == elementCount;
			} else if (name.equals("only-child")) {
				return position == 1 && elementCount == 1;
			} else if (name.equals("nth-child")) {
				return positionMatches(position, decodeIndex(args[0]));
			} else if (name.equals("nth-last-child")) {
				return positionMatches(elementCount - position + 1, decodeIndex(args[0]));
			} else {
				log.warn("Don't know how to match " + toString() + " pseudo-class");
				return false;
			}
		}
	}
	
	public static class PseudoElementImpl implements PseudoElement {
		
		private static enum PseudoElementDef {
			BEFORE("before"),
			AFTER("after"),
			DUPLICATE("duplicate"),
			LIST_ITEM("list-item"),
			TABLE_BY("table-by", 1);
			
			private final String name;
			private final int minArgs;
			private final int maxArgs;
			
			private PseudoElementDef(String name) {
				this.name = name;
				this.minArgs = 0;
				this.maxArgs = 0;
			}
			
			private PseudoElementDef(String name, int args) {
				this(name, args, args);
			}
			
			private PseudoElementDef(String name, int minArgs, int maxArgs) {
				this.name = name;
				this.minArgs = minArgs;
				this.maxArgs = maxArgs;
			}
		}
		
		private final static HashMap<String,PseudoElementDef> PSEUDO_ELEMENT_DEFS;
		static {
			PSEUDO_ELEMENT_DEFS = new HashMap<String,PseudoElementDef>();
			for (PseudoElementDef d : PseudoElementDef.values())
				PSEUDO_ELEMENT_DEFS.put(d.name, d);
		}
		
		private final String name;
		private final List<String> args;
		private final List<PseudoClassImpl> pseudoClasses = new ArrayList<PseudoClassImpl>();
		private PseudoElementImpl stackedPseudoElement = null;
		
		public PseudoElementImpl(String name, String... args) {
			this.name = name = name.toLowerCase(); // Pseudo-element names are case-insensitive
			this.args = new ArrayList<String>();
			if (name.startsWith("-"))
				for (String a : args)
					this.args.add(a);
			else {
				PseudoElementDef def;
				if (PSEUDO_ELEMENT_DEFS.containsKey(name))
					def = PSEUDO_ELEMENT_DEFS.get(name);
				else
					throw new IllegalArgumentException(name + " is not a valid pseudo-element name");
				if (args.length > 0 && def.maxArgs == 0)
					throw new IllegalArgumentException(name + " must not be a function");
				if (args.length == 0 && def.minArgs > 0)
					throw new IllegalArgumentException(name + " must be a function");
				if (def.minArgs > 0) {
					if (args.length < def.minArgs || args.length > def.maxArgs)
						throw new IllegalArgumentException(name + " requires " + def.minArgs
						                                   + (def.maxArgs > def.minArgs ? ".." + def.maxArgs : "") + " "
						                                   + (def.minArgs == 1 && def.maxArgs == 1 ? "argument" : "arguments"));
					for (String a : args)
						this.args.add(a);
				}
			}
		}
		
		public String getName() {
			return name;
		}
		
		public String[] getArguments() {
			return args.toArray(new String[args.size()]);
		}
		
		public void computeSpecificity(Specificity spec) {
			spec.add(Level.D);
		}
		
		public boolean matches(Element e, MatchCondition cond) {
			return true;
		}
		
		private boolean add(PseudoClassImpl pseudoClass) {
			if (stackedPseudoElement != null)
				return stackedPseudoElement.add(pseudoClass);
			else
				return pseudoClasses.add(pseudoClass);
		}
		
		private boolean add(PseudoElementImpl pseudoElement) {
			if (stackedPseudoElement != null)
				return stackedPseudoElement.add(pseudoElement);
			else {
				stackedPseudoElement = pseudoElement;
				return true;
			 }
		}
		
		public List<PseudoClassImpl> getPseudoClasses() {
			return pseudoClasses;
		}
		
		public boolean hasStackedPseudoElement() {
			return stackedPseudoElement != null;
		}
		
		public PseudoElementImpl getStackedPseudoElement() {
			return stackedPseudoElement;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb
				.append(OutputUtil.PAGE_OPENING)
				.append(OutputUtil.PAGE_OPENING)
				.append(name);
			if (args.size() > 0) {
				sb.append(OutputUtil.FUNCTION_OPENING);
				OutputUtil.appendList(sb, args, ", ");
				sb.append(OutputUtil.FUNCTION_CLOSING);
			}
			if (!pseudoClasses.isEmpty())
				for (PseudoClassImpl p : pseudoClasses)
					sb.append(p);
			if (stackedPseudoElement != null)
				sb.append(stackedPseudoElement);
			return sb.toString();
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + name.hashCode();
			result = prime * result + args.hashCode();
			result = prime * result + pseudoClasses.hashCode();
			result = prime * result
					+ ((stackedPseudoElement == null) ? 0
							: stackedPseudoElement.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PseudoElementImpl other = (PseudoElementImpl) obj;
			if (!name.equals(other.name))
				return false;
			if (!args.equals(other.args))
				return false;
			if (!pseudoClasses.equals(other.pseudoClasses))
				return false;
			if (stackedPseudoElement == null) {
				if (other.stackedPseudoElement != null)
					return false;
			} else if (!stackedPseudoElement.equals(other.stackedPseudoElement))
				return false;
			return true;
		}
	}
	
	private static final Logger log = LoggerFactory.getLogger(SelectorImpl.class);
	
}
