package fr.doan.achilles.entity.manager;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.doan.achilles.entity.EntityHelper;
import fr.doan.achilles.entity.metadata.EntityMeta;
import fr.doan.achilles.entity.operations.EntityLoader;
import fr.doan.achilles.entity.operations.EntityMerger;
import fr.doan.achilles.entity.operations.EntityPersister;
import fr.doan.achilles.entity.operations.EntityRefresher;
import fr.doan.achilles.entity.operations.EntityValidator;
import fr.doan.achilles.proxy.builder.EntityProxyBuilder;
import fr.doan.achilles.validation.Validator;

/**
 * ThriftEntityManager
 * 
 * @author DuyHai DOAN
 * 
 */
public class ThriftEntityManager implements EntityManager
{
	private static final Logger log = LoggerFactory.getLogger(ThriftEntityManager.class);

	private final Map<Class<?>, EntityMeta<?>> entityMetaMap;

	private EntityPersister persister = new EntityPersister();
	private EntityLoader loader = new EntityLoader();
	private EntityMerger merger = new EntityMerger();
	private EntityRefresher entityRefresher = new EntityRefresher();
	private EntityHelper helper = new EntityHelper();
	private EntityValidator entityValidator = new EntityValidator();

	private EntityProxyBuilder interceptorBuilder = new EntityProxyBuilder();

	public ThriftEntityManager(Map<Class<?>, EntityMeta<?>> entityMetaMap) {
		this.entityMetaMap = entityMetaMap;
	}

	@Override
	public void persist(Object entity)
	{
		log.debug("Persisting entity '{}'", entity);

		entityValidator.validateEntity(entity, entityMetaMap);
		if (helper.isProxy(entity))
		{
			throw new IllegalStateException(
					"Then entity is already in 'managed' state. Please use the merge() method instead of persist()");
		}

		EntityMeta<?> entityMeta = this.entityMetaMap.get(entity.getClass());

		this.persister.persist(entity, entityMeta);
	}

	@Override
	public <T> T merge(T entity)
	{
		entityValidator.validateEntity(entity, entityMetaMap);
		Class<?> baseClass = helper.deriveBaseClass(entity);
		EntityMeta<?> entityMeta = this.entityMetaMap.get(baseClass);
		return this.merger.mergeEntity(entity, entityMeta);
	}

	@Override
	public void remove(Object entity)
	{
		entityValidator.validateEntity(entity, entityMetaMap);
		if (helper.isProxy(entity))
		{
			Class<?> baseClass = helper.deriveBaseClass(entity);
			EntityMeta<?> entityMeta = this.entityMetaMap.get(baseClass);
			this.persister.remove(entity, entityMeta);

		}
		else
		{
			throw new IllegalStateException(
					"Then entity is not in 'managed' state. Please use the merge() or find() method to load it first");
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey)
	{
		Validator.validateNotNull(entityClass, "Entity class should not be null");
		Validator.validateNotNull(primaryKey, "Entity primaryKey should not be null");
		Validator
				.validateSerializable(primaryKey.getClass(),
						"Entity '" + entityClass.getCanonicalName()
								+ "' primaryKey should be Serializable");

		EntityMeta<Serializable> entityMeta = (EntityMeta<Serializable>) this.entityMetaMap
				.get(entityClass);

		T entity = (T) this.loader.load(entityClass, (Serializable) primaryKey, entityMeta);

		if (entity != null)
		{
			entity = (T) this.interceptorBuilder.build(entity, entityMeta);
		}

		return entity;
	}

	@Override
	public <T> T getReference(Class<T> entityClass, Object primaryKey)
	{
		return this.find(entityClass, primaryKey);
	}

	@Override
	public void flush()
	{
		throw new UnsupportedOperationException("This operation is not supported for Cassandra");
	}

	@Override
	public void setFlushMode(FlushModeType flushMode)
	{
		throw new UnsupportedOperationException("This operation is not supported for Cassandra");

	}

	@Override
	public FlushModeType getFlushMode()
	{
		return FlushModeType.AUTO;
	}

	@Override
	public void lock(Object entity, LockModeType lockMode)
	{
		throw new UnsupportedOperationException(
				"This operation is not supported for this Cassandra");
	}

	@Override
	public void refresh(Object entity)
	{

		if (!helper.isProxy(entity))
		{
			throw new IllegalStateException("The entity " + entity + " is not in 'managed' state");
		}
		else
		{
			entityRefresher.refresh(entity, entityMetaMap);
		}
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException(
				"This operation is not supported for this Cassandra");
	}

	@Override
	public boolean contains(Object entity)
	{
		throw new UnsupportedOperationException(
				"This operation is not supported for this Cassandra");
	}

	@Override
	public Query createQuery(String qlString)
	{
		throw new UnsupportedOperationException(
				"This operation is not supported for this Cassandra");
	}

	@Override
	public Query createNamedQuery(String name)
	{
		throw new UnsupportedOperationException(
				"This operation is not supported for this Entity Manager");
	}

	@Override
	public Query createNativeQuery(String sqlString)
	{
		throw new UnsupportedOperationException(
				"This operation is not supported for this Cassandra");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Query createNativeQuery(String sqlString, Class resultClass)
	{
		throw new UnsupportedOperationException(
				"This operation is not supported for this Cassandra");
	}

	@Override
	public Query createNativeQuery(String sqlString, String resultSetMapping)
	{
		throw new UnsupportedOperationException(
				"This operation is not supported for this Cassandra");
	}

	@Override
	public void joinTransaction()
	{
		throw new UnsupportedOperationException(
				"This operation is not supported for this Cassandra");

	}

	@Override
	public Object getDelegate()
	{
		return this;
	}

	@Override
	public void close()
	{
		throw new UnsupportedOperationException(
				"This operation is not supported for this Cassandra");

	}

	@Override
	public boolean isOpen()
	{
		throw new UnsupportedOperationException(
				"This operation is not supported for this Cassandra");
	}

	@Override
	public EntityTransaction getTransaction()
	{
		throw new UnsupportedOperationException(
				"This operation is not supported for this Cassandra");
	}
}
