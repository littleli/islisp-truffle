package com.github.arvyy.islisp.functions;

import com.github.arvyy.islisp.ISLISPContext;
import com.github.arvyy.islisp.exceptions.ISLISPError;
import com.github.arvyy.islisp.runtime.LispFunction;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.profiles.CountingConditionProfile;

import java.math.BigInteger;

/**
 * Implements numeric comparator `>`.
 */
public abstract class ISLISPNumericGt extends RootNode {

    private final CountingConditionProfile profile;

    ISLISPNumericGt(TruffleLanguage<?> language) {
        super(language);
        profile = CountingConditionProfile.create();
    }

    abstract Object executeGeneric(Object a, Object b);

    @Override
    public final Object execute(VirtualFrame frame) {
        return executeGeneric((Object) frame.getArguments()[1], (Object) frame.getArguments()[2]);
    }

    @Specialization
    Object doInts(int a, int b) {
        if (profile.profile(a > b)) {
            return ISLISPContext.get(this).getT();
        }
        return ISLISPContext.get(this).getNil();
    }

    @Specialization
    @CompilerDirectives.TruffleBoundary
    Object doBigInts(BigInteger a, BigInteger b) {
        if (profile.profile(a.compareTo(b) > 0)) {
            return ISLISPContext.get(this).getT();
        }
        return ISLISPContext.get(this).getNil();
    }

    @Fallback
    Object notNumbers(Object a, Object b) {
        throw new ISLISPError("Not numbers", this);
    }

    /**
     * Construct LispFunction using this root node.
     *
     * @param lang truffle language reference
     * @return lisp function
     */
    public static LispFunction makeLispFunction(TruffleLanguage<?> lang) {
        return new LispFunction(ISLISPNumericGtNodeGen.create(lang).getCallTarget());
    }

}
