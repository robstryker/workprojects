package net.oxbeef.jbt.plugins.dependencies.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.zest.core.widgets.Graph;

public interface IModelUI {
	public String[] getGraphTypes();
	public void fillGraph(Graph graph, String dir, String type, String component, IProgressMonitor mon);
}
