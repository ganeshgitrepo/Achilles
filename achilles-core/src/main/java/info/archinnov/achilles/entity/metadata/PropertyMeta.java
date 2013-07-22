package info.archinnov.achilles.entity.metadata;

import static info.archinnov.achilles.entity.metadata.PropertyType.*;
import static info.archinnov.achilles.helper.PropertyHelper.isSupportedType;
import info.archinnov.achilles.exception.AchillesException;
import info.archinnov.achilles.type.ConsistencyLevel;
import info.archinnov.achilles.type.KeyValue;
import info.archinnov.achilles.type.Pair;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.persistence.CascadeType;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Sets;

/**
 * PropertyMeta<K, V>
 * 
 * @author DuyHai DOAN
 * 
 */
public class PropertyMeta<K, V>
{
    private static final Logger log = LoggerFactory.getLogger(PropertyMeta.class);

    private ObjectMapper objectMapper;
    private PropertyType type;
    private String propertyName;
    private String entityClassName;
    private Class<K> keyClass;
    private Class<V> valueClass;
    private Method getter;
    private Method setter;
    private CounterProperties counterProperties;
    private JoinProperties joinProperties;
    private CompoundKeyProperties compoundKeyProperties;
    private String externalTableName;
    private Class<?> idClass;
    private Pair<ConsistencyLevel, ConsistencyLevel> consistencyLevels;

    private boolean compound;

    private static final Logger logger = LoggerFactory.getLogger(PropertyMeta.class);

    public V getValueFromString(Object stringValue)
    {
        log.trace("Getting value from string {} for property {} of entity class {}", stringValue,
                propertyName, entityClassName);
        try
        {
            if (valueClass == String.class)
            {
                log.trace("Casting value straight to string");
                return valueClass.cast(stringValue);
            }
            else
            {
                log.trace("Deserializing value from string");
                return this.objectMapper.readValue((String) stringValue, this.valueClass);
            }
        } catch (Exception e)
        {
            logger.error("Error while trying to deserialize the JSON : " + (String) stringValue, e);
            return null;
        }
    }

    public KeyValue<K, V> getKeyValueFromString(String stringKeyValue)
    {
        log.trace("Getting key/value from string {} for property {} of entity class {}",
                stringKeyValue, propertyName, entityClassName);
        try
        {
            return this.objectMapper.readValue(stringKeyValue, new TypeReference<KeyValue<K, V>>()
            {
            });
        } catch (Exception e)
        {
            logger.error("Error while trying to deserialize the JSON : " + stringKeyValue,
                    e);
            return null;
        }
    }

    public String writeValueToString(Object object)
    {
        log.trace("Writing value {} to string for property {} of entity class {}", object,
                propertyName, entityClassName);
        try
        {
            if (valueClass == String.class && type != MAP && type != LAZY_MAP)
            {
                log.trace("Casting value straight to string");
                return (String) object;
            }
            else
            {
                log.trace("Serializing value to string");
                return this.objectMapper.writeValueAsString(object);
            }
        } catch (Exception e)
        {
            logger.error("Error while trying to serialize to JSON the object : " + object, e);
            return null;
        }
    }

    public String jsonSerializeValue(Object object)
    {
        try
        {
            return this.objectMapper.writeValueAsString(object);
        } catch (Exception e)
        {
            logger.error("Error while trying to serialize to JSON the object : " + object, e);
            return null;
        }
    }

    public Object writeValueAsSupportedTypeOrString(Object value)
    {
        log.trace("Writing value {} as native type or string for property {} of entity class {}",
                value, propertyName, entityClassName);
        try
        {
            if (isSupportedType(valueClass))
            {
                log.trace("Value belongs to list of supported native types");
                return value;
            }
            else
            {
                log.trace("Serializing value to string");
                return this.objectMapper.writeValueAsString(value);
            }
        } catch (Exception e)
        {
            logger.error("Error while trying to serialize to JSON the object : " + value, e);
            return null;
        }
    }

    public V castValue(Object object)
    {
        try
        {
            if (isSupportedType(valueClass) || type.isJoin())
                return this.valueClass.cast(object);
            else
                return objectMapper.readValue((String) object, valueClass);
        } catch (Exception e)
        {
            throw new AchillesException("Error while trying to cast the object " + object
                    + " to type '" + this.valueClass.getCanonicalName() + "'", e);
        }
    }

    public List<Method> getComponentGetters()
    {
        List<Method> compGetters = new ArrayList<Method>();
        if (compoundKeyProperties != null)
        {
            compGetters = compoundKeyProperties.getComponentGetters();
        }
        return compGetters;
    }

    public Method getPartitionKeyGetter()
    {
        Method getter = null;
        if (compoundKeyProperties != null)
        {
            getter = compoundKeyProperties.getComponentGetters().get(0);
        }
        return getter;
    }

    public Method getPartitionKeySetter()
    {
        Method getter = null;
        if (compoundKeyProperties != null)
        {
            getter = compoundKeyProperties.getComponentSetters().get(0);
        }
        return getter;
    }

    public List<Method> getComponentSetters()
    {
        List<Method> compSetters = new ArrayList<Method>();
        if (compoundKeyProperties != null)
        {
            compSetters = compoundKeyProperties.getComponentSetters();
        }
        return compSetters;
    }

    public List<Class<?>> getComponentClasses()
    {
        List<Class<?>> compClasses = new ArrayList<Class<?>>();
        if (compoundKeyProperties != null)
        {
            compClasses = compoundKeyProperties.getComponentClasses();
        }
        return compClasses;
    }

    public String getCQLOrderingComponent()
    {
        String component = null;
        if (compoundKeyProperties != null)
        {
            return compoundKeyProperties.getCQLOrderingComponent();
        }
        return component;
    }

    public List<String> getComponentNames()
    {
        List<String> components = new ArrayList<String>();
        if (compoundKeyProperties != null)
        {
            return compoundKeyProperties.getComponentNames();
        }
        return components;
    }

    public List<String> getCQLComponentNames()
    {
        List<String> components = new ArrayList<String>();
        if (compoundKeyProperties != null)
        {
            return Collections.unmodifiableList(compoundKeyProperties.getCQLComponentNames());
        }
        return components;
    }

    public <T> Constructor<T> getCompoundKeyConstructor()
    {
        return compoundKeyProperties != null ? compoundKeyProperties.<T> getConstructor() : null;
    }

    public boolean hasDefaultConstructorForCompoundKey()
    {
        return compoundKeyProperties != null ? compoundKeyProperties
                .getConstructor()
                .getParameterTypes().length == 0 : false;
    }

    public boolean isJoin()
    {
        return type.isJoin();
    }

    public EntityMeta joinMeta()
    {
        return joinProperties != null ? joinProperties.getEntityMeta() : null;
    }

    public PropertyMeta<?, ?> joinIdMeta()
    {
        return joinMeta() != null ? joinMeta().getIdMeta() : null;
    }

    public PropertyMeta<?, ?> counterIdMeta()
    {
        return counterProperties != null ? counterProperties.getIdMeta() : null;
    }

    public String fqcn()
    {
        return counterProperties != null ? counterProperties.getFqcn() : null;
    }

    public boolean isLazy()
    {
        return this.type.isLazy();
    }

    public boolean isWideMap()
    {
        return this.type.isWideMap();
    }

    public boolean isCounter()
    {
        return this.type.isCounter();
    }

    public boolean isProxyType()
    {
        return this.type.isProxyType();
    }

    public boolean isEmbeddedId()
    {
        return type.isEmbeddedId();
    }

    public boolean hasCascadeType(CascadeType type)
    {
        if (joinProperties != null && joinProperties.getCascadeTypes().contains(type))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean hasAnyCascadeType(CascadeType... types)
    {
        return joinProperties != null
                && !Sets.intersection(joinProperties.getCascadeTypes(), Sets.newHashSet(types))
                        .isEmpty();
    }

    public boolean isJoinCollection()
    {
        return type == JOIN_LIST || type == JOIN_SET;
    }

    public boolean isJoinMap()
    {
        return type == JOIN_MAP;
    }

    public ConsistencyLevel getReadConsistencyLevel()
    {
        return consistencyLevels != null ? consistencyLevels.left : null;
    }

    public ConsistencyLevel getWriteConsistencyLevel()
    {
        return consistencyLevels != null ? consistencyLevels.right : null;
    }

    ////////////////////// CQL

    public Object getKeyFromCassandra(Object cassandraValue)
    {
        return convertValueFromCassandra(keyClass, cassandraValue);
    }

    public Object getValueFromCassandra(Object cassandraValue)
    {
        if (type.isJoin())
        {
            return joinIdMeta().getValueFromCassandra(cassandraValue);
        }
        else {
            return convertValueFromCassandra(valueClass, cassandraValue);
        }
    }

    public Collection<?> getValuesFromCassandra(Collection<?> cassandraValues)
    {
        List<Object> result = new ArrayList<Object>();
        for (Object cassandraValue : cassandraValues)
        {
            result.add(getValueFromCassandra(cassandraValue));
        }
        return result;
    }

    public Map<?, ?> getValuesFromCassandra(Map<?, ?> cassandraValues)
    {
        Map<Object, Object> result = new HashMap<Object, Object>();
        for (Entry<?, ?> entry : cassandraValues.entrySet())
        {
            result.put(getKeyFromCassandra(entry.getKey()), getValueFromCassandra(entry.getValue()));
        }
        return result;
    }

    public Object convertValueFromCassandra(Class<?> targetType, Object cassandraValue)
    {
        if (isSupportedType(targetType))
        {
            return cassandraValue;
        }
        else if (cassandraValue instanceof String)
        {
            try {
                return objectMapper.readValue((String) cassandraValue, targetType);
            } catch (Exception e) {
                throw new AchillesException("Error while deserializing value '" + cassandraValue + "' to type '"
                        + targetType.getCanonicalName() + "' for property '" + propertyName + "' of entity class '"
                        + entityClassName + "'", e);
            }
        }
        else
        {
            throw new AchillesException("Error while deserializing value '" + cassandraValue + "' to type '"
                    + targetType.getCanonicalName() + "' for property '" + propertyName + "' of entity class '"
                    + entityClassName + "'");
        }
    }

    public Object writeValueToCassandra(Class<?> sourceType, Object value)
    {
        try
        {
            if (isSupportedType(sourceType))
            {
                return value;
            }
            else
            {
                return this.objectMapper.writeValueAsString(value);
            }
        } catch (Exception e) {
            throw new AchillesException("Error while serializing value '" + value + "' from type '"
                    + sourceType.getCanonicalName() + "' for property '" + propertyName + "' of entity class '"
                    + entityClassName + "'");
        }
    }

    /////////////////////// End CQL
    public PropertyType type()
    {
        return type;
    }

    public void setType(PropertyType propertyType)
    {
        this.type = propertyType;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public String getCQLPropertyName()
    {
        return propertyName.toLowerCase();
    }

    public void setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
    }

    public Class<K> getKeyClass()
    {
        return keyClass;
    }

    public void setKeyClass(Class<K> keyClass)
    {
        this.keyClass = keyClass;
    }

    public Class<V> getValueClass()
    {
        return valueClass;
    }

    public void setValueClass(Class<V> valueClass)
    {
        this.valueClass = valueClass;
    }

    public Method getGetter()
    {
        return getter;
    }

    public void setGetter(Method getter)
    {
        this.getter = getter;
    }

    public Method getSetter()
    {
        return setter;
    }

    public void setSetter(Method setter)
    {
        this.setter = setter;
    }

    // TODO to be removed
    public CompoundKeyProperties getCompoundKeyProperties()
    {
        return compoundKeyProperties;
    }

    public void setCompoundKeyProperties(CompoundKeyProperties multiKeyProperties)
    {
        this.compoundKeyProperties = multiKeyProperties;
    }

    public Class<?> getIdClass()
    {
        return idClass;
    }

    public void setIdClass(Class<?> idClass)
    {
        this.idClass = idClass;
    }

    public K getKey(Object object)
    {
        return keyClass.cast(object);
    }

    public JoinProperties getJoinProperties()
    {
        return joinProperties;
    }

    public void setJoinProperties(JoinProperties joinProperties)
    {
        this.joinProperties = joinProperties;
    }

    public boolean isCompound() {
        return compound;
    }

    public void setCompound(boolean compound) {
        this.compound = compound;
    }

    public void setObjectMapper(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    public CounterProperties getCounterProperties()
    {
        return counterProperties;
    }

    public void setCounterProperties(CounterProperties counterProperties)
    {
        this.counterProperties = counterProperties;
    }

    public void setConsistencyLevels(Pair<ConsistencyLevel, ConsistencyLevel> consistencyLevels)
    {
        this.consistencyLevels = consistencyLevels;
    }

    public String getExternalTableName()
    {
        return externalTableName;
    }

    public String getCQLExternalTableName()
    {
        return externalTableName.toLowerCase();
    }

    public void setExternalTableName(String externalTableName)
    {
        this.externalTableName = externalTableName;
    }

    public String getEntityClassName()
    {
        return entityClassName;
    }

    public void setEntityClassName(String entityClassName)
    {
        this.entityClassName = entityClassName;
    }

    @Override
    public String toString()
    {
        StringBuilder description = new StringBuilder();
        description.append("PropertyMeta [type=").append(type).append(", ");
        description.append("propertyName=").append(propertyName).append(", ");
        description.append("entityClassName=").append(entityClassName).append(", ");
        if (keyClass != null)
            description.append("keyClass=").append(keyClass.getCanonicalName()).append(", ");

        description.append("valueClass=").append(valueClass.getCanonicalName()).append(", ");

        if (counterProperties != null)
            description.append("counterProperties=").append(counterProperties).append(", ");

        if (joinProperties != null)
            description.append("joinProperties=").append(joinProperties).append(", ");

        if (compoundKeyProperties != null)
            description.append("multiKeyProperties=").append(compoundKeyProperties).append(", ");

        if (StringUtils.isNotBlank(externalTableName))
            description.append("externalCfName=").append(externalTableName).append(", ");

        if (consistencyLevels != null)
        {
            description
                    .append("consistencyLevels=[")
                    .append(consistencyLevels.left.name())
                    .append(",");
            description.append(consistencyLevels.right.name()).append("], ");
        }
        description.append("compound=").append(compound).append("]");

        return description.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityClassName == null) ? 0 : entityClassName.hashCode());
        result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PropertyMeta<?, ?> other = (PropertyMeta<?, ?>) obj;
        if (entityClassName == null)
        {
            if (other.entityClassName != null)
                return false;
        }
        else if (!entityClassName.equals(other.entityClassName))
            return false;
        if (propertyName == null)
        {
            if (other.propertyName != null)
                return false;
        }
        else if (!propertyName.equals(other.propertyName))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

}
