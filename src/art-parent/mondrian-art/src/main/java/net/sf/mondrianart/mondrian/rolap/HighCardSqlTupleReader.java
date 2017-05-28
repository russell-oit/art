/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// You must accept the terms of that agreement to use this software.
//
// Copyright (C) 2004-2005 TONBELLER AG
// Copyright (C) 2005-2011 Pentaho and others
// All Rights Reserved.
*/
package net.sf.mondrianart.mondrian.rolap;

import net.sf.mondrianart.mondrian.calc.TupleList;
import net.sf.mondrianart.mondrian.calc.impl.DelegatingTupleList;
import net.sf.mondrianart.mondrian.calc.impl.UnaryTupleList;
import net.sf.mondrianart.mondrian.olap.*;
import net.sf.mondrianart.mondrian.olap.fun.FunUtil;
import net.sf.mondrianart.mondrian.rolap.sql.TupleConstraint;
import net.sf.mondrianart.mondrian.server.Locus;
import net.sf.mondrianart.mondrian.server.monitor.SqlStatementEvent;
import net.sf.mondrianart.mondrian.util.Pair;
import net.sf.mondrianart.mondrian.util.TraversalList;

import java.sql.SQLException;
import java.util.*;
import javax.sql.DataSource;

/**
 * Reads the members of a single level (level.members) or of multiple levels
 * (crossjoin).
 *
 * @author luis f. canals
 * @since Dec, 2007
 */
public class HighCardSqlTupleReader extends SqlTupleReader {
    private ResultLoader resultLoader;
    private boolean moreRows;

    int maxRows = 0;

    public HighCardSqlTupleReader(final TupleConstraint constraint) {
        super(constraint);
    }

    public void addLevelMembers(
        final RolapLevel level,
        final MemberBuilder memberBuilder,
        final List<RolapMember> srcMembers)
    {
        targets.add(new Target(
            level, memberBuilder, srcMembers, constraint, this));
    }

    protected void prepareTuples(
        final DataSource dataSource,
        final TupleList partialResult,
        final List<List<RolapMember>> newPartialResult)
    {
        String message = "Populating member cache with members for " + targets;
        SqlStatement stmt = null;
        boolean execQuery = (partialResult == null);
        try {
            if (execQuery) {
                // we're only reading tuples from the targets that are
                // non-enum targets
                List<TargetBase> partialTargets = new ArrayList<TargetBase>();
                for (TargetBase target : targets) {
                    if (target.getSrcMembers() == null) {
                        partialTargets.add(target);
                    }
                }
                final Pair<String, List<SqlStatement.Type>> pair =
                    makeLevelMembersSql(dataSource);
                String sql = pair.left;
                List<SqlStatement.Type> types = pair.right;
                stmt = RolapUtil.executeQuery(
                    dataSource, sql, types, maxRows, 0,
                    new SqlStatement.StatementLocus(
                        Locus.peek().execution,
                        "HighCardSqlTupleReader.readTuples " + partialTargets,
                        message,
                        SqlStatementEvent.Purpose.TUPLES, 0),
                    -1, -1);
            }

            for (TargetBase target : targets) {
                target.open();
            }

            // determine how many enum targets we have
            int enumTargetCount = getEnumTargetCount();

            int currPartialResultIdx = 0;
            if (execQuery) {
                this.moreRows = stmt.getResultSet().next();
                if (this.moreRows) {
                    ++stmt.rowCount;
                }
            } else {
                this.moreRows = currPartialResultIdx < partialResult.size();
            }

            this.resultLoader =
                new ResultLoader(
                    enumTargetCount,
                    targets, stmt, execQuery, partialResult,
                    newPartialResult);

            // Read first and second elements if exists (or marks
            // source as having "no more rows")
            readNextTuple();
            readNextTuple();
        } catch (SQLException sqle) {
            if (stmt != null) {
                throw stmt.handle(sqle);
            } else {
                throw Util.newError(sqle, message);
            }
        } finally {
            if (!moreRows) {
                if (stmt != null) {
                    stmt.close();
                }
            }
        }
    }

    public TupleList readMembers(
        final DataSource dataSource,
        final TupleList partialResult,
        final List<List<RolapMember>> newPartialResult)
    {
        prepareTuples(dataSource, partialResult, newPartialResult);
        assert targets.size() == 1;
        return new UnaryTupleList(
            Util.<Member>cast(targets.get(0).close()));
    }

    public TupleList readTuples(
        final DataSource jdbcConnection,
        final TupleList partialResult,
        final List<List<RolapMember>> newPartialResult)
    {
        prepareTuples(jdbcConnection, partialResult, newPartialResult);

        // List of tuples
        final int n = targets.size();
        @SuppressWarnings({"unchecked"})
        final List<Member>[] lists = new List[n];
        for (int i = 0; i < n; i++) {
            lists[i] = targets.get(i).close();
        }

        final List<List<Member>> list =
            new TraversalList<Member>(lists, Member.class);
        TupleList tupleList = new DelegatingTupleList(n, list);

        // need to hierarchize the columns from the enumerated targets
        // since we didn't necessarily add them in the order in which
        // they originally appeared in the cross product
        int enumTargetCount = getEnumTargetCount();
        if (enumTargetCount > 0) {
            tupleList = FunUtil.hierarchizeTupleList(tupleList, false);
        }
        return tupleList;
    }

    /**
     * Reads next tuple notifing all internal targets.
     *
     * @return whether there are any more rows
     */
    public boolean readNextTuple() {
        if (!this.moreRows) {
            return false;
        }
        try {
            this.moreRows = this.resultLoader.loadResult();
        } catch (SQLException sqle) {
            this.moreRows = false;
            throw this.resultLoader.handle(sqle);
        }
        if (!this.moreRows) {
            this.resultLoader.close();
        }
        return this.moreRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public int getMaxRows() {
        return maxRows;
    }

    Collection<RolapCube> getBaseCubeCollection(final Query query) {
        return query.getBaseCubes();
    }
}
// End HighCardSqlTupleReader.java
