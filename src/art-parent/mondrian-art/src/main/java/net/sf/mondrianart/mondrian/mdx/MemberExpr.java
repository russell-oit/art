/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2006-2010 Pentaho
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.mdx;

import net.sf.mondrianart.mondrian.calc.Calc;
import net.sf.mondrianart.mondrian.calc.ExpCompiler;
import net.sf.mondrianart.mondrian.calc.impl.ConstantCalc;
import net.sf.mondrianart.mondrian.olap.*;
import net.sf.mondrianart.mondrian.olap.type.MemberType;
import net.sf.mondrianart.mondrian.olap.type.Type;

/**
 * Usage of a {@link net.sf.mondrianart.mondrian.olap.Member} as an MDX expression.
 *
 * @author jhyde
 * @since Sep 26, 2005
 */
public class MemberExpr extends ExpBase implements Exp {
    private final Member member;
    private MemberType type;

    /**
     * Creates a member expression.
     *
     * @param member Member
     * @pre member != null
     */
    public MemberExpr(Member member) {
        Util.assertPrecondition(member != null, "member != null");
        this.member = member;
    }

    /**
     * Returns the member.
     *
     * @post return != null
     */
    public Member getMember() {
        return member;
    }

    public String toString() {
        return member.getUniqueName();
    }

    public Type getType() {
        if (type == null) {
            type = MemberType.forMember(member);
        }
        return type;
    }

    public MemberExpr clone() {
        return new MemberExpr(member);
    }

    public int getCategory() {
        return Category.Member;
    }

    public Exp accept(Validator validator) {
        return this;
    }

    public Calc accept(ExpCompiler compiler) {
        return ConstantCalc.constantMember(member);
    }

    public Object accept(MdxVisitor visitor) {
        return visitor.visit(this);
    }
}

// End MemberExpr.java
