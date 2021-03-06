package info.archinnov.achilles.composite;

import static info.archinnov.achilles.serializer.ThriftSerializerUtils.*;
import info.archinnov.achilles.compound.CompoundKeyValidator;
import info.archinnov.achilles.compound.ThriftCompoundKeyMapper;
import info.archinnov.achilles.compound.ThriftCompoundKeyValidator;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.type.BoundingMode;
import info.archinnov.achilles.type.OrderingMode;
import java.util.List;
import me.prettyprint.hector.api.beans.AbstractComposite.ComponentEquality;
import me.prettyprint.hector.api.beans.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThriftCompositeFactory
 * 
 * @author DuyHai DOAN
 * 
 */
public class ThriftCompositeFactory
{
    private static final Logger log = LoggerFactory.getLogger(ThriftCompositeFactory.class);

    private ComponentEqualityCalculator calculator = new ComponentEqualityCalculator();
    private ThriftCompoundKeyMapper compoundKeyMapper = new ThriftCompoundKeyMapper();
    private CompoundKeyValidator compoundKeyValidator = new ThriftCompoundKeyValidator();

    public <K, V, T> Composite createCompositeForClustered(PropertyMeta propertyMeta, T key)
    {
        log.trace("Creating base composite for propertyMeta {}", propertyMeta.getPropertyName());
        return compoundKeyMapper.fromCompoundToCompositeForInsertOrGet(key, propertyMeta);
    }

    public Composite[] createForClusteredQuery(PropertyMeta idMeta,
            List<Object> clusteringFrom, List<Object> clusteringTo, BoundingMode bounding,
            OrderingMode ordering)
    {

        compoundKeyValidator.validateComponentsForSliceQuery(idMeta, clusteringFrom,
                clusteringTo, ordering);
        ComponentEquality[] equalities = calculator.determineEquality(bounding, ordering);

        final Composite from = compoundKeyMapper.fromComponentsToCompositeForQuery(
                clusteringFrom,
                idMeta, equalities[0]);
        final Composite to = compoundKeyMapper.fromComponentsToCompositeForQuery(
                clusteringTo, idMeta,
                equalities[1]);

        return new Composite[]
        {
                from,
                to
        };

    }

    public Composite createKeyForCounter(String fqcn, Object key, PropertyMeta idMeta)
    {
        log.trace("Creating composite counter row key for entity class {} and primary key {}",
                fqcn, key);

        Composite comp = new Composite();
        comp.setComponent(0, fqcn, STRING_SRZ);
        comp.setComponent(1, idMeta.writeValueToString(key), STRING_SRZ);
        return comp;
    }

    public <K, V> Composite createBaseForGet(PropertyMeta propertyMeta)
    {
        log
                .trace("Creating base composite for propertyMeta {} get",
                        propertyMeta.getPropertyName());

        Composite composite = new Composite();
        composite.addComponent(0, propertyMeta.type().flag(), ComponentEquality.EQUAL);
        composite.addComponent(1, propertyMeta.getPropertyName(), ComponentEquality.EQUAL);
        composite.addComponent(2, 0, ComponentEquality.EQUAL);
        return composite;
    }

    public Composite createBaseForClusteredGet(Object compoundKey, PropertyMeta idMeta)
    {
        return compoundKeyMapper.fromCompoundToCompositeForInsertOrGet(compoundKey, idMeta);
    }

    public <K, V> Composite createBaseForCounterGet(PropertyMeta propertyMeta)
    {
        log
                .trace("Creating base composite for propertyMeta {} get",
                        propertyMeta.getPropertyName());

        Composite composite = new Composite();
        composite.addComponent(0, propertyMeta.getPropertyName(), ComponentEquality.EQUAL);
        return composite;
    }

    public <K, V> Composite createBaseForQuery(PropertyMeta propertyMeta,
            ComponentEquality equality)
    {
        log.trace("Creating base composite for propertyMeta {} query and equality {}",
                propertyMeta.getPropertyName(), equality.name());

        Composite composite = new Composite();
        composite.addComponent(0, propertyMeta.type().flag(), ComponentEquality.EQUAL);
        composite.addComponent(1, propertyMeta.getPropertyName(), equality);
        return composite;
    }

    public <K, V> Composite createForBatchInsertSingleValue(PropertyMeta propertyMeta)
    {
        log.trace("Creating base composite for propertyMeta {} for single value batch insert",
                propertyMeta.getPropertyName());

        Composite composite = new Composite();
        composite.setComponent(0, propertyMeta.type().flag(), BYTE_SRZ, BYTE_SRZ
                .getComparatorType()
                .getTypeName());
        composite.setComponent(1, propertyMeta.getPropertyName(), STRING_SRZ, STRING_SRZ
                .getComparatorType()
                .getTypeName());
        composite.setComponent(2, 0, INT_SRZ, INT_SRZ.getComparatorType().getTypeName());
        return composite;
    }

    public <K, V> Composite createForBatchInsertSingleCounter(PropertyMeta propertyMeta)
    {
        log
                .trace("Creating base composite for propertyMeta {} for single counter value batch insert",
                        propertyMeta.getPropertyName());

        Composite composite = new Composite();
        composite.setComponent(0, propertyMeta.getPropertyName(), STRING_SRZ, STRING_SRZ
                .getComparatorType()
                .getTypeName());
        return composite;
    }

    public <K, V> Composite createForBatchInsertMultiValue(PropertyMeta propertyMeta,
            int hashOrPosition)
    {
        log
                .trace("Creating base composite for propertyMeta {} for multi value batch insert with hash or position {}",
                        propertyMeta.getPropertyName(), hashOrPosition);

        Composite composite = new Composite();
        composite.setComponent(0, propertyMeta.type().flag(), BYTE_SRZ, BYTE_SRZ
                .getComparatorType()
                .getTypeName());
        composite.setComponent(1, propertyMeta.getPropertyName(), STRING_SRZ, STRING_SRZ
                .getComparatorType()
                .getTypeName());
        composite.setComponent(2, hashOrPosition, INT_SRZ, INT_SRZ
                .getComparatorType()
                .getTypeName());
        return composite;
    }

}
