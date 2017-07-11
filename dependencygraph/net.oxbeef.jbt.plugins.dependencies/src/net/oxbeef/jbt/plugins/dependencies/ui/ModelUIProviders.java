package net.oxbeef.jbt.plugins.dependencies.ui;

import java.util.HashMap;
import java.util.Set;

public class ModelUIProviders {
	public static HashMap<String, IModelUI> map = new HashMap<String, IModelUI>();
	public static IModelUI getUI(String strategy) {
		return map.get(strategy);
	}
	public static void register(String strategy, IModelUI ui) {
		map.put(strategy, ui);
	}
	public static String[] getUIList() {
		Set<String> keys = map.keySet();
		return (String[]) keys.toArray(new String[keys.size()]);
	}
}
