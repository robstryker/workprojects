package net.oxbeef.jbt.plugins.dependencies.views;

import net.oxbeef.jbt.plugins.dependencies.dialogs.LoadGraphDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class DependencyView extends ViewPart {
	private static DependencyView instance;
	public static final DependencyView getInstance() {
		return instance;
	}
	
	public DependencyView() {
		super();
		instance = this;
	}
	
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "net.oxbeef.jbt.deptree.views.SampleView";

	private Graph graph;
	private int layout = 1;
	public Graph getGraph() {
		return graph;
	}
	
	private void addMenu(final Graph graph) {
	    Menu menu = new Menu(graph.getShell(), SWT.POP_UP);
	    graph.setMenu(menu);
	    
	    
	    MenuItem item = new MenuItem(menu, SWT.PUSH);
	    item.setText("Clear Graph");
	    item.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				GraphUtil.clearGraph(graph);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	    
	    MenuItem loadGraph = new MenuItem(menu, SWT.PUSH);
	    loadGraph.setText("Load Graph...");
	    loadGraph.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				// TODO open a dialog! 
				// options:  show externals, which component, etc
				GraphUtil.clearGraph(graph);
				LoadGraphDialog d = new LoadGraphDialog(graph.getShell());
				int ret = d.open();
				String comp = d.getComponentName();
				String type = d.getGraphType();
				if( type.equals(LoadGraphDialog.FULL_GRAPH)) {
					new GraphContentUtil().createFullGraph(graph, false, false);
				} else if( type.equals(LoadGraphDialog.FULL_GRAPH_EXTERNALS)) {
					new GraphContentUtil().createFullGraph(graph, false, true);
				} else if( type.equals(LoadGraphDialog.FULL_GRAPH_REDUNDANT)) {
					new GraphContentUtil().createFullGraph(graph, true, false);
				} else if( type.equals(LoadGraphDialog.ONE_COMP)) {
					new GraphContentUtil().createOneComponentDepGraph(graph, comp, false);
				} else if( type.equals(LoadGraphDialog.ONE_COMP_EXTERNALS)) {
					new GraphContentUtil().createOneComponentDepGraph(graph, comp, true);
				} else if( type.equals(LoadGraphDialog.OTHER_COMPONENTS_AGAINST_THIS)) {
					new GraphContentUtil().createComponentsToOneComplonentPluginGraph(graph, comp, false);
				} else if( type.equals(LoadGraphDialog.OTHER_COMPONENTS_AGAINST_THIS_EXTERNALS)) {
					new GraphContentUtil().createComponentsToOneComplonentPluginGraph(graph, comp, true);
				}
				GraphUtil.organizeGraph(graph);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	    MenuItem organizeGraph = new MenuItem(menu, SWT.PUSH);
	    organizeGraph.setText("Organize Graph");
	    organizeGraph.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				GraphUtil.organizeGraph(graph);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}
	
	public void createPartControl(Composite parent) {
		
		// Graph will hold all other objects
		graph = new Graph(parent, SWT.NONE);

		addMenu(graph);
	    
	    
		//GraphUtil.createFullGraph(graph);
		//GraphUtil.createOneComponentDepGraph(graph, "common", false) {
		new GraphContentUtil().createOneComponentDepGraph(graph, "as", false);
		
		graph.setLayoutAlgorithm(new RadialLayoutAlgorithm(
				LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		// Selection listener on graphConnect or GraphNode is not supported
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=236528
		graph.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(e);
			}

		});
		GraphUtil.organizeGraph(graph);
	}
	

	public void setLayoutManager() {
		switch (layout) {
		case 1:
			graph.setLayoutAlgorithm(new TreeLayoutAlgorithm(
					LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
			layout++;
			break;
		case 2:
			graph.setLayoutAlgorithm(new SpringLayoutAlgorithm(
					LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
			layout = 1;
			break;

		}

	}

	/** * Passing the focus request to the viewer's control. */

	public void setFocus() {
	}
}