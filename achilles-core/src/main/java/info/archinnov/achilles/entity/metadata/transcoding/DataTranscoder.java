package info.archinnov.achilles.entity.metadata.transcoding;

import info.archinnov.achilles.entity.metadata.PropertyMeta;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Transcoder
 * 
 * @author DuyHai DOAN
 * 
 */
public interface DataTranscoder {

    // Encode
    public Object encode(PropertyMeta<?, ?> pm, Object entityValue);

    public List<?> encode(PropertyMeta<?, ?> pm, List<?> entityValue);

    public Set<?> encode(PropertyMeta<?, ?> pm, Set<?> entityValue);

    public Map<?, ?> encode(PropertyMeta<?, ?> pm, Map<?, ?> entityValue);

    public List<?> encodeToComponents(PropertyMeta<?, ?> pm, Object compoundKey);

    public String forceEncodeToJSON(Object object);

    //Decode
    public Object decode(PropertyMeta<?, ?> pm, Object cassandraValue);

    public List<?> decode(PropertyMeta<?, ?> pm, List<?> cassandraValue);

    public Set<?> decode(PropertyMeta<?, ?> pm, Set<?> cassandraValue);

    public Map<?, ?> decode(PropertyMeta<?, ?> pm, Map<?, ?> cassandraValue);

    public Object decodeFromComponents(PropertyMeta<?, ?> pm, List<?> components);

    public Object forceDecodeFromJSON(String cassandraValue, Class<?> targetType);
}
