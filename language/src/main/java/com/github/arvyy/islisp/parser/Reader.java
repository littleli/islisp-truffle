package com.github.arvyy.islisp.parser;

import com.github.arvyy.islisp.ISLISPContext;
import com.github.arvyy.islisp.runtime.LispInteger;
import com.github.arvyy.islisp.runtime.Pair;
import com.github.arvyy.islisp.runtime.Symbol;
import com.github.arvyy.islisp.runtime.Value;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Reader {

    private final Lexer lexer;
    private final Source source;

    public Reader(Source source) {
        this.source = source;
        lexer = new ISLISPLexer(source.getReader());
    }

    SourceSection section() {
        return source.createSection(lexer.getLine(), lexer.getColumn(), lexer.getLength());
    }

    public List<Value> readAll() {
        var lst = new ArrayList<Value>();
        Optional<Value> maybeValue = readSingle();
        while (maybeValue.isPresent()) {
            lst.add(maybeValue.get());
            maybeValue = readSingle();
        }
        return lst;
    }

    public Optional<Value> readSingle() {
        Optional<Token> maybeT = lexer.getToken();
        if (maybeT.isEmpty())
            return Optional.empty();
        var t = maybeT.get();
        if (t instanceof Token.IdentifierToken) {
            var identifier = ((Token.IdentifierToken) t).identifier();
            var symbol = ISLISPContext.get(null).namedSymbol(identifier);
            var symbolWithSource = new Symbol(symbol.name(), symbol.identityReference(), section());
            return Optional.of(symbolWithSource);
        }
        if (t instanceof Token.ExactNumberToken) {
            var value = ((Token.ExactNumberToken) t).value();
            var lispInt = new LispInteger(value, section());
            return Optional.of(lispInt);
        }
        /*
        if (t instanceof Token.VectorBracketOpenToken) {
            Optional<Token> next;
            var lst = new ArrayList<Value>();
            while (true) {
                next = lexer.peekToken();
                if (next.isEmpty())
                    throw new RuntimeException("Premature end of file");
                var token = next.get();
                if (token instanceof Token.BracketCloseToken) {
                    lexer.getToken();
                    return Optional.of(new Value.ImmutableVector(lst));
                }
                lst.add(readSingle().orElseThrow()); //TODO
            }
        }
         */
        if (t instanceof Token.BracketOpenToken) {
            var startLine = lexer.getLine();
            var startColumn = lexer.getColumn();
            Optional<Token> next;
            var lst = new ArrayList<Value>();
            while (true) {
                next = lexer.peekToken();
                if (next.isEmpty())
                    throw new RuntimeException("Premature end of file");
                var token = next.get();
                if (token instanceof Token.BracketCloseToken) {
                    lexer.getToken();
                    var endLine = lexer.getLine();
                    var endColumn = lexer.getColumn();
                    var section = source.createSection(startLine, startColumn, endLine, endColumn);
                    if (lst.isEmpty()) {
                        var nil = ISLISPContext.get(null).getNIL();
                        var nilWithPos = new Symbol(nil.name(), nil.identityReference(), section);
                        return Optional.of(nilWithPos);
                    } else {
                        Value tail = ISLISPContext.get(null).getNIL();
                        for (var i = lst.size() - 1; i >= 0; i--) {
                            tail = new Pair(lst.get(i), tail, null);
                        }
                        var parsedTail = (Pair) tail;
                        var finalList = new Pair(parsedTail.car(), parsedTail.cdr(), section);
                        return Optional.of(finalList);
                    }
                }
                lst.add(readSingle().orElseThrow()); //TODO
            }
        }
        return Optional.empty();
    }
}
