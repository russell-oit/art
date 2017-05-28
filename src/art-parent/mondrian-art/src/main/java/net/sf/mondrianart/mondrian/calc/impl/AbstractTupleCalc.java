/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2006-2009 Pentaho
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.calc.impl;

import net.sf.mondrianart.mondrian.calc.Calc;
import net.sf.mondrianart.mondrian.calc.TupleCalc;
import net.sf.mondrianart.mondrian.olap.Evaluator;
import net.sf.mondrianart.mondrian.olap.Exp;

/**
 * Abstract implementation of the {@link net.sf.mondrianart.mondrian.calc.TupleCalc} interface.
 *
 * <p>The derived class must
 * implement the {@link #evaluateTuple(mondrian.olap.Evaluator)} method,
 * and the {@link #evaluate(mondrian.olap.Evaluator)} method will call it.
 *
 * @author jhyde
 * @since Sep 27, 2005
 */
public abstract class AbstractTupleCalc
    extends AbstractCalc
    implements TupleCalc
{
    /**
     * Creates an AbstractTupleCalc.
     *
     * @param exp Source expression
     * @param calcs Child compiled expressions
     */
    protected AbstractTupleCalc(Exp exp, Calc[] calcs) {
        super(exp, calcs);
    }

    public Object evaluate(Evaluator evaluator) {
        return evaluateTuple(evaluator);
    }
}

// End AbstractTupleCalc.java
