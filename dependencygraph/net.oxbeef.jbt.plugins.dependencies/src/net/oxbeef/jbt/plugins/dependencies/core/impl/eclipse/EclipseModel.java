package net.oxbeef.jbt.plugins.dependencies.core.impl.eclipse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.oxbeef.jbt.plugins.dependencies.core.IDependencyModel;
import net.oxbeef.jbt.plugins.dependencies.core.IModelType;
import net.oxbeef.jbt.plugins.dependencies.core.IModule;
import net.oxbeef.jbt.plugins.dependencies.core.IModule.IModuleType;
import net.oxbeef.jbt.plugins.dependencies.core.impl.PhantomModule;
import net.oxbeef.jbt.plugins.dependencies.model.SimplifiedManifest;

public class EclipseModel implements IDependencyModel {
	private List<IModule> modules;
	private List<IModule> phantomModules;
	
	private Map<String, IModule[]> upstreamMap = null;
	private Map<String, IModule[]> downstreamMap = null;
	

	public EclipseModel(List<IModule> list) {
		this.modules = list;
		phantomModules = new ArrayList<IModule>();
		this.upstreamMap = new HashMap<String, IModule[]>();
		this.downstreamMap = new HashMap<String, IModule[]>();
		
	}
	
	@Override
	public IModule[] getModules() {
		return (IModule[]) modules.toArray(new IModule[modules.size()]);
	}

	@Override
	public IModule[] getUpstreamModules(IModule module) {
		if( upstreamMap.get(module.getName()) == null ) {
			upstreamMap.put(module.getName(), loadUpstreamModules(module));
		}
		return upstreamMap.get(module.getName());
	}
	
	// find what this module depends on
	public IModule[] loadUpstreamModules(IModule module) {
		if( module instanceof EclipseModule) {
			SimplifiedManifest sm = ((EclipseModule)module).getManifest();
			if( sm == null ) {
				System.err.println("Module " + module.getName() + " has no manifest");
				return new IModule[] {};
			}
			
			ArrayList<String> upstreamIds = sm.getDependencies();
			Iterator<String> upstreamIt = upstreamIds.iterator();
			
			ArrayList<IModule> ret = new ArrayList<IModule>();
			while(upstreamIt.hasNext()) {
				String id = upstreamIt.next();
				IModule found = findOrCreateModule(id);
				ret.add(found);
			}
			return (IModule[]) ret.toArray(new IModule[ret.size()]);
		}
		return new IModule[] {};
	}
	
	private IModule findOrCreateModule(String id) {
		IModule found = findModule(id);
		if( found == null ) {
			// This module depends on an upstream module not in this source tree
			found = new PhantomModule(id, EclipseModelLoader.getTrimmedName(id), 
					getUnknownComponent(id), IModuleType.PLUGIN, null);
			phantomModules.add(found);
		}
		return found;
	}
	

	@Override
	public IModule[] getDownstreamModules(IModule module) {
		if( downstreamMap.get(module.getName()) == null ) {
			downstreamMap.put(module.getName(), loadDownstreamModules(module));
		}
		return downstreamMap.get(module.getName());
	}
	
	private IModule[] loadDownstreamModules(IModule module) {
		String id = module.getName();
		Iterator<IModule> it = modules.iterator();
		ArrayList<IModule> ret = new ArrayList<IModule>();
		while(it.hasNext()) {
			IModule m = it.next();
			if( m instanceof EclipseModule) {
				SimplifiedManifest sm = ((EclipseModule)m).getManifest();
				ArrayList<String> upstreamIds = sm.getDependencies();
				if( upstreamIds.contains(id)) {
					ret.add(m);
				}
			}
		}
		return (IModule[]) ret.toArray(new IModule[ret.size()]);
	}

	private String getUnknownComponent(String moduleId) {
		String[] segments = moduleId.split("\\.");
		if( segments.length > 3) {
			return segments[0] + "." + segments[1] + "." + segments[2];
		}
		return moduleId;
	}

	private IModule findModule(String id) {
		Iterator<IModule> it = modules.iterator();
		while(it.hasNext()) {
			IModule n = it.next();
			if( n.getName().equals(id)) {
				return n;
			}
		}
		// Not found in our source modules, check upstream
		Iterator<IModule> it2 = phantomModules.iterator();
		while(it2.hasNext()) {
			IModule n = it2.next();
			if( n.getName().equals(id)) {
				return n;
			}
		}

		return null;
	}
	

	@Override
	public IModelType getType() {
		return EclipseModelType.TYPE;
	}

	
	public String[] getSourceComponents() {
		return getComponents(modules);
	}
	public String[] getPhantomComponents() {
		return getComponents(phantomModules);
	}
	
	public boolean isPhantomComponent(String component) {
		List<String> sourceComponents = Arrays.asList(getSourceComponents());
		return !sourceComponents.contains(component);
	}
	
	public String[] getAllComponents() {
		final String[] sourceComponents = getSourceComponents();
		final String[] phantomComponents = getPhantomComponents();
		Set<String> ret = new HashSet<String>();
		ret.addAll(Arrays.asList(sourceComponents));
		ret.addAll(Arrays.asList(phantomComponents));
		ArrayList<String> ret2 = new ArrayList<String>(ret);
		Collections.sort(ret2, new Comparator<String>() {
			public int compare(String o1, String o2) {
				boolean o1Source = Arrays.asList(sourceComponents).contains(o1);
				boolean o2Source = Arrays.asList(sourceComponents).contains(o2);
				if( o1Source == o2Source)
					return o1.compareTo(o2);
				else if( o1Source )
					return -1;
				else if( o2Source)
					return 1;
				return 0;
			}
		});
		return (String[]) ret2.toArray(new String[ret2.size()]);
	}
	
	public String[] getComponents(List<IModule> list) {
		Iterator<IModule> it = list.iterator();
		Set<String> ret = new HashSet<String>();
		while(it.hasNext()) {
			ret.add(it.next().getComponent());
		}
		return (String[]) ret.toArray(new String[ret.size()]);
	}

	@Override
	public String[] getComponents() {
		return getAllComponents();
	}

	@Override
	public IModule[] getModules(String component) {
		return getModules(component, null);
	}

	@Override
	public IModule[] getModules(IModuleType type) {
		return getModules(null, type);
	}

	@Override
	public IModule[] getModules(String component, IModuleType type) {
		ArrayList<IModule> m2 =  new ArrayList<IModule>(modules);
		Iterator<IModule> mit = m2.iterator();
		while(mit.hasNext()) {
			IModule im = mit.next();
			if( component != null && !component.equals(im.getComponent())) {
				mit.remove();
			} else if( type != null && im.getModuleType() != type) {
				mit.remove();
			}
		}
		return (IModule[]) m2.toArray(new IModule[m2.size()]);
	}
	
}
