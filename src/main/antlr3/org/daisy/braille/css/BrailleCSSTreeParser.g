tree grammar BrailleCSSTreeParser;

options {
    tokenVocab=BrailleCSSLexer;
    ASTLabelType=CommonTree;
}

import CSSTreeParser;

@header {
package org.daisy.braille.css;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.MediaQuery;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleList;
import cz.vutbr.web.csskit.antlr.Preparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
}

@members {
    private static Logger log = LoggerFactory.getLogger(BrailleCSSTreeParser.class);
    
    public void init(Preparator preparator, List<MediaQuery> wrapMedia) {
        gCSSTreeParser.init(preparator, wrapMedia);
    }
    
    public RuleList getRules() {
        return gCSSTreeParser.getRules();
    }
    
    public List<List<MediaQuery>> getImportMedia() {
        return gCSSTreeParser.getImportMedia();
    }
    
    public List<String> getImportPaths() {
        return gCSSTreeParser.getImportPaths();
    }
    
    public RuleVolume prepareRuleVolume(List<Declaration> declarations, String pseudo, String pseudoFuncArg) {
        if (declarations == null || declarations.isEmpty()) {
            log.debug("Empty RuleVolume was ommited");
            return null; }
        RuleVolume rv = new RuleVolume(pseudo, pseudoFuncArg);
        rv.replaceAll(declarations);
        log.info("Create @volume as with:\n{}", rv);
        return rv;
    }
}

unknown_atrule returns [RuleBlock<?> stmnt]
@init { $stmnt = null; }
    : (v=volume) { $stmnt = v; }
    | INVALID_ATSTATEMENT { log.debug("Skipping invalid at statement"); }
    ;

volume returns [RuleVolume stmnt]
@init {
    String pseudo = null;
    String pseudoFuncArg = null;
}
    : ^(VOLUME
        ( ^(PSEUDO i=IDENT)
           { pseudo = i.getText(); }
        | ^(PSEUDO f=FUNCTION n=NUMBER)
           { pseudo = f.getText();
             pseudoFuncArg = n.getText(); }
        | ^(PSEUDO f=FUNCTION n=INDEX)
           { pseudo = f.getText();
             pseudoFuncArg = n.getText(); }
        )?
        decl=declarations
      )
      {
        $stmnt = prepareRuleVolume(decl, pseudo, pseudoFuncArg);
      }
    ;
