package info.archinnov.achilles.entity.parsing;

import static info.archinnov.achilles.entity.metadata.PropertyType.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import info.archinnov.achilles.consistency.AchillesConsistencyLevelPolicy;
import info.archinnov.achilles.context.ConfigurationContext;
import info.archinnov.achilles.context.ConfigurationContext.Impl;
import info.archinnov.achilles.entity.metadata.CounterProperties;
import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.JoinProperties;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.entity.metadata.PropertyType;
import info.archinnov.achilles.entity.parsing.context.EntityParsingContext;
import info.archinnov.achilles.exception.AchillesBeanMappingException;
import info.archinnov.achilles.json.ObjectMapperFactory;
import info.archinnov.achilles.table.TableCreator;
import info.archinnov.achilles.test.parser.entity.Bean;
import info.archinnov.achilles.test.parser.entity.BeanWithClusteredId;
import info.archinnov.achilles.test.parser.entity.BeanWithColumnFamilyName;
import info.archinnov.achilles.test.parser.entity.BeanWithDuplicatedColumnName;
import info.archinnov.achilles.test.parser.entity.BeanWithDuplicatedJoinColumnName;
import info.archinnov.achilles.test.parser.entity.BeanWithJoinColumnAsEntity;
import info.archinnov.achilles.test.parser.entity.BeanWithNoId;
import info.archinnov.achilles.test.parser.entity.BeanWithSimpleCounter;
import info.archinnov.achilles.test.parser.entity.ChildBean;
import info.archinnov.achilles.test.parser.entity.ClusteredEntity;
import info.archinnov.achilles.test.parser.entity.ClusteredEntityWithJoin;
import info.archinnov.achilles.test.parser.entity.ClusteredEntityWithNotSupportedPropertyType;
import info.archinnov.achilles.test.parser.entity.ClusteredEntityWithTwoProperties;
import info.archinnov.achilles.test.parser.entity.CompoundKey;
import info.archinnov.achilles.test.parser.entity.UserBean;
import info.archinnov.achilles.type.ConsistencyLevel;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.CascadeType;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * AchillesEntityParserTest
 * 
 * @author DuyHai DOAN
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityParserTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @InjectMocks
    private EntityParser parser;

    private Map<PropertyMeta, Class<?>> joinPropertyMetaToBeFilled = new HashMap<PropertyMeta, Class<?>>();

    @Mock
    private AchillesConsistencyLevelPolicy policy;

    @Mock
    private TableCreator thriftTableCreator;

    @Mock
    private Map<Class<?>, EntityMeta> entityMetaMap;

    private ConfigurationContext configContext = new ConfigurationContext();

    private ObjectMapperFactory objectMapperFactory = new ObjectMapperFactory() {
        @Override
        public <T> ObjectMapper getMapper(Class<T> type) {
            return objectMapper;
        }

    };
    private ObjectMapper objectMapper = new ObjectMapper();

    private EntityParsingContext entityContext;

    @Before
    public void setUp() {
        joinPropertyMetaToBeFilled.clear();
        configContext.setConsistencyPolicy(policy);
        configContext.setObjectMapperFactory(objectMapperFactory);

        when(policy.getDefaultGlobalReadConsistencyLevel()).thenReturn(ConsistencyLevel.ONE);
        when(policy.getDefaultGlobalWriteConsistencyLevel()).thenReturn(ConsistencyLevel.ALL);
    }

    @Test
    public void should_parse_entity() throws Exception {

        initEntityParsingContext(Bean.class);
        EntityMeta meta = parser.parseEntity(entityContext);

        assertThat(meta.getClassName()).isEqualTo("info.archinnov.achilles.test.parser.entity.Bean");
        assertThat(meta.getTableName()).isEqualTo("Bean");
        assertThat((Class<Long>) meta.getIdMeta().getValueClass()).isEqualTo(Long.class);
        assertThat(meta.getIdMeta().getPropertyName()).isEqualTo("id");
        assertThat((Class<Long>) meta.getIdClass()).isEqualTo(Long.class);
        assertThat(meta.getPropertyMetas()).hasSize(7);

        PropertyMeta id = meta.getPropertyMetas().get("id");
        PropertyMeta name = meta.getPropertyMetas().get("name");
        PropertyMeta age = meta.getPropertyMetas().get("age_in_year");
        PropertyMeta friends = (PropertyMeta) meta.getPropertyMetas().get("friends");
        PropertyMeta followers = (PropertyMeta) meta.getPropertyMetas().get("followers");
        PropertyMeta preferences = (PropertyMeta) meta.getPropertyMetas().get(
                "preferences");

        PropertyMeta creator = (PropertyMeta) meta.getPropertyMetas().get("creator");

        assertThat(id).isNotNull();
        assertThat(name).isNotNull();
        assertThat(age).isNotNull();
        assertThat(friends).isNotNull();
        assertThat(followers).isNotNull();
        assertThat(preferences).isNotNull();
        assertThat(creator).isNotNull();

        assertThat(id.getPropertyName()).isEqualTo("id");
        assertThat((Class<Long>) id.getValueClass()).isEqualTo(Long.class);
        assertThat(id.type()).isEqualTo(ID);
        assertThat(id.getReadConsistencyLevel()).isEqualTo(ConsistencyLevel.ONE);
        assertThat(id.getWriteConsistencyLevel()).isEqualTo(ConsistencyLevel.ALL);

        assertThat(name.getPropertyName()).isEqualTo("name");
        assertThat((Class<String>) name.getValueClass()).isEqualTo(String.class);
        assertThat(name.type()).isEqualTo(SIMPLE);
        assertThat(name.getReadConsistencyLevel()).isEqualTo(ConsistencyLevel.ONE);
        assertThat(name.getWriteConsistencyLevel()).isEqualTo(ConsistencyLevel.ALL);

        assertThat(age.getPropertyName()).isEqualTo("age_in_year");
        assertThat((Class<Long>) age.getValueClass()).isEqualTo(Long.class);
        assertThat(age.type()).isEqualTo(SIMPLE);
        assertThat(age.getReadConsistencyLevel()).isEqualTo(ConsistencyLevel.ONE);
        assertThat(age.getWriteConsistencyLevel()).isEqualTo(ConsistencyLevel.ALL);

        assertThat(friends.getPropertyName()).isEqualTo("friends");
        assertThat((Class) friends.getValueClass()).isEqualTo(String.class);
        assertThat(friends.type()).isEqualTo(PropertyType.LAZY_LIST);
        assertThat(friends.type().isLazy()).isTrue();
        assertThat(friends.getReadConsistencyLevel()).isEqualTo(ConsistencyLevel.ONE);
        assertThat(friends.getWriteConsistencyLevel()).isEqualTo(ConsistencyLevel.ALL);

        assertThat(followers.getPropertyName()).isEqualTo("followers");
        assertThat((Class) followers.getValueClass()).isEqualTo(String.class);
        assertThat(followers.type()).isEqualTo(PropertyType.SET);
        assertThat(followers.getReadConsistencyLevel()).isEqualTo(ConsistencyLevel.ONE);
        assertThat(followers.getWriteConsistencyLevel()).isEqualTo(ConsistencyLevel.ALL);

        assertThat(preferences.getPropertyName()).isEqualTo("preferences");
        assertThat((Class) preferences.getValueClass()).isEqualTo(String.class);
        assertThat(preferences.type()).isEqualTo(PropertyType.MAP);
        assertThat((Class) preferences.getKeyClass()).isEqualTo(Integer.class);
        assertThat(preferences.getReadConsistencyLevel()).isEqualTo(ConsistencyLevel.ONE);
        assertThat(preferences.getWriteConsistencyLevel()).isEqualTo(ConsistencyLevel.ALL);

        assertThat(creator.getPropertyName()).isEqualTo("creator");
        assertThat((Class) creator.getValueClass()).isEqualTo(UserBean.class);
        assertThat(creator.type()).isEqualTo(JOIN_SIMPLE);
        assertThat(creator.getJoinProperties().getCascadeTypes()).containsExactly(CascadeType.ALL);

        assertThat((Class) joinPropertyMetaToBeFilled.get(creator)).isEqualTo(UserBean.class);

        assertThat(meta.getReadConsistencyLevel()).isEqualTo(ConsistencyLevel.ONE);
        assertThat(meta.getWriteConsistencyLevel()).isEqualTo(ConsistencyLevel.ALL);

        assertThat(meta.getEagerMetas()).containsOnly(id, name, age, followers, preferences);
        assertThat(meta.getEagerGetters()).containsOnly(id.getGetter(), name.getGetter(), age.getGetter(),
                followers.getGetter(), preferences.getGetter());

        verify(policy).setConsistencyLevelForRead(ConsistencyLevel.ONE, meta.getTableName());
        verify(policy).setConsistencyLevelForWrite(ConsistencyLevel.ALL, meta.getTableName());
    }

    @Test
    public void should_parse_entity_with_embedded_id() throws Exception {
        initEntityParsingContext(BeanWithClusteredId.class);

        EntityMeta meta = parser.parseEntity(entityContext);

        assertThat(meta).isNotNull();

        assertThat((Class<CompoundKey>) meta.getIdClass()).isEqualTo(CompoundKey.class);
        PropertyMeta idMeta = (PropertyMeta) meta.getIdMeta();

        assertThat(idMeta.isEmbeddedId()).isTrue();
        assertThat(idMeta.getComponentClasses()).containsExactly(Long.class, String.class);

    }

    @Test
    public void should_parse_entity_with_table_name() throws Exception {

        initEntityParsingContext(BeanWithColumnFamilyName.class);

        EntityMeta meta = parser.parseEntity(entityContext);

        assertThat(meta).isNotNull();
        assertThat(meta.getTableName()).isEqualTo("myOwnCF");
    }

    @Test
    public void should_parse_inherited_bean() throws Exception {
        initEntityParsingContext(ChildBean.class);
        EntityMeta meta = parser.parseEntity(entityContext);

        assertThat(meta).isNotNull();
        assertThat(meta.getIdMeta().getPropertyName()).isEqualTo("id");
        assertThat(meta.getPropertyMetas().get("name").getPropertyName()).isEqualTo("name");
        assertThat(meta.getPropertyMetas().get("address").getPropertyName()).isEqualTo("address");
        assertThat(meta.getPropertyMetas().get("nickname").getPropertyName()).isEqualTo("nickname");
    }

    @Test
    public void should_parse_bean_with_simple_counter_field() throws Exception {
        initEntityParsingContext(BeanWithSimpleCounter.class);
        EntityMeta meta = parser.parseEntity(entityContext);

        assertThat(meta).isNotNull();
        assertThat(entityContext.getHasSimpleCounter()).isTrue();
        PropertyMeta idMeta = (PropertyMeta) meta.getIdMeta();
        assertThat(idMeta).isNotNull();
        PropertyMeta counterMeta = meta.getPropertyMetas().get("counter");
        assertThat(counterMeta).isNotNull();

        CounterProperties counterProperties = counterMeta.getCounterProperties();

        assertThat(counterProperties).isNotNull();
        assertThat(counterProperties.getFqcn()).isEqualTo(BeanWithSimpleCounter.class.getCanonicalName());
        assertThat((PropertyMeta) counterProperties.getIdMeta()).isSameAs(idMeta);
    }

    @Test
    public void should_exception_when_entity_has_no_id() throws Exception {
        initEntityParsingContext(BeanWithNoId.class);

        expectedEx.expect(AchillesBeanMappingException.class);
        expectedEx
                .expectMessage("The entity '"
                        + BeanWithNoId.class.getCanonicalName()
                        + "' should have at least one field with javax.persistence.Id/javax.persistence.EmbeddedId annotation");
        parser.parseEntity(entityContext);
    }

    @Test
    public void should_exception_when_entity_has_duplicated_column_name() throws Exception {
        initEntityParsingContext(BeanWithDuplicatedColumnName.class);
        expectedEx.expect(AchillesBeanMappingException.class);
        expectedEx.expectMessage("The property 'name' is already used for the entity '"
                + BeanWithDuplicatedColumnName.class.getCanonicalName() + "'");

        parser.parseEntity(entityContext);
    }

    @Test
    public void should_exception_when_entity_has_duplicated_join_column_name() throws Exception {
        initEntityParsingContext(BeanWithDuplicatedJoinColumnName.class);
        expectedEx.expect(AchillesBeanMappingException.class);
        expectedEx.expectMessage("The property 'name' is already used for the entity '"
                + BeanWithDuplicatedJoinColumnName.class.getCanonicalName() + "'");

        parser.parseEntity(entityContext);
    }

    @Test
    public void should_parse_wide_row() throws Exception {
        initEntityParsingContext(ClusteredEntity.class);
        EntityMeta meta = parser.parseEntity(entityContext);

        assertThat(meta.isClusteredEntity()).isTrue();

        assertThat(meta.getIdMeta().getPropertyName()).isEqualTo("id");
        assertThat((Class<CompoundKey>) meta.getIdMeta().getValueClass()).isEqualTo(CompoundKey.class);

        assertThat(meta.getPropertyMetas()).hasSize(2);
        assertThat(meta.getPropertyMetas().get("id").type()).isEqualTo(EMBEDDED_ID);
        assertThat(meta.getPropertyMetas().get("value").type()).isEqualTo(SIMPLE);
    }

    @Test
    public void should_parse_clustered_entity_with_join() throws Exception {
        initEntityParsingContext(ClusteredEntityWithJoin.class);
        EntityMeta meta = parser.parseEntity(entityContext);

        assertThat(meta.isClusteredEntity()).isTrue();
        assertThat(meta.getIdMeta().getPropertyName()).isEqualTo("id");
        assertThat((Class<CompoundKey>) meta.getIdMeta().getValueClass()).isEqualTo(CompoundKey.class);

        Map<String, PropertyMeta> propertyMetas = meta.getPropertyMetas();
        assertThat(propertyMetas).hasSize(2);
        PropertyMeta friendMeta = propertyMetas.get("friend");

        assertThat(friendMeta.type()).isEqualTo(JOIN_SIMPLE);

        JoinProperties joinProperties = friendMeta.getJoinProperties();
        assertThat(joinProperties).isNotNull();
        assertThat(joinProperties.getCascadeTypes()).containsExactly(CascadeType.ALL);

        EntityMeta joinEntityMeta = joinProperties.getEntityMeta();
        assertThat(joinEntityMeta).isNull();
    }

    @Test
    public void should_exception_when_clustered_entity_more_than_one_mapped_column() throws Exception {
        initEntityParsingContext(ClusteredEntityWithTwoProperties.class);
        configContext.setImpl(Impl.THRIFT);
        expectedEx.expect(AchillesBeanMappingException.class);
        expectedEx.expectMessage("The clustered entity '" + ClusteredEntityWithTwoProperties.class.getCanonicalName()
                + "' should not have more than two properties annotated with @EmbeddedId/@Column/@JoinColumn");

        parser.parseEntity(entityContext);

    }

    @Test
    public void should_exception_when_clustered_entity_has_unsupported_property_type() throws Exception {
        initEntityParsingContext(ClusteredEntityWithNotSupportedPropertyType.class);
        configContext.setImpl(Impl.THRIFT);

        expectedEx.expect(AchillesBeanMappingException.class);
        expectedEx.expectMessage("The clustered entity '"
                + ClusteredEntityWithNotSupportedPropertyType.class.getCanonicalName()
                + "' should have a single @Column/@JoinColumn property of type simple/join simple/counter");

        parser.parseEntity(entityContext);

    }

    @Test
    public void should_fill_join_entity_meta_map_with_entity_meta() throws Exception {
        initEntityParsingContext(null);

        EntityMeta joinEntityMeta = new EntityMeta();
        joinEntityMeta.setClusteredEntity(false);
        joinEntityMeta.setIdClass(Long.class);

        PropertyMeta joinPropertyMeta = new PropertyMeta();
        joinPropertyMeta.setJoinProperties(new JoinProperties());
        joinPropertyMeta.setType(JOIN_MAP);
        joinPropertyMeta.setIdClass(Long.class);

        joinPropertyMetaToBeFilled.put(joinPropertyMeta, BeanWithJoinColumnAsEntity.class);
        entityMetaMap = new HashMap<Class<?>, EntityMeta>();
        entityMetaMap.put(BeanWithJoinColumnAsEntity.class, joinEntityMeta);
        parser.fillJoinEntityMeta(entityContext, entityMetaMap);

        assertThat(joinPropertyMeta.getJoinProperties().getEntityMeta()).isSameAs(joinEntityMeta);
    }

    @Test
    public void should_exception_when_join_entity_is_a_clustered_entity() throws Exception {
        initEntityParsingContext(BeanWithJoinColumnAsEntity.class);

        EntityMeta joinEntityMeta = new EntityMeta();
        joinEntityMeta.setClusteredEntity(true);
        joinEntityMeta.setClassName(BeanWithJoinColumnAsEntity.class.getCanonicalName());
        PropertyMeta joinPropertyMeta = new PropertyMeta();

        joinPropertyMetaToBeFilled.put(joinPropertyMeta, BeanWithJoinColumnAsEntity.class);
        entityMetaMap = new HashMap<Class<?>, EntityMeta>();
        entityMetaMap.put(BeanWithJoinColumnAsEntity.class, joinEntityMeta);

        expectedEx.expect(AchillesBeanMappingException.class);
        expectedEx.expectMessage("The entity '" + BeanWithJoinColumnAsEntity.class.getCanonicalName()
                + "' is a clustered entity and cannot be a join entity");

        parser.fillJoinEntityMeta(entityContext, entityMetaMap);

    }

    @Test
    public void should_exception_when_no_entity_meta_found_for_join_property() throws Exception {
        initEntityParsingContext(null);

        PropertyMeta joinPropertyMeta = new PropertyMeta();

        joinPropertyMetaToBeFilled.put(joinPropertyMeta, BeanWithJoinColumnAsEntity.class);
        entityMetaMap = new HashMap<Class<?>, EntityMeta>();

        expectedEx.expect(AchillesBeanMappingException.class);
        expectedEx.expectMessage("Cannot find mapping for join entity '"
                + BeanWithJoinColumnAsEntity.class.getCanonicalName() + "'");

        parser.fillJoinEntityMeta(entityContext, entityMetaMap);

    }

    private <T> void initEntityParsingContext(Class<T> entityClass) {
        entityContext = new EntityParsingContext( //
                joinPropertyMetaToBeFilled, //
                configContext, entityClass);
    }
}
