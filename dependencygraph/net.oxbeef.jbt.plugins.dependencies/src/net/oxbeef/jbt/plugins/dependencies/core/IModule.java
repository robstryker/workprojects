package net.oxbeef.jbt.plugins.dependencies.core;

public interface IModule {
	public String getName();
	public String getShortName();
	public String getComponent();
	public IModuleType getModuleType();
	public boolean isPhantom();
	
	public enum IModuleType {
		MODULE, PLUGIN, FEATURE, TEST_PLUGIN
	}
}
