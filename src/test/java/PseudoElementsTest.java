import java.io.IOException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.PseudoElement;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.csskit.antlr.CSSParserFactory.SourceType;
import cz.vutbr.web.csskit.DefaultNetworkProcessor;

import org.daisy.braille.css.BrailleCSSDeclarationTransformer;
import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.BrailleCSSRuleFactory;
import org.daisy.braille.css.SelectorImpl.StackedPseudoElementImpl;
import org.daisy.braille.css.SupportedBrailleCSS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class PseudoElementsTest {
	
	private static final RuleFactory rf = new BrailleCSSRuleFactory();
	
	@Before
	public void init() {
		CSSFactory.registerSupportedCSS(SupportedBrailleCSS.getInstance());
		CSSFactory.registerDeclarationTransformer(new BrailleCSSDeclarationTransformer());
		CSSFactory.registerRuleFactory(rf);
	}
	
	@Test
	public void testStackedPseudoElements() throws CSSException, IOException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			"p::after::before { content: 'foo' }",
			new DefaultNetworkProcessor(), null, SourceType.EMBEDDED, new URL("file:///base/url/is/not/specified"));
		assertEquals(1, sheet.size());
		RuleSet rule = (RuleSet)sheet.get(0);
		List<CombinedSelector> cslist = new ArrayList<CombinedSelector>();
		CombinedSelector cs = (CombinedSelector)rf.createCombinedSelector().unlock();
		Selector s = (Selector)rf.createSelector().unlock();
		s.add(rf.createElement("p"));
		s.add(rf.createPseudoElement("after"));
		s.add(rf.createPseudoElement("before"));
		cs.add(s);
		cslist.add(cs);
		assertEquals(cslist, rule.getSelectors());
		PseudoElement p = s.getPseudoElement();
		assertTrue(p instanceof StackedPseudoElementImpl);
		assertEquals("::after::before", p.getName());
	}
}
