/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2011-2011 Pentaho
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.server;

import net.sf.mondrianart.mondrian.olap.MondrianServer;
import net.sf.mondrianart.mondrian.rolap.RolapConnection;

import java.util.Stack;

/**
 * Point of execution from which a service is invoked.
 */
public class Locus {
    public final Execution execution;
    public final String message;
    public final String component;

    private static final ThreadLocal<Stack<Locus>> THREAD_LOCAL =
        new ThreadLocal<Stack<Locus>>() {
            protected Stack<Locus> initialValue() {
                return new Stack<Locus>();
            }
        };

    /**
     * Creates a Locus.
     *
     * @param execution Execution context
     * @param component Description of a the component executing the query,
     *   generally a method name, e.g. "SqlTupleReader.readTuples"
     * @param message Description of the purpose of this statement, to be
     *   printed if there is an error
     */
    public Locus(
        Execution execution,
        String component,
        String message)
    {
        assert execution != null;
        this.execution = execution;
        this.component = component;
        this.message = message;
    }

    public static void pop(Locus locus) {
        final Locus pop = THREAD_LOCAL.get().pop();
        assert locus == pop;
    }

    public static void push(Locus locus) {
        THREAD_LOCAL.get().push(locus);
    }

    public static Locus peek() {
        return THREAD_LOCAL.get().peek();
    }

    public static <T> T execute(
        RolapConnection connection,
        String component,
        Action<T> action)
    {
        final Statement statement = connection.getInternalStatement();
        final Execution execution = new Execution(statement, 0);
        return execute(execution, component, action);
    }

    public static <T> T execute(
        Execution execution,
        String component,
        Action<T> action)
    {
        final Locus locus =
            new Locus(
                execution,
                component,
                null);
        Locus.push(locus);
        try {
            return action.execute();
        } finally {
            Locus.pop(locus);
        }
    }

    public final MondrianServer getServer() {
        return execution.statement.getMondrianConnection().getServer();
    }

    public interface Action<T> {
        T execute();
    }
}

// End Locus.java
