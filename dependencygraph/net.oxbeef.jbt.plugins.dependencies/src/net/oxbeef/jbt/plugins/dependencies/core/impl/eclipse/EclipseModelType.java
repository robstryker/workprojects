package net.oxbeef.jbt.plugins.dependencies.core.impl.eclipse;

import net.oxbeef.jbt.plugins.dependencies.core.IModelLoader;
import net.oxbeef.jbt.plugins.dependencies.core.IModelType;

public class EclipseModelType implements IModelType {
	public static final EclipseModelType TYPE = new EclipseModelType();
	
	@Override
	public String getTypeId() {
		return "eclipse";
	}

	@Override
	public IModelLoader getLoader() {
		return EclipseModelLoader.instance;
	}

}
