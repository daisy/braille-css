package org.daisy.braille.css;

import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.csskit.RuleFactoryImpl;

public class BrailleCSSRuleFactory extends RuleFactoryImpl {
	
	public BrailleCSSRuleFactory() {}
	
	@Override
	public RuleMargin createMargin(String area) {
		return new RuleMarginImpl(area);
	}
}
