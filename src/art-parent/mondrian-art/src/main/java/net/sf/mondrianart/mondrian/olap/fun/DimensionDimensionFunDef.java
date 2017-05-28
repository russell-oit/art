/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2009-2009 Pentaho and others
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.olap.fun;

import net.sf.mondrianart.mondrian.calc.Calc;
import net.sf.mondrianart.mondrian.calc.ExpCompiler;
import net.sf.mondrianart.mondrian.calc.impl.ConstantCalc;
import net.sf.mondrianart.mondrian.mdx.DimensionExpr;
import net.sf.mondrianart.mondrian.mdx.ResolvedFunCall;
import net.sf.mondrianart.mondrian.olap.Dimension;
import net.sf.mondrianart.mondrian.olap.type.DimensionType;

/**
 * Definition of the <code>&lt;Dimension&gt;.Dimension</code>
 * MDX builtin function.
 *
 * @author jhyde
 * @since Jul 20, 2009
 */
class DimensionDimensionFunDef extends FunDefBase {
    public static final FunDefBase INSTANCE = new DimensionDimensionFunDef();

    private DimensionDimensionFunDef() {
        super(
            "Dimension",
            "Returns the dimension that contains a specified hierarchy.",
            "pdd");
    }

    public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler)
    {
        Dimension dimension =
            ((DimensionExpr) call.getArg(0)).getDimension();
        return new ConstantCalc(
            DimensionType.forDimension(dimension),
            dimension);
    }
}

// End DimensionDimensionFunDef.java
