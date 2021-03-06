package info.archinnov.achilles.entity.metadata;

import static org.fest.assertions.api.Assertions.assertThat;
import info.archinnov.achilles.test.builders.PropertyMetaTestBuilder;
import info.archinnov.achilles.type.ConsistencyLevel;
import org.apache.cassandra.utils.Pair;

import org.junit.Test;


/**
 * CounterPropertiesTest
 * 
 * @author DuyHai DOAN
 * 
 */
public class CounterPropertiesTest
{
	@Test
	public void should_to_string() throws Exception
	{
		PropertyMeta idMeta = PropertyMetaTestBuilder //
				.completeBean(Void.class, Long.class) //
				.field("id").type(PropertyType.SIMPLE)
				//
				//
				.consistencyLevels(
						Pair.create(ConsistencyLevel.ALL,
								ConsistencyLevel.ALL))//
				.build();
		CounterProperties props = new CounterProperties("fqcn", idMeta);

		assertThat(props.toString()).isEqualTo(
				"CounterProperties [fqcn=fqcn, idMeta=" + idMeta.toString() + "]");
	}
}
