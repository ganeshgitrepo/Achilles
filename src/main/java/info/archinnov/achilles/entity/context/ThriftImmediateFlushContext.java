package info.archinnov.achilles.entity.context;

import static info.archinnov.achilles.entity.context.AchillesFlushContext.FlushType.IMMEDIATE;
import info.archinnov.achilles.consistency.AchillesConsistencyLevelPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BatchContext
 * 
 * @author DuyHai DOAN
 * 
 */
public class ThriftImmediateFlushContext extends ThriftAbstractFlushContext
{
	private static final Logger log = LoggerFactory.getLogger(ThriftImmediateFlushContext.class);

	public ThriftImmediateFlushContext(ThriftDaoContext thriftDaoContext, AchillesConsistencyLevelPolicy policy)
	{
		super(thriftDaoContext, policy);
	}

	@Override
	public void startBatch()
	{
		throw new UnsupportedOperationException(
				"Cannot start a batch with a normal EntityManager. Please create a BatchingEntityManager instead");
	}

	@Override
	public void flush()
	{
		log.debug("Flush immediatly all pending mutations");
		doFlush();
	}

	@Override
	public void endBatch()
	{
		throw new UnsupportedOperationException(
				"Cannot end a batch with a normal EntityManager. Please create a BatchingEntityManager instead");
	}

	@Override
	public FlushType type()
	{
		return IMMEDIATE;
	}
}
