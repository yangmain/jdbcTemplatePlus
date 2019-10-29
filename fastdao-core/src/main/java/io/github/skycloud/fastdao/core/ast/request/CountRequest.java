/**
 * @(#)ICountRequest.java, 10月 20, 2019.
 * <p>
 * Copyright 2019 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package io.github.skycloud.fastdao.core.ast.request;

import io.github.skycloud.fastdao.core.ast.Condition;
import io.github.skycloud.fastdao.core.ast.ConditionalRequest;
import io.github.skycloud.fastdao.core.ast.Request;
import io.github.skycloud.fastdao.core.ast.SqlAst;
import io.github.skycloud.fastdao.core.ast.Visitor;
import io.github.skycloud.fastdao.core.exceptions.IllegalConditionException;
import io.github.skycloud.fastdao.core.table.Column;
import lombok.Getter;

import java.util.function.Function;

/**
 * @author yuntian
 */
public interface CountRequest extends ConditionalRequest<CountRequest> {

    /**
     * SELECT DISTINCT COUNT(*) ...
     */
    CountRequest distinct();

    /**
     * SELECT COUNT(field)
     */
    CountRequest setCountField(String field);

    /**
     * SELECT COUNT(field)
     */
    CountRequest setCountField(Column field);

    /**
     * @author yuntian
     */
    @Getter
    class CountRequestAst implements CountRequest, SqlAst {

        private boolean distinct;

        private String countField;

        private Condition condition;

        private Function<IllegalConditionException, ?> onSyntaxError;

        @Override
        public CountRequest setCountField(String field) {
            this.countField = field;
            return this;
        }

        @Override
        public CountRequest setCountField(Column field) {
            this.countField = field.toString();
            return this;
        }

        @Override
        public CountRequest distinct() {
            distinct = true;
            return this;
        }

        @Override
        public CountRequest setCondition(Condition condition) {
            this.condition = condition;
            return this;
        }

        @Override
        public <T extends Request> T onSyntaxError(Function<IllegalConditionException, ?> action) {
            this.onSyntaxError = action;
            return (T) this;
        }

        @Override
        public Function<IllegalConditionException, ?> getOnSyntaxError() {
            return onSyntaxError;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public SqlAst copy() {
            CountRequestAst request = new CountRequestAst();
            request.distinct = distinct;
            request.countField = countField;
            if (condition != null) {
                request.condition = (Condition) ((SqlAst) condition).copy();
            }
            request.onSyntaxError = onSyntaxError;
            return request;
        }

    }
}