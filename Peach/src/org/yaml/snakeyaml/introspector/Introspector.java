package org.yaml.snakeyaml.introspector;

import java.util.ArrayList;

/**
 * @author Winfxk
 */
public class Introspector {

	@SuppressWarnings("unused")
	public static ss getBeanInfo(Class<?> type) {
		return new ss();
	}

	public static class ss {
		public ArrayList<PropertyDescriptor> getPropertyDescriptors() {
			return new ArrayList<>();
		}
	}
}
