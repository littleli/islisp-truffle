package com.github.arvyy.islisp.nodes;

import com.github.arvyy.islisp.ISLISPContext;
import com.github.arvyy.islisp.runtime.DynamicVar;
import com.github.arvyy.islisp.runtime.Symbol;
import com.github.arvyy.islisp.runtime.Value;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.source.SourceSection;

public class ISLISPDynamicLetNode extends ISLISPExpressionNode {

    @CompilerDirectives.CompilationFinal
    private DynamicVar vars[];
    private final Symbol symbols[];

    @Children
    ISLISPExpressionNode initializers[];

    @Children
    ISLISPExpressionNode body[];

    public ISLISPDynamicLetNode(Symbol symbols[], ISLISPExpressionNode initializers[], ISLISPExpressionNode body[], SourceSection sourceSection) {
        super(sourceSection);
        this.symbols = symbols;
        this.initializers = initializers;
        this.body = body;
    }

    @Override
    @ExplodeLoop
    public Value executeGeneric(VirtualFrame frame) {
        var ctx = ISLISPContext.get(this);
        if (vars == null) {
            vars = new DynamicVar[symbols.length];
            for (int i = 0; i < vars.length; i++) {
                var existing = ctx.lookupDynamicVar(symbols[i].identityReference());
                if (existing == null) {
                    vars[i] = new DynamicVar();
                    vars[i].setValue(null);
                    ctx.registerDynamicVar(symbols[i].identityReference(), vars[i]);
                } else {
                    vars[i] = existing;
                }
            }
        }
        var oldValues = new Value[vars.length];
        var values = new Value[vars.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = initializers[i].executeGeneric(frame);
            oldValues[i] = vars[i].getValue();
        }
        for (int i = 0; i < vars.length; i++) {
            vars[i].setValue(values[i]);
        }
        try {
            if (body.length == 0)
                return ctx.getNIL();
            for (int i = 0; i < body.length - 1; i++) {
                body[i].executeGeneric(frame);
            }
            return body[body.length - 1].executeGeneric(frame);
        } finally {
            for (int i = 0; i < vars.length; i++) {
                vars[i].setValue(oldValues[i]);
            }
        }
    }
}