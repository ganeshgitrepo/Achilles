package info.archinnov.achilles.proxy.wrapper;

import info.archinnov.achilles.proxy.wrapper.builder.IteratorWrapperBuilder;
import java.util.Collection;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CollectionWrapper
 * 
 * @author DuyHai DOAN
 * 
 */
public class CollectionWrapper extends AbstractWrapper implements Collection<Object> {
    private static final Logger log = LoggerFactory.getLogger(CollectionWrapper.class);

    protected Collection<Object> target;

    public CollectionWrapper(Collection<Object> target) {
        this.target = target;
    }

    @Override
    public boolean add(Object arg0) {
        log.trace("Mark collection property {} of entity class {} dirty upon element addition",
                propertyMeta.getPropertyName(), propertyMeta.getEntityClassName());
        this.markDirty();
        boolean result = target.add(proxifier.unwrap(arg0));

        return result;
    }

    @Override
    public boolean addAll(Collection<?> arg0) {
        boolean result = false;
        result = target.addAll(proxifier.unwrap(arg0));
        if (result) {
            log.trace("Mark collection property {} of entity class {} dirty upon elements addition",
                    propertyMeta.getPropertyName(), propertyMeta.getEntityClassName());
            this.markDirty();
        }
        return result;
    }

    @Override
    public void clear() {
        if (this.target.size() > 0) {
            log.trace("Mark collection property {} of entity class {} dirty upon clearance",
                    propertyMeta.getPropertyName(), propertyMeta.getEntityClassName());
            this.markDirty();
        }
        this.target.clear();
    }

    @Override
    public boolean contains(Object arg0) {
        return this.target.contains(proxifier.unwrap(arg0));
    }

    @Override
    public boolean containsAll(Collection<?> arg0) {
        return this.target.containsAll(proxifier.unwrap(arg0));
    }

    @Override
    public boolean isEmpty() {
        return this.target.isEmpty();
    }

    @Override
    public Iterator<Object> iterator() {
        log.trace("Build iterator wrapper for collection property {} of entity class {}",
                propertyMeta.getPropertyName(), propertyMeta.getEntityClassName());

        return IteratorWrapperBuilder.builder(context, this.target.iterator()) //
                .dirtyMap(dirtyMap)
                .setter(setter)
                .propertyMeta(propertyMeta)
                .proxifier(proxifier)
                .build();
    }

    @Override
    public boolean remove(Object arg0) {
        boolean result = false;
        result = this.target.remove(proxifier.unwrap(arg0));
        if (result) {
            log.trace("Mark collection property {} of entity class {} dirty upon element removal",
                    propertyMeta.getPropertyName(), propertyMeta.getEntityClassName());
            this.markDirty();
        }
        return result;
    }

    @Override
    public boolean removeAll(Collection<?> arg0) {
        boolean result = false;
        result = this.target.removeAll(proxifier.unwrap(arg0));
        if (result) {
            log.trace("Mark collection property {} of entity class {} dirty upon elements removal",
                    propertyMeta.getPropertyName(), propertyMeta.getEntityClassName());
            this.markDirty();
        }
        return result;
    }

    @Override
    public boolean retainAll(Collection<?> arg0) {
        boolean result = false;
        result = this.target.retainAll(proxifier.unwrap(arg0));
        if (result) {
            log.trace("Mark collection property {} of entity class {} dirty upon elements retentions",
                    propertyMeta.getPropertyName(), propertyMeta.getEntityClassName());
            this.markDirty();
        }
        return result;
    }

    @Override
    public int size() {
        return this.target.size();
    }

    @Override
    public Object[] toArray() {
        Object[] result = null;
        if (isJoin()) {
            log
                    .trace("Build proxies for join entities of collection property {} of entity class {} upon toArray() call",
                            propertyMeta.getPropertyName(), propertyMeta.getEntityClassName());
            Object[] array = new Object[this.target.size()];
            int i = 0;
            for (Object joinEntity : this.target) {
                array[i] = proxifier.buildProxy(joinEntity, joinContext(joinEntity));
                i++;
            }
            result = array;
        } else {
            result = this.target.toArray();
        }
        return result;
    }

    @Override
    public <T> T[] toArray(T[] arg0) {
        T[] result = null;
        if (isJoin()) {
            log
                    .trace("Build proxies for join entities of collection property {} of entity class {} upon toArray(T[] arg) call",
                            propertyMeta.getPropertyName(), propertyMeta.getEntityClassName());
            T[] array = this.target.toArray(arg0);

            for (int i = 0; i < array.length; i++) {
                array[i] = proxifier.buildProxy(array[i], joinContext(array[i]));
            }
            result = array;
        } else {
            result = this.target.toArray(arg0);
        }
        return result;
    }

    public Collection<Object> getTarget() {
        return this.target;
    }
}
