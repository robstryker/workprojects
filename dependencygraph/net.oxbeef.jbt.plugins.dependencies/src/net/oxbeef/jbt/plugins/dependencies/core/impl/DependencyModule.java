package net.oxbeef.jbt.plugins.dependencies.core.impl;

import java.io.File;

import net.oxbeef.jbt.plugins.dependencies.core.IModule;

public class DependencyModule implements IModule {
	private String name;
	private String shortName;
	private String component;
	private IModuleType type;
	private File location;

	public DependencyModule(String name, String shortName, String component, 
			IModuleType type, File location) {
		this.name = name;
		this.shortName = shortName;
		this.component = component;
		this.type = type;
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	public String getComponent() {
		return component;
	}

	public IModuleType getModuleType() {
		return type;
	}

	public File getLocation() {
		return location;
	}
	
	public boolean isPhantom() {
		return false;
	}

}
