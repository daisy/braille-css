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
    
    public RuleVolume prepareRuleVolume(List<Declaration> declarations,
                                        List<RuleVolumeArea> volumeAreas,
                                        String pseudo,
                                        String pseudoFuncArg) {
        if ((declarations == null || declarations.isEmpty()) &&
	         (volumeAreas == null || volumeAreas.isEmpty())) {
            log.debug("Empty RuleVolume was omited");
            return null; }
        RuleVolume rv = new RuleVolume(pseudo, pseudoFuncArg);
        if (declarations != null)
            for (Declaration d : declarations)
                rv.add(d);
        if (volumeAreas != null)
            for (RuleVolumeArea a : volumeAreas)
                rv.add(a);
        log.info("Create @volume as with:\n{}", rv);
        return rv;
    }
    
    public RuleVolumeArea prepareRuleVolumeArea(String area, List<Declaration> declarations) {
        if ((declarations == null || declarations.isEmpty())) {
            log.debug("Empty RuleVolumeArea was omited");
            return null; }
        RuleVolumeArea rva = new RuleVolumeArea(area);
        rva.replaceAll(declarations);
        log.info("Create @" + area + " with:\n{}", rva);
        return rva;
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
        ( ^(PSEUDOCLASS i=IDENT)
           { pseudo = i.getText(); }
        | ^(PSEUDOCLASS f=FUNCTION n=NUMBER)
           { pseudo = f.getText();
             pseudoFuncArg = n.getText(); }
        )?
        decl=declarations
        areas=volume_areas
      )
      {
        $stmnt = prepareRuleVolume(decl, areas, pseudo, pseudoFuncArg);
      }
    ;

volume_areas returns [List<RuleVolumeArea> list]
@init {
    $list = new ArrayList<RuleVolumeArea>();
}
    : ^(SET
        ( a=volume_area {
            if (a!=null) {
              list.add(a);
              log.debug("Inserted volume area rule #{} into @volume", $list.size()+1);
            }
          }
        )*
      )
    ;

volume_area returns [RuleVolumeArea area]
    : ^( a=VOLUME_AREA
         decl=declarations )
      {
        $area = prepareRuleVolumeArea(a.getText().substring(1), decl);
      }
    ;
