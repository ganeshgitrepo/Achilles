package info.archinnov.achilles.entity.operations.impl;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import info.archinnov.achilles.composite.ThriftCompositeFactory;
import info.archinnov.achilles.context.ThriftConsistencyContext;
import info.archinnov.achilles.context.ThriftPersistenceContext;
import info.archinnov.achilles.context.execution.SafeExecutionContext;
import info.archinnov.achilles.dao.ThriftGenericEntityDao;
import info.archinnov.achilles.dao.ThriftGenericWideRowDao;
import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.iterator.ThriftCounterSliceIterator;
import info.archinnov.achilles.iterator.ThriftJoinSliceIterator;
import info.archinnov.achilles.iterator.ThriftSliceIterator;
import info.archinnov.achilles.query.SliceQuery;
import info.archinnov.achilles.test.integration.entity.ClusteredEntity;
import info.archinnov.achilles.type.ConsistencyLevel;
import info.archinnov.achilles.type.BoundingMode;
import info.archinnov.achilles.type.OrderingMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.prettyprint.hector.api.beans.Composite;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.HCounterColumn;
import me.prettyprint.hector.api.mutation.Mutator;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

/**
 * ThriftQueryExecutorImplTest
 * 
 * @author DuyHai DOAN
 * 
 */

@RunWith(MockitoJUnitRunner.class)
public class ThriftQueryExecutorImplTest
{

	@InjectMocks
	private ThriftQueryExecutorImpl executor;

	@Mock
	private ThriftCompositeFactory compositeFactory;

	@Mock
	private ThriftPersistenceContext context;

	@Mock
	private ThriftConsistencyContext consistencyContext;

	@Mock
	private ThriftGenericWideRowDao wideRowDao;

	@Mock
	private ThriftGenericEntityDao entityDao;

	@Mock
	private SliceQuery<ClusteredEntity> query;

	@Mock
	private PropertyMeta<?, ?> idMeta;

	@Mock
	private PropertyMeta<Object, Object> pm;

	@Mock
	private Mutator<Object> mutator;

	@Captor
	private ArgumentCaptor<SafeExecutionContext<List<HCounterColumn<Composite>>>> counterColumnsCaptor;

	@Captor
	private ArgumentCaptor<SafeExecutionContext<List<HColumn<Composite, Object>>>> columnsCaptor;

	@Captor
	private ArgumentCaptor<SafeExecutionContext<Void>> removeCaptor;

	@Captor
	private ArgumentCaptor<SafeExecutionContext<ThriftSliceIterator<Long, Object>>> iteratorCaptor;

	@Captor
	private ArgumentCaptor<SafeExecutionContext<ThriftJoinSliceIterator<Long, Object, Object>>> joinIteratorCaptor;

	@Captor
	private ArgumentCaptor<SafeExecutionContext<ThriftCounterSliceIterator<Long>>> counterIteratorCaptor;

	private EntityMeta meta;

	private Long partitionKey = RandomUtils.nextLong();

	private List<Object> clusteringsFrom = Arrays.<Object> asList();

	private List<Object> clusteringsTo = Arrays.<Object> asList();

	private OrderingMode ordering = OrderingMode.ASCENDING;

	private BoundingMode bounding = BoundingMode.EXCLUSIVE_BOUNDS;

	private ConsistencyLevel consistencyLevel = ConsistencyLevel.ALL;

	private int limit = RandomUtils.nextInt();

	private int batchSize = RandomUtils.nextInt();

	@Before
	public void setUp()
	{
		meta = new EntityMeta();
		meta.setIdMeta(idMeta);
		meta.setPropertyMetas(ImmutableMap.<String, PropertyMeta<?, ?>> of("pm", pm));

		when(query.getMeta()).thenReturn(meta);
		when(query.getClusteringsFrom()).thenReturn(clusteringsFrom);
		when(query.getClusteringsTo()).thenReturn(clusteringsTo);
		when(query.getBounding()).thenReturn(bounding);
		when(query.getOrdering()).thenReturn(ordering);
		when(query.getConsistencyLevel()).thenReturn(consistencyLevel);
		when(query.getLimit()).thenReturn(limit);
		when(query.getBatchSize()).thenReturn(batchSize);
		when(query.getPartitionKey()).thenReturn(partitionKey);

		when(context.getWideRowDao()).thenReturn(wideRowDao);
		when(context.getPartitionKey()).thenReturn(partitionKey);
		when(wideRowDao.buildMutator()).thenReturn(mutator);
	}

	@Test
	public void should_find_counter_columns() throws Exception
	{
		Composite comp1 = new Composite();
		Composite comp2 = new Composite();
		Composite[] composites = new Composite[]
		{
				comp1,
				comp2
		};

		when(compositeFactory.createForClusteredQuery(idMeta, clusteringsFrom, clusteringsTo,
				bounding, ordering)).thenReturn(composites);

		List<HCounterColumn<Composite>> hCounterColumns = new ArrayList<HCounterColumn<Composite>>();

		when(wideRowDao.findCounterColumnsRange(partitionKey, comp1, comp2, limit,
				ordering.isReverse())).thenReturn(hCounterColumns);

		executor.findCounterColumns(query, context);

		verify(context).executeWithReadConsistencyLevel(counterColumnsCaptor.capture(),
				eq(consistencyLevel));

		List<HCounterColumn<Composite>> actual = counterColumnsCaptor.getValue().execute();

		assertThat(actual).isSameAs(hCounterColumns);
	}

	@Test
	public void should_find_columns() throws Exception
	{
		Composite comp1 = new Composite();
		Composite comp2 = new Composite();
		Composite[] composites = new Composite[]
		{
				comp1,
				comp2
		};

		when(compositeFactory.createForClusteredQuery(idMeta, clusteringsFrom, clusteringsTo,
				bounding, ordering)).thenReturn(composites);

		List<HColumn<Composite, Object>> hColumns = new ArrayList<HColumn<Composite, Object>>();

		when(wideRowDao.findRawColumnsRange(partitionKey, comp1, comp2, limit,
				ordering.isReverse())).thenReturn(hColumns);

		executor.findColumns(query, context);

		verify(context).executeWithReadConsistencyLevel(columnsCaptor.capture(),
				eq(consistencyLevel));

		List<HColumn<Composite, Object>> actual = columnsCaptor.getValue().execute();

		assertThat(actual).isSameAs(hColumns);
	}

	@Test
	public void should_get_columns_iterator() throws Exception
	{
		Composite comp1 = new Composite();
		Composite comp2 = new Composite();
		Composite[] composites = new Composite[]
		{
				comp1,
				comp2
		};

		when(compositeFactory.createForClusteredQuery(idMeta, clusteringsFrom, clusteringsTo,
				bounding, ordering)).thenReturn(composites);

		ThriftSliceIterator<Long, Object> iterator = mock(ThriftSliceIterator.class);

		when(wideRowDao.getColumnsIterator(partitionKey, comp1, comp2,
				ordering.isReverse(), batchSize)).thenReturn(iterator);

		executor.getColumnsIterator(query, context);

		verify(context).executeWithReadConsistencyLevel(iteratorCaptor.capture(),
				eq(consistencyLevel));

		ThriftSliceIterator<Long, Object> actual = iteratorCaptor.getValue().execute();

		assertThat(actual).isSameAs(iterator);
	}

	@Test
	public void should_get_join_columns_iterator() throws Exception
	{
		Composite comp1 = new Composite();
		Composite comp2 = new Composite();
		Composite[] composites = new Composite[]
		{
				comp1,
				comp2
		};

		EntityMeta joinMeta = new EntityMeta();
		joinMeta.setTableName("tableName");

		when(pm.joinMeta()).thenReturn(joinMeta);
		when(context.findEntityDao("tableName")).thenReturn(entityDao);

		when(compositeFactory.createForClusteredQuery(idMeta, clusteringsFrom, clusteringsTo,
				bounding, ordering)).thenReturn(composites);

		ThriftJoinSliceIterator<Long, Object, Object> iterator = mock(ThriftJoinSliceIterator.class);

		when(wideRowDao.getJoinColumnsIterator(entityDao, pm, partitionKey, comp1, comp2,
				ordering.isReverse(), batchSize)).thenReturn(iterator);

		executor.getJoinColumnsIterator(query, context);

		verify(context).executeWithReadConsistencyLevel(joinIteratorCaptor.capture(),
				eq(consistencyLevel));

		ThriftJoinSliceIterator<Long, Object, Object> actual = joinIteratorCaptor
				.getValue().execute();

		assertThat(actual).isSameAs(iterator);
	}

	@Test
	public void should_get_counter_columns_iterator() throws Exception
	{
		Composite comp1 = new Composite();
		Composite comp2 = new Composite();
		Composite[] composites = new Composite[]
		{
				comp1,
				comp2
		};

		when(compositeFactory.createForClusteredQuery(idMeta, clusteringsFrom, clusteringsTo,
				bounding, ordering)).thenReturn(composites);

		ThriftCounterSliceIterator<Long> iterator = mock(ThriftCounterSliceIterator.class);

		when(wideRowDao.getCounterColumnsIterator(partitionKey, comp1, comp2,
				ordering.isReverse(), batchSize)).thenReturn(iterator);

		executor.getCounterColumnsIterator(query, context);

		verify(context).executeWithReadConsistencyLevel(counterIteratorCaptor.capture(),
				eq(consistencyLevel));

		ThriftCounterSliceIterator<Long> actual = counterIteratorCaptor.getValue().execute();

		assertThat(actual).isSameAs(iterator);
	}

	@Test
	public void should_remove_columns() throws Exception
	{
		Composite name = new Composite();
		HColumn<Composite, Object> column = mock(HColumn.class);
		when(column.getName()).thenReturn(name);

		List<HColumn<Composite, Object>> columns = Arrays.asList(column);

		executor.removeColumns(columns, consistencyLevel, context);

		verify(wideRowDao).removeColumnBatch(partitionKey, name, mutator);
		verify(context).executeWithWriteConsistencyLevel(removeCaptor.capture(),
				eq(consistencyLevel));

		removeCaptor.getValue().execute();

		verify(wideRowDao).executeMutator(mutator);
	}

	@Test
	public void should_remove_counter_columns() throws Exception
	{
		Composite name = new Composite();
		HCounterColumn<Composite> counterColumn = mock(HCounterColumn.class);
		when(counterColumn.getName()).thenReturn(name);

		List<HCounterColumn<Composite>> counterColumns = Arrays.asList(counterColumn);

		executor.removeCounterColumns(counterColumns, consistencyLevel, context);

		verify(wideRowDao).removeCounterBatch(partitionKey, name, mutator);
		verify(context).executeWithWriteConsistencyLevel(removeCaptor.capture(),
				eq(consistencyLevel));

		removeCaptor.getValue().execute();

		verify(wideRowDao).executeMutator(mutator);
	}

}
