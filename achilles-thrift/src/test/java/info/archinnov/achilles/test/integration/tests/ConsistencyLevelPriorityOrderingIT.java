package info.archinnov.achilles.test.integration.tests;

import static info.archinnov.achilles.type.ConsistencyLevel.*;
import static org.fest.assertions.api.Assertions.assertThat;
import info.archinnov.achilles.consistency.ThriftConsistencyLevelPolicy;
import info.archinnov.achilles.entity.manager.ThriftBatchingEntityManager;
import info.archinnov.achilles.entity.manager.ThriftEntityManager;
import info.archinnov.achilles.entity.manager.ThriftEntityManagerFactory;
import info.archinnov.achilles.exception.AchillesException;
import info.archinnov.achilles.junit.AchillesTestResource.Steps;
import info.archinnov.achilles.junit.AchillesInternalThriftResource;
import info.archinnov.achilles.test.integration.entity.ClusteredEntity;
import info.archinnov.achilles.test.integration.entity.EntityWithConsistencyLevelOnClassAndField;
import info.archinnov.achilles.test.integration.utils.CassandraLogAsserter;
import info.archinnov.achilles.type.Counter;
import me.prettyprint.hector.api.exceptions.HInvalidRequestException;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * ConsistencyLevelPriorityIT
 * 
 * @author DuyHai DOAN
 * 
 */
public class ConsistencyLevelPriorityOrderingIT
{
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Rule
    public AchillesInternalThriftResource resource = new AchillesInternalThriftResource(Steps.AFTER_TEST, "clustered");

    private ThriftEntityManagerFactory emf = resource.getFactory();

    private ThriftEntityManager em = resource.getEm();

    private ThriftConsistencyLevelPolicy policy = resource.getConsistencyPolicy();

    private CassandraLogAsserter logAsserter = new CassandraLogAsserter();

    // Normal type
    @Test
    public void should_override_mapping_on_class_by_runtime_value_on_batch_mode_for_normal_type()
            throws Exception
    {
        EntityWithConsistencyLevelOnClassAndField entity = new EntityWithConsistencyLevelOnClassAndField();
        long id = RandomUtils.nextLong();
        entity.setId(id);
        entity.setName("name");

        em.persist(entity);

        ThriftBatchingEntityManager batchEm = emf.createBatchingEntityManager();
        batchEm.startBatch(ONE);
        logAsserter.prepareLogLevel();

        entity = batchEm.find(EntityWithConsistencyLevelOnClassAndField.class, entity.getId());

        logAsserter.assertConsistencyLevels(ONE, ONE);
        batchEm.endBatch();

        assertThatConsistencyLevelsAreReinitialized();
        assertThat(entity.getName()).isEqualTo("name");

        expectedEx.expect(AchillesException.class);
        expectedEx
                .expectMessage("Error when loading entity type '"
                        + EntityWithConsistencyLevelOnClassAndField.class.getCanonicalName()
                        + "' with key '"
                        + id
                        + "'. Cause : InvalidRequestException(why:consistency level LOCAL_QUORUM not compatible with replication strategy (org.apache.cassandra.locator.SimpleStrategy))");
        em.find(EntityWithConsistencyLevelOnClassAndField.class, entity.getId());
    }

    @Test
    public void should_not_override_batch_mode_level_by_runtime_value_for_normal_type()
            throws Exception
    {
        EntityWithConsistencyLevelOnClassAndField entity = new EntityWithConsistencyLevelOnClassAndField();
        entity.setId(RandomUtils.nextLong());
        entity.setName("name sdfsdf");
        em.persist(entity);

        ThriftBatchingEntityManager batchEm = emf.createBatchingEntityManager();

        batchEm.startBatch(EACH_QUORUM);

        expectedEx.expect(AchillesException.class);
        expectedEx
                .expectMessage("Runtime custom Consistency Level cannot be set for batch mode. Please set the Consistency Levels at batch start with 'startBatch(readLevel,writeLevel)'");

        entity = batchEm.find(EntityWithConsistencyLevelOnClassAndField.class, entity.getId(), ONE);
    }

    // Counter type
    @Test
    public void should_override_mapping_on_class_by_mapping_on_field_for_counter_type()
            throws Exception
    {
        EntityWithConsistencyLevelOnClassAndField entity = new EntityWithConsistencyLevelOnClassAndField();
        entity.setId(RandomUtils.nextLong());
        entity.setName("name");
        entity = em.merge(entity);

        Counter counter = entity.getCount();
        counter.incr(10L);

        logAsserter.prepareLogLevel();
        assertThat(counter.get()).isEqualTo(10L);
        logAsserter.assertConsistencyLevels(ONE, ONE);
        assertThatConsistencyLevelsAreReinitialized();
    }

    @Test
    public void should_override_mapping_on_field_by_batch_value_for_counter_type() throws Exception
    {
        EntityWithConsistencyLevelOnClassAndField entity = new EntityWithConsistencyLevelOnClassAndField();
        entity.setId(RandomUtils.nextLong());
        entity.setName("name");

        ThriftBatchingEntityManager batchEm = emf.createBatchingEntityManager();
        batchEm.startBatch(EACH_QUORUM);
        entity = batchEm.merge(entity);

        expectedEx.expect(HInvalidRequestException.class);
        expectedEx
                .expectMessage("InvalidRequestException(why:consistency level EACH_QUORUM not compatible with replication strategy (org.apache.cassandra.locator.SimpleStrategy))");

        Counter counter = entity.getCount();
        counter.incr(10L);

    }

    @Test
    public void should_override_mapping_on_field_by_runtime_value_for_counter_type()
            throws Exception
    {
        EntityWithConsistencyLevelOnClassAndField entity = new EntityWithConsistencyLevelOnClassAndField();
        entity.setId(RandomUtils.nextLong());
        entity.setName("name");
        entity = em.merge(entity);

        Counter counter = entity.getCount();
        counter.incr(10L);
        assertThat(counter.get()).isEqualTo(10L);

        expectedEx.expect(HInvalidRequestException.class);
        expectedEx
                .expectMessage("InvalidRequestException(why:EACH_QUORUM ConsistencyLevel is only supported for writes)");

        counter.get(EACH_QUORUM);
    }

    @Test
    public void should_override_batch_level_by_runtime_value_for_counter_type()
            throws Exception
    {
        EntityWithConsistencyLevelOnClassAndField entity = new EntityWithConsistencyLevelOnClassAndField();
        entity.setId(RandomUtils.nextLong());
        entity.setName("name");

        ThriftBatchingEntityManager batchEm = emf.createBatchingEntityManager();
        batchEm.startBatch(ONE);
        entity = batchEm.merge(entity);

        Counter counter = entity.getCount();
        counter.incr(10L);
        assertThat(counter.get()).isEqualTo(10L);

        expectedEx.expect(HInvalidRequestException.class);
        expectedEx
                .expectMessage("InvalidRequestException(why:EACH_QUORUM ConsistencyLevel is only supported for writes)");

        counter.get(EACH_QUORUM);
    }

    @Test
    public void should_override_batch_level_by_runtime_value_for_slice_query()
            throws Exception
    {

        ThriftBatchingEntityManager batchEm = emf.createBatchingEntityManager();
        batchEm.startBatch(ONE);

        expectedEx.expect(HInvalidRequestException.class);
        expectedEx
                .expectMessage("InvalidRequestException(why:EACH_QUORUM ConsistencyLevel is only supported for writes)");

        batchEm.sliceQuery(ClusteredEntity.class)
                .partitionKey(11L)
                .consistencyLevel(EACH_QUORUM)
                .get(10);
    }

    private void assertThatConsistencyLevelsAreReinitialized()
    {
        assertThat(policy.getCurrentReadLevel()).isNull();
        assertThat(policy.getCurrentWriteLevel()).isNull();
    }

}
