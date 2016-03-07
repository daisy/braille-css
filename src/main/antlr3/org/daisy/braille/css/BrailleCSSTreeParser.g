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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
}

@members {
    private static Logger log = LoggerFactory.getLogger(BrailleCSSTreeParser.class);
    
    private Preparator preparator;
    
    public void init(Preparator preparator, List<MediaQuery> wrapMedia) {
        gCSSTreeParser.init(preparator, wrapMedia);
        this.preparator = preparator;
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
        $stmnt = preparator.prepareRuleVolume(decl, areas, pseudo, pseudoFuncArg);
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
        $area = preparator.prepareRuleVolumeArea(a.getText().substring(1), decl);
      }
    ;

pseudo returns [cz.vutbr.web.css.Selector.PseudoPage pseudoPage]
    : ^(PSEUDOCLASS m=MINUS? i=IDENT) {
          String name = i.getText();
          if (m != null) name = "-" + name;
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoClass(name);
          } catch (Exception e1) {
              // maybe a single colon was used for a pseudo element
              try {
                  $pseudoPage = gCSSTreeParser.rf.createPseudoElement(name); }
              catch (Exception e2) {
                  log.error("invalid pseudo declaration: " + name);
                  $pseudoPage = null;
              }
          }
      }
    | ^(PSEUDOCLASS NOT s=selector) {
          $pseudoPage = new SelectorImpl.NegationPseudoClassImpl(s);
      }
    | ^(PSEUDOCLASS f=FUNCTION i=IDENT) {
          $pseudoPage = gCSSTreeParser.rf.createPseudoClassFunction(f.getText(), i.getText());
      }
    | ^(PSEUDOCLASS f=FUNCTION m=MINUS? n=NUMBER) {
          String exp = n.getText();
          if (m != null) exp = "-" + exp;
              $pseudoPage = gCSSTreeParser.rf.createPseudoClassFunction(f.getText(), exp);
      }
    | ^(PSEUDOCLASS f=FUNCTION m=MINUS? n=INDEX) {
          String exp = n.getText();
          if (m != null) exp = "-" + exp;
          $pseudoPage = gCSSTreeParser.rf.createPseudoClassFunction(f.getText(), exp);
      }
    | ^(PSEUDOELEM m=MINUS? i=IDENT) {
          String name = i.getText();
          if (m != null) name = "-" + name;
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoElement(name);
          } catch (Exception e) {
              log.error("invalid pseudo declaration: " + name);
              $pseudoPage = null;
          }
      }
    | ^(PSEUDOELEM f=FUNCTION i=IDENT) {
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoElementFunction(f.getText(), i.getText());
          } catch (Exception e) {
            log.error("invalid pseudo declaration", e);
          }
      }
    | ^(PSEUDOELEM f=FUNCTION m=MINUS? n=NUMBER) {
          String exp = n.getText();
          if (m != null) exp = "-" + exp;
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoElementFunction(f.getText(), exp);
          } catch (Exception e) {
              log.error("invalid pseudo declaration", e);
          }
      }
    | ^(PSEUDOELEM f=FUNCTION m=MINUS? n=INDEX) {
          String exp = n.getText();
          if (m != null) exp = "-" + exp;
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoElementFunction(f.getText(), exp);
          } catch (Exception e) {
              log.error("invalid pseudo declaration", e);
          }
      }
    ;
