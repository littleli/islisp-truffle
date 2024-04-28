package com.github.arvyy.islisp.functions;

import com.github.arvyy.islisp.ISLISPContext;
import com.github.arvyy.islisp.Utils;
import com.github.arvyy.islisp.exceptions.ISLISPError;
import com.github.arvyy.islisp.nodes.ISLISPErrorSignalerNode;
import com.github.arvyy.islisp.runtime.*;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.RootNode;

/**
 * Implements `elt` function, that returns an element in sequence for a given index.
 */
public abstract class ISLISPElt extends RootNode {

    @Child
    ISLISPErrorSignalerNode errorSignalerNode;

    ISLISPElt(TruffleLanguage<?> language) {
        super(language);
        errorSignalerNode = new ISLISPErrorSignalerNode(this);
    }

    @Override
    public final Object execute(VirtualFrame frame) {
        if (frame.getArguments().length != 3) {
            return errorSignalerNode.signalWrongArgumentCount(frame.getArguments().length - 1, 2, 2);
        }
        return executeGeneric(frame.getArguments()[1], frame.getArguments()[2]);
    }

    abstract Object executeGeneric(Object seq, Object index);

    @Specialization
    Object doList(Pair p, int index) {
        var value = p;
        if (index < 0) {
            return errorSignalerNode.signalIndexOutOfRange(index, Utils.readList(p).size());
        }
        for (int i = 0; i < index; i++) {
            if (value.cdr() instanceof Pair pair) {
                value = pair;
                continue;
            }
            if (Utils.isNil(value.cdr())) {
                return errorSignalerNode.signalIndexOutOfRange(index,  Utils.readList(p).size());
            } else {
                var ctx = ISLISPContext.get(this);
                return errorSignalerNode.signalDomainError(
                    "Not a proper list",
                    value.cdr(),
                    ctx.lookupClass("<list>"));
            }
        }
        return value.car();
    }

    @Specialization
    Object doVector(LispVector vec, int index) {
        if (index < 0 || index >= vec.values().length) {
            return errorSignalerNode.signalIndexOutOfRange(index, vec.values().length);
        }
        return vec.values()[index];
    }

    @Specialization
    Object doString(String str, int index) {
        if (index < 0 || index >= str.length()) {
            return errorSignalerNode.signalIndexOutOfRange(index, str.length());
        }
        return new LispChar(str.codePointAt(index));
    }

    @Specialization
    Object doMutableString(LispMutableString str, int index) {
        if (index < 0 || index >= str.chars().length) {
            return errorSignalerNode.signalIndexOutOfRange(index, str.chars().length);
        }
        return str.chars()[index];
    }

    @Specialization(guards = {
        "interop.hasArrayElements(o)"
    }, limit = "3")
    Object doTruffleVector(
        Object o,
        int index,
        @CachedLibrary("o") InteropLibrary interop
    ) {
        try {
            return interop.readArrayElement(o, index);
        } catch (UnsupportedMessageException | InvalidArrayIndexException e) {
            throw new ISLISPError("Interop error", this);
        }
    }

    @Fallback
    Object fallback(Object seq, Object index) {
        throw new ISLISPError("Bad sequence or index", this);
    }

    /**
     * Construct LispFunction using this root node.
     * @param lang truffle language reference
     * @return lisp function
     */
    public static LispFunction makeLispFunction(TruffleLanguage<?> lang) {
        return new LispFunction(ISLISPEltNodeGen.create(lang).getCallTarget());
    }

}
