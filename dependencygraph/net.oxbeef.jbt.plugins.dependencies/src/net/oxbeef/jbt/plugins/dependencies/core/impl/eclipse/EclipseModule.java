package net.oxbeef.jbt.plugins.dependencies.core.impl.eclipse;

import java.io.File;
import java.io.IOException;
import java.io.IOError;

import net.oxbeef.jbt.plugins.dependencies.core.impl.DependencyModule;
import net.oxbeef.jbt.plugins.dependencies.model.SimplifiedManifest;

public class EclipseModule extends DependencyModule{
	private SimplifiedManifest manifest;
	public EclipseModule(String name, String shortName, String component, IModuleType type, 
			File location) {
		super(name, shortName, component, type, location);
		if( IModuleType.PLUGIN == type || IModuleType.TEST_PLUGIN == type) {
			File mfFile = new File(location, "META-INF/MANIFEST.MF");
			try {
				SimplifiedManifest sm = new SimplifiedManifest(mfFile);
				if (sm.isValid()) {
					this.manifest = sm;
				}
			} catch(IOException ioe) {
				System.err.println("Module " + name + " has an invalid manifest");
			}
		}
	}
	
	public SimplifiedManifest getManifest() {
		return manifest;
	}

}
