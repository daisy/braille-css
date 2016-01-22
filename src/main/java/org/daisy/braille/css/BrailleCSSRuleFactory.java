package org.daisy.braille.css;

import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.PseudoElement;
import cz.vutbr.web.csskit.RuleFactoryImpl;

public class BrailleCSSRuleFactory extends RuleFactoryImpl {
	
	public BrailleCSSRuleFactory() {}
	
	@Override
	public RuleMargin createMargin(String area) {
		return new RuleMarginImpl(area);
	}
	
	@Override
	public Selector createSelector() {
		return new SelectorImpl();
	}
	
	public PseudoElement createPseudoElement(String name) {
		return new SelectorImpl.PseudoElementImpl(name);
	}
}
