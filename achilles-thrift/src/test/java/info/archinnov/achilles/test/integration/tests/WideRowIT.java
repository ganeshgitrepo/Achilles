package info.archinnov.achilles.test.integration.tests;

import static info.archinnov.achilles.common.ThriftCassandraDaoTest.getColumnFamilyDao;
import static info.archinnov.achilles.serializer.ThriftSerializerUtils.*;
import static info.archinnov.achilles.table.TableNameNormalizer.normalizerAndValidateColumnFamilyName;
import static info.archinnov.achilles.type.BoundingMode.*;
import static info.archinnov.achilles.type.OrderingMode.DESCENDING;
import static org.fest.assertions.api.Assertions.assertThat;
import info.archinnov.achilles.common.ThriftCassandraDaoTest;
import info.archinnov.achilles.dao.ThriftGenericWideRowDao;
import info.archinnov.achilles.entity.manager.ThriftEntityManager;
import info.archinnov.achilles.test.integration.entity.ClusteredEntity;
import info.archinnov.achilles.test.integration.entity.ClusteredEntity.ClusteredKey;
import java.util.Iterator;
import java.util.List;
import me.prettyprint.hector.api.beans.Composite;
import me.prettyprint.hector.api.mutation.Mutator;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.After;
import org.junit.Test;
import com.google.common.base.Optional;

/**
 * WideRowIT
 * 
 * @author DuyHai DOAN
 * 
 */
public class WideRowIT
{

    private ThriftGenericWideRowDao dao = getColumnFamilyDao(
            normalizerAndValidateColumnFamilyName("clustered"), Long.class,
            String.class);

    private ThriftEntityManager em = ThriftCassandraDaoTest.getEm();

    private ClusteredEntity entity;

    private ClusteredKey compoundKey;

    @Test
    public void should_persist_and_find() throws Exception
    {
        compoundKey = new ClusteredKey(RandomUtils.nextLong(), RandomUtils.nextInt(), "name");

        entity = new ClusteredEntity(compoundKey, "clustered_value");

        em.persist(entity);

        ClusteredEntity found = em.find(ClusteredEntity.class, compoundKey);

        assertThat(found.getId()).isEqualTo(compoundKey);
        assertThat(found.getValue()).isEqualTo("clustered_value");
    }

    @Test
    public void should_merge_and_get_reference() throws Exception
    {
        compoundKey = new ClusteredKey(RandomUtils.nextLong(), RandomUtils.nextInt(), "name");

        entity = new ClusteredEntity(compoundKey, "clustered_value");

        em.merge(entity);

        ClusteredEntity found = em.getReference(ClusteredEntity.class, compoundKey);

        assertThat(found.getId()).isEqualTo(compoundKey);
        assertThat(found.getValue()).isEqualTo("clustered_value");
    }

    @Test
    public void should_merge_modifications() throws Exception
    {
        compoundKey = new ClusteredKey(RandomUtils.nextLong(), RandomUtils.nextInt(), "name");

        entity = new ClusteredEntity(compoundKey, "clustered_value");

        entity = em.merge(entity);

        entity.setValue("new_clustered_value");
        em.merge(entity);

        entity = em.find(ClusteredEntity.class, compoundKey);

        assertThat(entity.getValue()).isEqualTo("new_clustered_value");
    }

    @Test
    public void should_remove() throws Exception
    {
        compoundKey = new ClusteredKey(RandomUtils.nextLong(), RandomUtils.nextInt(), "name");

        entity = new ClusteredEntity(compoundKey, "clustered_value");

        entity = em.merge(entity);

        em.remove(entity);

        assertThat(em.find(ClusteredEntity.class, compoundKey)).isNull();

    }

    @Test
    public void should_refresh() throws Exception
    {

        long partitionKey = RandomUtils.nextLong();
        int count = RandomUtils.nextInt();
        String name = "name";
        compoundKey = new ClusteredKey(partitionKey, count, name);

        entity = new ClusteredEntity(compoundKey, "clustered_value");

        entity = em.merge(entity);

        Composite comp = new Composite();
        comp.setComponent(0, count, INT_SRZ);
        comp.setComponent(1, name, STRING_SRZ);
        Mutator<Long> mutator = dao.buildMutator();
        dao.insertColumnBatch(partitionKey, comp, "new_clustered_value",
                Optional.<Integer> absent(), mutator);
        dao.executeMutator(mutator);

        em.refresh(entity);

        assertThat(entity.getValue()).isEqualTo("new_clustered_value");

    }

    @Test
    public void should_query_with_default_params() throws Exception
    {
        long partitionKey = RandomUtils.nextLong();
        List<ClusteredEntity> entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .fromClusterings(2, "name2")
                .toClusterings(4, "name4")
                .get();

        assertThat(entities).isEmpty();

        String clusteredValuePrefix = insertValues(partitionKey, 5);

        entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .fromClusterings(2, "name2")
                .toClusterings(4, "name4")
                .get();

        assertThat(entities).hasSize(3);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 2);
        assertThat(entities.get(0).getId().getId()).isEqualTo(partitionKey);
        assertThat(entities.get(0).getId().getCount()).isEqualTo(2);
        assertThat(entities.get(0).getId().getName()).isEqualTo("name2");
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 3);
        assertThat(entities.get(1).getId().getId()).isEqualTo(partitionKey);
        assertThat(entities.get(1).getId().getCount()).isEqualTo(3);
        assertThat(entities.get(1).getId().getName()).isEqualTo("name3");
        assertThat(entities.get(2).getValue()).isEqualTo(clusteredValuePrefix + 4);
        assertThat(entities.get(2).getId().getId()).isEqualTo(partitionKey);
        assertThat(entities.get(2).getId().getCount()).isEqualTo(4);
        assertThat(entities.get(2).getId().getName()).isEqualTo("name4");

        entities = em.sliceQuery(ClusteredEntity.class)
                .fromEmbeddedId(new ClusteredKey(partitionKey, 2, "name2"))
                .toEmbeddedId(new ClusteredKey(partitionKey, 4, "name4"))
                .get();

        assertThat(entities).hasSize(3);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 2);
        assertThat(entities.get(0).getId().getId()).isEqualTo(partitionKey);
        assertThat(entities.get(0).getId().getCount()).isEqualTo(2);
        assertThat(entities.get(0).getId().getName()).isEqualTo("name2");
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 3);
        assertThat(entities.get(1).getId().getId()).isEqualTo(partitionKey);
        assertThat(entities.get(1).getId().getCount()).isEqualTo(3);
        assertThat(entities.get(1).getId().getName()).isEqualTo("name3");
        assertThat(entities.get(2).getValue()).isEqualTo(clusteredValuePrefix + 4);
        assertThat(entities.get(2).getId().getId()).isEqualTo(partitionKey);
        assertThat(entities.get(2).getId().getCount()).isEqualTo(4);
        assertThat(entities.get(2).getId().getName()).isEqualTo("name4");
    }

    @Test
    public void should_query_with_custom_params() throws Exception
    {
        long partitionKey = RandomUtils.nextLong();
        String clusteredValuePrefix = insertValues(partitionKey, 5);

        List<ClusteredEntity> entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .fromClusterings(4, "name4")
                .toClusterings(1, "name1")
                .bounding(INCLUSIVE_END_BOUND_ONLY)
                .ordering(DESCENDING)
                .limit(2)
                .get();

        assertThat(entities).hasSize(2);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 3);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 2);

        entities = em.sliceQuery(ClusteredEntity.class)
                .fromEmbeddedId(new ClusteredKey(partitionKey, 4, "name4"))
                .toEmbeddedId(new ClusteredKey(partitionKey, 1, "name1"))
                .bounding(INCLUSIVE_END_BOUND_ONLY)
                .ordering(DESCENDING)
                .limit(4)
                .get();

        assertThat(entities).hasSize(3);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 3);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 2);
        assertThat(entities.get(2).getValue()).isEqualTo(clusteredValuePrefix + 1);

    }

    @Test
    public void should_query_with_getFirst() throws Exception
    {
        long partitionKey = RandomUtils.nextLong();
        ClusteredEntity entity = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .getFirstOccurence();

        assertThat(entity).isNull();

        String clusteredValuePrefix = insertValues(partitionKey, 5);

        entity = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .getFirstOccurence();

        assertThat(entity.getValue()).isEqualTo(clusteredValuePrefix + 1);

        entity = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .getFirstOccurence();

        assertThat(entity.getValue()).isEqualTo(clusteredValuePrefix + 1);

        List<ClusteredEntity> entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .getFirst(3);

        assertThat(entities).hasSize(3);
        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 1);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 2);
        assertThat(entities.get(2).getValue()).isEqualTo(clusteredValuePrefix + 3);

        insertClusteredEntity(partitionKey, 4, "name42", clusteredValuePrefix + 42);

        entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .getFirst(3, 4);

        assertThat(entities).hasSize(2);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 4);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 42);

    }

    @Test
    public void should_query_with_getLast() throws Exception
    {
        long partitionKey = RandomUtils.nextLong();

        ClusteredEntity entity = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .getLastOccurence();

        assertThat(entity).isNull();

        String clusteredValuePrefix = insertValues(partitionKey, 5);

        entity = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .getLastOccurence();

        assertThat(entity.getValue()).isEqualTo(clusteredValuePrefix + 5);

        entity = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .getLastOccurence();

        assertThat(entity.getValue()).isEqualTo(clusteredValuePrefix + 5);

        List<ClusteredEntity> entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .getLast(3);

        assertThat(entities).hasSize(3);
        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 5);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 4);
        assertThat(entities.get(2).getValue()).isEqualTo(clusteredValuePrefix + 3);

        insertClusteredEntity(partitionKey, 4, "name42", clusteredValuePrefix + 42);
        insertClusteredEntity(partitionKey, 4, "name43", clusteredValuePrefix + 43);
        insertClusteredEntity(partitionKey, 4, "name44", clusteredValuePrefix + 44);
        insertClusteredEntity(partitionKey, 4, "name45", clusteredValuePrefix + 45);

        entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .getLast(3, 4);

        assertThat(entities).hasSize(3);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 45);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 44);
        assertThat(entities.get(2).getValue()).isEqualTo(clusteredValuePrefix + 43);

    }

    @Test
    public void should_iterate_with_default_params() throws Exception
    {
        long partitionKey = RandomUtils.nextLong();
        String clusteredValuePrefix = insertValues(partitionKey, 5);

        Iterator<ClusteredEntity> iter = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .iterator();

        assertThat(iter.hasNext()).isTrue();
        ClusteredEntity next = iter.next();
        assertThat(next.getValue()).isEqualTo(clusteredValuePrefix + 1);
        assertThat(next.getId().getId()).isEqualTo(partitionKey);
        assertThat(next.getId().getCount()).isEqualTo(1);
        assertThat(next.getId().getName()).isEqualTo("name1");
        assertThat(iter.hasNext()).isTrue();

        assertThat(iter.hasNext()).isTrue();
        next = iter.next();
        assertThat(next.getId().getId()).isEqualTo(partitionKey);
        assertThat(next.getId().getCount()).isEqualTo(2);
        assertThat(next.getId().getName()).isEqualTo("name2");
        assertThat(next.getValue()).isEqualTo(clusteredValuePrefix + 2);

        assertThat(iter.hasNext()).isTrue();
        next = iter.next();
        assertThat(next.getId().getId()).isEqualTo(partitionKey);
        assertThat(next.getId().getCount()).isEqualTo(3);
        assertThat(next.getId().getName()).isEqualTo("name3");
        assertThat(next.getValue()).isEqualTo(clusteredValuePrefix + 3);

        assertThat(iter.hasNext()).isTrue();
        next = iter.next();
        assertThat(next.getId().getId()).isEqualTo(partitionKey);
        assertThat(next.getId().getCount()).isEqualTo(4);
        assertThat(next.getId().getName()).isEqualTo("name4");
        assertThat(next.getValue()).isEqualTo(clusteredValuePrefix + 4);

        assertThat(iter.hasNext()).isTrue();
        next = iter.next();
        assertThat(next.getId().getId()).isEqualTo(partitionKey);
        assertThat(next.getId().getCount()).isEqualTo(5);
        assertThat(next.getId().getName()).isEqualTo("name5");
        assertThat(next.getValue()).isEqualTo(clusteredValuePrefix + 5);
        assertThat(iter.hasNext()).isFalse();
    }

    @Test
    public void should_iterate_with_custom_params() throws Exception
    {
        long partitionKey = RandomUtils.nextLong();
        String clusteredValuePrefix = insertValues(partitionKey, 5);

        Iterator<ClusteredEntity> iter = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .fromClusterings(3)
                .iterator(2);

        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next().getValue()).isEqualTo(clusteredValuePrefix + 3);
        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next().getValue()).isEqualTo(clusteredValuePrefix + 4);
        assertThat(iter.hasNext()).isTrue();
        assertThat(iter.next().getValue()).isEqualTo(clusteredValuePrefix + 5);
        assertThat(iter.hasNext()).isFalse();
    }

    @Test
    public void should_remove_with_default_params() throws Exception
    {
        long partitionKey = RandomUtils.nextLong();
        String clusteredValuePrefix = insertValues(partitionKey, 5);

        em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .fromClusterings(2, "name2")
                .toClusterings(4)
                .remove();

        List<ClusteredEntity> entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .get(100);

        assertThat(entities).hasSize(2);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 1);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 5);
    }

    @Test
    public void should_remove_with_custom_params() throws Exception
    {
        long partitionKey = RandomUtils.nextLong();
        String clusteredValuePrefix = insertValues(partitionKey, 5);

        em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .fromClusterings(5)
                .toClusterings(1, "name2")
                .bounding(EXCLUSIVE_BOUNDS)
                .ordering(DESCENDING)
                .limit(2)
                .remove();

        List<ClusteredEntity> entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .get(100);

        assertThat(entities).hasSize(3);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 1);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 2);
        assertThat(entities.get(2).getValue()).isEqualTo(clusteredValuePrefix + 5);
    }

    @Test
    public void should_remove_n() throws Exception
    {
        long partitionKey = RandomUtils.nextLong();
        String clusteredValuePrefix = insertValues(partitionKey, 5);

        em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .remove(3);

        List<ClusteredEntity> entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .get(100);

        assertThat(entities).hasSize(2);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 4);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 5);
    }

    @Test
    public void should_remove_first() throws Exception
    {
        long partitionKey = RandomUtils.nextLong();
        String clusteredValuePrefix = insertValues(partitionKey, 5);

        em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .removeFirstOccurence();

        List<ClusteredEntity> entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .get(100);

        assertThat(entities).hasSize(4);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 2);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 3);
        assertThat(entities.get(2).getValue()).isEqualTo(clusteredValuePrefix + 4);
        assertThat(entities.get(3).getValue()).isEqualTo(clusteredValuePrefix + 5);
    }

    @Test
    public void should_remove_first_with_clustering_components() throws Exception
    {
        long partitionKey = RandomUtils.nextLong();
        String clusteredValuePrefix = insertValues(partitionKey, 5);
        insertClusteredEntity(partitionKey, 2, "name22", clusteredValuePrefix + 22);
        insertClusteredEntity(partitionKey, 2, "name23", clusteredValuePrefix + 23);
        insertClusteredEntity(partitionKey, 2, "name24", clusteredValuePrefix + 24);

        em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .removeFirstOccurence(2);

        List<ClusteredEntity> entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .get(100);

        assertThat(entities).hasSize(7);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 1);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 22);
        assertThat(entities.get(2).getValue()).isEqualTo(clusteredValuePrefix + 23);
        assertThat(entities.get(3).getValue()).isEqualTo(clusteredValuePrefix + 24);
        assertThat(entities.get(4).getValue()).isEqualTo(clusteredValuePrefix + 3);
        assertThat(entities.get(5).getValue()).isEqualTo(clusteredValuePrefix + 4);
        assertThat(entities.get(6).getValue()).isEqualTo(clusteredValuePrefix + 5);
    }

    @Test
    public void should_remove_first_n() throws Exception {
        long partitionKey = RandomUtils.nextLong();
        String clusteredValuePrefix = insertValues(partitionKey, 5);

        em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .removeFirst(2);

        List<ClusteredEntity> entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .get(100);

        assertThat(entities).hasSize(3);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 3);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 4);
        assertThat(entities.get(2).getValue()).isEqualTo(clusteredValuePrefix + 5);
    }

    @Test
    public void should_remove_first_n_with_clustering_components() throws Exception {
        long partitionKey = RandomUtils.nextLong();
        String clusteredValuePrefix = insertValues(partitionKey, 5);

        insertClusteredEntity(partitionKey, 4, "name42", clusteredValuePrefix + 42);
        insertClusteredEntity(partitionKey, 4, "name43", clusteredValuePrefix + 43);

        em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .removeFirst(5, 4);

        List<ClusteredEntity> entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .get(100);

        assertThat(entities).hasSize(4);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 1);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 2);
        assertThat(entities.get(2).getValue()).isEqualTo(clusteredValuePrefix + 3);
        assertThat(entities.get(3).getValue()).isEqualTo(clusteredValuePrefix + 5);
    }

    @Test
    public void should_remove_last() throws Exception
    {
        long partitionKey = RandomUtils.nextLong();
        String clusteredValuePrefix = insertValues(partitionKey, 5);

        em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .removeLastOccurence();

        List<ClusteredEntity> entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .get(100);

        assertThat(entities).hasSize(4);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 1);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 2);
        assertThat(entities.get(2).getValue()).isEqualTo(clusteredValuePrefix + 3);
        assertThat(entities.get(3).getValue()).isEqualTo(clusteredValuePrefix + 4);
    }

    @Test
    public void should_remove_last_n() throws Exception
    {
        long partitionKey = RandomUtils.nextLong();
        String clusteredValuePrefix = insertValues(partitionKey, 5);

        em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .removeLast(2);

        List<ClusteredEntity> entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .get(100);

        assertThat(entities).hasSize(3);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 1);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 2);
        assertThat(entities.get(2).getValue()).isEqualTo(clusteredValuePrefix + 3);

    }

    @Test
    public void should_remove_last_n_with_clustering_keys() throws Exception
    {
        long partitionKey = RandomUtils.nextLong();
        String clusteredValuePrefix = insertValues(partitionKey, 5);
        insertClusteredEntity(partitionKey, 4, "name42", clusteredValuePrefix + 42);
        insertClusteredEntity(partitionKey, 4, "name43", clusteredValuePrefix + 43);
        insertClusteredEntity(partitionKey, 4, "name44", clusteredValuePrefix + 44);

        em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .removeLast(2, 4);

        List<ClusteredEntity> entities = em.sliceQuery(ClusteredEntity.class)
                .partitionKey(partitionKey)
                .get(100);

        assertThat(entities).hasSize(6);

        assertThat(entities.get(0).getValue()).isEqualTo(clusteredValuePrefix + 1);
        assertThat(entities.get(1).getValue()).isEqualTo(clusteredValuePrefix + 2);
        assertThat(entities.get(2).getValue()).isEqualTo(clusteredValuePrefix + 3);
        assertThat(entities.get(3).getValue()).isEqualTo(clusteredValuePrefix + 4);
        assertThat(entities.get(4).getValue()).isEqualTo(clusteredValuePrefix + 42);
        assertThat(entities.get(5).getValue()).isEqualTo(clusteredValuePrefix + 5);

    }

    private String insertValues(long partitionKey, int count)
    {
        String namePrefix = "name";
        String clusteredValuePrefix = "value";

        for (int i = 1; i <= count; i++)
        {
            insertClusteredEntity(partitionKey, i, namePrefix + i, clusteredValuePrefix + i);
        }
        return clusteredValuePrefix;
    }

    private void insertClusteredEntity(Long partitionKey, int count, String name,
            String clusteredValue)
    {
        ClusteredKey embeddedId = new ClusteredKey(partitionKey, count, name);
        ClusteredEntity entity = new ClusteredEntity(embeddedId, clusteredValue);
        em.persist(entity);
    }

    @After
    public void tearDown()
    {
        dao.truncate();
    }
}
