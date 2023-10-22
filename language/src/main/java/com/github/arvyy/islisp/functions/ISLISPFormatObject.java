package com.github.arvyy.islisp.functions;

import com.github.arvyy.islisp.ISLISPContext;
import com.github.arvyy.islisp.Utils;
import com.github.arvyy.islisp.exceptions.ISLISPError;
import com.github.arvyy.islisp.nodes.ISLISPErrorSignalerNode;
import com.github.arvyy.islisp.runtime.LispFunction;
import com.github.arvyy.islisp.runtime.LispOutputStream;
import com.github.arvyy.islisp.runtime.Pair;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Implements `format-object` function, that writes a given object to output stream.
 */
public abstract class ISLISPFormatObject extends RootNode {

    @Child
    ISLISPErrorSignalerNode errorSignalerNode;

    ISLISPFormatObject(TruffleLanguage<?> language) {
        super(language);
        errorSignalerNode = new ISLISPErrorSignalerNode();
    }

    @Override
    public final Object execute(VirtualFrame frame) {
        return executeGeneric(frame.getArguments()[1], frame.getArguments()[2], frame.getArguments()[3]);
    }

    abstract Object executeGeneric(Object stream, Object obj, Object escape);

    @Specialization
    Object doProper(LispOutputStream stream, Object obj, Object escape) {
        var nil = ISLISPContext.get(this).getNil();
        doPrint(stream.outputStream(), obj, escape != nil);
        return nil;
    }

    @Fallback
    Object doFallback(Object stream, Object obj, Object escape) {
        return errorSignalerNode.signalWrongType(stream, ISLISPContext.get(this).lookupClass("<output-stream>"));
    }

    @CompilerDirectives.TruffleBoundary
    void doPrint(OutputStream os, Object value, boolean escape) {
        //TODO implement escape
        var writer = new OutputStreamWriter(os);
        doPrint(writer, value);
        try {
            writer.flush();
        } catch (IOException e) {
            throw new ISLISPError(e.getMessage(), this);
        }
    }

    void doPrint(Writer writer, Object value) {
        try {
            if (value instanceof String s) {
                writer.write(s);
                return;
            }
            if (value instanceof Integer i) {
                writer.write(i.toString());
                return;
            }
            if (value instanceof Pair p) {
                writer.write("(");
                var first = true;
                for (var e: Utils.readList(p)) {
                    if (!first) {
                        writer.write(" ");
                    } else {
                        first = false;
                    }
                    doPrint(writer, e);
                }
                writer.write(")");
                return;
            }
        } catch (IOException e) {
            throw new ISLISPError(e.getMessage(), this);
        }
    }

    /**
     * Construct LispFunction using this root node.
     * @param lang truffle language reference
     * @return lisp function
     */
    public static LispFunction makeLispFunction(TruffleLanguage<?> lang) {
        return new LispFunction(ISLISPFormatObjectNodeGen.create(lang).getCallTarget());
    }
}
