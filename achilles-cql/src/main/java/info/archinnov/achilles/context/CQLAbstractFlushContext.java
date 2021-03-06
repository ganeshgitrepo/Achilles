package info.archinnov.achilles.context;

import static info.archinnov.achilles.consistency.CQLConsistencyConvertor.getCQLLevel;
import info.archinnov.achilles.statement.prepared.BoundStatementWrapper;
import info.archinnov.achilles.type.ConsistencyLevel;
import java.util.ArrayList;
import java.util.List;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Query;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;

/**
 * CQLFlushContext
 * 
 * @author DuyHai DOAN
 * 
 */
public abstract class CQLAbstractFlushContext<T extends CQLAbstractFlushContext<T>> extends
        FlushContext<T>
{
    protected CQLDaoContext daoContext;

    protected List<BoundStatementWrapper> boundStatementWrappers = new ArrayList<BoundStatementWrapper>();
    protected List<Statement> statements = new ArrayList<Statement>();

    protected ConsistencyLevel consistencyLevel;

    public CQLAbstractFlushContext(CQLDaoContext daoContext, ConsistencyLevel consistencyLevel)
    {
        this.daoContext = daoContext;
        this.consistencyLevel = consistencyLevel;
    }

    protected CQLAbstractFlushContext(CQLDaoContext daoContext,
            List<BoundStatementWrapper> boundStatementWrappers,
            ConsistencyLevel consistencyLevel)
    {
        this.boundStatementWrappers = boundStatementWrappers;
        this.daoContext = daoContext;
        this.consistencyLevel = consistencyLevel;
    }

    @Override
    public void cleanUp()
    {
        boundStatementWrappers.clear();
        statements.clear();
        consistencyLevel = null;
    }

    protected void doFlush()
    {
        for (BoundStatementWrapper wrapper : boundStatementWrappers)
        {

            daoContext.execute(wrapper.getBs(), wrapper.getValues());
        }
        for (Statement statement : statements)
        {
            daoContext.execute(statement);
        }

        cleanUp();

    }

    public void pushBoundStatement(BoundStatementWrapper bsWrapper,
            ConsistencyLevel writeConsistencyLevel)
    {
        BoundStatement boundStatement = bsWrapper.getBs();
        if (consistencyLevel != null)
        {
            boundStatement.setConsistencyLevel(getCQLLevel(consistencyLevel));
        }
        else
        {
            boundStatement.setConsistencyLevel(getCQLLevel(writeConsistencyLevel));
        }
        boundStatementWrappers.add(bsWrapper);
    }

    public void pushStatement(Statement statement,
            ConsistencyLevel writeConsistencyLevel)
    {

        if (consistencyLevel != null)
        {
            statement.setConsistencyLevel(getCQLLevel(consistencyLevel));
        }
        else
        {
            statement.setConsistencyLevel(getCQLLevel(writeConsistencyLevel));
        }
        statements.add(statement);
    }

    public ResultSet executeImmediateWithConsistency(Query query,
            ConsistencyLevel readConsistencyLevel, Object... boundValues)
    {
        query.setConsistencyLevel(getCQLLevel(readConsistencyLevel));
        return daoContext.execute(query, boundValues);
    }

    public List<BoundStatementWrapper> getBoundStatementWrappers()
    {
        return boundStatementWrappers;
    }

    @Override
    public void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    @Override
    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

}
