tree grammar BrailleCSSTreeParser;

options {
    tokenVocab=BrailleCSSLexer;
    ASTLabelType=CommonTree;
}

import CSSTreeParser;

@header {
package org.daisy.braille.css;
import java.util.Arrays;
import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.MediaQuery;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RuleList;
import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.ElementName;
import cz.vutbr.web.csskit.RuleArrayList;
import cz.vutbr.web.csskit.antlr.SimplePreparator;
}

@members {
    private Preparator preparator;
    
    public void init(Preparator preparator, List<MediaQuery> wrapMedia, RuleFactory ruleFactory) {
        gCSSTreeParser.init(preparator, wrapMedia);
        gCSSTreeParser.rf = ruleFactory;
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

// @Override
// Added volume and text_transform_def
unknown_atrule returns [RuleBlock<?> stmnt]
@init { $stmnt = null; }
    : (v=volume) { $stmnt = v; }
    | (tt=text_transform_def) { $stmnt = tt; }
    | INVALID_ATSTATEMENT { gCSSTreeParser.debug("Skipping invalid at statement"); }
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
              gCSSTreeParser.debug("Inserted volume area rule #{} into @volume", $list.size()+1);
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

text_transform_def returns [RuleTextTransform def]
    : ^( TEXT_TRANSFORM n=IDENT decl=declarations ) {
          $def = preparator.prepareRuleTextTransform(n.getText(), decl);
      }
    ;

// @Override
// Added :not() and :has()
pseudo returns [Selector.PseudoPage pseudoPage]
    : ^(PSEUDOCLASS m=MINUS? i=IDENT) {
          String name = i.getText();
          if (m != null) name = "-" + name;
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoClass(name);
          } catch (Exception e1) {
              // maybe a single colon was used for a pseudo element, or it'a custom pseudo class,
              // which we implement via a pseudo element
              try {
                  $pseudoPage = new SelectorImpl.PseudoElementImpl(":" + name);
                  gCSSTreeParser.warn(i, "Use a double colon for pseudo element ::" + name); }
              catch (Exception e2) {
                  gCSSTreeParser.error(i, "invalid pseudo declaration :" + name);
                  $pseudoPage = null;
              }
          }
      }
    | ^(PSEUDOCLASS NOT sl=selector_list) {
          $pseudoPage = new SelectorImpl.NegationPseudoClassImpl(sl);
      }
    | ^(PSEUDOCLASS HAS rsl=relative_selector_list) {
          $pseudoPage = new SelectorImpl.RelationalPseudoClassImpl(rsl);
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
              gCSSTreeParser.error(i, "invalid pseudo declaration ::" + name);
              $pseudoPage = null;
          }
      }
    | ^(PSEUDOELEM f=FUNCTION i=IDENT) {
          String func = f.getText();
          String arg = i.getText();
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoElementFunction(func, arg);
          } catch (Exception e) {
            gCSSTreeParser.error(f, "invalid pseudo declaration ::{}({})", func, arg);
          }
      }
    | ^(PSEUDOELEM f=FUNCTION m=MINUS? n=NUMBER) {
          String func = f.getText();
          String exp = n.getText();
          if (m != null) exp = "-" + exp;
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoElementFunction(func, exp);
          } catch (Exception e) {
              gCSSTreeParser.error(f, "invalid pseudo declaration ::{}({})", func, exp);
          }
      }
    | ^(PSEUDOELEM f=FUNCTION m=MINUS? n=INDEX) {
          String func = f.getText();
          String exp = n.getText();
          if (m != null) exp = "-" + exp;
          try {
              $pseudoPage = gCSSTreeParser.rf.createPseudoElementFunction(f.getText(), exp);
          } catch (Exception e) {
              gCSSTreeParser.error(f, "invalid pseudo declaration ::{}({})", func, exp);
          }
      }
    ;

/*
 * Selector list
 * (https://drafts.csswg.org/selectors-4/#selector-list), used in
 * negation pseudo-class
 * (https://drafts.csswg.org/selectors-4/#negation-pseudo)
 */
selector_list returns [List<Selector> list]
@init {
    $list = new ArrayList<Selector>();
}
    : (s=selector { list.add(s); })+
    ;

relative_selector_list returns [List<CombinedSelector> list]
@init {
    $list = new ArrayList<CombinedSelector>();
}
    : (s=relative_selector { list.add(s); })+
    ;

/*
 * Relative selector
 * (https://drafts.csswg.org/selectors-4/#relative-selector), used in
 * relational pseudo-class
 * (https://drafts.csswg.org/selectors-4/#relational)
 */
relative_selector returns [CombinedSelector combinedSelector]
@init {
    $combinedSelector = (CombinedSelector)gCSSTreeParser.rf.createCombinedSelector().unlock();
}
    : ASTERISK (c=combinator s=selector { combinedSelector.add(s.setCombinator(c)); })+
    ;

/*
 * Rule with selector relative to a certain element. An ampersand indicates that the relative
 * selector should be "chained" onto the element selector (cfr. the "parent reference" in SASS).
 *
 * Not part of inlineset because in general we don't allow these rules inside style elements. For
 * now they are only allowed in the special case when an individual style element is parsed.
 */
relative_rule returns [RuleSet rs]
@init {
    boolean attach = false;
    List<Selector> sel = new ArrayList<Selector>();
    boolean invalid = false;
}
    : ^(RULE
        ((AMPERSAND s=selector) {
            attach = true;
            // may not start with a type selector
            if (s.size() > 0 && s.get(0) instanceof ElementName) {
                invalid = true;
            }
            sel.add(s);
         }
         | (c=combinator s=selector) {
            sel.add(s.setCombinator(c));
         }
        )
        (c=combinator s=selector {
            sel.add(s.setCombinator(c));
        })*
        decl=declarations
      ) {
          if (!invalid) {
              CombinedSelector cs = (CombinedSelector)gCSSTreeParser.rf.createCombinedSelector().unlock();
              Selector first = (Selector)gCSSTreeParser.rf.createSelector().unlock();
              first.add(gCSSTreeParser.rf.createElementDOM(((SimplePreparator)preparator).elem, false)); // inlinePriority does not matter
              if (attach) {
                  first.addAll(sel.get(0));
                  sel.remove(0);
              }
              cs.add(first);
              cs.addAll(sel);
              $rs = gCSSTreeParser.rf.createSet();
              $rs.replaceAll(decl);
              $rs.setSelectors(Arrays.asList(cs));
          }
      }
    ;

/*
 * Simple list of declarations.
 */
simple_inlinestyle returns [List<Declaration> style]
    : ^(INLINESTYLE decl=declarations) {
          $style = decl;
      }
    ;

/*
 * Format allowed in style attributes that are the result of
 * "inlining" a style sheet attached to a document. Inlining is an
 * operation intended to be done by CSS processors internally, and as
 * such the resulting style attributes are not valid in an input
 * document. See the "inlinestyle" rule for what is allowed in style
 * attributes of an input document.
 */
inlinedstyle returns [RuleList rules]
@init {
    $rules = gCSSTreeParser.rules = new RuleArrayList();
}
    : ^(INLINESTYLE decl=declarations) {
          RuleBlock<?> b = preparator.prepareInlineRuleSet(decl, null);
          $rules.add(b);
      }
    | ^(INLINESTYLE (ib=inlineblock {
          // TODO: check that there is at most one block of simple
          // declarations, that all page at-rules have a different
          // pseudo-class, etc.
          if (ib != null) {
              $rules.add(ib);
          }
      })+ )
    ;

inlineblock returns [RuleBlock<?> b]
    : ^(RULE decl=declarations) {
          $b = preparator.prepareInlineRuleSet(decl, null);
      }
    | rr=relative_rule { $b = rr; }
    | tt=text_transform_def { $b = tt; }

// TODO: allowed as well but skip for now:
//  | p=page { $b = p; }

// TODO: need a slightly different format that allows @page inside @begin and @end:
//  | v=volume { $b = v; }
    ;

// TODO: move to CSSTreeParser.g
page returns [RuleBlock<?> stmnt]
@init {
    List<RuleSet> rules = null;
    List<RuleMargin> margins = null;
    String pseudo = null;
}
    : ^(PAGE
          (^(PSEUDOCLASS i=IDENT) {
              pseudo = i.getText();
          })?
          decl=declarations
          ^(SET
              (m=margin {
                  if (m != null) {
                      if (margins == null) margins = new ArrayList<RuleMargin>();
                      margins.add(m);
                  }
              })*
          )
      ) {
          $stmnt = preparator.prepareRulePage(decl, margins, null, pseudo);
      }
    ;
