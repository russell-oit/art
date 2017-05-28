/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2006-2007 Pentaho
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.calc;

import net.sf.mondrianart.mondrian.olap.Evaluator;
import net.sf.mondrianart.mondrian.olap.Member;

/**
 * Expression which yields a {@link Member}.
 *
 * <p>When implementing this interface, it is convenient to extend
 * {@link net.sf.mondrianart.mondrian.calc.impl.AbstractMemberCalc}, but it is not required.

 * @author jhyde
 * @since Sep 26, 2005
 */
public interface MemberCalc extends Calc {
    /**
     * Evaluates this expression to yield a member.
     *
     * <p>May return the null member (see
     * {@link net.sf.mondrianart.mondrian.olap.Hierarchy#getNullMember()}) but never null.
     *
     * @param evaluator Evaluation context
     * @return a member
     */
    Member evaluateMember(Evaluator evaluator);
}

// End MemberCalc.java
