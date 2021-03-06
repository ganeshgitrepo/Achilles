package info.archinnov.achilles.proxy.wrapper;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import info.archinnov.achilles.consistency.AchillesConsistencyLevelPolicy;
import info.archinnov.achilles.context.PersistenceContext;
import info.archinnov.achilles.entity.metadata.EntityMeta;
import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.entity.metadata.PropertyType;
import info.archinnov.achilles.entity.operations.EntityProxifier;
import info.archinnov.achilles.test.builders.CompleteBeanTestBuilder;
import info.archinnov.achilles.test.builders.PropertyMetaTestBuilder;
import info.archinnov.achilles.test.mapping.entity.CompleteBean;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * AchillesCollectionWrapperTest
 * 
 * @author DuyHai DOAN
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectionWrapperTest
{
    @Mock
    private Map<Method, PropertyMeta> dirtyMap;

    private Method setter;

    @Mock
    private PropertyMeta propertyMeta;

    @Mock
    private PropertyMeta joinPropertyMeta;

    @Mock
    private EntityProxifier<PersistenceContext> proxifier;

    @Mock
    private AchillesConsistencyLevelPolicy policy;

    @Mock
    private PersistenceContext context;

    @Mock
    private PersistenceContext joinContext;

    private EntityMeta entityMeta;

    private CompleteBean entity = CompleteBeanTestBuilder.builder().randomId().buid();

    @Before
    public void setUp() throws Exception
    {
        setter = CompleteBean.class.getDeclaredMethod("setFriends", List.class);
        when(propertyMeta.type()).thenReturn(PropertyType.LIST);

        PropertyMeta idMeta = PropertyMetaTestBuilder //
                .completeBean(Void.class, Long.class)
                .field("id")
                .type(PropertyType.SIMPLE)
                .accessors()
                .build();

        entityMeta = new EntityMeta();
        entityMeta.setIdMeta(idMeta);
    }

    @Test
    public void should_mark_dirty_on_element_add() throws Exception
    {
        ArrayList<Object> target = new ArrayList<Object>();
        ListWrapper wrapper = prepareListWrapper(target);
        when(proxifier.unwrap("a")).thenReturn("a");
        wrapper.add("a");

        assertThat(target).hasSize(1);
        assertThat(target.get(0)).isEqualTo("a");

        verify(dirtyMap).put(setter, propertyMeta);
    }

    @Test
    public void should_not_mark_dirty_on_element_add() throws Exception
    {
        ArrayList<Object> target = new ArrayList<Object>();
        ListWrapper wrapper = prepareListWrapper(target);
        when(proxifier.unwrap("a")).thenReturn("a");
        when(dirtyMap.containsKey(setter)).thenReturn(true);
        wrapper.add("a");

        verify(dirtyMap, never()).put(setter, propertyMeta);
    }

    @Test
    public void should_mark_dirty_on_add_all() throws Exception
    {

        ArrayList<Object> target = new ArrayList<Object>();
        ListWrapper wrapper = prepareListWrapper(target);
        Collection<String> list = Arrays.asList("a", "b");

        wrapper.setProxifier(proxifier);

        when(proxifier.unwrap(any(Collection.class))).thenReturn((Collection) list);

        wrapper.addAll(list);

        verify(proxifier).unwrap(list);

        assertThat(target).hasSize(2);
        assertThat(target.get(0)).isEqualTo("a");
        assertThat(target.get(1)).isEqualTo("b");

        verify(dirtyMap).put(setter, propertyMeta);
    }

    @Test
    public void should_not_mark_dirty_on_empty_add_all() throws Exception
    {

        ArrayList<Object> target = new ArrayList<Object>();
        ListWrapper wrapper = prepareListWrapper(target);
        wrapper.addAll(new ArrayList<Object>());

        assertThat(target).hasSize(0);

        verify(dirtyMap, never()).put(setter, propertyMeta);
    }

    @Test
    public void should_mark_dirty_on_clear() throws Exception
    {

        ArrayList<Object> target = new ArrayList<Object>();
        target.add("a");
        ListWrapper wrapper = prepareListWrapper(target);
        wrapper.clear();

        assertThat(target).hasSize(0);

        verify(dirtyMap).put(setter, propertyMeta);
    }

    @Test
    public void should_not_mark_dirty_on_clear_when_empty() throws Exception
    {

        ArrayList<Object> target = new ArrayList<Object>();
        ListWrapper wrapper = prepareListWrapper(target);
        wrapper.clear();

        assertThat(target).hasSize(0);

        verify(dirtyMap, never()).put(setter, propertyMeta);
    }

    @Test
    public void should_return_true_on_contains() throws Exception
    {
        ListWrapper wrapper = prepareListWrapper(Arrays.<Object> asList("a", "b"));
        when(proxifier.unwrap("a")).thenReturn("a");
        assertThat(wrapper.contains("a")).isTrue();
    }

    @Test
    public void should_return_true_on_contains_all() throws Exception
    {
        ListWrapper wrapper = prepareListWrapper(Arrays.<Object> asList("a", "b", "c", "d"));

        List<Object> check = Arrays.<Object> asList("a", "c");
        when(proxifier.unwrap(check)).thenReturn(check);
        assertThat(wrapper.containsAll(check)).isTrue();
    }

    @Test
    public void should_return_true_on_empty_target() throws Exception
    {
        ListWrapper wrapper = prepareListWrapper(new ArrayList<Object>());
        assertThat(wrapper.isEmpty()).isTrue();
    }

    @Test
    public void should_mark_dirty_on_remove() throws Exception
    {
        ArrayList<Object> target = new ArrayList<Object>();
        target.add("a");
        target.add("b");
        ListWrapper wrapper = prepareListWrapper(target);
        when(proxifier.unwrap("a")).thenReturn("a");
        wrapper.remove("a");

        assertThat(target).hasSize(1);
        assertThat(target.get(0)).isEqualTo("b");

        verify(dirtyMap).put(setter, propertyMeta);
    }

    @Test
    public void should_not_mark_dirty_on_remove_when_no_match() throws Exception
    {

        ArrayList<Object> target = new ArrayList<Object>();
        target.add("a");
        target.add("b");
        ListWrapper wrapper = prepareListWrapper(target);
        wrapper.remove("c");

        assertThat(target).hasSize(2);
        assertThat(target.get(0)).isEqualTo("a");
        assertThat(target.get(1)).isEqualTo("b");

        verify(dirtyMap, never()).put(setter, propertyMeta);
    }

    @Test
    public void should_mark_dirty_on_remove_all() throws Exception
    {

        List<Object> target = new ArrayList<Object>();
        target.add("a");
        target.add("b");
        target.add("c");
        ListWrapper wrapper = prepareListWrapper(target);
        wrapper.setProxifier(proxifier);

        Collection<String> list = Arrays.asList("a", "c");
        when(proxifier.unwrap(any(Collection.class))).thenReturn((Collection) list);

        wrapper.removeAll(list);

        assertThat(target).hasSize(1);
        assertThat(target.get(0)).isEqualTo("b");

        verify(dirtyMap).put(setter, propertyMeta);
    }

    @Test
    public void should_not_mark_dirty_on_remove_all_when_no_match() throws Exception
    {

        ArrayList<Object> target = new ArrayList<Object>();
        target.add("a");
        target.add("b");
        target.add("c");
        ListWrapper wrapper = prepareListWrapper(target);
        wrapper.removeAll(Arrays.asList("d", "e"));

        assertThat(target).hasSize(3);
        assertThat(target.get(0)).isEqualTo("a");
        assertThat(target.get(1)).isEqualTo("b");
        assertThat(target.get(2)).isEqualTo("c");

        verify(dirtyMap, never()).put(setter, propertyMeta);
    }

    @Test
    public void should_mark_dirty_on_retain_all() throws Exception
    {

        ArrayList<Object> target = new ArrayList<Object>();
        target.add("a");
        target.add("b");
        target.add("c");
        ListWrapper wrapper = prepareListWrapper(target);
        wrapper.setProxifier(proxifier);
        Collection<String> list = Arrays.asList("a", "c");
        when(proxifier.unwrap(any(Collection.class))).thenReturn((Collection) list);

        wrapper.retainAll(list);

        assertThat(target).hasSize(2);
        assertThat(target.get(0)).isEqualTo("a");
        assertThat(target.get(1)).isEqualTo("c");

        verify(dirtyMap).put(setter, propertyMeta);
    }

    @Test
    public void should_not_mark_dirty_on_retain_all_when_all_match() throws Exception
    {

        ArrayList<Object> target = new ArrayList<Object>();
        target.add("a");
        target.add("b");
        target.add("c");
        ListWrapper wrapper = prepareListWrapper(target);
        wrapper.setProxifier(proxifier);
        Collection<String> list = Arrays.asList("a", "b", "c");
        when(proxifier.unwrap(any(Collection.class))).thenReturn((Collection) list);

        wrapper.retainAll(list);

        assertThat(target).hasSize(3);
        assertThat(target.get(0)).isEqualTo("a");
        assertThat(target.get(1)).isEqualTo("b");
        assertThat(target.get(2)).isEqualTo("c");

        verify(dirtyMap, never()).put(setter, propertyMeta);
    }

    @Test
    public void should_mark_dirty_on_iterator_remove() throws Exception
    {
        ArrayList<Object> target = new ArrayList<Object>();
        target.add("a");
        target.add("b");
        target.add("c");
        ListWrapper wrapper = prepareListWrapper(target);

        Iterator<Object> iteratorWrapper = wrapper.iterator();

        assertThat(iteratorWrapper).isInstanceOf(IteratorWrapper.class);

        iteratorWrapper.next();
        iteratorWrapper.remove();

        verify(dirtyMap).put(setter, propertyMeta);
    }

    @Test
    public void should_return_size() throws Exception
    {
        ArrayList<Object> target = new ArrayList<Object>();
        target.add("a");
        target.add("b");
        target.add("c");
        ListWrapper wrapper = prepareListWrapper(target);
        assertThat(wrapper.size()).isEqualTo(3);
    }

    @Test
    public void should_return_array_for_join() throws Exception
    {
        ArrayList<Object> target = new ArrayList<Object>();
        CompleteBean bean1 = CompleteBeanTestBuilder.builder().randomId().buid();
        CompleteBean bean2 = CompleteBeanTestBuilder.builder().randomId().buid();
        CompleteBean bean3 = CompleteBeanTestBuilder.builder().randomId().buid();

        target.add(bean1);
        target.add(bean2);
        target.add(bean3);
        ListWrapper wrapper = prepareJoinListWrapper(target);

        when(joinPropertyMeta.type()).thenReturn(PropertyType.JOIN_LIST);
        when(joinPropertyMeta.joinMeta()).thenReturn(entityMeta);

        when(context.createContextForJoin(eq(entityMeta), any())).thenReturn(joinContext);

        when(proxifier.buildProxy(bean1, joinContext)).thenReturn(bean1);
        when(proxifier.buildProxy(bean2, joinContext)).thenReturn(bean2);
        when(proxifier.buildProxy(bean3, joinContext)).thenReturn(bean3);

        assertThat(wrapper.toArray()).contains(bean1, bean2, bean3);
    }

    @Test
    public void should_return_array() throws Exception
    {
        ArrayList<Object> target = new ArrayList<Object>();
        target.add("a");
        target.add("b");
        target.add("c");
        ListWrapper wrapper = prepareListWrapper(target);

        when(propertyMeta.type()).thenReturn(PropertyType.LIST);
        assertThat(wrapper.toArray()).contains("a", "b", "c");
    }

    @Test
    public void should_return_array_with_argument_for_join() throws Exception
    {
        ArrayList<Object> target = new ArrayList<Object>();
        CompleteBean bean1 = CompleteBeanTestBuilder.builder().randomId().buid();
        CompleteBean bean2 = CompleteBeanTestBuilder.builder().randomId().buid();
        CompleteBean bean3 = CompleteBeanTestBuilder.builder().randomId().buid();

        target.add(bean1);
        target.add(bean2);
        target.add(bean3);
        ListWrapper wrapper = prepareJoinListWrapper(target);

        when(joinPropertyMeta.type()).thenReturn(PropertyType.JOIN_LIST);
        when(joinPropertyMeta.joinMeta()).thenReturn(entityMeta);

        when(context.createContextForJoin(eq(entityMeta), any())).thenReturn(joinContext);

        when(proxifier.buildProxy(bean1, joinContext)).thenReturn(bean1);
        when(proxifier.buildProxy(bean2, joinContext)).thenReturn(bean2);
        when(proxifier.buildProxy(bean3, joinContext)).thenReturn(bean3);

        assertThat(wrapper.toArray()).contains(bean1, bean2, bean3);

        assertThat(wrapper.toArray(new CompleteBean[]
        {
                bean1,
                bean2
        })).contains(bean1, bean2);
    }

    @Test
    public void should_return_array_with_argument() throws Exception
    {
        ArrayList<Object> target = new ArrayList<Object>();
        target.add("a");
        target.add("b");
        target.add("c");
        ListWrapper wrapper = prepareListWrapper(target);

        when(propertyMeta.type()).thenReturn(PropertyType.LIST);
        assertThat(wrapper.toArray(new String[]
        {
                "a",
                "c"
        })).contains("a", "c");
    }

    @Test
    public void should_return_target() throws Exception
    {
        ArrayList<Object> target = new ArrayList<Object>();
        target.add("a");
        CollectionWrapper wrapper = new CollectionWrapper(target);
        assertThat(wrapper.getTarget()).isSameAs(target);
    }

    private ListWrapper prepareListWrapper(List<Object> target)
    {
        ListWrapper wrapper = new ListWrapper(target);
        wrapper.setDirtyMap(dirtyMap);
        wrapper.setSetter(setter);
        wrapper.setPropertyMeta(propertyMeta);
        wrapper.setProxifier(proxifier);
        wrapper.setContext(context);
        return wrapper;
    }

    private ListWrapper prepareJoinListWrapper(List<Object> target)
    {
        ListWrapper wrapper = new ListWrapper(target);
        wrapper.setDirtyMap(dirtyMap);
        wrapper.setSetter(setter);
        wrapper.setPropertyMeta(joinPropertyMeta);
        wrapper.setProxifier(proxifier);
        wrapper.setContext(context);
        return wrapper;
    }

}
