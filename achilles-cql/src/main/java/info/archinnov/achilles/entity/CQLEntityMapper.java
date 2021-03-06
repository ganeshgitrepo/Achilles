package info.archinnov.achilles.entity;

import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.helper.EntityMapper;
import info.archinnov.achilles.proxy.CQLRowMethodInvoker;
import java.util.Map;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.Row;

/**
 * CQLEntityMapper
 * 
 * @author DuyHai DOAN
 * 
 */
public class CQLEntityMapper extends EntityMapper
{

    private CQLRowMethodInvoker cqlRowInvoker = new CQLRowMethodInvoker();

    public void setEagerPropertiesToEntity(Row row, EntityMeta entityMeta, Object entity)
    {
        for (PropertyMeta pm : entityMeta.getEagerMetas())
        {
            setPropertyToEntity(row, pm, entity);
        }
    }

    public void setPropertyToEntity(Row row, PropertyMeta pm, Object entity)
    {
        if (row != null)
        {
            if (pm.isEmbeddedId())
            {
                Object compoundKey = cqlRowInvoker.invokeOnRowForCompoundKey(row, pm);
                invoker.setValueToField(entity, pm.getSetter(), compoundKey);
            }
            else {
                String propertyName = pm.getPropertyName();
                if (!row.isNull(propertyName))
                {
                    Object value = cqlRowInvoker.invokeOnRowForFields(row, pm);
                    invoker.setValueToField(entity, pm.getSetter(), value);
                }
            }
        }
    }

    public void setJoinValueToEntity(Object value, PropertyMeta pm, Object entity)
    {
        invoker.setValueToField(entity, pm.getSetter(), value);
    }

    public <T> T mapRowToEntity(Class<T> entityClass, Row row, Map<String, PropertyMeta> propertiesMap)
    {
        T entity = null;
        ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
        if (columnDefinitions != null)
        {
            entity = invoker.instanciate(entityClass);
            for (Definition column : columnDefinitions)
            {
                String columnName = column.getName();
                PropertyMeta pm = propertiesMap.get(columnName);
                if (pm != null && !pm.isJoin())
                {
                    Object value = cqlRowInvoker.invokeOnRowForFields(row, pm);
                    invoker.setValueToField(entity, pm.getSetter(), value);
                }
            }
        }
        return entity;
    }
}
