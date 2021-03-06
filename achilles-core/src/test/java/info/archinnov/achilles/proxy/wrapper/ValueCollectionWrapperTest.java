package info.archinnov.achilles.proxy.wrapper;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * AchillesValueCollectionWrapperTest
 * 
 * @author DuyHai DOAN
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ValueCollectionWrapperTest
{

    @Test(expected = UnsupportedOperationException.class)
    public void should_exception_on_add() throws Exception
    {
        ValueCollectionWrapper wrapper = new ValueCollectionWrapper(Arrays.<Object> asList("a"));

        wrapper.add("");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void should_exception_on_add_all() throws Exception
    {
        ValueCollectionWrapper wrapper = new ValueCollectionWrapper(Arrays.<Object> asList("a"));

        wrapper.addAll(Arrays.asList("a", "b"));
    }
}
