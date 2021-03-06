package info.archinnov.achilles.entity.operations;

import info.archinnov.achilles.proxy.CQLRowMethodInvoker;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;

/**
 * CQLNativeQueryExecutor
 * 
 * @author DuyHai DOAN
 * 
 */
public class CQLNativeQueryMapper {

    private CQLRowMethodInvoker cqlRowInvoker = new CQLRowMethodInvoker();

    public List<Map<String, Object>> mapRows(List<Row> rows)
    {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (!rows.isEmpty())
        {
            for (Row row : rows)
            {
                mapRow(result, row);
            }
        }
        return result;
    }

    private void mapRow(List<Map<String, Object>> result, Row row) {
        ColumnDefinitions columnDefinitions = row.getColumnDefinitions();
        if (columnDefinitions != null)
        {
            Map<String, Object> line = new LinkedHashMap<String, Object>();
            for (Definition column : columnDefinitions)
            {
                mapColumn(row, line, column);
            }
            result.add(line);
        }
    }

    private void mapColumn(Row row, Map<String, Object> line, Definition column) {
        DataType type = column.getType();
        Class<?> javaClass = type.asJavaClass();
        String name = column.getName();
        Object value;
        if (type.isCollection())
        {
            List<DataType> typeArguments = type.getTypeArguments();
            if (List.class.isAssignableFrom(javaClass))
            {
                value = row.getList(name, typeArguments.get(0).asJavaClass());
            }
            else if (Set.class.isAssignableFrom(javaClass))
            {
                value = row.getSet(name, typeArguments.get(0).asJavaClass());
            }
            else
            {
                value = row.getMap(name, typeArguments.get(0).asJavaClass(), typeArguments.get(1)
                        .asJavaClass());
            }
        }
        else
        {
            value = cqlRowInvoker.invokeOnRowForType(row, javaClass, name);
        }

        line.put(name, value);
    }
}
