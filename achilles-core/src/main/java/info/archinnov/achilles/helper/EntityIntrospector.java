package info.archinnov.achilles.helper;

import static info.archinnov.achilles.helper.LoggerHelper.*;
import info.archinnov.achilles.annotations.Consistency;
import info.archinnov.achilles.configuration.ConfigurationParameters;
import info.archinnov.achilles.consistency.AchillesConsistencyLevelPolicy;
import info.archinnov.achilles.entity.parsing.PropertyFilter;
import info.archinnov.achilles.exception.AchillesBeanMappingException;
import info.archinnov.achilles.table.TableNameNormalizer;
import info.archinnov.achilles.type.ConsistencyLevel;
import info.archinnov.achilles.type.Counter;
import info.archinnov.achilles.type.Pair;
import info.archinnov.achilles.type.WideMap;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Table;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;

/**
 * EntityIntrospector
 * 
 * @author DuyHai DOAN
 * 
 */
public class EntityIntrospector {
    private static final Logger log = LoggerFactory.getLogger(EntityIntrospector.class);

    private PropertyFilter filter = new PropertyFilter();

    protected String[] deriveGetterName(Field field) {
        log.debug("Derive getter name for field {} from class {}", field.getName(), field
                .getDeclaringClass()
                .getCanonicalName());

        String camelCase = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);

        String[] getters;

        if (StringUtils.equals(field.getType().toString(), "boolean")) {
            getters = new String[] { "is" + camelCase, "get" + camelCase };
        } else {
            getters = new String[] { "get" + camelCase };
        }
        if (log.isTraceEnabled()) {
            log.trace("Derived getters : {}", StringUtils.join(getters, ","));
        }
        return getters;
    }

    protected String deriveSetterName(Field field) {
        log.debug("Derive setter name for field {} from class {}", field.getName(), field
                .getDeclaringClass()
                .getCanonicalName());

        String fieldName = field.getName();
        String setter = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        log.trace("Derived setter : {}", setter);
        return setter;
    }

    public Method findGetter(Class<?> beanClass, Field field) {
        log.debug("Find getter for field {} in class {}", field.getName(), beanClass.getCanonicalName());

        Method getterMethod = null;
        String fieldName = field.getName();
        String[] getters = this.deriveGetterName(field);

        for (String getter : getters) {
            try {
                getterMethod = beanClass.getMethod(getter);
                if (getterMethod.getReturnType() != field.getType()) {
                    throw new AchillesBeanMappingException("The getter for field '" + fieldName + "' of type '"
                            + field.getDeclaringClass().getCanonicalName() + "' does not return correct type");
                }
            } catch (NoSuchMethodException e) {
                // Do nothing here
            }
        }
        if (getterMethod == null) {
            throw new AchillesBeanMappingException("The getter for field '" + fieldName + "' of type '"
                    + field.getDeclaringClass().getCanonicalName() + "' does not exist");
        }

        log.trace("Derived getter method : {}", getterMethod.getName());
        return getterMethod;
    }

    public Method findSetter(Class<?> beanClass, Field field) {
        log.debug("Find setter for field {} in class {}", field.getName(), beanClass.getCanonicalName());

        String fieldName = field.getName();

        try {
            String setter = this.deriveSetterName(field);
            Method setterMethod = beanClass.getMethod(setter, field.getType());

            if (!setterMethod.getReturnType().toString().equals("void")) {
                throw new AchillesBeanMappingException("The setter for field '" + fieldName + "' of type '"
                        + field.getDeclaringClass().getCanonicalName()
                        + "' does not return correct type or does not have the correct parameter");
            }

            log.trace("Derived setter method : {}", setterMethod.getName());
            return setterMethod;

        } catch (NoSuchMethodException e) {
            throw new AchillesBeanMappingException("The setter for field '" + fieldName + "' of type '"
                    + field.getDeclaringClass().getCanonicalName() + "' does not exist or is incorrect");
        }
    }

    public Method[] findAccessors(Class<?> beanClass, Field field) {
        log.debug("Find accessors for field {} in class {}", field.getName(), beanClass.getCanonicalName());

        Method[] accessors = new Method[2];

        accessors[0] = findGetter(beanClass, field);
        if (field.getType() == WideMap.class || field.getType() == Counter.class) {
            accessors[1] = null;
        } else {
            accessors[1] = findSetter(beanClass, field);
        }

        return accessors;
    }

    public String inferColumnFamilyName(Class<?> entity, String canonicalName) {
        String columnFamilyName = null;
        Table table = entity.getAnnotation(javax.persistence.Table.class);
        if (table != null) {
            if (StringUtils.isNotBlank(table.name())) {
                columnFamilyName = table.name();
            }
        }

        if (!StringUtils.isBlank(columnFamilyName)) {
            columnFamilyName = TableNameNormalizer.normalizerAndValidateColumnFamilyName(columnFamilyName);
        } else {
            columnFamilyName = TableNameNormalizer.normalizerAndValidateColumnFamilyName(canonicalName);
        }

        log.debug("Inferred columnFamilyName for entity {} : {}", canonicalName, columnFamilyName);
        return columnFamilyName;
    }

    public <T> Pair<ConsistencyLevel, ConsistencyLevel> findConsistencyLevels(Class<T> entity,
            AchillesConsistencyLevelPolicy policy) {
        log.debug("Find consistency levels for entity class {}", entity.getCanonicalName());

        ConsistencyLevel defaultGlobalRead = getDefaultGlobalReadConsistency(policy);
        ConsistencyLevel defaultGlobalWrite = getDefaultGlobalWriteConsistency(policy);

        Consistency clevel = entity.getAnnotation(Consistency.class);

        if (clevel != null) {
            defaultGlobalRead = clevel.read();
            defaultGlobalWrite = clevel.write();
        }

        log.trace("Found consistency levels : {}/{}", defaultGlobalRead, defaultGlobalWrite);

        return new Pair<ConsistencyLevel, ConsistencyLevel>(defaultGlobalRead, defaultGlobalWrite);
    }

    public List<Field> getInheritedPrivateFields(Class<?> type) {
        log.debug("Find inherited private fields from hierarchy for entity class {}", type.getCanonicalName());

        List<Field> fields = new ArrayList<Field>();

        Class<?> i = type;
        while (i != null && i != Object.class) {
            for (Field declaredField : i.getDeclaredFields()) {
                if (filter.matches(declaredField)) {
                    fields.add(declaredField);
                }
            }
            i = i.getSuperclass();
        }
        if (log.isTraceEnabled()) {
            log.trace("Found inherited private fields : {}", Lists.transform(fields, fieldToStringFn));
        }
        return fields;
    }

    public Field getInheritedPrivateFields(Class<?> type, Class<?> annotation) {
        log.debug("Find private field from hierarchy with annotation {} for entity class {}",
                annotation.getCanonicalName(), type.getCanonicalName());

        Class<?> i = type;
        while (i != null && i != Object.class) {
            for (Field declaredField : i.getDeclaredFields()) {
                if (filter.matches(declaredField, annotation)) {
                    log.trace("Found inherited private field : {}", declaredField);
                    return declaredField;
                }
            }
            i = i.getSuperclass();
        }
        return null;
    }

    public Field getInheritedPrivateFields(Class<?> type, Class<?> annotation, String name) {
        log.debug("Find private field with name {} having annotation {} from hierarchy for entity class {}", name,
                annotation.getCanonicalName(), type.getCanonicalName());

        Class<?> i = type;
        while (i != null && i != Object.class) {
            for (Field declaredField : i.getDeclaredFields()) {
                if (filter.matches(declaredField, annotation, name)) {
                    log.trace("Found inherited private field : {}", declaredField);
                    return declaredField;
                }
            }
            i = i.getSuperclass();
        }
        return null;
    }

    public ConsistencyLevel getDefaultGlobalReadConsistency(AchillesConsistencyLevelPolicy policy) {
        return policy.getDefaultGlobalReadConsistencyLevel() != null ? policy.getDefaultGlobalReadConsistencyLevel() : ConfigurationParameters.DEFAULT_LEVEL;
    }

    public ConsistencyLevel getDefaultGlobalWriteConsistency(AchillesConsistencyLevelPolicy policy) {
        return policy.getDefaultGlobalWriteConsistencyLevel() != null ? policy
                .getDefaultGlobalWriteConsistencyLevel() : ConfigurationParameters.DEFAULT_LEVEL;
    }
}
