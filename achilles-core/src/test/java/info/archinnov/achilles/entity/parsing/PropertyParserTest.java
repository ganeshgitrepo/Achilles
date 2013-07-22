package info.archinnov.achilles.entity.parsing;

import static info.archinnov.achilles.entity.metadata.PropertyType.*;
import static info.archinnov.achilles.type.ConsistencyLevel.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import info.archinnov.achilles.annotations.Consistency;
import info.archinnov.achilles.annotations.Lazy;
import info.archinnov.achilles.consistency.AchillesConsistencyLevelPolicy;
import info.archinnov.achilles.context.ConfigurationContext;
import info.archinnov.achilles.entity.metadata.CompoundKeyProperties;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.entity.metadata.PropertyType;
import info.archinnov.achilles.entity.parsing.context.EntityParsingContext;
import info.archinnov.achilles.entity.parsing.context.PropertyParsingContext;
import info.archinnov.achilles.exception.AchillesBeanMappingException;
import info.archinnov.achilles.test.builders.PropertyMetaTestBuilder;
import info.archinnov.achilles.test.mapping.entity.CompleteBean;
import info.archinnov.achilles.test.parser.entity.CompoundKey;
import info.archinnov.achilles.test.parser.entity.CompoundKeyWithNegativeOrder;
import info.archinnov.achilles.test.parser.entity.CorrectCompoundKey;
import info.archinnov.achilles.test.parser.entity.CorrectMultiKeyUnorderedKeys;
import info.archinnov.achilles.type.ConsistencyLevel;
import info.archinnov.achilles.type.Counter;
import info.archinnov.achilles.type.WideMap;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * PropertyParserTest
 * 
 * @author DuyHai DOAN
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertyParserTest
{

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private PropertyParser parser = new PropertyParser();

    private Map<PropertyMeta<?, ?>, Class<?>> joinPropertyMetaToBeFilled = new HashMap<PropertyMeta<?, ?>, Class<?>>();
    private EntityParsingContext entityContext;
    private ConfigurationContext configContext;

    @Mock
    private AchillesConsistencyLevelPolicy policy;

    @Before
    public void setUp()
    {
        joinPropertyMetaToBeFilled.clear();
        configContext = new ConfigurationContext();
        configContext.setConsistencyPolicy(policy);

        when(policy.getDefaultGlobalReadConsistencyLevel()).thenReturn(ConsistencyLevel.ONE);
        when(policy.getDefaultGlobalWriteConsistencyLevel()).thenReturn(ConsistencyLevel.ALL);
    }

    @Test
    public void should_parse_primary_key() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Id
            private Long id;

            public Long getId()
            {
                return id;
            }

            public void setId(Long id)
            {
                this.id = id;
            }
        }

        PropertyParsingContext context = newContext(Test.class, Test.class.getDeclaredField("id"));
        context.setPrimaryKey(true);

        PropertyMeta<Void, Long> meta = (PropertyMeta<Void, Long>) parser.parse(context);

        assertThat(meta.getPropertyName()).isEqualTo("id");
        assertThat(meta.getValueClass()).isEqualTo(Long.class);
        assertThat(context.getPropertyMetas()).hasSize(1);

    }

    @Test
    public void should_parse_embedded_id() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {

            @EmbeddedId
            private CompoundKey id;

            public CompoundKey getId()
            {
                return id;
            }

            public void setId(CompoundKey id)
            {
                this.id = id;
            }

        }

        PropertyParsingContext context = newContext(Test.class, Test.class.getDeclaredField("id"));
        context.isEmbeddedId(true);

        PropertyMeta<Void, CompoundKey> meta = (PropertyMeta<Void, CompoundKey>) parser
                .parse(context);

        Method userIdGetter = CompoundKey.class.getDeclaredMethod("getUserId");
        Method userIdSetter = CompoundKey.class.getDeclaredMethod("setUserId", Long.class);

        Method nameGetter = CompoundKey.class.getDeclaredMethod("getName");
        Method nameSetter = CompoundKey.class.getDeclaredMethod("setName", String.class);

        assertThat(meta.getPropertyName()).isEqualTo("id");
        assertThat(meta.getValueClass()).isEqualTo(CompoundKey.class);
        CompoundKeyProperties compoundKeyProperties = meta.getCompoundKeyProperties();
        assertThat(compoundKeyProperties).isNotNull();
        assertThat(compoundKeyProperties.getComponentClasses()).contains(Long.class, String.class);
        assertThat(compoundKeyProperties.getComponentNames()).contains("id", "name");
        assertThat(compoundKeyProperties.getComponentGetters()).contains(userIdGetter, nameGetter);
        assertThat(compoundKeyProperties.getComponentSetters()).contains(userIdSetter, nameSetter);
        assertThat(context.getPropertyMetas()).hasSize(1);

    }

    @Test
    public void should_parse_simple_property_string() throws Exception
    {

        @SuppressWarnings("unused")
        class Test
        {
            @Column
            private String name;

            public String getName()
            {
                return name;
            }

            public void setName(String name)
            {
                this.name = name;
            }
        }

        PropertyParsingContext context = newContext(Test.class, Test.class.getDeclaredField("name"));

        PropertyMeta<Void, String> meta = (PropertyMeta<Void, String>) parser.parse(context);

        assertThat(meta.getPropertyName()).isEqualTo("name");
        assertThat(meta.getValueClass()).isEqualTo(String.class);

        assertThat(meta.getGetter().getName()).isEqualTo("getName");
        assertThat((Class<String>) meta.getGetter().getReturnType()).isEqualTo(String.class);
        assertThat(meta.getSetter().getName()).isEqualTo("setName");
        assertThat((Class<String>) meta.getSetter().getParameterTypes()[0]).isEqualTo(String.class);

        assertThat(meta.type()).isEqualTo(PropertyType.SIMPLE);

        assertThat((PropertyMeta<Void, String>) context.getPropertyMetas().get("name")).isSameAs(
                meta);

    }

    @Test
    public void should_parse_simple_property_and_override_name() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column(name = "firstname")
            private String name;

            public String getName()
            {
                return name;
            }

            public void setName(String name)
            {
                this.name = name;
            }
        }
        PropertyParsingContext context = newContext(Test.class, Test.class.getDeclaredField("name"));

        PropertyMeta<?, ?> meta = parser.parse(context);

        assertThat(meta.getPropertyName()).isEqualTo("firstname");
    }

    @Test
    public void should_parse_primitive_property() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column
            private boolean active;

            public boolean isActive()
            {
                return active;
            }

            public void setActive(boolean active)
            {
                this.active = active;
            }

        }
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("active"));
        PropertyMeta<?, ?> meta = parser.parse(context);

        assertThat((Class<Boolean>) meta.getValueClass()).isEqualTo(boolean.class);
    }

    @Test
    public void should_parse_counter_property() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column
            private Counter counter;

            public Counter getCounter()
            {
                return counter;
            }
        }
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("counter"));

        PropertyMeta<Void, Long> meta = (PropertyMeta<Void, Long>) parser.parse(context);

        assertThat(meta.type()).isEqualTo(PropertyType.COUNTER);
        assertThat(meta.getCounterProperties()).isNotNull();
        assertThat(meta.getCounterProperties().getFqcn()).isEqualTo(Test.class.getCanonicalName());
        assertThat((PropertyMeta<Void, Long>) context.getCounterMetas().get(0)).isSameAs(meta);
    }

    @Test
    public void should_parse_counter_property_with_consistency_level() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Consistency(read = ONE, write = ALL)
            @Column
            private Counter counter;

            public Counter getCounter()
            {
                return counter;
            }

        }
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("counter"));
        PropertyMeta<?, ?> meta = parser.parse(context);

        assertThat(meta.type()).isEqualTo(PropertyType.COUNTER);
        assertThat(meta.getReadConsistencyLevel()).isEqualTo(ONE);
        assertThat(meta.getWriteConsistencyLevel()).isEqualTo(ALL);
    }

    @Test
    public void should_exception_when_counter_consistency_is_any_for_read() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Consistency(read = ANY, write = ALL)
            @Column
            private Counter counter;

            public Counter getCounter()
            {
                return counter;
            }
        }
        expectedEx.expect(AchillesBeanMappingException.class);
        expectedEx
                .expectMessage("Counter field 'counter' of entity 'null' cannot have ANY as read/write consistency level. All consistency levels except ANY are allowed");
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("counter"));
        parser.parse(context);
    }

    @Test
    public void should_exception_when_counter_consistency_is_any_for_write() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Consistency(read = ONE, write = ANY)
            @Column
            private Counter counter;

            public Counter getCounter()
            {
                return counter;
            }
        }

        expectedEx.expect(AchillesBeanMappingException.class);
        expectedEx
                .expectMessage("Counter field 'counter' of entity 'null' cannot have ANY as read/write consistency level. All consistency levels except ANY are allowed");
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("counter"));
        parser.parse(context);
    }

    @Test
    public void should_parse_enum_property() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column
            private PropertyType type;

            public PropertyType getType()
            {
                return type;
            }

            public void setType(PropertyType type)
            {
                this.type = type;
            }
        }
        PropertyParsingContext context = newContext(Test.class, Test.class.getDeclaredField("type"));
        PropertyMeta<?, ?> meta = parser.parse(context);

        assertThat((Class<PropertyType>) meta.getValueClass()).isEqualTo(PropertyType.class);
    }

    @Test
    public void should_parse_allowed_type_property() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column
            private UUID uuid;

            public UUID getUuid()
            {
                return uuid;
            }

            public void setUuid(UUID uuid)
            {
                this.uuid = uuid;
            }
        }
        PropertyParsingContext context = newContext(Test.class, Test.class.getDeclaredField("uuid"));

        PropertyMeta<?, ?> meta = parser.parse(context);

        assertThat((Class<UUID>) meta.getValueClass()).isEqualTo(UUID.class);
    }

    @Test
    public void should_parse_lazy() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column
            @Lazy
            private List<String> friends;

            public List<String> getFriends()
            {
                return friends;
            }

            public void setFriends(List<String> friends)
            {
                this.friends = friends;
            }
        }
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("friends"));
        PropertyMeta<?, ?> meta = parser.parse(context);
        assertThat(meta.type().isLazy()).isTrue();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void should_parse_list() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column
            private List<String> friends;

            public List<String> getFriends()
            {
                return friends;
            }

            public void setFriends(List<String> friends)
            {
                this.friends = friends;
            }
        }
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("friends"));
        PropertyMeta<?, ?> meta = parser.parse(context);

        assertThat(meta.getPropertyName()).isEqualTo("friends");
        assertThat((Class<String>) meta.getValueClass()).isEqualTo(String.class);

        assertThat(meta.getGetter().getName()).isEqualTo("getFriends");
        assertThat((Class<List>) meta.getGetter().getReturnType()).isEqualTo(List.class);
        assertThat(meta.getSetter().getName()).isEqualTo("setFriends");
        assertThat((Class<List>) meta.getSetter().getParameterTypes()[0]).isEqualTo(List.class);

        assertThat(meta.type()).isEqualTo(PropertyType.LIST);
        assertThat(meta.isLazy()).isFalse();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void should_parse_set() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column
            private Set<Long> followers;

            public Set<Long> getFollowers()
            {
                return followers;
            }

            public void setFollowers(Set<Long> followers)
            {
                this.followers = followers;
            }
        }
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("followers"));
        PropertyMeta<?, ?> meta = parser.parse(context);

        assertThat(meta.getPropertyName()).isEqualTo("followers");
        assertThat((Class<Long>) meta.getValueClass()).isEqualTo(Long.class);

        assertThat(meta.getGetter().getName()).isEqualTo("getFollowers");
        assertThat((Class<Set>) meta.getGetter().getReturnType()).isEqualTo(Set.class);
        assertThat(meta.getSetter().getName()).isEqualTo("setFollowers");
        assertThat((Class<Set>) meta.getSetter().getParameterTypes()[0]).isEqualTo(Set.class);

        assertThat(meta.type()).isEqualTo(PropertyType.SET);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void should_parse_map() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column
            private Map<Integer, String> preferences;

            public Map<Integer, String> getPreferences()
            {
                return preferences;
            }

            public void setPreferences(Map<Integer, String> preferences)
            {
                this.preferences = preferences;
            }
        }
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("preferences"));
        PropertyMeta<?, ?> meta = parser.parse(context);

        assertThat(meta.getPropertyName()).isEqualTo("preferences");
        assertThat((Class<String>) meta.getValueClass()).isEqualTo(String.class);
        assertThat(meta.type()).isEqualTo(PropertyType.MAP);

        assertThat((Class<Integer>) meta.getKeyClass()).isEqualTo(Integer.class);

        assertThat(meta.getGetter().getName()).isEqualTo("getPreferences");
        assertThat((Class<Map>) meta.getGetter().getReturnType()).isEqualTo(Map.class);
        assertThat(meta.getSetter().getName()).isEqualTo("setPreferences");
        assertThat((Class<Map>) meta.getSetter().getParameterTypes()[0]).isEqualTo(Map.class);
    }

    @Test
    public void should_parse_wide_map() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column(table = "xxx")
            private WideMap<UUID, String> tweets;

            public WideMap<UUID, String> getTweets()
            {
                return tweets;
            }

            public void setTweets(WideMap<UUID, String> tweets)
            {
                this.tweets = tweets;
            }
        }
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("tweets"));
        PropertyMeta<?, ?> meta = parser.parse(context);

        assertThat(meta.type()).isEqualTo(PropertyType.WIDE_MAP);
        assertThat(meta.getExternalTableName()).isEqualTo("xxx");
        assertThat(meta.getPropertyName()).isEqualTo("tweets");
        assertThat((Class<String>) meta.getValueClass()).isEqualTo(String.class);

        assertThat((Class<UUID>) meta.getKeyClass()).isEqualTo(UUID.class);

    }

    @Test
    public void should_exception_when_no_external_cf_defined_for_widemap() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column
            private WideMap<UUID, String> wideMap;

            public WideMap<UUID, String> getWideMap()
            {
                return wideMap;
            }
        }

        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("wideMap"));

        expectedEx.expect(AchillesBeanMappingException.class);
        expectedEx
                .expectMessage("External Column Family should be defined for WideMap property 'wideMap' of entity '"
                        + Test.class.getCanonicalName()
                        + "'. Did you forget to add 'table' attribute to @Column/@JoinColumn annotation ?");
        parser.parse(context);
    }

    @Test
    public void should_parse_counter_widemap() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column(table = "counter_xxx")
            private WideMap<UUID, Counter> counters;

            public WideMap<UUID, Counter> getCounters()
            {
                return counters;
            }
        }
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("counters"));
        PropertyMeta<UUID, Counter> meta = (PropertyMeta<UUID, Counter>) parser.parse(context);

        assertThat(meta.type()).isEqualTo(COUNTER_WIDE_MAP);
        assertThat(meta.getPropertyName()).isEqualTo("counters");
        assertThat(meta.getExternalTableName()).isEqualTo("counter_xxx");
        assertThat(meta.getValueClass()).isEqualTo(Counter.class);
        assertThat(meta.getKeyClass()).isEqualTo(UUID.class);

        assertThat((PropertyMeta<UUID, Counter>) context.getCounterMetas().get(0)).isSameAs(meta);
    }

    @Test
    public void should_exception_when_no_external_cf_defined_for_counter_widemap() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column
            private WideMap<UUID, Counter> counterWideMap;

            public WideMap<UUID, Counter> getCounterWideMap()
            {
                return counterWideMap;
            }
        }

        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("counterWideMap"));

        expectedEx.expect(AchillesBeanMappingException.class);
        expectedEx
                .expectMessage("External Column Family should be defined for WideMap property 'counterWideMap' of entity '"
                        + Test.class.getCanonicalName()
                        + "'. Did you forget to add 'table' attribute to @Column/@JoinColumn annotation ?");
        parser.parse(context);
    }

    @Test
    public void should_exception_when_consistency_level_defined_for_counter_widemap()
            throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Consistency(read = ONE, write = ONE)
            @Column(table = "table")
            private WideMap<UUID, Counter> counterWideMap;

            public WideMap<UUID, Counter> getCounterWideMap()
            {
                return counterWideMap;
            }
        }

        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("counterWideMap"));

        expectedEx.expect(AchillesBeanMappingException.class);
        expectedEx
                .expectMessage("Counter WideMap type '"
                        + Test.class.getCanonicalName()
                        + "' does not support @ConsistencyLevel annotation. Only runtime consistency level is allowed");
        parser.parse(context);
    }

    @Test
    public void should_fill_widemap_hashmap() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column(table = "tablename")
            private WideMap<UUID, String> tweets;

            public WideMap<UUID, String> getTweets()
            {
                return tweets;
            }

            public void setTweets(WideMap<UUID, String> tweets)
            {
                this.tweets = tweets;
            }
        }
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("tweets"));
        parser.parse(context);

        PropertyMeta<?, ?> propertyMeta = context.getWideMaps().keySet().iterator().next();
        assertThat(context.getJoinWideMaps()).isEmpty();
        assertThat(propertyMeta.type()).isEqualTo(PropertyType.WIDE_MAP);
        assertThat(propertyMeta.getPropertyName()).isEqualTo("tweets");
    }

    @Test
    public void should_fill_join_widemap_hashmap() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @JoinColumn(table = "tablename")
            private WideMap<UUID, CompleteBean> beans;

            public WideMap<UUID, CompleteBean> getBeans()
            {
                return beans;
            }

        }
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("beans"));
        context.setJoinColumn(true);

        parser.parse(context);

        PropertyMeta<?, ?> propertyMeta = context.getJoinWideMaps().keySet().iterator().next();
        assertThat(context.getWideMaps()).isEmpty();
        assertThat(propertyMeta.type()).isEqualTo(PropertyType.WIDE_MAP);
        assertThat(propertyMeta.getPropertyName()).isEqualTo("beans");
    }

    @Test
    public void should_fill_widemap_hashmap_when_clustered_entity() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column(table = "xxx")
            private WideMap<UUID, String> tweets;

            public WideMap<UUID, String> getTweets()
            {
                return tweets;
            }

        }

        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("tweets"));
        entityContext.setClusteredEntity(true);

        parser.parse(context);

        Entry<PropertyMeta<?, ?>, String> entry = context
                .getWideMaps()
                .entrySet()
                .iterator()
                .next();

        PropertyMeta<?, ?> propertyMeta = entry.getKey();
        assertThat(propertyMeta.type()).isEqualTo(PropertyType.WIDE_MAP);
        assertThat(propertyMeta.getPropertyName()).isEqualTo("tweets");
        assertThat(entry.getValue()).isNull();
    }

    @Test
    public void should_set_widemap_consistency_level() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column(table = "tablename")
            @Consistency(read = QUORUM, write = ALL)
            private WideMap<UUID, String> tweets;

            public WideMap<UUID, String> getTweets()
            {
                return tweets;
            }

            public void setTweets(WideMap<UUID, String> tweets)
            {
                this.tweets = tweets;
            }
        }
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("tweets"));

        PropertyMeta<?, ?> propertyMeta = parser.parse(context);
        assertThat(propertyMeta.getReadConsistencyLevel()).isEqualTo(QUORUM);
        assertThat(propertyMeta.getWriteConsistencyLevel()).isEqualTo(ALL);
    }

    @Test
    public void should_parse_widemap() throws Exception
    {
        PropertyMeta<Void, Long> idMeta = PropertyMetaTestBuilder.valueClass(Long.class).build();
        PropertyMeta<Long, UUID> propertyMeta = PropertyMetaTestBuilder//
                .keyValueClass(Long.class, UUID.class)
                .type(WIDE_MAP)
                .build();

        initEntityParsingContext();

        parser.fillWideMap(entityContext, idMeta, propertyMeta, "externalTableName");

        assertThat((Class<Long>) propertyMeta.getIdClass()).isEqualTo(Long.class);
    }

    @Test
    public void should_exception_when_invalid_wide_map_key() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column
            private WideMap<Void, String> tweets;

            public WideMap<Void, String> getTweets()
            {
                return tweets;
            }

            public void setTweets(WideMap<Void, String> tweets)
            {
                this.tweets = tweets;
            }
        }

        expectedEx.expect(AchillesBeanMappingException.class);
        expectedEx.expectMessage("The class '" + Void.class.getCanonicalName()
                + "' is not allowed as WideMap key");
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("tweets"));

        parser.parse(context);
    }

    @Test
    public void should_parse_compound_key_wide_map() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column(table = "tweets_xxx")
            private WideMap<CorrectCompoundKey, String> tweets;

            public WideMap<CorrectCompoundKey, String> getTweets()
            {
                return tweets;
            }

            public void setTweets(WideMap<CorrectCompoundKey, String> tweets)
            {
                this.tweets = tweets;
            }
        }

        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("tweets"));

        PropertyMeta<?, ?> meta = parser.parse(context);

        assertThat(meta.isEmbeddedId()).isFalse();
        assertThat(meta.getPropertyName()).isEqualTo("tweets");
        assertThat((Class<String>) meta.getValueClass()).isEqualTo(String.class);
        assertThat(meta.type()).isEqualTo(PropertyType.WIDE_MAP);
        assertThat(meta.isCompound()).isTrue();

        assertThat((Class<CorrectCompoundKey>) meta.getKeyClass()).isEqualTo(
                CorrectCompoundKey.class);

        assertThat(meta.getComponentGetters()).hasSize(2);
        assertThat(meta.getComponentGetters().get(0).getName()).isEqualTo("getName");
        assertThat(meta.getComponentGetters().get(1).getName()).isEqualTo("getRank");

        assertThat(meta.getComponentSetters()).hasSize(2);
        assertThat(meta.getComponentSetters().get(0).getName()).isEqualTo("setName");
        assertThat(meta.getComponentSetters().get(1).getName()).isEqualTo("setRank");

        assertThat(meta.getComponentClasses()).hasSize(2);
        assertThat((Class<String>) meta.getComponentClasses().get(0)).isEqualTo(String.class);
        assertThat((Class<Integer>) meta.getComponentClasses().get(1)).isEqualTo(int.class);
    }

    @Test
    public void should_parse_compound_key_wide_map_unordered_keys() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column(table = "tweets_xxx")
            private WideMap<CorrectMultiKeyUnorderedKeys, String> tweets;

            public WideMap<CorrectMultiKeyUnorderedKeys, String> getTweets()
            {
                return tweets;
            }

            public void setTweets(WideMap<CorrectMultiKeyUnorderedKeys, String> tweets)
            {
                this.tweets = tweets;
            }
        }
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("tweets"));

        PropertyMeta<?, ?> meta = parser.parse(context);

        assertThat(meta.getPropertyName()).isEqualTo("tweets");
        assertThat((Class<String>) meta.getValueClass()).isEqualTo(String.class);
        assertThat(meta.type()).isEqualTo(PropertyType.WIDE_MAP);
        assertThat(meta.isCompound()).isTrue();

        assertThat((Class<CorrectMultiKeyUnorderedKeys>) meta.getKeyClass()).isEqualTo(
                CorrectMultiKeyUnorderedKeys.class);

        assertThat(meta.getComponentGetters()).hasSize(2);
        assertThat(meta.getComponentGetters().get(0).getName()).isEqualTo("getName");
        assertThat(meta.getComponentGetters().get(1).getName()).isEqualTo("getRank");

        assertThat(meta.getComponentClasses()).hasSize(2);
        assertThat((Class<String>) meta.getComponentClasses().get(0)).isEqualTo(String.class);
        assertThat((Class<Integer>) meta.getComponentClasses().get(1)).isEqualTo(int.class);
    }

    @Test
    public void should_exception_when_invalid_multi_key_negative_order() throws Exception
    {
        @SuppressWarnings("unused")
        class Test
        {
            @Column
            private WideMap<CompoundKeyWithNegativeOrder, String> tweets;

            public WideMap<CompoundKeyWithNegativeOrder, String> getTweets()
            {
                return tweets;
            }
        }

        expectedEx.expect(AchillesBeanMappingException.class);
        expectedEx.expectMessage("The key orders is wrong for @CompoundKey class '"
                + CompoundKeyWithNegativeOrder.class.getCanonicalName() + "'");
        PropertyParsingContext context = newContext(Test.class,
                Test.class.getDeclaredField("tweets"));

        parser.parse(context);

    }

    private <T> PropertyParsingContext newContext(Class<T> entityClass, Field field)
    {
        entityContext = new EntityParsingContext( //
                joinPropertyMetaToBeFilled, //
                configContext, entityClass);

        return entityContext.newPropertyContext(field);
    }

    private void initEntityParsingContext()
    {
        entityContext = new EntityParsingContext( //
                joinPropertyMetaToBeFilled, //
                configContext, CompleteBean.class);
    }

}
