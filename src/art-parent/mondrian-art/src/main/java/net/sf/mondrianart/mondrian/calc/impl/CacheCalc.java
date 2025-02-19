/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2006-2006 Pentaho
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.calc.impl;

import net.sf.mondrianart.mondrian.calc.Calc;
import net.sf.mondrianart.mondrian.olap.*;

/**
 * Calculation which retrieves the value of an underlying calculation
 * from cache.
 *
 * @author jhyde
 * @since Oct 10, 2005
 */
public class CacheCalc extends GenericCalc {
    private final ExpCacheDescriptor key;

    public CacheCalc(Exp exp, ExpCacheDescriptor key) {
        super(exp);
        this.key = key;
    }

    public Object evaluate(Evaluator evaluator) {
        return evaluator.getCachedResult(key);
    }

    public Calc[] getCalcs() {
        return new Calc[] {key.getCalc()};
    }
}

// End CacheCalc.java
