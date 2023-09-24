package com.github.arvyy.islisp.nodes;

import com.github.arvyy.islisp.exceptions.ISLISPTagbodyGoException;
import com.github.arvyy.islisp.runtime.Value;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;

public class ISLISPTagBodyGoNode extends ISLISPExpressionNode {

    private final int tagId;

    public ISLISPTagBodyGoNode(int tagId, SourceSection sourceSection) {
        super(sourceSection);
        this.tagId = tagId;
    }

    @Override
    public Value executeGeneric(VirtualFrame frame) {
        throw new ISLISPTagbodyGoException(tagId);
    }
}