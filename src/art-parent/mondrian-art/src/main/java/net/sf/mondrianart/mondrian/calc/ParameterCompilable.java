/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2006-2006 Pentaho
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.calc;

import net.sf.mondrianart.mondrian.olap.Parameter;

/**
 * Extension to {@link net.sf.mondrianart.mondrian.olap.Parameter} which allows compilation.
 *
 * @author jhyde
 * @since Jul 22, 2006
 */
public interface ParameterCompilable extends Parameter {
    Calc compile(ExpCompiler compiler);
}

// End ParameterCompilable.java
