package info.archinnov.achilles.query.cql;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import info.archinnov.achilles.context.CQLDaoContext;
import info.archinnov.achilles.entity.operations.CQLNativeQueryMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;

/**
 * CQLNativeQueryTest
 * 
 * @author DuyHai DOAN
 * 
 */

@RunWith(MockitoJUnitRunner.class)
public class CQLNativeQueryBuilderTest {

    @InjectMocks
    private CQLNativeQueryBuilder query;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CQLDaoContext daoContext;

    private String queryString = "query";

    @Mock
    private CQLNativeQueryMapper mapper;

    @Mock
    private Row row;

    @Before
    public void setUp()
    {
        Whitebox.setInternalState(query, String.class, queryString);
        Whitebox.setInternalState(query, CQLNativeQueryMapper.class, mapper);
    }

    @Test
    public void should_get() throws Exception
    {
        List<Row> rows = Arrays.asList(row);
        when(daoContext.execute(any(SimpleStatement.class)).all()).thenReturn(rows);

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        when(mapper.mapRows(rows)).thenReturn(result);

        List<Map<String, Object>> actual = query.get();

        assertThat(actual).isSameAs(result);
    }

    @Test
    public void should_get_one() throws Exception
    {

        List<Row> rows = Arrays.asList(row);
        when(daoContext.execute(any(SimpleStatement.class)).all()).thenReturn(rows);

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Map<String, Object> line = new LinkedHashMap<String, Object>();
        result.add(line);
        when(mapper.mapRows(rows)).thenReturn(result);

        Map<String, Object> actual = query.first();
        assertThat(actual).isSameAs(line);
    }

    @Test
    public void should_return_null_when_no_row() throws Exception
    {

        List<Row> rows = Arrays.asList(row);
        when(daoContext.execute(any(SimpleStatement.class)).all()).thenReturn(rows);

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        when(mapper.mapRows(rows)).thenReturn(result);

        Map<String, Object> actual = query.first();
        assertThat(actual).isNull();
    }

}
