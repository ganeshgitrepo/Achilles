package info.archinnov.achilles.query;

import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.type.BoundingMode;
import info.archinnov.achilles.type.ConsistencyLevel;
import info.archinnov.achilles.type.OrderingMode;
import info.archinnov.achilles.validation.Validator;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;

/**
 * SliceQuery
 * 
 * @author DuyHai DOAN
 * 
 */
public class SliceQuery<T>
{
    public static final int DEFAULT_LIMIT = 100;
    public static final int DEFAULT_BATCH_SIZE = 100;

    private Class<T> entityClass;
    private EntityMeta meta;
    private Object partitionKey;
    private List<Object> clusteringsFrom;
    private List<Object> clusteringsTo;
    private OrderingMode ordering;
    private BoundingMode bounding;
    private ConsistencyLevel consistencyLevel;
    private int batchSize;
    private int limit;
    private boolean limitSet;
    private boolean noComponent;

    public SliceQuery(Class<T> entityClass, EntityMeta meta, Object partitionKey,
            Object[] clusteringsFrom, Object[] clusteringsTo, OrderingMode ordering,
            BoundingMode bounding, ConsistencyLevel consistencyLevel, int limit, int batchSize, boolean limitSet)
    {

        this.limitSet = limitSet;
        Validator.validateNotNull(partitionKey,
                "Partition key should be set for slice query for entity class '%s'", entityClass.getCanonicalName());

        this.entityClass = entityClass;
        this.meta = meta;

        this.partitionKey = partitionKey;
        this.noComponent = clusteringsFrom == null && clusteringsTo == null;

        PropertyMeta idMeta = meta.getIdMeta();
        List<Object> componentsFrom = Arrays.<Object> asList(ArrayUtils.add(clusteringsFrom, 0,
                partitionKey));
        this.clusteringsFrom = idMeta.encodeComponents(componentsFrom);
        List<Object> componentsTo = Arrays.<Object> asList(ArrayUtils.add(clusteringsTo, 0,
                partitionKey));
        this.clusteringsTo = idMeta.encodeComponents(componentsTo);
        this.ordering = ordering;
        this.bounding = bounding;
        this.consistencyLevel = consistencyLevel;
        this.limit = limit;
        this.batchSize = batchSize;
    }

    public Class<T> getEntityClass()
    {
        return entityClass;
    }

    public EntityMeta getMeta()
    {
        return meta;
    }

    public Object getPartitionKey()
    {
        return partitionKey;
    }

    public List<Object> getClusteringsFrom()
    {

        return clusteringsFrom;
    }

    public List<Object> getClusteringsTo()
    {
        return clusteringsTo;
    }

    public OrderingMode getOrdering()
    {
        return ordering;
    }

    public BoundingMode getBounding()
    {
        return bounding;
    }

    public ConsistencyLevel getConsistencyLevel()
    {
        return consistencyLevel;
    }

    public int getLimit()
    {
        return limit;
    }

    public int getBatchSize()
    {
        return batchSize;
    }

    public boolean isLimitSet() {
        return limitSet;
    }

    public boolean hasNoComponent() {
        return noComponent;
    }

}
