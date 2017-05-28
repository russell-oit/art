/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2008-2009 Pentaho
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.calc.impl;

import net.sf.mondrianart.mondrian.calc.Calc;
import net.sf.mondrianart.mondrian.calc.DateTimeCalc;
import net.sf.mondrianart.mondrian.olap.Evaluator;
import net.sf.mondrianart.mondrian.olap.Exp;

/**
 * Abstract implementation of the {@link net.sf.mondrianart.mondrian.calc.DateTimeCalc} interface.
 *
 * <p>The derived class must
 * implement the {@link #evaluateDateTime(mondrian.olap.Evaluator)} method,
 * and the {@link #evaluate(mondrian.olap.Evaluator)} method will call it.
 *
 * @author jhyde
 * @since Sep 26, 2005
 */
public abstract class AbstractDateTimeCalc
    extends AbstractCalc
    implements DateTimeCalc
{
    /**
     * Creates an AbstractDateTimeCalc.
     *
     * @param exp Source expression
     * @param calcs Child compiled expressions
     */
    protected AbstractDateTimeCalc(Exp exp, Calc[] calcs) {
        super(exp, calcs);
    }

    public Object evaluate(Evaluator evaluator) {
        return evaluateDateTime(evaluator);
    }
}

// End AbstractDateTimeCalc.java
