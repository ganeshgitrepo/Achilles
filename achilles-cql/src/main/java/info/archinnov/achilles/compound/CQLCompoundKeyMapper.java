package info.archinnov.achilles.compound;

import info.archinnov.achilles.entity.metadata.PropertyMeta;
import info.archinnov.achilles.exception.AchillesException;
import info.archinnov.achilles.proxy.CQLRowMethodInvoker;
import info.archinnov.achilles.proxy.ReflectionInvoker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.Row;

public class CQLCompoundKeyMapper
{

	private CQLRowMethodInvoker cqlRowInvoker = new CQLRowMethodInvoker();
	private ReflectionInvoker invoker = new ReflectionInvoker();

	public Object createFromRow(Row row, PropertyMeta<?, ?> pm)
	{
		boolean bySetter = pm.hasDefaultConstructorForCompoundKey();

		Constructor<Object> constructor = pm.getCompoundKeyConstructor();
		List<String> componentNames = pm.getCQLComponentNames();
		List<Class<?>> componentClasses = pm.getComponentClasses();
		List<Object> values = new ArrayList<Object>();
		List<Method> componentSetters = pm.getComponentSetters();

		Object compoundKey = null;
		if (bySetter)
			compoundKey = invoker.instanciate(constructor);

		for (int i = 0; i < componentNames.size(); i++)
		{
			String componentName = componentNames.get(i);
			Class<?> clazz = componentClasses.get(i);
			if (row.isNull(componentName))
			{
				throw new AchillesException("Error, the component '" + componentName
						+ "' from @CompoundKey class '"
						+ pm.getValueClass() + "' cannot be found from Cassandra");
			}
			else if (clazz.isEnum())
			{
				mapEnumValue(row, compoundKey, values, bySetter, componentSetters, i, componentName,
						clazz);
			}
			else
			{
				mapValue(row, compoundKey, values, bySetter, componentSetters, i, componentName, clazz);
			}
		}

		if (!bySetter)
			compoundKey = invoker.instanciate(constructor,
					values.toArray(new Object[values.size()]));

		return compoundKey;
	}

	public List<Object> extractComponents(Object primaryKey, PropertyMeta<?, ?> idMeta)
	{
		List<Object> values = new ArrayList<Object>();
		List<Method> componentGetters = idMeta.getComponentGetters();
		List<Class<?>> componentClasses = idMeta.getComponentClasses();

		for (int i = 0; i < componentGetters.size(); i++)
		{
			Method componentGetter = componentGetters.get(i);
			Class<?> clazz = componentClasses.get(i);

			Object valueFromField = invoker.getValueFromField(primaryKey, componentGetter);
			if (clazz.isEnum())
			{
				valueFromField = ((Enum) valueFromField).name();
			}
			values.add(valueFromField);
		}

		return values;
	}

	private void mapValue(Row row, Object compoundKey, List<Object> values, boolean bySetter,
			List<Method> componentSetters, int i, String component, Class<?> clazz)
	{
		Object value = cqlRowInvoker.invokeOnRowForProperty(row, component, clazz);
		if (bySetter)
			invoker.setValueToField(compoundKey, componentSetters.get(i), value);
		else
			values.add(value);
	}

	private void mapEnumValue(Row row, Object compoundKey, List<Object> values, boolean bySetter,
			List<Method> componentSetters, int i, String component, Class<?> clazz)
	{
		String enumAsName = (String) cqlRowInvoker.invokeOnRowForProperty(row, component,
				String.class);
		Enum<?> enumInstance = Enum.valueOf((Class<Enum>) clazz, enumAsName);
		if (bySetter)
			invoker.setValueToField(compoundKey, componentSetters.get(i), enumInstance);
		else
			values.add(enumInstance);
	}
}
