parser grammar BrailleCSSParser;

options {
    output = AST;
    tokenVocab=BrailleCSSLexer;
    k = 2;
}

import CSSParser;

@header {package org.daisy.braille.css;}

@members {
    public void init() {
        gCSSParser.init();
    }
}

unknown_atrule
    : volume
    | ATKEYWORD S* LCURLY any* RCURLY -> INVALID_ATSTATEMENT
    ;

volume
    : VOLUME S* (volume_pseudo S*) ? LCURLY S* declarations RCURLY
      -> ^(VOLUME volume_pseudo? declarations)
    ;

volume_pseudo
    : pseudocolon^ ( IDENT | FUNCTION S!* ( NUMBER | INDEX ) S!* RPAREN! )
    ;
