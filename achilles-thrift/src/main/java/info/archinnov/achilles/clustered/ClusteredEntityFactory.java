package info.archinnov.achilles.clustered;

import info.archinnov.achilles.composite.ThriftCompositeTransformer;
import info.archinnov.achilles.context.ThriftPersistenceContext;
import info.archinnov.achilles.dao.ThriftGenericEntityDao;
import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.entity.operations.ThriftJoinEntityLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.prettyprint.hector.api.beans.Composite;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.HCounterColumn;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class ClusteredEntityFactory
{

    private ThriftCompositeTransformer transformer = new ThriftCompositeTransformer();
    private ThriftJoinEntityLoader joinHelper = new ThriftJoinEntityLoader();

    public <T> List<T> buildClusteredEntities(Class<T> entityClass,
            ThriftPersistenceContext context,
            List<HColumn<Composite, Object>> hColumns)
    {
        boolean isJoin = context.isValueless() ? false : context.getFirstMeta().isJoin();
        if (hColumns.isEmpty())
        {
            return new ArrayList<T>();
        }
        else if (isJoin)
        {
            return buildJoinClusteredEntities(entityClass, context, hColumns);
        }
        else
        {
            return buildSimpleClusteredEntities(entityClass, context, hColumns);
        }
    }

    private <T> List<T> buildSimpleClusteredEntities(Class<T> entityClass,
            ThriftPersistenceContext context,
            List<HColumn<Composite, Object>> hColumns)
    {
        Function<HColumn<Composite, Object>, T> function;
        if (context.isValueless())
        {

            function = transformer.valuelessClusteredEntityTransformer(entityClass, context);
        }
        else
        {
            function = transformer.clusteredEntityTransformer(entityClass, context);
        }

        return Lists.transform(hColumns, function);
    }

    private <T> List<T> buildJoinClusteredEntities(Class<T> entityClass,
            ThriftPersistenceContext context,
            List<HColumn<Composite, Object>> hColumns)
    {
        PropertyMeta pm = context.getFirstMeta();
        EntityMeta joinMeta = pm.joinMeta();

        List<Object> joinIds = Lists.transform(hColumns, transformer.buildRawValueTransformer());
        Map<Object, Object> joinEntitiesMap = loadJoinEntities(context, pm, joinMeta, joinIds);

        Function<HColumn<Composite, Object>, T> function = transformer
                .joinClusteredEntityTransformer(entityClass, context, joinEntitiesMap);

        return Lists.transform(hColumns, function);
    }

    public <T> List<T> buildCounterClusteredEntities(Class<T> entityClass,
            ThriftPersistenceContext context,
            List<HCounterColumn<Composite>> hColumns)
    {
        Function<HCounterColumn<Composite>, T> function = transformer
                .counterClusteredEntityTransformer(
                        entityClass, context);

        return Lists.transform(hColumns, function);
    }

    private Map<Object, Object> loadJoinEntities(ThriftPersistenceContext context,
            PropertyMeta pm,
            EntityMeta joinMeta, List<Object> joinIds)
    {
        ThriftGenericEntityDao joinEntityDao = context.findEntityDao(joinMeta.getTableName());

        Map<Object, Object> joinEntities = joinHelper.loadJoinEntities((Class<Object>) pm.getValueClass(), joinIds,
                joinMeta, joinEntityDao);

        return joinEntities;
    }
}
