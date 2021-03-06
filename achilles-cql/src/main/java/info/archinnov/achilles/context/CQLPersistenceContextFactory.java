package info.archinnov.achilles.context;

import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.entity.operations.CQLEntityProxifier;
import info.archinnov.achilles.proxy.ReflectionInvoker;
import info.archinnov.achilles.type.ConsistencyLevel;
import info.archinnov.achilles.type.Options;
import info.archinnov.achilles.type.OptionsBuilder;
import info.archinnov.achilles.validation.Validator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.google.common.base.Optional;

/**
 * CQLPersistenceContextFactory
 * 
 * @author DuyHai DOAN
 * 
 */
public class CQLPersistenceContextFactory implements PersistenceContextFactory {

    public static final Optional<ConsistencyLevel> NO_CONSISTENCY_LEVEL = Optional
            .<ConsistencyLevel> absent();
    public static final Optional<Integer> NO_TTL = Optional.<Integer> absent();

    private CQLDaoContext daoContext;
    private ConfigurationContext configContext;
    private Map<Class<?>, EntityMeta> entityMetaMap;
    private CQLEntityProxifier proxifier = new CQLEntityProxifier();
    private ReflectionInvoker invoker = new ReflectionInvoker();

    public CQLPersistenceContextFactory(CQLDaoContext daoContext, ConfigurationContext configContext,
            Map<Class<?>, EntityMeta> entityMetaMap) {
        this.daoContext = daoContext;
        this.configContext = configContext;
        this.entityMetaMap = entityMetaMap;
    }

    public CQLPersistenceContext newContextForJoin(Object joinEntity, CQLAbstractFlushContext<?> flushContext,
            Set<String> entitiesIdentity)
    {
        Validator.validateNotNull(joinEntity, "join entity should not be null for persistence context creation");
        Class<?> entityClass = proxifier.deriveBaseClass(joinEntity);
        EntityMeta joinMeta = entityMetaMap.get(entityClass);

        return new CQLPersistenceContext(joinMeta, configContext, daoContext,
                flushContext.duplicate(), joinEntity, OptionsBuilder.noOptions(), entitiesIdentity);
    }

    public CQLPersistenceContext newContextForJoin(Class<?> entityClass,
            Object joinId, CQLAbstractFlushContext<?> flushContext,
            Set<String> entitiesIdentity)
    {
        Validator.validateNotNull(entityClass, "entityClass should not be null for persistence context creation");
        Validator.validateNotNull(joinId, "joinId should not be null for persistence context creation");
        EntityMeta joinMeta = entityMetaMap.get(entityClass);
        return new CQLPersistenceContext(joinMeta, configContext, daoContext,
                flushContext.duplicate(), entityClass, joinId, OptionsBuilder.noOptions(), entitiesIdentity);
    }

    @Override
    public CQLPersistenceContext newContext(Object entity, Options options)
    {
        Validator.validateNotNull(entity, "entity should not be null for persistence context creation");
        Class<?> entityClass = proxifier.deriveBaseClass(entity);
        EntityMeta meta = entityMetaMap.get(entityClass);
        CQLImmediateFlushContext flushContext = buildImmediateFlushContext(options);

        return new CQLPersistenceContext(meta, configContext, daoContext,
                flushContext, entity, options, new HashSet<String>());
    }

    @Override
    public CQLPersistenceContext newContext(Object entity)
    {
        return newContext(entity, OptionsBuilder.noOptions());
    }

    @Override
    public CQLPersistenceContext newContext(Class<?> entityClass, Object primaryKey, Options options)
    {
        Validator.validateNotNull(entityClass, "entityClass should not be null for persistence context creation");
        Validator.validateNotNull(primaryKey, "primaryKey should not be null for persistence context creation");
        EntityMeta meta = entityMetaMap.get(entityClass);
        CQLImmediateFlushContext flushContext = buildImmediateFlushContext(options);

        return new CQLPersistenceContext(meta, configContext, daoContext,
                flushContext, entityClass, primaryKey, options, new HashSet<String>());
    }

    @Override
    public CQLPersistenceContext newContextForSliceQuery(Class<?> entityClass, Object partitionKey,
            ConsistencyLevel cl)
    {
        EntityMeta meta = entityMetaMap.get(entityClass);
        PropertyMeta idMeta = meta.getIdMeta();
        Object embeddedId = invoker.instanciateEmbeddedIdWithPartitionKey(idMeta, partitionKey);

        CQLImmediateFlushContext flushContext = buildImmediateFlushContext(OptionsBuilder.withConsistency(cl));

        return new CQLPersistenceContext(meta, configContext, daoContext, flushContext, entityClass,
                embeddedId, OptionsBuilder.withConsistency(cl), new HashSet<String>());
    }

    private CQLImmediateFlushContext buildImmediateFlushContext(Options options)
    {
        return new CQLImmediateFlushContext(daoContext, options.getConsistencyLevel().orNull());
    }
}
