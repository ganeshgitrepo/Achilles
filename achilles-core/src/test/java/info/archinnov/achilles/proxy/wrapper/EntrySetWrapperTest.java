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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * AchillesEntrySetWrapperTest
 * 
 * @author DuyHai DOAN
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class EntrySetWrapperTest
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

    private EntityMeta entityMeta;

    private CompleteBean entity = CompleteBeanTestBuilder.builder().randomId().buid();

    @Before
    public void setUp() throws Exception
    {
        setter = CompleteBean.class.getDeclaredMethod("setFriends", List.class);

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
    public void should_mark_dirty_on_clear() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");
        map.put(2, "Paris");
        map.put(3, "75014");

        EntrySetWrapper wrapper = prepareWrapper(map);

        wrapper.clear();

        verify(dirtyMap).put(setter, propertyMeta);
    }

    @Test
    public void should_return_true_on_contains() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");
        map.put(2, "Paris");
        map.put(3, "75014");

        EntrySetWrapper wrapper = prepareWrapper(map);
        Entry<Object, Object> entry = map.entrySet().iterator().next();
        when(proxifier.unwrap(any())).thenReturn(entry);

        assertThat(wrapper.contains(entry)).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_return_true_on_containsAll() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");
        map.put(2, "Paris");
        map.put(3, "75014");

        EntrySetWrapper wrapper = prepareWrapper(map);
        Iterator<Entry<Object, Object>> iterator = map.entrySet().iterator();

        Entry<Object, Object> entry1 = iterator.next();
        Entry<Object, Object> entry2 = iterator.next();

        when(proxifier.unwrap(entry1)).thenReturn(entry1);
        when(proxifier.unwrap(entry2)).thenReturn(entry2);

        assertThat(wrapper.containsAll(Arrays.asList(entry1, entry2))).isTrue();
    }

    @Test
    public void should_return_true_on_isEmpty() throws Exception
    {
        EntrySetWrapper wrapper = prepareWrapper(new HashMap<Object, Object>());
        assertThat(wrapper.isEmpty()).isTrue();
    }

    @Test
    public void should_return_iterator() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");
        map.put(2, "Paris");
        map.put(3, "75014");

        EntrySetWrapper wrapper = prepareWrapper(map);

        assertThat(wrapper.iterator()).isNotNull();
    }

    @Test
    public void should_mark_dirty_on_remove() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");
        map.put(2, "Paris");
        map.put(3, "75014");

        EntrySetWrapper wrapper = prepareWrapper(map);
        Entry<Object, Object> entry = map.entrySet().iterator().next();
        when(proxifier.unwrap(any())).thenReturn(entry);
        wrapper.remove(entry);

        verify(dirtyMap).put(setter, propertyMeta);
    }

    @Test
    public void should_not_mark_dirty_on_remove_external_element() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");
        map.put(2, "Paris");
        map.put(3, "75014");

        EntrySetWrapper wrapper = prepareWrapper(map);
        Map.Entry<Object, Object> entry = new AbstractMap.SimpleEntry<Object, Object>(4, "csdf");
        wrapper.remove(entry);

        verify(dirtyMap, never()).put(setter, propertyMeta);
    }

    @Test
    public void should_mark_dirty_on_remove_all() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");
        map.put(2, "Paris");
        map.put(3, "75014");

        EntrySetWrapper wrapper = prepareWrapper(map);

        Iterator<Entry<Object, Object>> iterator = map.entrySet().iterator();

        Entry<Object, Object> entry1 = iterator.next();
        Entry<Object, Object> entry2 = iterator.next();
        List<Entry<Object, Object>> list = new ArrayList<Map.Entry<Object, Object>>();
        list.add(entry1);
        list.add(entry2);

        when(proxifier.unwrap((Collection<Entry<Object, Object>>) list)).thenReturn(list);

        wrapper.removeAll(list);

        verify(dirtyMap).put(setter, propertyMeta);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_not_mark_dirty_on_remove_all_not_matching() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");
        map.put(2, "Paris");
        map.put(3, "75014");

        EntrySetWrapper wrapper = prepareWrapper(map);

        Map.Entry<Object, Object> entry1 = new AbstractMap.SimpleEntry<Object, Object>(4, "csdf");
        Map.Entry<Object, Object> entry2 = new AbstractMap.SimpleEntry<Object, Object>(5, "csdf");

        wrapper.removeAll(Arrays.asList(entry1, entry2));

        verify(dirtyMap, never()).put(setter, propertyMeta);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_retain_all() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");
        map.put(2, "Paris");
        map.put(3, "75014");

        Iterator<Entry<Object, Object>> iterator = map.entrySet().iterator();
        Entry<Object, Object> entry1 = iterator.next();
        Entry<Object, Object> entry2 = iterator.next();
        List<Entry<Object, Object>> list = Arrays.asList(entry1, entry2);

        when(proxifier.unwrap((Collection<Entry<Object, Object>>) list)).thenReturn(list);

        EntrySetWrapper wrapper = prepareWrapper(map);
        wrapper.retainAll(list);

        verify(dirtyMap).put(setter, propertyMeta);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_retain_all_no_dirty_when_all_match() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");

        Entry<Object, Object> entry1 = new AbstractMap.SimpleEntry<Object, Object>(1, "FR");
        List<Entry<Object, Object>> list = Arrays.asList(entry1);
        when(proxifier.unwrap((Collection<Entry<Object, Object>>) list)).thenReturn(list);
        EntrySetWrapper wrapper = prepareWrapper(map);

        wrapper.retainAll(list);

        verify(dirtyMap, never()).put(setter, propertyMeta);
    }

    @Test
    public void should_get_size() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");
        EntrySetWrapper wrapper = prepareWrapper(map);
        assertThat(wrapper.size()).isEqualTo(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_return_array() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");
        EntrySetWrapper wrapper = prepareWrapper(map);
        when(propertyMeta.type()).thenReturn(PropertyType.SET);

        Object[] array = wrapper.toArray();

        assertThat(array).hasSize(1);
        assertThat(array[0]).isInstanceOf(Map.Entry.class);
        assertThat(((Entry<Object, Object>) array[0]).getValue()).isEqualTo("FR");
    }

    @Test
    public void should_return_array_when_join() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");
        EntrySetWrapper wrapper = prepareWrapper(map);
        when(propertyMeta.type()).thenReturn(PropertyType.JOIN_SET);

        Object[] array = wrapper.toArray();

        assertThat(array).hasSize(1);
        assertThat(array[0]).isInstanceOf(MapEntryWrapper.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_return_array_with_argument() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");
        Entry<Object, Object> entry = map.entrySet().iterator().next();

        EntrySetWrapper wrapper = prepareWrapper(map);
        when(propertyMeta.type()).thenReturn(PropertyType.SET);

        Object[] array = wrapper.toArray(new Entry[]
        {
                entry
        });

        assertThat(array).hasSize(1);
        assertThat(((Entry<Object, Object>) array[0]).getValue()).isEqualTo("FR");
    }

    @Test
    public void should_return_array_with_argument_when_join_entity() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, entity);
        Entry<Object, Object> entry = map.entrySet().iterator().next();

        EntrySetWrapper wrapper = prepareJoinWrapper(map);
        when(joinPropertyMeta.type()).thenReturn(PropertyType.JOIN_SET);

        when(joinPropertyMeta.joinMeta()).thenReturn(entityMeta);
        when(proxifier.buildProxy(eq(entity), any(PersistenceContext.class))).thenReturn(
                entity);

        Object[] array = wrapper.toArray(new Entry[]
        {
                entry
        });

        assertThat(array).hasSize(1);
        assertThat(((Entry<Integer, CompleteBean>) array[0]).getValue()).isEqualTo(entity);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void should_exception_when_add() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");
        map.put(2, "Paris");
        map.put(3, "75014");

        EntrySetWrapper wrapper = prepareWrapper(map);

        Map.Entry<Object, Object> entry = new AbstractMap.SimpleEntry<Object, Object>(4, "csdf");

        wrapper.add(entry);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = UnsupportedOperationException.class)
    public void should_exception_when_add_all() throws Exception
    {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(1, "FR");
        map.put(2, "Paris");
        map.put(3, "75014");

        EntrySetWrapper wrapper = prepareWrapper(map);

        Map.Entry<Object, Object> entry1 = new AbstractMap.SimpleEntry<Object, Object>(4, "csdf");
        Map.Entry<Object, Object> entry2 = new AbstractMap.SimpleEntry<Object, Object>(5, "csdf");

        wrapper.addAll(Arrays.asList(entry1, entry2));
    }

    private EntrySetWrapper prepareWrapper(Map<Object, Object> map)
    {
        EntrySetWrapper wrapper = new EntrySetWrapper(map.entrySet());
        wrapper.setContext(context);
        wrapper.setDirtyMap(dirtyMap);
        wrapper.setSetter(setter);
        wrapper.setPropertyMeta(propertyMeta);
        wrapper.setProxifier(proxifier);
        return wrapper;
    }

    private EntrySetWrapper prepareJoinWrapper(Map<Object, Object> map)
    {
        EntrySetWrapper wrapper = new EntrySetWrapper(map.entrySet());
        wrapper.setContext(context);
        wrapper.setDirtyMap(dirtyMap);
        wrapper.setSetter(setter);
        wrapper.setPropertyMeta(joinPropertyMeta);
        wrapper.setProxifier(proxifier);
        return wrapper;
    }
}
