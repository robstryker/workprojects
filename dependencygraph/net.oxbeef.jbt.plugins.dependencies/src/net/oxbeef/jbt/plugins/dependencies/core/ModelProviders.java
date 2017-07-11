package net.oxbeef.jbt.plugins.dependencies.core;

import java.util.HashMap;
import java.util.Set;

public class ModelProviders {
	
	private static HashMap<String, IModelLoader> map = new HashMap<String, IModelLoader>();
	
	public static IModelLoader getLoader(String id) {
		return map.get(id);
	}
	
	public static void registerLoader(String id, IModelLoader loader) {
		map.put(id,  loader);
	}
	
	public static String[] getLoaderIds() {
		Set<String> ret = map.keySet();
		return (String[]) ret.toArray(new String[ret.size()]);
	}
}
