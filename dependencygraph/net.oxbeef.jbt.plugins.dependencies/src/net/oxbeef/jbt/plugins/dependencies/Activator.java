package net.oxbeef.jbt.plugins.dependencies;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.oxbeef.jbt.deptree"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	private NodeModel model = null;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		model = new NodeModel();
	}

	public NodeModel getModel() {
		return model;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public static File getRepositoryRoot() {
		return new File("/home/rob/code/jbtools/jbosstools/trunk/");
	}
	
	public static class NodeModel {
		public HashMap<String, ArrayList<String>> componentToComponents; // maps components to the components they depend on
		public HashMap<String, String> pluginToComponent; // maps one plugin to it's owner component
		public HashMap<String, ArrayList<String>> pluginToPlugin; // maps plugins to the plugins they depend on
		
		public NodeModel() {
			componentToComponents = new HashMap<String, ArrayList<String>>();
			pluginToPlugin = new HashMap<String, ArrayList<String>>();
			pluginToComponent = new HashMap<String, String>();
			
			// Load all refs
			File root = getRepositoryRoot();
			
			File[] components = root.listFiles();
			for( int i = 0; i < components.length; i++ ) {
				if( components[i].isDirectory()) {
					addComponent(components[i]);
				}
			}
			
			// Done loading. Now summarize
			Iterator<String> it =  pluginToPlugin.keySet().iterator();
			while(it.hasNext()) {
				String p1 = it.next();
				String p1Component = pluginToComponent.get(p1);
				ArrayList<String> list = componentToComponents.get(p1Component);
				if( list == null ) {
					list = new ArrayList<String>();
					componentToComponents.put(p1Component, list);
				}
				ArrayList<String> deps = pluginToPlugin.get(p1);
				for( int i = 0; i < deps.size(); i++ ) {
					String oneDep = deps.get(i);
					String oneDepComponent = pluginToComponent.get(oneDep);
					if( oneDepComponent == null ) {
						oneDepComponent = trimmedEclipseDepName(oneDep);
						pluginToComponent.put(oneDep, oneDepComponent);
						componentToComponents.put(oneDepComponent, new ArrayList<String>());
					}
					if( oneDepComponent != null ) {
						if( !list.contains(oneDepComponent) && !oneDepComponent.equals(p1Component))
							list.add(oneDepComponent);
					}
				}
			}
		}
		private String trimmedEclipseDepName(String oneDep) {
			String[] segments = oneDep.split("\\.");
			if( segments.length < 3 )
				return oneDep;
			return segments[0] + "." + segments[1] + "." + segments[2];
		}
		
		private void addComponent(File f) {
			String compName = f.getName();
			File pluginsRoot = new File(f, "plugins");
			File[] plugins = pluginsRoot.listFiles();
			if( plugins == null ) {
				return;
			}
			for( int i = 0; i < plugins.length; i++ ) {
				addPlugin(compName, plugins[i]);
			}
		}
		private void addPlugin(String compName, File plugin) {
			File manifest = new File(plugin, "META-INF/MANIFEST.MF");
			if( !manifest.exists())
				return;
			
			try {
				StupidManifest sm = new StupidManifest(compName, manifest);
				if( sm.isValid()) {
					pluginToComponent.put(sm.getPluginName(), compName);
					pluginToPlugin.put(sm.getPluginName(), sm.getDependencies());
				}
			} catch(IOException ioe) {
				// DO NOTHING
			}
		}
		
	}
	
	public static class StupidManifest {
		private boolean isValid = true;
		private String pluginName = null;
		private ArrayList<String> dependencies;
		public StupidManifest(String componentName, File f) throws IOException {
			Manifest mf = new Manifest(new FileInputStream(f));
			Map<String,Attributes> map = mf.getEntries();
			Attributes main = mf.getMainAttributes();
			String a = main.getValue("Bundle-SymbolicName");
			pluginName = a == null ? null : !a.contains(";") ? a.trim() : a.substring(0, a.indexOf(";")).trim();
			String req = main.getValue("Require-Bundle");
			if( req == null ) {
				dependencies = new ArrayList<String>();
				System.out.println("DIE HERE");
				return;
			}
			String req2 = req.replaceAll("\\\"[^\\\"]*\\\"", "\"\"");
			String req3 = req2.replaceAll(";[^,]*,", ",");
			String[] asDeps = req3.split(",");
			if( asDeps[asDeps.length-1].contains(";")) {
				asDeps[asDeps.length-1] = asDeps[asDeps.length-1].substring(0, asDeps[asDeps.length-1].indexOf(";"));
			}
			ArrayList<String> depsList = new ArrayList<String>();
			depsList.addAll(Arrays.asList(asDeps));
			dependencies = depsList;
			System.out.println(main);
			System.out.println(a);
		}
		public String getPluginName() {
			return pluginName;
		}
		public ArrayList<String> getDependencies() {
			return dependencies;
		}
		public boolean isValid() {
			return isValid;
		}
	}
}
