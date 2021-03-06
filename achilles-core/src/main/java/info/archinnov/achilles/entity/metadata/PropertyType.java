package info.archinnov.achilles.entity.metadata;

import info.archinnov.achilles.entity.metadata.util.PropertyTypeExclude;
import info.archinnov.achilles.entity.metadata.util.PropertyTypeFilter;
import java.util.Set;
import com.google.common.collect.Sets;

/**
 * PropertyType
 * 
 * @author DuyHai DOAN
 * 
 */
public enum PropertyType {

    ID(5), //
    EMBEDDED_ID(10), //
    SIMPLE(10), //
    LIST(10), //
    SET(10), //
    MAP(10), //
    LAZY_SIMPLE(30), //
    COUNTER(30), //
    LAZY_LIST(30), //
    LAZY_SET(30), //
    LAZY_MAP(30), //
    JOIN_SIMPLE(30), //
    JOIN_LIST(30), //
    JOIN_SET(30), //
    JOIN_MAP(30);

    private final int flag;

    PropertyType(int flag) {
        this.flag = flag;
    }

    public byte[] flag() {
        return new byte[] { (byte) flag };
    }

    public boolean isLazy() {
        return (this == COUNTER //
                || this == LAZY_SIMPLE //
                || this == LAZY_LIST //
                || this == LAZY_SET //
                || this == LAZY_MAP //
                || this == JOIN_SIMPLE //
                || this == JOIN_LIST //
                || this == JOIN_SET //
        || this == JOIN_MAP);
    }

    public boolean isJoin() {
        return (this == JOIN_SIMPLE //
                || this == JOIN_LIST //
                || this == JOIN_SET //
        || this == JOIN_MAP //
        );
    }

    public boolean isCounter() {
        return (this == COUNTER);
    }

    public boolean isEmbeddedId() {
        return this == EMBEDDED_ID;
    }

    public static PropertyType[] nonProxyJoinTypes() {
        return new PropertyType[] { JOIN_SIMPLE, JOIN_LIST, JOIN_SET, JOIN_MAP };
    }

    public boolean isValidClusteredValueType() {
        return (this == SIMPLE || this == JOIN_SIMPLE || this == COUNTER);
    }

    public static PropertyTypeFilter joinPropertyType = new PropertyTypeFilter(nonProxyJoinTypes());
    public static PropertyTypeFilter joinSimpleType = new PropertyTypeFilter(JOIN_SIMPLE);
    public static PropertyTypeFilter joinCollectionType = new PropertyTypeFilter(JOIN_LIST, JOIN_SET);
    public static PropertyTypeFilter joinMapType = new PropertyTypeFilter(JOIN_MAP);
    public static PropertyTypeFilter counterType = new PropertyTypeFilter(COUNTER);

    public static PropertyTypeFilter eagerType = new PropertyTypeFilter(ID, EMBEDDED_ID, SIMPLE, LIST, SET, MAP);
    public static PropertyTypeFilter lazyType = new PropertyTypeFilter(LAZY_SIMPLE, LAZY_LIST,
            LAZY_SET, LAZY_MAP, JOIN_SIMPLE, JOIN_LIST, JOIN_SET, JOIN_MAP, COUNTER);

    public static PropertyTypeExclude excludeIdType = new PropertyTypeExclude(ID, EMBEDDED_ID);

    public static PropertyTypeExclude excludeCounterType = new PropertyTypeExclude(COUNTER);

    public static Set<PropertyType> multiValuesNonProxyTypes = Sets.newHashSet(LIST, LAZY_LIST, SET, LAZY_SET, MAP,
            LAZY_MAP, JOIN_LIST, JOIN_SET, JOIN_MAP);

}
