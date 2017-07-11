package net.oxbeef.jbt.plugins.dependencies.core;

import net.oxbeef.jbt.plugins.dependencies.core.IModule.IModuleType;

public interface IDependencyModel {
	public IModule[] getModules();
	public IModule[] getUpstreamModules(IModule module);
	public IModule[] getDownstreamModules(IModule module);
	
	public String[] getComponents();
	public boolean isPhantomComponent(String component);
		
		
	
	public IModule[] getModules(String component);
	public IModule[] getModules(IModuleType type);
	public IModule[] getModules(String component, IModuleType type);
	
	public IModelType getType();
}
