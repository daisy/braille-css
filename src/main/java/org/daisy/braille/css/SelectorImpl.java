package org.daisy.braille.css;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cz.vutbr.web.css.CombinedSelector.Specificity;
import cz.vutbr.web.css.CombinedSelector.Specificity.Level;
import cz.vutbr.web.css.MatchCondition;
import cz.vutbr.web.csskit.OutputUtil;

import org.w3c.dom.Element;

public class SelectorImpl extends cz.vutbr.web.csskit.SelectorImpl {
	
	@Override
	public boolean add(SelectorPart part) {
		if (part instanceof PseudoElement && size() > 0) {
			SelectorPart lastPart = get(size() - 1);
			if (lastPart instanceof PseudoElement) {
				remove(size() - 1);
				return add(new StackedPseudoElementImpl((PseudoElement)lastPart, (PseudoElement)part)); }
		}
		return super.add(part);
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
		
		private final PseudoElementDef def;
		private final List<String> args;
		
		public PseudoElementImpl(String name, String... args) {
			name = name.toLowerCase(); // Pseudo-element names are case-insensitive
			if (PSEUDO_ELEMENT_DEFS.containsKey(name))
				def = PSEUDO_ELEMENT_DEFS.get(name);
			else
				throw new IllegalArgumentException(name + " is not a valid pseudo-element name");
			if (args.length > 0 && def.maxArgs == 0)
				throw new IllegalArgumentException(name + " must not be a function");
			if (args.length == 0 && def.minArgs > 0)
				throw new IllegalArgumentException(name + " must be a function");
			this.args = new ArrayList<String>();
			if (def.minArgs > 0) {
				if (args.length < def.minArgs || args.length > def.maxArgs)
					throw new IllegalArgumentException(name + " requires " + def.minArgs
					                                   + (def.maxArgs > def.minArgs ? ".." + def.maxArgs : "") + " "
					                                   + (def.minArgs == 1 && def.maxArgs == 1 ? "argument" : "arguments"));
				for (String a : args)
					this.args.add(a);
			}
		}
		
		public String getName() {
			return def.name;
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
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb
				.append(OutputUtil.PAGE_OPENING)
				.append(OutputUtil.PAGE_OPENING)
				.append(def.name);
			if (args.size() > 0) {
				sb.append(OutputUtil.FUNCTION_OPENING);
				OutputUtil.appendList(sb, args, ", ");
				sb.append(OutputUtil.FUNCTION_CLOSING);
			}
			sb.append(OutputUtil.PAGE_CLOSING);
			return sb.toString();
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + def.hashCode();
			result = prime * result + args.hashCode();
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof PseudoElementImpl))
				return false;
			PseudoElementImpl other = (PseudoElementImpl) obj;
			if (!def.equals(other.def))
				return false;
			if (!args.equals(other.args))
				return false;
			return true;
		}
	}
	
	public static class StackedPseudoElementImpl implements PseudoElement, Iterable<PseudoElement> {
		
		private List<PseudoElement> list = new ArrayList<PseudoElement>();
		
		protected StackedPseudoElementImpl(PseudoElement element1, PseudoElement element2, PseudoElement... moreElements) {
			add(element1).add(element2).add(moreElements);
		}
		
		protected StackedPseudoElementImpl add(PseudoElement... elements) {
			for (PseudoElement e : elements)
				list.add(e);
			return this;
		}
		
		public String getName() {
			throw new UnsupportedOperationException("getName() can only be called for individual stacked pseudo-elements");
		}
		
		public String[] getArguments() {
			throw new UnsupportedOperationException("getArguments() can only be called for individual stacked pseudo-elements");
		}
		
		public boolean matches(Element e, MatchCondition cond) {
			return true;
		}

		public void computeSpecificity(Specificity spec) {
			spec.add(Level.D);
		}

		public Iterator<PseudoElement> iterator() {
			return list.iterator();
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb = OutputUtil.appendList(sb, list, "");
			return sb.toString();
		}
		
		@Override
		public int hashCode() {
			return list.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof StackedPseudoElementImpl))
				return false;
			StackedPseudoElementImpl other = (StackedPseudoElementImpl) obj;
			if (!list.equals(other.list))
				return false;
			return true;
		}
	}
}
