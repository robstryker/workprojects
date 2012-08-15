package net.oxbeef.jbt.plugins.dependencies.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.oxbeef.jbt.plugins.dependencies.Activator;
import net.oxbeef.jbt.plugins.dependencies.Activator.NodeModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;

public class GraphUtil {
	public static Color veryLight = null;

	private static void initColors(Display display) {
		if( veryLight == null )
			veryLight = new Color(display, 220, 220, 200);
	}
	
	private static String ensmallenPluginName(String name) {
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
	
	// creates a map of one component's plugins and it's lower level plugin deps
	public static void createComponentsToOneComplonentPluginGraph(Graph graph, String lowComponent, boolean showExternals) {
		NodeModel nm = Activator.getDefault().getModel();
		HashMap<String, ArrayList<String>> customMap = new HashMap<String, ArrayList<String>>();
		Iterator<String> it = nm.pluginToPlugin.keySet().iterator();
		// Iterate through ALL plugin mappings
		while(it.hasNext()) {
			String source = it.next();
			String sourceComponent = nm.pluginToComponent.get(source);
			if( sourceComponent.equals(lowComponent)) {
				// just add an empty array to make the nodes later
				customMap.put(ensmallenPluginName(source), new ArrayList<String>());
			} else {
				// IF the source is not the targeted component (common)
				ArrayList<String> alldeps = nm.pluginToPlugin.get(source);
				Iterator<String> alldepsIterator = alldeps.iterator();
				while(alldepsIterator.hasNext()) {
					String oneDepPlugin = alldepsIterator.next();
					String depPluginTrimmed = ensmallenPluginName(oneDepPlugin);
					// if the dependency *is* from our targeted component and the higher plugin is not
					if( nm.pluginToComponent.get(oneDepPlugin).equals(lowComponent)) {
						ArrayList<String> l = customMap.get(sourceComponent);
						if( l == null ) {
							l = new ArrayList<String>();
							customMap.put(sourceComponent, l);
						}
						if( !l.contains(depPluginTrimmed))
							l.add(depPluginTrimmed);
					}
				}
			}
		}
		createFullGraph(graph, customMap, false);
	}
	
	// creates a map of one component's plugins and it's lower level plugin deps
	public static void createOneComponentDepGraph(Graph graph, String component, boolean showExternals) {
		NodeModel nm = Activator.getDefault().getModel();
		HashMap<String, ArrayList<String>> customMap = new HashMap<String, ArrayList<String>>();
		Iterator<String> it = nm.pluginToPlugin.keySet().iterator();
		while(it.hasNext()) {
			String plugin = it.next();
			if( nm.pluginToComponent.get(plugin).equals(component)) {
				ArrayList<String> customdeps = customMap.get(plugin);
				if( customdeps == null ) {
					customdeps = new ArrayList<String>();
					customMap.put(plugin, customdeps);
				}
				Iterator<String> jit = nm.pluginToPlugin.get(plugin).iterator();
				while(jit.hasNext()) {
					String onedep = jit.next();
					String onedepComponent = nm.pluginToComponent.get(onedep);
					String nodeName = onedepComponent;
					if( onedepComponent.equals(component)) {
						nodeName = onedep; // if it's in the same component, make the node to the plugin instead
					}
					if( showExternals || nodeName.startsWith("org.jboss")) {
						if( !customdeps.contains(nodeName))
							customdeps.add(nodeName);
						if( !customMap.containsKey(nodeName) ) {
							customMap.put(nodeName, new ArrayList<String>());
						}
					}
				}
			}
		}
		
		createFullGraph(graph, customMap, false);
	}
	
	public static void createFullGraph(Graph graph) {
		NodeModel nm = Activator.getDefault().getModel();
		HashMap<String, ArrayList<String>> compToComp = nm.componentToComponents;
		createFullGraph(graph, compToComp, true);
	}
	
	private static void createFullGraph(Graph graph, HashMap<String, ArrayList<String>> map, boolean showRedundant) {
		initColors(graph.getDisplay());
		
		HashMap<String, GraphNode> graphNodeMap = new HashMap<String, GraphNode>();
		Iterator<String> it = map.keySet().iterator();
		while(it.hasNext()) {
			String compName = it.next();
			GraphNode node1 = new GraphNode(graph, SWT.NONE, compName);
			graphNodeMap.put(compName, node1);
		}
		
		it = map.keySet().iterator();
		while(it.hasNext()) {
			String compName = it.next();
			GraphNode sourceNode = graphNodeMap.get(compName);
			ArrayList<String> deps = map.get(compName);
			for( int i = 0; i < deps.size(); i++ ) {
				String depComponentName = deps.get(i);
				GraphNode depNode = graphNodeMap.get(depComponentName); 
				if( sourceNode == null || depNode == null ) {
					System.out.println("BREAK");
				} else {
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
	
	private static boolean isRedundant(HashMap<String, ArrayList<String>> map, String source, String dep, boolean ignoreDep) {
		ArrayList<String> allDeps = map.get(source);
		if( allDeps == null ) {
			return false;
		}
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
}
