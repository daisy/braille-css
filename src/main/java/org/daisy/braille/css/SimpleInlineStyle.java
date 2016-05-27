package org.daisy.braille.css;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.domassign.DeclarationTransformer;
import cz.vutbr.web.domassign.SingleMapNodeData;

public class SimpleInlineStyle extends SingleMapNodeData implements NodeData, Cloneable {
	
	private final static SupportedCSS cssInstance = SupportedBrailleCSS.getInstance();
	private static BrailleCSSDeclarationTransformer transformerInstance; static {
		// SupportedCSS injected via CSSFactory in DeclarationTransformer.<init>
		CSSFactory.registerSupportedCSS(cssInstance);
		transformerInstance = new BrailleCSSDeclarationTransformer(); }
	private final static BrailleCSSParserFactory parserFactory = new BrailleCSSParserFactory();
	
	public SimpleInlineStyle(String style) {
		super();
		transformer = transformerInstance;
		css = cssInstance;
		for (Declaration d : parserFactory.parseSimpleInlineStyle(style)) {
			// SupportedCSS injected via CSSFactory in Repeater.assignDefaults, Variator.assignDefaults
			CSSFactory.registerSupportedCSS(css);
			push(d); }
	}
	
	public SimpleInlineStyle(List<Declaration> declarations) {
		super();
		transformer = transformerInstance;
		css = cssInstance;
		for (Declaration d : declarations) {
			// SupportedCSS injected via CSSFactory in Repeater.assignDefaults, Variator.assignDefaults
			CSSFactory.registerSupportedCSS(css);
			push(d); }
	}
	
	public void removeProperty(String name) {
		map.remove(name);
	}
	
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	public int size() {
		return map.size();
	}
	
	@Override
	public Object clone() {
		
		// FIXME: instead of serializing and parsing again, clone the map field directly
		return new SimpleInlineStyle(toString());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		List<String> keys = new ArrayList<String>(map.keySet());
		Collections.sort(keys);
		for (String key: keys) {
			if (sb.length() > 0)
				sb.append("; ");
			sb.append(key).append(": ");
			Term<?> value = getValue(key, false);
			if (value != null)
				sb.append(value);
			else
				sb.append(getProperty(key)); }
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof SimpleInlineStyle)
			return map.equals(((SimpleInlineStyle)other).map);
		return false;
	}
}
