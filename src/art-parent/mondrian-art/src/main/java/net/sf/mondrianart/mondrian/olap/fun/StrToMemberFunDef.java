/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2010-2010 Pentaho
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.olap.fun;

import net.sf.mondrianart.mondrian.calc.*;
import net.sf.mondrianart.mondrian.calc.impl.AbstractMemberCalc;
import net.sf.mondrianart.mondrian.mdx.ResolvedFunCall;
import net.sf.mondrianart.mondrian.olap.*;
import net.sf.mondrianart.mondrian.resource.MondrianResource;

/**
 * Definition of the <code>StrToMember</code> MDX function.
 *
 * <p>Syntax:
 * <blockquote><code>StrToMember(&lt;String Expression&gt;)
 * </code></blockquote>
 */
class StrToMemberFunDef extends FunDefBase {
    public static final FunDef INSTANCE = new StrToMemberFunDef();

    private StrToMemberFunDef() {
        super(
            "StrToMember",
            "Returns a member from a unique name String in MDX format.",
            "fmS");
    }

    public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final StringCalc memberNameCalc =
            compiler.compileString(call.getArg(0));
        return new AbstractMemberCalc(call, new Calc[] {memberNameCalc}) {
            public Member evaluateMember(Evaluator evaluator) {
                String memberName =
                    memberNameCalc.evaluateString(evaluator);
                if (memberName == null) {
                    throw newEvalException(
                        MondrianResource.instance().NullValue.ex());
                }
                return parseMember(evaluator, memberName, null);
            }
        };
    }
}

// End StrToMemberFunDef.java
