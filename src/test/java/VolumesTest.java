import java.io.IOException;

import java.net.URL;

import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.csskit.antlr.CSSParserFactory.SourceType;
import cz.vutbr.web.csskit.DefaultNetworkProcessor;

import org.daisy.braille.css.BrailleCSSDeclarationTransformer;
import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.braille.css.RuleVolume;
import org.daisy.braille.css.SupportedBrailleCSS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class VolumesTest {
	
	public static final String SIMPLE_VOLUME_RULE = "@volume { max-length: 100; }";
	
	@Before
	public void init() {
		CSSFactory.registerSupportedCSS(SupportedBrailleCSS.getInstance());
		CSSFactory.registerDeclarationTransformer(new BrailleCSSDeclarationTransformer());
	}
	
	@Test
	public void testSimpleVolumeRule() throws IOException, CSSException {
		StyleSheet sheet = new BrailleCSSParserFactory().parse(SIMPLE_VOLUME_RULE,
			new DefaultNetworkProcessor(), null, SourceType.EMBEDDED, new URL("file:///base/url/is/not/specified"));
		assertEquals(1, sheet.size());
		Rule<?> rule = sheet.get(0);
		assertTrue(rule instanceof RuleVolume);
		RuleVolume volumeRule = (RuleVolume)rule;
		assertEquals(1, volumeRule.size());
		Declaration decl = volumeRule.get(0);
		assertEquals("max-length", decl.getProperty());
	}
}
