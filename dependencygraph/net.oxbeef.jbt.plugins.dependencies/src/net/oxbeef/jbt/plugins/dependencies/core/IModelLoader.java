package net.oxbeef.jbt.plugins.dependencies.core;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IModelLoader {
	public IDependencyModel loadModel(File root, Map<String, String> prefs, 
			IProgressMonitor monitor);
}
