/**
 * @(#)SqlVisitor.java, 10月 01, 2019.
 * <p>
 * Copyright 2019 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package io.github.skycloud.fastdao.core.ast;

import io.github.skycloud.fastdao.core.SqlVisitor;
import io.github.skycloud.fastdao.core.ast.conditions.AndCondition.DefaultAndCondition;
import io.github.skycloud.fastdao.core.ast.conditions.EqualCondition.DefaultEqualCondition;
import io.github.skycloud.fastdao.core.ast.conditions.IsNullCondition.DefaultIsNullCondition;
import io.github.skycloud.fastdao.core.ast.conditions.LikeCondition.DefaultLikeCondition;
import io.github.skycloud.fastdao.core.ast.conditions.OrCondition.DefaultOrCondition;
import io.github.skycloud.fastdao.core.ast.conditions.RangeCondition.DefaultRangeCondition;
import io.github.skycloud.fastdao.core.ast.constants.SQLConstant;
import io.github.skycloud.fastdao.core.ast.model.Sort;
import io.github.skycloud.fastdao.core.ast.model.SortLimitClause;
import io.github.skycloud.fastdao.core.ast.request.CountRequest.DefaultCountRequest;
import io.github.skycloud.fastdao.core.ast.request.DeleteRequest.DefaultDeleteRequest;
import io.github.skycloud.fastdao.core.ast.request.InsertRequest.DefaultInsertRequest;
import io.github.skycloud.fastdao.core.ast.request.QueryRequest.DefaultQueryRequest;
import io.github.skycloud.fastdao.core.ast.request.UpdateRequest.DefaultUpdateRequest;
import io.github.skycloud.fastdao.core.exceptions.IllegalConditionException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author yuntian
 */
public class MysqlVisitor extends SqlVisitor {


    public MysqlVisitor(String tableName, ValueParser valueParser) {
        super(tableName, valueParser);
    }

    @Override
    public void visit(DefaultQueryRequest request) {
        sb.append(SQLConstant.SELECT);
        if (request.isDistinct()) {
            sb.append(SQLConstant.DISTINCT);
        }
        if (CollectionUtils.isEmpty(request.getSelectFields())) {
            sb.append(SQLConstant.ALL_FIELD);
        } else {
            visitCollection(request.getSelectFields(), this::visitField, SQLConstant.COMMA);
        }
        sb.append(SQLConstant.FROM);
        visitField(tableName);
        visitWhereClause(request.getCondition());
        visit(request.getSortLimitClause());
    }

    @Override
    public void visit(DefaultCountRequest request) {
        sb.append(SQLConstant.SELECT);
        sb.append(SQLConstant.COUNT);
        sb.append(SQLConstant.LB);
        if (request.isDistinct()) {
            sb.append(SQLConstant.DISTINCT);
        }
        if (StringUtils.isNotBlank(request.getCountField())) {
            visitField(request.getCountField());
        }
        sb.append(SQLConstant.ALL_FIELD);
        sb.append(SQLConstant.RB);
        sb.append(SQLConstant.FROM);
        visitField(tableName);
        visitWhereClause(request.getCondition());
    }

    private void visitWhereClause(Condition condition) {
        // if no condition exist then return all data
        if (condition == null) {
            return;
        }
        try {
            if (!condition.isLegal()) {
                throw new IllegalConditionException();
            }
            if (condition.isEmpty()) {
                return;
            }
            sb.append(SQLConstant.WHERE);
            ((SqlAst) condition).accept(this);
        } catch (IllegalConditionException e) {
            isLegal = false;
        }
    }

    @Override
    public void visit(SortLimitClause sortLimitClause) {
        if (sortLimitClause == null) {
            return;
        }
        if (CollectionUtils.isNotEmpty(sortLimitClause.getSorts())) {
            sb.append(SQLConstant.ORDER_BY);
            List<Sort> sorts = sortLimitClause.getSorts();
            visitCollection(sorts, this::visit, SQLConstant.COMMA);
        }

        // visit limit part
        Integer limit = sortLimitClause.getLimit();
        Integer offset = sortLimitClause.getOffset();
        if (limit != null) {
            sb.append(SQLConstant.LIMIT);
            sb.append(limit);
            if (offset == null) {
                return;
            }
            sb.append(SQLConstant.COMMA);
            sb.append(offset);
        }
    }

    public void visitFieldSetClause(Map<String, Object> setFields) {
        sb.append(SQLConstant.SET);
        visitCollection(setFields.entrySet(), (entry) -> {
            visitField(entry.getKey());
            sb.append(SQLConstant.EQUAL);
            visitValue(entry.getKey(), entry.getValue());
        }, SQLConstant.COMMA);
    }


    @Override
    public void visit(DefaultUpdateRequest request) {
        sb.append(SQLConstant.UPDATE);
        visitField(tableName);
        visitFieldSetClause(request.getUpdateFields());
        visitWhereClause(request.getCondition());
        visit(request.getSortLimitClause());
    }

    @Override
    public void visit(DefaultDeleteRequest request) {
        sb.append(SQLConstant.DELETE);
        sb.append(SQLConstant.FROM);
        visitField(tableName);
        visitWhereClause(request.getCondition());
        visit(request.getSortLimitClause());
    }

    @Override
    public void visit(DefaultInsertRequest request) {
        sb.append(SQLConstant.INSERT_INTO);
        visitField(tableName);
        sb.append(SQLConstant.SET);
        visitCollection(request.getInsertFields().entrySet(), (entry) -> {
            visitField(entry.getKey());
            sb.append(SQLConstant.EQUAL);
            visitValue(entry.getKey(), entry.getValue());
        }, SQLConstant.COMMA);
    }


    @Override
    public void visit(DefaultAndCondition condition) {
        List<Condition> subConditions = condition.getSubConditions();
        visitConditions(subConditions, cond -> ((SqlAst) cond).accept(this), SQLConstant.AND);
    }


    @Override
    public void visit(DefaultOrCondition condition) {
        sb.append(SQLConstant.LB);
        visitConditions(condition.getSubConditions(), cond -> ((SqlAst) cond).accept(this), SQLConstant.OR);
        sb.append(SQLConstant.RB);
    }


    @Override
    public void visit(DefaultEqualCondition condition) {
        visitField(condition.getField());
        if (condition.getValue() instanceof Collection && ((Collection) condition.getValue()).size() > 1) {
            sb.append(SQLConstant.IN);
            sb.append(SQLConstant.LB);
            visitValue(condition.getField(), condition.getValue());
            sb.append(SQLConstant.RB);
        } else {
            sb.append(SQLConstant.EQUAL);
            visitValue(condition.getField(), condition.getValue());
        }
    }


    @Override
    public void visit(DefaultRangeCondition condition) {
        if (condition.getGt() != null) {
            visitField(condition.getField());
            sb.append(condition.isEgt() ? SQLConstant.GTE : SQLConstant.GT);
            visitValue(condition.getField(), condition.getGt());
            if (condition.getLt() != null) {
                sb.append(SQLConstant.AND);
            }
        }
        if (condition.getLt() != null) {
            visitField(condition.getField());
            sb.append(condition.isElt() ? SQLConstant.LTE : SQLConstant.LT);
            visitValue(condition.getField(), condition.getLt());
        }
    }


    @Override
    public void visit(DefaultLikeCondition condition) {
        visitField(condition.getField());
        sb.append(SQLConstant.LIKE);
        String value =
                (condition.isMatchLeft() ? "%" : "") + condition.getValue() + (condition.isMatchRight() ? "%" : "");
        visitValue(condition.getField(), value);
    }

    @Override
    public void visit(DefaultIsNullCondition condition) {
        visitField(condition.getField());
        sb.append(SQLConstant.IS_NULL);
    }


    private void visit(Sort sort) {
        visitField(sort.getField());
        sb.append(sort.getOrder().name());
    }

    protected void visitValue(String field, Object value) {
        sb.append(valueParser.parseValue(field, value));
    }

    protected void visitField(String field) {
        sb.append(valueParser.parseField(field));
    }

    private <T> void visitCollection(Collection<T> collection, Consumer<T> action, String separator) {
        boolean first = true;
        for (T t : collection) {
            if (first) {
                first = false;
            } else {
                sb.append(separator);
            }
            action.accept(t);
        }
    }

    private <T extends Condition> void visitConditions(Collection<T> collection, Consumer<T> action, String separator) {
        boolean first = true;
        for (T t : collection) {
            if (t.isEmpty()) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                sb.append(separator);
            }
            action.accept(t);
        }
    }
}