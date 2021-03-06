package info.archinnov.achilles.helper;

import info.archinnov.achilles.annotations.Consistency;
import info.archinnov.achilles.annotations.Lazy;
import info.archinnov.achilles.consistency.AchillesConsistencyLevelPolicy;
import info.archinnov.achilles.exception.AchillesBeanMappingException;
import info.archinnov.achilles.type.ConsistencyLevel;
import org.apache.cassandra.utils.Pair;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PropertyHelper
 * 
 * @author DuyHai DOAN
 * 
 */
public class PropertyHelper
{
    private static final Logger log = LoggerFactory.getLogger(PropertyHelper.class);

    public static Set<Class<?>> allowedTypes = new HashSet<Class<?>>();
    protected EntityIntrospector entityIntrospector = new EntityIntrospector();

    static
    {
        // Bytes
        allowedTypes.add(byte[].class);
        allowedTypes.add(ByteBuffer.class);

        // Boolean
        allowedTypes.add(Boolean.class);
        allowedTypes.add(boolean.class);

        // Date
        allowedTypes.add(Date.class);

        // Double
        allowedTypes.add(Double.class);
        allowedTypes.add(double.class);

        // Char
        allowedTypes.add(Character.class);

        // Float
        allowedTypes.add(Float.class);
        allowedTypes.add(float.class);

        // Integer
        allowedTypes.add(BigInteger.class);
        allowedTypes.add(Integer.class);
        allowedTypes.add(int.class);

        // Long
        allowedTypes.add(Long.class);
        allowedTypes.add(long.class);

        // Short
        allowedTypes.add(Short.class);
        allowedTypes.add(short.class);

        // String
        allowedTypes.add(String.class);

        // UUID
        allowedTypes.add(UUID.class);

    }

    public PropertyHelper() {
    }

    public <T> Class<T> inferValueClassForListOrSet(Type genericType, Class<?> entityClass)
    {
        log.debug("Infer parameterized value class for collection type {} of entity class {} ",
                genericType.toString(), entityClass.getCanonicalName());

        Class<T> valueClass;
        if (genericType instanceof ParameterizedType)
        {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type[] actualTypeArguments = pt.getActualTypeArguments();
            if (actualTypeArguments.length > 0)
            {
                Type type = actualTypeArguments[actualTypeArguments.length - 1];
                valueClass = getClassFromType(type);
            }
            else
            {
                throw new AchillesBeanMappingException("The type '"
                        + genericType.getClass().getCanonicalName()
                        + "' of the entity '" + entityClass.getCanonicalName()
                        + "' should be parameterized");
            }
        }
        else
        {
            throw new AchillesBeanMappingException("The type '"
                    + genericType.getClass().getCanonicalName()
                    + "' of the entity '" + entityClass.getCanonicalName()
                    + "' should be parameterized");
        }

        log.trace("Inferred value class : {}", valueClass.getCanonicalName());

        return valueClass;
    }

    public boolean isLazy(Field field)
    {
        log.debug("Check @Lazy annotation on field {} of class {}", field.getName(), field
                .getDeclaringClass()
                .getCanonicalName());

        boolean lazy = false;
        if (field.getAnnotation(Lazy.class) != null)
        {
            lazy = true;
        }
        return lazy;
    }

    public boolean hasConsistencyAnnotation(Field field)
    {
        log.debug("Check @Consistency annotation on field {} of class {}", field.getName(), field
                .getDeclaringClass()
                .getCanonicalName());

        boolean consistency = false;
        if (field.getAnnotation(Consistency.class) != null)
        {
            consistency = true;
        }
        return consistency;
    }

    public static <T> boolean isSupportedType(Class<T> valueClass)
    {
        return allowedTypes.contains(valueClass);
    }

    public <T> Pair<ConsistencyLevel, ConsistencyLevel> findConsistencyLevels(Field field,
            AchillesConsistencyLevelPolicy policy)
    {
        log.debug("Find consistency configuration for field {} of class {}", field.getName(), field
                .getDeclaringClass()
                .getCanonicalName());

        Consistency clevel = field.getAnnotation(Consistency.class);

        ConsistencyLevel defaultGlobalRead = entityIntrospector
                .getDefaultGlobalReadConsistency(policy);
        ConsistencyLevel defaultGlobalWrite = entityIntrospector
                .getDefaultGlobalWriteConsistency(policy);

        if (clevel != null)
        {
            defaultGlobalRead = clevel.read();
            defaultGlobalWrite = clevel.write();
        }

        log.trace("Found consistency levels : {} / {}", defaultGlobalRead, defaultGlobalWrite);
        return Pair.create(defaultGlobalRead, defaultGlobalWrite);
    }

    public <T> Class<T> getClassFromType(Type type)
    {
        if (type instanceof ParameterizedType)
        {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return (Class<T>) parameterizedType.getRawType();
        }
        else if (type instanceof Class)
        {
            return (Class<T>) type;
        }
        else
        {
            throw new IllegalArgumentException("Cannot determine java class of type '" + type + "'");
        }
    }
}
