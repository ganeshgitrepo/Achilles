package info.archinnov.achilles.entity.manager;

import info.archinnov.achilles.compound.ThriftCompoundKeyValidator;
import info.archinnov.achilles.context.ConfigurationContext;
import info.archinnov.achilles.context.ThriftDaoContext;
import info.archinnov.achilles.context.ThriftPersistenceContext;
import info.archinnov.achilles.context.ThriftPersistenceContextFactory;
import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.operations.EntityValidator;
import info.archinnov.achilles.entity.operations.ThriftEntityProxifier;
import info.archinnov.achilles.entity.operations.ThriftSliceQueryExecutor;
import info.archinnov.achilles.query.slice.SliceQueryBuilder;
import info.archinnov.achilles.type.Options;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThriftEntityManager
 * 
 * @author DuyHai DOAN
 * 
 */
public class ThriftEntityManager extends EntityManager<ThriftPersistenceContext>
{
    private static final Logger log = LoggerFactory.getLogger(ThriftEntityManager.class);

    protected ThriftDaoContext daoContext;
    protected ThriftPersistenceContextFactory contextFactory;
    private ThriftSliceQueryExecutor sliceQueryExecutor;
    private ThriftCompoundKeyValidator compoundKeyValidator = new ThriftCompoundKeyValidator();

    /**
     * Create a new ThriftEntityManager with a configuration map
     * 
     * @param configurationMap
     *            Check documentation for more details on configuration parameters
     */
    ThriftEntityManager(Map<Class<?>, EntityMeta> entityMetaMap, //
            ThriftPersistenceContextFactory contextFactory,
            ThriftDaoContext daoContext, //
            ConfigurationContext configContext)
    {
        super(entityMetaMap, configContext);
        this.contextFactory = contextFactory;
        this.daoContext = daoContext;
        super.proxifier = new ThriftEntityProxifier();
        super.entityValidator = new EntityValidator<ThriftPersistenceContext>(super.proxifier);
        this.sliceQueryExecutor = new ThriftSliceQueryExecutor(contextFactory, configContext);
    }

    /**
     * Create a new slice query builder for entity of type T<br/>
     * <br/>
     * 
     * @param entityClass
     *            Entity class
     * @return SliceQueryBuilder<T>
     */
    @Override
    public <T> SliceQueryBuilder<ThriftPersistenceContext, T> sliceQuery(Class<T> entityClass)
    {
        EntityMeta meta = entityMetaMap.get(entityClass);
        return new SliceQueryBuilder<ThriftPersistenceContext, T>(sliceQueryExecutor, compoundKeyValidator,
                entityClass, meta);
    }

    @Override
    protected ThriftPersistenceContext initPersistenceContext(Class<?> entityClass, Object primaryKey, Options options)
    {
        return contextFactory.newContext(entityClass, primaryKey, options);
    }

    @Override
    protected ThriftPersistenceContext initPersistenceContext(Object entity, Options options)
    {
        return contextFactory.newContext(entity, options);
    }

    protected void setThriftDaoContext(ThriftDaoContext thriftDaoContext) {
        this.daoContext = thriftDaoContext;
    }

    protected void setQueryExecutor(ThriftSliceQueryExecutor queryExecutor) {
        this.sliceQueryExecutor = queryExecutor;
    }

    protected void setCompoundKeyValidator(ThriftCompoundKeyValidator compoundKeyValidator) {
        this.compoundKeyValidator = compoundKeyValidator;
    }

    protected void setContextFactory(ThriftPersistenceContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

}
