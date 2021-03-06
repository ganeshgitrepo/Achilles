package info.archinnov.achilles.dao;

import static info.archinnov.achilles.dao.ThriftGenericEntityDao.*;
import static org.fest.assertions.api.Assertions.assertThat;
import info.archinnov.achilles.serializer.ThriftSerializerUtils;
import org.apache.cassandra.utils.Pair;
import me.prettyprint.cassandra.model.ExecutingKeyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.AbstractComposite.ComponentEquality;
import me.prettyprint.hector.api.beans.Composite;
import me.prettyprint.hector.api.mutation.Mutator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

/**
 * ThriftGenericEntityDaoTest
 * 
 * @author DuyHai DOAN
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ThriftGenericEntityDaoTest
{

	@InjectMocks
	private ThriftGenericEntityDao dao = new ThriftGenericEntityDao();

	@Mock
	private ExecutingKeyspace keyspace;

	@Mock
	private final Serializer<Long> serializer = ThriftSerializerUtils.LONG_SRZ;

	@Test
	public void should_build_mutator() throws Exception
	{
		dao = new ThriftGenericEntityDao(Pair.create(Long.class,
				String.class));
		Mutator<Long> mutator = dao.buildMutator();
		assertThat(mutator).isNotNull();
	}

	@Test
	public void should_build_start_composite_for_eager_fetch() throws Exception
	{

		Composite comp = Whitebox.getInternalState(dao, "startCompositeForEagerFetch");

		assertThat(comp.getComponent(0).getValue()).isEqualTo(START_EAGER);
		assertThat(comp.getComponent(0).getEquality()).isSameAs(ComponentEquality.EQUAL);
	}

	@Test
	public void should_build_end_composite_for_eager_fetch() throws Exception
	{

		Composite comp = Whitebox.getInternalState(dao, "endCompositeForEagerFetch");

		assertThat(comp.getComponent(0).getValue()).isEqualTo(END_EAGER);
		assertThat(comp.getComponent(0).getEquality()).isSameAs(
				ComponentEquality.GREATER_THAN_EQUAL);
	}
}
