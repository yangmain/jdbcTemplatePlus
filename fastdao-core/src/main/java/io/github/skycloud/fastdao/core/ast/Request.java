/**
 * @(#)Request.java, 10月 12, 2019.
 * <p>
 * Copyright 2019 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package io.github.skycloud.fastdao.core.ast;

import io.github.skycloud.fastdao.core.ast.request.CountRequest;
import io.github.skycloud.fastdao.core.ast.request.CountRequest.CountRequestAst;
import io.github.skycloud.fastdao.core.ast.request.DeleteRequest;
import io.github.skycloud.fastdao.core.ast.request.DeleteRequest.DeleteRequestAst;
import io.github.skycloud.fastdao.core.ast.request.InsertRequest;
import io.github.skycloud.fastdao.core.ast.request.InsertRequest.InsertRequestAst;
import io.github.skycloud.fastdao.core.ast.request.QueryRequest;
import io.github.skycloud.fastdao.core.ast.request.QueryRequest.QueryRequestAst;
import io.github.skycloud.fastdao.core.ast.request.UpdateRequest;
import io.github.skycloud.fastdao.core.ast.request.UpdateRequest.UpdateRequestAst;
import io.github.skycloud.fastdao.core.exceptions.IllegalConditionException;
import io.github.skycloud.fastdao.core.plugins.Pluggable;

import java.util.function.Function;

/**
 * @author yuntian
 * all request can be get from here by static method, or you can just new one
 * <p>
 * all request are provided to user by interface just to hide unsafe method
 */
public interface Request extends Pluggable {

    <T extends Request> T onSyntaxError(Function<IllegalConditionException, ?> action);

    Function<IllegalConditionException, ?> getOnSyntaxError();

    static UpdateRequest updateRequest() {
        return new UpdateRequestAst();
    }

    static DeleteRequest deleteRequest() {
        return new DeleteRequestAst();
    }

    static InsertRequest insertRequest() {
        return new InsertRequestAst();
    }

    static CountRequest countRequest() {
        return new CountRequestAst();
    }

    static QueryRequest queryRequest() {
        return new QueryRequestAst();
    }
}