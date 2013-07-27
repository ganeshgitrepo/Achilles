package info.archinnov.achilles.table;

import static info.archinnov.achilles.counter.AchillesCounter.*;
import static info.archinnov.achilles.entity.metadata.PropertyType.*;
import static org.mockito.Mockito.*;
import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.test.builders.PropertyMetaTestBuilder;
import info.archinnov.achilles.test.mapping.entity.UserBean;
import info.archinnov.achilles.test.parser.entity.CompoundKey;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.TableMetadata;
import com.google.common.collect.ImmutableMap;

/**
 * CQLTableValidatorTest
 * 
 * @author DuyHai DOAN
 * 
 */

@RunWith(MockitoJUnitRunner.class)
public class CQLTableValidatorTest {

    private CQLTableValidator validator;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Cluster cluster;

    @Mock
    private TableMetadata tableMetadata;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ColumnMetadata columnMetadata;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ColumnMetadata columnMetadataForField;

    private String keyspaceName = "keyspace";

    private EntityMeta entityMeta;

    @Before
    public void setUp()
    {
        validator = new CQLTableValidator(cluster, keyspaceName);
        entityMeta = new EntityMeta();
    }

    @Test
    public void should_validate_id_for_entity() throws Exception
    {
        PropertyMeta<?, ?> idMeta = PropertyMetaTestBuilder
                .completeBean(Void.class, Long.class)
                .field("id")
                .type(ID)
                .build();

        entityMeta.setIdMeta(idMeta);
        entityMeta.setPropertyMetas(new HashMap<String, PropertyMeta<?, ?>>());

        when(tableMetadata.getName()).thenReturn("table");
        when(tableMetadata.getColumn("id")).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(DataType.bigint());

        validator.validateForEntity(entityMeta, tableMetadata);
    }

    @Test
    public void should_validate_embedded_id_for_entity() throws Exception
    {
        PropertyMeta<?, ?> idMeta = PropertyMetaTestBuilder
                .valueClass(CompoundKey.class)
                .compNames("userId", "name")
                .compClasses(Long.class, String.class)
                .type(EMBEDDED_ID)
                .build();

        entityMeta.setIdMeta(idMeta);
        entityMeta.setPropertyMetas(new HashMap<String, PropertyMeta<?, ?>>());

        when(tableMetadata.getName()).thenReturn("table");
        ColumnMetadata userIdMetadata = mock(ColumnMetadata.class);
        when(tableMetadata.getColumn("userid")).thenReturn(userIdMetadata);
        when(userIdMetadata.getType()).thenReturn(DataType.bigint());

        ColumnMetadata nameMetadata = mock(ColumnMetadata.class);
        when(tableMetadata.getColumn("name")).thenReturn(nameMetadata);
        when(nameMetadata.getType()).thenReturn(DataType.text());

        validator.validateForEntity(entityMeta, tableMetadata);
    }

    @Test
    public void should_validate_simple_field_for_entity() throws Exception
    {
        PropertyMeta<?, ?> idMeta = PropertyMetaTestBuilder
                .completeBean(Void.class, Long.class)
                .field("id")
                .type(ID)
                .build();

        PropertyMeta<Void, String> pm = PropertyMetaTestBuilder.
                completeBean(Void.class, String.class)
                .field("name")
                .type(SIMPLE)
                .build();

        entityMeta.setIdMeta(idMeta);
        entityMeta.setPropertyMetas(ImmutableMap.<String, PropertyMeta<?, ?>> of("name", pm));

        when(tableMetadata.getName()).thenReturn("table");
        when(tableMetadata.getColumn("id")).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(DataType.bigint());

        when(tableMetadata.getColumn("name")).thenReturn(columnMetadataForField);
        when(columnMetadataForField.getType()).thenReturn(DataType.text());

        validator.validateForEntity(entityMeta, tableMetadata);

        pm = PropertyMetaTestBuilder.
                completeBean(Void.class, String.class)
                .field("name")
                .type(LAZY_SIMPLE)
                .build();
        entityMeta.setPropertyMetas(ImmutableMap.<String, PropertyMeta<?, ?>> of("name", pm));
        validator.validateForEntity(entityMeta, tableMetadata);
    }

    @Test
    public void should_validate_join_simple_field_for_entity() throws Exception
    {
        PropertyMeta<?, ?> idMeta = PropertyMetaTestBuilder
                .completeBean(Void.class, Long.class)
                .field("id")
                .type(ID)
                .build();

        EntityMeta joinMeta = new EntityMeta();
        joinMeta.setIdMeta(idMeta);

        PropertyMeta<Void, UserBean> pm = PropertyMetaTestBuilder.
                completeBean(Void.class, UserBean.class)
                .field("user")
                .type(JOIN_SIMPLE)
                .joinMeta(joinMeta)
                .build();

        entityMeta.setIdMeta(idMeta);
        entityMeta.setPropertyMetas(ImmutableMap.<String, PropertyMeta<?, ?>> of("user", pm));

        when(tableMetadata.getName()).thenReturn("table");
        when(tableMetadata.getColumn("id")).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(DataType.bigint());

        when(tableMetadata.getColumn("user")).thenReturn(columnMetadataForField);
        when(columnMetadataForField.getType()).thenReturn(DataType.bigint());

        validator.validateForEntity(entityMeta, tableMetadata);
    }

    @Test
    public void should_validate_list_field_for_entity() throws Exception
    {
        PropertyMeta<?, ?> idMeta = PropertyMetaTestBuilder
                .completeBean(Void.class, Long.class)
                .field("id")
                .type(ID)
                .build();

        PropertyMeta<Void, String> pm = PropertyMetaTestBuilder.
                completeBean(Void.class, String.class)
                .field("friends")
                .type(LIST)
                .build();

        entityMeta.setIdMeta(idMeta);
        entityMeta.setPropertyMetas(ImmutableMap.<String, PropertyMeta<?, ?>> of("friends", pm));

        when(tableMetadata.getName()).thenReturn("table");
        when(tableMetadata.getColumn("id")).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(DataType.bigint());

        when(tableMetadata.getColumn("friends")).thenReturn(columnMetadataForField);
        when(columnMetadataForField.getType()).thenReturn(DataType.list(DataType.text()));

        validator.validateForEntity(entityMeta, tableMetadata);

        pm = PropertyMetaTestBuilder.
                completeBean(Void.class, String.class)
                .field("friends")
                .type(LAZY_LIST)
                .build();
        entityMeta.setPropertyMetas(ImmutableMap.<String, PropertyMeta<?, ?>> of("friends", pm));
        validator.validateForEntity(entityMeta, tableMetadata);
    }

    @Test
    public void should_validate_join_list_field_for_entity() throws Exception
    {
        PropertyMeta<?, ?> idMeta = PropertyMetaTestBuilder
                .completeBean(Void.class, Long.class)
                .field("id")
                .type(ID)
                .build();

        EntityMeta joinMeta = new EntityMeta();
        joinMeta.setIdMeta(idMeta);

        PropertyMeta<Void, UserBean> pm = PropertyMetaTestBuilder.
                completeBean(Void.class, UserBean.class)
                .field("friends")
                .joinMeta(joinMeta)
                .type(JOIN_LIST)
                .build();

        entityMeta.setIdMeta(idMeta);
        entityMeta.setPropertyMetas(ImmutableMap.<String, PropertyMeta<?, ?>> of("friends", pm));

        when(tableMetadata.getName()).thenReturn("table");
        when(tableMetadata.getColumn("id")).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(DataType.bigint());

        when(tableMetadata.getColumn("friends")).thenReturn(columnMetadataForField);
        when(columnMetadataForField.getType()).thenReturn(DataType.list(DataType.bigint()));

        validator.validateForEntity(entityMeta, tableMetadata);
    }

    @Test
    public void should_validate_set_field_for_entity() throws Exception
    {
        PropertyMeta<?, ?> idMeta = PropertyMetaTestBuilder
                .completeBean(Void.class, Long.class)
                .field("id")
                .type(ID)
                .build();

        PropertyMeta<Void, String> pm = PropertyMetaTestBuilder.
                completeBean(Void.class, String.class)
                .field("followers")
                .type(SET)
                .build();

        entityMeta.setIdMeta(idMeta);
        entityMeta.setPropertyMetas(ImmutableMap.<String, PropertyMeta<?, ?>> of("followers", pm));

        when(tableMetadata.getName()).thenReturn("table");
        when(tableMetadata.getColumn("id")).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(DataType.bigint());

        when(tableMetadata.getColumn("followers")).thenReturn(columnMetadataForField);
        when(columnMetadataForField.getType()).thenReturn(DataType.set(DataType.text()));

        validator.validateForEntity(entityMeta, tableMetadata);
    }

    @Test
    public void should_validate_join_set_field_for_entity() throws Exception
    {
        PropertyMeta<?, ?> idMeta = PropertyMetaTestBuilder
                .completeBean(Void.class, Long.class)
                .field("id")
                .type(ID)
                .build();

        EntityMeta joinMeta = new EntityMeta();
        joinMeta.setIdMeta(idMeta);

        PropertyMeta<Void, UserBean> pm = PropertyMetaTestBuilder.
                completeBean(Void.class, UserBean.class)
                .field("followers")
                .type(JOIN_SET)
                .joinMeta(joinMeta)
                .build();

        entityMeta.setIdMeta(idMeta);
        entityMeta.setPropertyMetas(ImmutableMap.<String, PropertyMeta<?, ?>> of("followers", pm));

        when(tableMetadata.getName()).thenReturn("table");
        when(tableMetadata.getColumn("id")).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(DataType.bigint());

        when(tableMetadata.getColumn("followers")).thenReturn(columnMetadataForField);
        when(columnMetadataForField.getType()).thenReturn(DataType.set(DataType.bigint()));

        validator.validateForEntity(entityMeta, tableMetadata);
    }

    @Test
    public void should_validate_map_field_for_entity() throws Exception
    {
        PropertyMeta<?, ?> idMeta = PropertyMetaTestBuilder
                .completeBean(Void.class, Long.class)
                .field("id")
                .type(ID)
                .build();

        PropertyMeta<Integer, String> pm = PropertyMetaTestBuilder.
                completeBean(Integer.class, String.class)
                .field("preferences")
                .type(MAP)
                .build();

        entityMeta.setIdMeta(idMeta);
        entityMeta.setPropertyMetas(ImmutableMap.<String, PropertyMeta<?, ?>> of("preferences", pm));

        when(tableMetadata.getName()).thenReturn("table");
        when(tableMetadata.getColumn("id")).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(DataType.bigint());

        when(tableMetadata.getColumn("preferences")).thenReturn(columnMetadataForField);
        when(columnMetadataForField.getType()).thenReturn(DataType.map(DataType.cint(), DataType.text()));

        validator.validateForEntity(entityMeta, tableMetadata);

        pm = PropertyMetaTestBuilder.
                completeBean(Integer.class, String.class)
                .field("preferences")
                .type(LAZY_MAP)
                .build();
        entityMeta.setPropertyMetas(ImmutableMap.<String, PropertyMeta<?, ?>> of("preferences", pm));
        validator.validateForEntity(entityMeta, tableMetadata);
    }

    @Test
    public void should_validate_join_map_field_for_entity() throws Exception
    {
        PropertyMeta<?, ?> idMeta = PropertyMetaTestBuilder
                .completeBean(Void.class, Long.class)
                .field("id")
                .type(ID)
                .build();

        EntityMeta joinMeta = new EntityMeta();
        joinMeta.setIdMeta(idMeta);

        PropertyMeta<Integer, String> pm = PropertyMetaTestBuilder.
                completeBean(Integer.class, String.class)
                .field("preferences")
                .type(JOIN_MAP)
                .joinMeta(joinMeta)
                .build();

        entityMeta.setIdMeta(idMeta);
        entityMeta.setPropertyMetas(ImmutableMap.<String, PropertyMeta<?, ?>> of("preferences", pm));

        when(tableMetadata.getName()).thenReturn("table");
        when(tableMetadata.getColumn("id")).thenReturn(columnMetadata);
        when(columnMetadata.getType()).thenReturn(DataType.bigint());

        when(tableMetadata.getColumn("preferences")).thenReturn(columnMetadataForField);
        when(columnMetadataForField.getType()).thenReturn(DataType.map(DataType.cint(), DataType.bigint()));

        validator.validateForEntity(entityMeta, tableMetadata);
    }

    @Test
    public void should_validate_achilles_counter() throws Exception
    {
        KeyspaceMetadata keyspaceMeta = mock(KeyspaceMetadata.class);
        when(cluster.getMetadata().getKeyspace(keyspaceName)).thenReturn(keyspaceMeta);
        when(keyspaceMeta.getTable(CQL_COUNTER_TABLE)).thenReturn(tableMetadata);

        ColumnMetadata fqcnColumnMeta = mock(ColumnMetadata.class);
        when(tableMetadata.getColumn(CQL_COUNTER_FQCN)).thenReturn(fqcnColumnMeta);

        ColumnMetadata pkColumnMeta = mock(ColumnMetadata.class);
        when(tableMetadata.getColumn(CQL_COUNTER_PRIMARY_KEY)).thenReturn(pkColumnMeta);

        ColumnMetadata propertyColumnMeta = mock(ColumnMetadata.class);
        when(tableMetadata.getColumn(CQL_COUNTER_PROPERTY_NAME)).thenReturn(propertyColumnMeta);

        ColumnMetadata counterColumnMeta = mock(ColumnMetadata.class);
        when(tableMetadata.getColumn(CQL_COUNTER_VALUE)).thenReturn(counterColumnMeta);

        when(fqcnColumnMeta.getType()).thenReturn(DataType.text());
        when(pkColumnMeta.getType()).thenReturn(DataType.text());
        when(propertyColumnMeta.getType()).thenReturn(DataType.text());
        when(counterColumnMeta.getType()).thenReturn(DataType.counter());

        validator.validateAchillesCounter();

    }
}
