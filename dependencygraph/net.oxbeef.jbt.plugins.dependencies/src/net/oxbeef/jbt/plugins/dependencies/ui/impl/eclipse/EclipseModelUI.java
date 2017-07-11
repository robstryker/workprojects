package net.oxbeef.jbt.plugins.dependencies.ui.impl.eclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.zest.core.widgets.Graph;

import net.oxbeef.jbt.plugins.dependencies.core.IDependencyModel;
import net.oxbeef.jbt.plugins.dependencies.core.IModule;
import net.oxbeef.jbt.plugins.dependencies.core.IModule.IModuleType;
import net.oxbeef.jbt.plugins.dependencies.core.ModelProviders;
import net.oxbeef.jbt.plugins.dependencies.ui.IModelUI;
import net.oxbeef.jbt.plugins.dependencies.ui.views.GraphContentUtil;

public class EclipseModelUI implements IModelUI {

	public static final String COMPONENT_TO_COMPONENT_ALL = "Component to component (all)";
	public static final String COMPONENT_TO_COMPONENT_IGNORE_PHANTOMS = "Component to component (ignore phantoms)";
	public static final String COMPONENT_TO_COMPONENT_IGNORE_TESTS = "Component to component (ignore tests)";
	public static final String COMPONENT_TO_COMPONENT_IGNORE_TESTS_PHANTOMS = "Component to component (ignore phantoms and tests)";
	
	@Override
	public String[] getGraphTypes() {
		return new String[] {
				COMPONENT_TO_COMPONENT_ALL,
				COMPONENT_TO_COMPONENT_IGNORE_PHANTOMS,
				COMPONENT_TO_COMPONENT_IGNORE_TESTS,
				COMPONENT_TO_COMPONENT_IGNORE_TESTS_PHANTOMS
		};
	}

	@Override
	public void fillGraph(Graph graph, String dir, String type, String component, IProgressMonitor mon) {
		
		IDependencyModel model = ModelProviders.getLoader("eclipse")
				.loadModel(new File(dir), new HashMap<String,String>(), mon);
		
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		IModule[] all = model.getModules();
		for( int j = 0; j < all.length; j++ ) {
			IModule oneMod = all[j];
			if( oneMod.getModuleType() == IModuleType.TEST_PLUGIN )
				continue;
			if( oneMod.isPhantom() ) 
				continue;
			IModule[] deps = model.getUpstreamModules(oneMod);
			ArrayList<String> collector = new ArrayList<String>();
			for( int k = 0; k < deps.length; k++ ) {
				IModule oneDep = deps[k];
				if( oneDep.isPhantom()) {
					continue;
				}
				collector.add(oneDep.getName());
			}
			if( !oneMod.isPhantom())
				map.put(oneMod.getName(), collector);
		}
		
		
		// We now have a mostly complete graph, but we should still check it for cycles. 
		
		GraphContentUtil.createFullGraph(graph, map, false, false);
		
	}
	///home/rob/code/eclipse/wtp_clean_2_test
	public void fillGraphComponentToComponent(Graph graph, String dir, String type, String component, IProgressMonitor mon) {
		IDependencyModel model = ModelProviders.getLoader("eclipse")
				.loadModel(new File(dir), new HashMap<String,String>(), mon);
		
		boolean ignoreTests = false;
		if( COMPONENT_TO_COMPONENT_IGNORE_TESTS.equals(type) || COMPONENT_TO_COMPONENT_IGNORE_TESTS_PHANTOMS.equals(type))
			ignoreTests = true;

		boolean ignorePhantoms = false;
		if( COMPONENT_TO_COMPONENT_IGNORE_PHANTOMS.equals(type) || COMPONENT_TO_COMPONENT_IGNORE_TESTS_PHANTOMS.equals(type))
			ignorePhantoms = true;

		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();

		String[] components = model.getComponents();
		
		for( int i = 0; i < components.length; i++ ) {
			HashMap<String, HashMap<String, ArrayList<String>>> store = new HashMap<String, HashMap<String, ArrayList<String>>>();
			
			
			Set<String> thingsWeDependOn = new HashSet<String>();
			IModule[] componentModules = model.getModules(components[i]);
			for( int j = 0; j < componentModules.length; j++ ) {
				IModule oneMod = componentModules[j];
				if( oneMod.getModuleType() == IModuleType.TEST_PLUGIN && ignoreTests)
					continue;
				if( oneMod.isPhantom() && ignorePhantoms) 
					continue;
				
				
				IModule[] deps = model.getUpstreamModules(oneMod);
				for( int k = 0; k < deps.length; k++ ) {
					IModule oneDep = deps[k];
					if( oneDep.isPhantom() && ignorePhantoms)
						continue;
					String oneDepsComponent = oneDep.getComponent();
					if( !oneDepsComponent.equals(components[i])) {
						//System.out.println(components[i] + " depends on " + oneDepsComponent + " via dependency on " + oneDep.getName() + " by " + oneMod.getName());
						
						// Let's keep track of this info
						HashMap<String, ArrayList<String>> depedComponentMap = store.get(oneDepsComponent);
						if( depedComponentMap == null ) {
							depedComponentMap = new HashMap<String, ArrayList<String>>();
							store.put(oneDepsComponent, depedComponentMap);
						}
						ArrayList<String> plugins = depedComponentMap.get(oneDep.getName());
						if( plugins == null ) {
							plugins = new ArrayList<String>();
							depedComponentMap.put(oneDep.getName(), plugins);
						}
						if( !plugins.contains(oneMod.getName())) {
							plugins.add(oneMod.getName());
						}
						thingsWeDependOn.add(oneDepsComponent);
					}
				}
			}
			// Let's print out the map
			StringBuffer sb = new StringBuffer();
			sb.append(components[i] + " depends on:");
			Iterator<String> printIt = store.keySet().iterator();
			while(printIt.hasNext()) {
				String k1 = printIt.next();
				HashMap<String, ArrayList<String>> t1 = store.get(k1);
				sb.append("\n    " + k1 + " via dependency on ");
				Iterator<String> printIt2 = t1.keySet().iterator();
				while(printIt2.hasNext()) {
					String k2 = printIt2.next();
					sb.append("\n        " + k2 + " by plugins: " );
					ArrayList<String> list = t1.get(k2);
					sb.append(String.join(", ", list));
				}
			}
			System.out.println(sb.toString());

//			if( thingsWeDependOn.size() > 0 )
				map.put(components[i], new ArrayList<String>(thingsWeDependOn));
		}
		
		
		// We now have a mostly complete graph, but we should still check it for cycles. 
		
		GraphContentUtil.createFullGraph(graph, map, false, false);
	}

}
