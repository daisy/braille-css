package org.daisy.braille.css;

import java.util.ArrayList;
import java.util.HashSet;
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
		
		final static HashSet<String> PSEUDO_CLASS_DEFS = new HashSet<String>();
		static {
			PSEUDO_CLASS_DEFS.add(PseudoElement.BEFORE);
			PSEUDO_CLASS_DEFS.add(PseudoElement.AFTER);
			PSEUDO_CLASS_DEFS.add("duplicate");
		}
		
		private final String name;
		
		public PseudoElementImpl(String name) {
			if (PSEUDO_CLASS_DEFS.contains(name))
				this.name = name;
			else
				throw new IllegalArgumentException(name + " is not a valid pseudo-class name");
		}
		
		public String getName() {
			return name;
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
				.append(name)
				.append(OutputUtil.PAGE_CLOSING);
			return sb.toString();
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + name.hashCode();
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
			if (!name.equals(other.name))
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
		
		@Override
		public String getName() {
			return toString();
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
