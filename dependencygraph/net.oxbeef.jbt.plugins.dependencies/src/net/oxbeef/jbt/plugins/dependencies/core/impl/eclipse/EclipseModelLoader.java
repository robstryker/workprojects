package net.oxbeef.jbt.plugins.dependencies.core.impl.eclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import net.oxbeef.jbt.plugins.dependencies.core.IDependencyModel;
import net.oxbeef.jbt.plugins.dependencies.core.IModelLoader;
import net.oxbeef.jbt.plugins.dependencies.core.IModule;
import net.oxbeef.jbt.plugins.dependencies.core.IModule.IModuleType;

public class EclipseModelLoader implements IModelLoader {

	public static void main(String[] args) {
		File f = new File("/home/rob/code/eclipse/wtp_clean");
		EclipseModel em = (EclipseModel)new EclipseModelLoader().loadModel(f,null, null);
		String[] components = em.getAllComponents();
		List<String> sorted = Arrays.asList(components);
		Collections.sort(sorted);
		for( int i = 0; i < components.length; i++ ) {
			System.out.println(components[i]);
		}
		
	}
	
	
	public static EclipseModelLoader instance = new EclipseModelLoader();
	
	@Override
	public IDependencyModel loadModel(File root, Map<String, String> prefs, IProgressMonitor monitor) {
		ArrayList<IModule> modules = new ArrayList<IModule>(); 
		
		// First traverse the source trees to locate all known components
		traverseRepo(modules, root, 4);
		EclipseModel model = new EclipseModel(modules);
		
		// Load downstream maps and upstream maps for each module
		Iterator<IModule> it = modules.iterator();
		while(it.hasNext()) {
			IModule n = it.next();
			model.getUpstreamModules(n);
		}
		return model;
	}

	private void traverseRepo(ArrayList<IModule> modules, File root, int maxDepth) {
		File[] components = root.listFiles();
		for (int i = 0; i < components.length; i++) {
			if (components[i].isDirectory()) {
				traverseFolder(modules, components[i], maxDepth-1, root);
			}
		}
	}
	
	private void traverseFolder(ArrayList<IModule> modules, File folder, int maxDepth, File root) {
		if( hasFeatureXml(folder) && isInFeaturesFolder(folder)) {
			String featureName = folder.getName();
			String trimmed = getTrimmedName(featureName);
			String componentName = getComponentName(folder, root);
			modules.add(new EclipseModule(featureName, trimmed, componentName, 
					IModuleType.FEATURE, folder));
		} else if( hasManifest(folder) && isInTestsFolder(folder)) {
			String test = folder.getName();
			String trimmed = getTrimmedName(test);
			String componentName = getComponentName(folder, root);
			modules.add(new EclipseModule(test, trimmed, componentName, 
					IModuleType.TEST_PLUGIN, folder));
		} else if( hasManifest(folder) && isInPluginTypeFolder(folder)) {
			String plugin = folder.getName();
			String trimmed = getTrimmedName(plugin);
			String componentName = getComponentName(folder, root);
			modules.add(new EclipseModule(plugin, trimmed, componentName, 
					IModuleType.PLUGIN, folder));
		} else if( hasManifest(folder) ) {
			// nodejs folder, docs folder, etc... wtf wtp
			String plugin = folder.getName();
			String trimmed = getTrimmedName(plugin);
			String componentName = getComponentName(folder, root);
			modules.add(new EclipseModule(plugin, trimmed, componentName, 
					IModuleType.PLUGIN, folder));
		}
		else {
			if( maxDepth > 0 ) {
				File[] children = folder.listFiles();
				for (int i = 0; i < children.length; i++) {
					if (children[i].isDirectory() && !Arrays.asList(getDefaultExclusions()).contains(children[i].getName())) {
						traverseFolder(modules, children[i], maxDepth-1, root);
					}
				}
			} else {
				System.out.println("No module found; giving up: " + folder.getAbsolutePath());
			}
		}
	}
	public static String getTrimmedName(String moduleId) {
		String[] segments = moduleId.split("\\.");
		if (segments.length < 3)
			return moduleId.trim();
		StringBuilder sb = new StringBuilder();
		for( int i = 0; i < segments.length; i++ ) {
			if( i < 3 ) {
				sb.append(segments[i].charAt(0));
			} else {
				sb.append(segments[i]);
			}
			sb.append(".");
		}
		return sb.toString().trim();
	}
	
	private String getComponentName(File folder, File root) {
		File container = folder.getParentFile(); // features, plugins, bundles, docs, etc
		ArrayList<String> store = new ArrayList<String>();
		File working = container.getParentFile();
		while(!working.equals(root)) {
			store.add(0, working.getName());
			working = working.getParentFile();
		}
		String ret = String.join("_", store);
		return ret.trim();
	}
	
	public String[] getDefaultExclusions() {
		return new String[] { ".git", "target"};
	}
	
	public boolean hasManifest(File folder) {
		return hasFile(folder, "META-INF/MANIFEST.MF");
	}
	
	public boolean hasFeatureXml(File folder) {
		return hasFile(folder, "feature.xml");
	}
	
	public boolean hasFile(File folder, String file) {
		File featurexml = new File(folder, file);
		if (!featurexml.exists())
			return false;
		return true;
	}
	
	public boolean isInTestsFolder(File folder) {
		return "tests".equals(folder.getParentFile().getName());
	}
	public boolean isInFeaturesFolder(File folder) {
		return "features".equals(folder.getParentFile().getName());
	}
	public boolean isInPluginTypeFolder(File folder) {
		String parentDir = folder.getParentFile().getName();
		return "plugins".equals(parentDir) || "bundles".equals(parentDir) || "docs".equals(parentDir); // stupid, docs dont belong here,. wtf 
	}
	
	
	
}
