package info.archinnov.achilles.entity.manager;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import info.archinnov.achilles.consistency.AchillesConsistencyLevelPolicy;
import info.archinnov.achilles.context.CQLDaoContext;
import info.archinnov.achilles.context.CQLPersistenceContext;
import info.archinnov.achilles.context.ConfigurationContext;
import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.entity.metadata.PropertyType;
import info.archinnov.achilles.entity.operations.CQLEntityProxifier;
import info.archinnov.achilles.test.builders.CompleteBeanTestBuilder;
import info.archinnov.achilles.test.builders.PropertyMetaTestBuilder;
import info.archinnov.achilles.test.mapping.entity.CompleteBean;
import info.archinnov.achilles.type.ConsistencyLevel;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import com.google.common.base.Optional;

/**
 * CqlEntityManagerTest
 * 
 * @author DuyHai DOAN
 * 
 */

@RunWith(MockitoJUnitRunner.class)
public class CQLEntityManagerTest
{
    private CQLEntityManager manager;

    @Mock
    private CQLEntityManagerFactory factory;

    @Mock
    private CQLEntityProxifier proxifier;

    @Mock
    private CQLDaoContext daoContext;

    @Mock
    private ConfigurationContext configContext;

    @Mock
    private AchillesConsistencyLevelPolicy policy;

    private Map<Class<?>, EntityMeta> entityMetaMap = new HashMap<Class<?>, EntityMeta>();

    private EntityMeta meta;

    private PropertyMeta<?, ?> idMeta;

    private CompleteBean entity = CompleteBeanTestBuilder.builder().randomId().buid();

    private Optional<ConsistencyLevel> noConsistency = Optional.<ConsistencyLevel> absent();
    private Optional<Integer> noTtl = Optional.<Integer> absent();

    @Before
    public void setUp() throws Exception
    {
        idMeta = PropertyMetaTestBuilder
                .completeBean(Void.class, Long.class)
                .field("id")
                .accessors()
                .type(PropertyType.SIMPLE)
                .build();

        meta = new EntityMeta();
        meta.setIdMeta(idMeta);
        meta.setTableName("table");
        meta.setEntityClass(CompleteBean.class);

        when(configContext.getConsistencyPolicy()).thenReturn(policy);
        when(policy.getDefaultGlobalReadConsistencyLevel()).thenReturn(ConsistencyLevel.EACH_QUORUM);

        manager = new CQLEntityManager(factory, entityMetaMap, configContext, daoContext);
        Whitebox.setInternalState(manager, CQLEntityProxifier.class, proxifier);

        manager.setEntityMetaMap(entityMetaMap);
        entityMetaMap.put(CompleteBean.class, meta);
    }

    @Test
    public void should_init_persistence_context_with_entity() throws Exception
    {
        when((Class<CompleteBean>) proxifier.deriveBaseClass(entity))
                .thenReturn(CompleteBean.class);

        CQLPersistenceContext context = manager.initPersistenceContext(entity, noConsistency,
                noConsistency, noTtl);

        assertThat(context.getConfigContext()).isSameAs(configContext);
        assertThat(context.getEntity()).isSameAs(entity);
        assertThat(context.getEntityMeta()).isSameAs(meta);
        assertThat(context.getPrimaryKey()).isEqualTo(entity.getId());
        assertThat(context.getTableName()).isEqualTo("table");
    }

    @Test
    public void should_init_persistence_context_with_type_and_id() throws Exception
    {
        CQLPersistenceContext context = manager.initPersistenceContext(CompleteBean.class,
                entity.getId(), noConsistency, noConsistency, noTtl);

        assertThat(context.getConfigContext()).isSameAs(configContext);
        assertThat(context.getEntity()).isNull();
        assertThat(context.getEntityMeta()).isSameAs(meta);
        assertThat(context.getPrimaryKey()).isEqualTo(entity.getId());
        assertThat(context.getTableName()).isEqualTo("table");
    }
}
