import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

import org.apache.xerces.parsers.DOMParser;

import org.daisy.braille.css.BrailleCSSDeclarationTransformer;
import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.BrailleCSSRuleFactory;
import org.daisy.braille.css.SelectorImpl.NegationPseudoClassImpl;
import org.daisy.braille.css.SupportedBrailleCSS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

public class NegationPseudoClassTest {
	
	private static final RuleFactory rf = new BrailleCSSRuleFactory();
	
	@Before
	public void init() {
		CSSFactory.registerSupportedCSS(SupportedBrailleCSS.getInstance());
		CSSFactory.registerDeclarationTransformer(new BrailleCSSDeclarationTransformer());
		CSSFactory.registerRuleFactory(rf);
	}
	
	@Test
	public void testNegationPseudoClass() throws CSSException, IOException, SAXException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(
			":not(.foo) { display: none }",
			new DefaultNetworkProcessor(), null, SourceType.EMBEDDED, new URL("file:///base/url/is/not/specified"));
		assertEquals(1, sheet.size());
		RuleSet rule = (RuleSet)sheet.get(0);
		List<CombinedSelector> cslist = new ArrayList<CombinedSelector>();
		CombinedSelector cs = (CombinedSelector)rf.createCombinedSelector().unlock();
		Selector s = (Selector)rf.createSelector().unlock();
		Selector negated = (Selector)rf.createSelector().unlock();
		negated.add(rf.createClass("foo"));
		s.add(new NegationPseudoClassImpl(negated));
		cs.add(s);
		cslist.add(cs);
		assertEquals(cslist, rule.getSelectors());
		DOMParser parser = new DOMParser();
		parser.parse(new InputSource(new ByteArrayInputStream(
			"<html><div class='foo'></div><div></div><div></div></html>"
			.getBytes(StandardCharsets.UTF_8))));
		Document doc = parser.getDocument();
		NodeList divs = doc.getElementsByTagName("div");
		assertFalse(s.matches((Element)divs.item(0)));
		assertTrue(s.matches((Element)divs.item(1)));
		assertTrue(s.matches((Element)divs.item(2)));
	}
}
