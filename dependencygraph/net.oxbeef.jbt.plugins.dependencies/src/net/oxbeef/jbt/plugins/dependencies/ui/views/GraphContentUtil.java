package net.oxbeef.jbt.plugins.dependencies.ui.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;

public class GraphContentUtil {
	public static Color veryLight = null;

	private static void initColors(Display display) {
		if( veryLight == null )
			veryLight = new Color(display, 220, 220, 200);
	}
	
	public static String ensmallenPluginName(String name) {
		String[] parts = name.split("\\.");
		StringBuffer ret = new StringBuffer();
		for( int i = 0; i < parts.length-2; i++ ) {
			ret.append(parts[i].charAt(0));
			ret.append(".");
		}
		ret.append(parts[parts.length-2]);
		ret.append(".");
		ret.append(parts[parts.length-1]);
		return ret.toString();
	}
	
	
	public static void cleanCycles(HashMap<String, ArrayList<String>> map) {
		Set<String> keys = new HashSet<String>(map.keySet());
		Iterator<String> keyIt = keys.iterator();
		while(keyIt.hasNext()) {
			String k = keyIt.next();
			cleanCycles(map, k, new ArrayList<String>());
		}
	}
	public static void cleanCycles(HashMap<String, ArrayList<String>> map, String current, ArrayList<String> seen) {
		ArrayList<String> original = map.get(current);
		ArrayList<String> deps = null;
		if( original == null )
			deps = new ArrayList<String>();
		else
			deps = new ArrayList<String>(original);
		
		
		Iterator<String> depsIt = deps.iterator();
		if( seen.contains(current)) {
			String last = seen.get(seen.size()-1);
			ArrayList<String> lastSeensDepList= map.get(last);
			lastSeensDepList.remove(current);
			String cycleKey = current + "_cycle";
			if( !lastSeensDepList.contains(cycleKey))
				lastSeensDepList.add(cycleKey);
			map.put(cycleKey, new ArrayList<String>());
		} else {
			ArrayList<String> tmp = new ArrayList<String>(seen);
			tmp.add(current);
			while(depsIt.hasNext()) {
				String oneDep = depsIt.next();
				cleanCycles(map, oneDep, tmp);
			}
		}
	}
	
	
	public static void createFullGraph(Graph graph, HashMap<String, ArrayList<String>> map, 
			boolean showRedundant, boolean showExternals) {
		
		cleanCycles(map);
		
		initColors(graph.getDisplay());
		
		HashMap<String, GraphNode> graphNodeMap;
		graphNodeMap = new HashMap<String, GraphNode>();
		Iterator<String> it = map.keySet().iterator();
		while(it.hasNext()) {
			String compName = it.next();
			//if( showExternals || compName.indexOf(".") == -1) {
				GraphNode node1 = new GraphNode(graph, SWT.NONE, compName);
				graphNodeMap.put(compName, node1);
			//}
		}
		
		it = map.keySet().iterator();
		while(it.hasNext()) {
			String compName = it.next();
			GraphNode sourceNode = graphNodeMap.get(compName);
			ArrayList<String> deps = map.get(compName);
			for( int i = 0; i < deps.size(); i++ ) {
				String depComponentName = deps.get(i);
				GraphNode depNode = graphNodeMap.get(depComponentName); 
				if( sourceNode != null && depNode != null ) {
					boolean redundant = isRedundant(map, compName, depComponentName, true); 
					if( !redundant || showRedundant ) {
						GraphConnection c = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, sourceNode, depNode);
						if(redundant) {
							c.setLineColor(veryLight);
							c.setLineWidth(1);
						} else {
							c.setLineWidth(showRedundant ? 4 : 2);
						}
					}
				}
			}
		}
	}
	
	public static boolean isRedundant(HashMap<String, ArrayList<String>> map, String source, String dep, boolean ignoreDep) {
		ArrayList<String> allDeps = map.get(source);
		if( allDeps == null ) {
			return false;
		}
		// Lines from a node to itself are redundant
		if( source.equals(dep))
			return true;
		Iterator<String> i = allDeps.iterator();
		if( !ignoreDep && allDeps.contains(dep))
			return true;
		
		while(i.hasNext()) {
			String s = i.next();
			if( !s.equals(dep)) {
				if( isRedundant(map, s, dep, false))
					return true;
			}
		}
		return false;
	}
	
	
	
	
	
	
	
	
	
	
	
	
//	
//
//	// creates a map of one component's plugins and it's lower level plugin deps
//	public void createComponentsToOneComplonentPluginGraph(Graph graph, String lowComponent, boolean showExternals) {
//		DependencyModel nm = Activator.getDefault().getModel();
//		HashMap<String, ArrayList<String>> customMap = new HashMap<String, ArrayList<String>>();
//		Iterator<String> it = nm.pluginToPlugin.keySet().iterator();
//		// Iterate through ALL plugin mappings
//		while(it.hasNext()) {
//			String source = it.next();
//			String sourceComponent = nm.pluginToComponent.get(source);
//			if( sourceComponent.equals(lowComponent)) {
//				// just add an empty array to make the nodes later
//				customMap.put(ensmallenPluginName(source), new ArrayList<String>());
//			} else {
//				// IF the source is not the targeted component (common)
//				ArrayList<String> alldeps = nm.pluginToPlugin.get(source);
//				Iterator<String> alldepsIterator = alldeps.iterator();
//				while(alldepsIterator.hasNext()) {
//					String oneDepPlugin = alldepsIterator.next();
//					String depPluginTrimmed = ensmallenPluginName(oneDepPlugin);
//					// if the dependency *is* from our targeted component and the higher plugin is not
//					if( nm.pluginToComponent.get(oneDepPlugin).equals(lowComponent)) {
//						ArrayList<String> l = customMap.get(sourceComponent);
//						if( l == null ) {
//							l = new ArrayList<String>();
//							customMap.put(sourceComponent, l);
//						}
//						if( !l.contains(depPluginTrimmed))
//							l.add(depPluginTrimmed);
//					}
//				}
//			}
//		}
//		createFullGraph(graph, customMap, false, true);
//	}
//	
//	// creates a map of one component's plugins and it's lower level plugin deps
//	public void createOneComponentDepGraph(Graph graph, String component, boolean showExternals) {
//		DependencyModel nm = Activator.getDefault().getModel();
//		HashMap<String, ArrayList<String>> customMap = new HashMap<String, ArrayList<String>>();
//		Iterator<String> it = nm.pluginToPlugin.keySet().iterator();
//		while(it.hasNext()) {
//			String plugin = it.next();
//			if( nm.pluginToComponent.get(plugin).equals(component)) {
//				ArrayList<String> customdeps = customMap.get(plugin);
//				if( customdeps == null ) {
//					customdeps = new ArrayList<String>();
//					customMap.put(plugin, customdeps);
//				}
//				Iterator<String> jit = nm.pluginToPlugin.get(plugin).iterator();
//				while(jit.hasNext()) {
//					String onedep = jit.next();
//					String onedepComponent = nm.pluginToComponent.get(onedep);
//					String nodeName = onedepComponent;
//					if( onedepComponent.equals(component)) {
//						nodeName = onedep; // if it's in the same component, make the node to the plugin instead
//					}
//					if( showExternals || nodeName.startsWith("org.jboss")) {
//						if( !customdeps.contains(nodeName))
//							customdeps.add(nodeName);
//						if( !customMap.containsKey(nodeName) ) {
//							customMap.put(nodeName, new ArrayList<String>());
//						}
//					}
//				}
//			}
//		}
//		
//		createFullGraph(graph, customMap, false, true);
//	}
//	
//	public void createFullGraph(Graph graph, boolean showRedundant, boolean includeExternals) {
//		DependencyModel nm = Activator.getDefault().getModel();
//		HashMap<String, ArrayList<String>> compToComp = nm.componentToComponents;
//		createFullGraph(graph, compToComp, showRedundant, includeExternals);
//	}
//
//	
//	public void createPluginToPluginGraph(Graph graph, boolean showRedundant, boolean includeExternals) {
//		DependencyModel nm = Activator.getDefault().getModel();
//		HashMap<String, ArrayList<String>> pluginToPlugin = nm.pluginToPlugin;
//		createFullGraph(graph, pluginToPlugin, showRedundant, includeExternals);
//	}

	
}
