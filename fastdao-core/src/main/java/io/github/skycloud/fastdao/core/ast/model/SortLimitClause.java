/**
 * @(#)SortLimitClause.java, 10月 07, 2019.
 * <p>
 *
 */
package io.github.skycloud.fastdao.core.ast.model;

import com.google.common.collect.Lists;
import io.github.skycloud.fastdao.core.ast.SqlAst;
import io.github.skycloud.fastdao.core.ast.enums.OrderEnum;
import io.github.skycloud.fastdao.core.ast.visitor.Visitor;

import java.util.List;

/**
 * @author yuntian
 */
public class SortLimitClause implements SqlAst {

    private List<Sort> sorts = Lists.newArrayList();

    private Integer limit;

    private Integer offset;

    public List<Sort> getSorts() {
        return sorts;
    }

    public void setSorts(List<Sort> sorts) {
        this.sorts = sorts;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public void addSort(String field, OrderEnum order) {
        this.sorts.add(new Sort(field, order));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public SqlAst copy() {
        SortLimitClause sortLimitClause = new SortLimitClause();
        for (Sort sort : sorts) {
            sortLimitClause.sorts.add(new Sort(sort.getField(), sort.getOrder()));
        }
        sortLimitClause.limit = limit;
        sortLimitClause.offset = offset;
        return sortLimitClause;
    }
}