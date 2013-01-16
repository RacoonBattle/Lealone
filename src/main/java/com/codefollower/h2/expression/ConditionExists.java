/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.codefollower.h2.expression;


import com.codefollower.h2.command.dml.Query;
import com.codefollower.h2.engine.Session;
import com.codefollower.h2.result.ResultInterface;
import com.codefollower.h2.table.ColumnResolver;
import com.codefollower.h2.table.TableFilter;
import com.codefollower.h2.util.StringUtils;
import com.codefollower.h2.value.Value;
import com.codefollower.h2.value.ValueBoolean;
import com.codefollower.yourbase.result.CombinedResult;

/**
 * An 'exists' condition as in WHERE EXISTS(SELECT ...)
 */
public class ConditionExists extends Condition {

    private final Query query;

    public ConditionExists(Query query) {
        this.query = query;
        query.setSubquery(true);
    }

    public Value getValue(Session session) {
        query.setSession(session);
        ResultInterface result = new CombinedResult(session, query, 1); //query.query(1);
        session.addTemporaryResult(result);
        boolean r = result.getRowCount() > 0;
        return ValueBoolean.get(r);
    }

    public Expression optimize(Session session) {
        query.prepare();
        return this;
    }

    public String getSQL(boolean isDistributed) {
        return "EXISTS(\n" + StringUtils.indent(query.getPlanSQL(), 4, false) + ")";
    }

    public void updateAggregate(Session session) {
        // TODO exists: is it allowed that the subquery contains aggregates?
        // probably not
        // select id from test group by id having exists (select * from test2
        // where id=count(test.id))
    }

    public void mapColumns(ColumnResolver resolver, int level) {
        query.mapColumns(resolver, level + 1);
    }

    public void setEvaluatable(TableFilter tableFilter, boolean b) {
        query.setEvaluatable(tableFilter, b);
    }

    public boolean isEverything(ExpressionVisitor visitor) {
        return query.isEverything(visitor);
    }

    public int getCost() {
        return query.getCostAsExpression();
    }

}