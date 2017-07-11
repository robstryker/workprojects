package net.oxbeef.jbt.plugins.dependencies.core.impl;

import java.io.File;

public class PhantomModule extends DependencyModule {

	public PhantomModule(String name, String shortName, String component, IModuleType type, File location) {
		super(name, shortName, component, type, location);
	}

	
	public boolean isPhantom() {
		return true;
	}
}
