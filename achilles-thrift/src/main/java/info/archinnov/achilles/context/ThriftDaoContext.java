package info.archinnov.achilles.context;

import info.archinnov.achilles.dao.ThriftCounterDao;
import info.archinnov.achilles.dao.ThriftGenericEntityDao;
import info.archinnov.achilles.dao.ThriftGenericWideRowDao;

import java.util.Map;

/**
 * ThriftDaoContext
 * 
 * @author DuyHai DOAN
 * 
 */
public class ThriftDaoContext
{
	private final Map<String, ThriftGenericEntityDao> entityDaosMap;
	private final Map<String, ThriftGenericWideRowDao> wideRowDaosMap;
	private final ThriftCounterDao thriftCounterDao;

	public ThriftDaoContext(Map<String, ThriftGenericEntityDao> entityDaosMap,
			Map<String, ThriftGenericWideRowDao> wideRowDaosMap, ThriftCounterDao thriftCounterDao)
	{
		this.entityDaosMap = entityDaosMap;
		this.wideRowDaosMap = wideRowDaosMap;
		this.thriftCounterDao = thriftCounterDao;
	}

	public ThriftCounterDao getCounterDao()
	{
		return thriftCounterDao;
	}

	public ThriftGenericEntityDao findEntityDao(String columnFamilyName)
	{
		return entityDaosMap.get(columnFamilyName);
	}

	public ThriftGenericWideRowDao findWideRowDao(String columnFamilyName)
	{
		return wideRowDaosMap.get(columnFamilyName);
	}
}
