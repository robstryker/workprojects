package net.oxbeef.jbt.plugins.dependencies.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

public class GraphUtil {
	public static void clearGraph( Graph g ) { 

		// remove all the connections 
		Object[] objects = g.getConnections().toArray() ; 
		for ( int x = 0 ; x < objects.length; x++ )
			((GraphConnection) objects[x]).dispose();

		// remove all the nodes 
		objects = g.getNodes().toArray() ; 
		for ( int x = 0 ; x < objects.length; x++ ) 
			((GraphNode) objects[x]).dispose(); 
	}
	
	public static void organizeGraph(final Graph g) {
		g.setLayoutAlgorithm(new TreeLayoutAlgorithm(
				LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		// The algorithm is slow. Do this too. 
		Display.getDefault().asyncExec(new Runnable() { 
			public void run() {
				organizeGraph2(g);
			}
		});
	}
	public static void organizeGraph2(final Graph g) {
		findIsolatedNodes(g);
		HashMap<Integer, ArrayList<GraphNode>> map = getCurrentPositionMap(g);
		spaceOutLevels(g, map);
		optimizePositionMap(g, map);
		spaceOutLevels(g, map);
	}
	
	public static int findRowNumber(GraphNode n, HashMap<Integer, ArrayList<GraphNode>> map) {
		Set<Integer> s = map.keySet();
		ArrayList<Integer> yPositions = new ArrayList<Integer>(s);
		Collections.sort(yPositions);
		return yPositions.indexOf(findYPosition(n, map));
	}
	
	public static int findYPosition(GraphNode n, HashMap<Integer, ArrayList<GraphNode>> map) {
		Iterator<Integer> i = map.keySet().iterator();
		while(i.hasNext()) {
			int y = i.next();
			if( map.get(y) != null && map.get(y).contains(n))
				return y;
		}
		return -1;
	}
	
	public static void optimizePositionMap(Graph g, HashMap<Integer, ArrayList<GraphNode>> map) {
		List l = g.getNodes();
		Iterator i = l.iterator();
		while(i.hasNext()) {
			GraphNode n = (GraphNode)i.next();
			System.out.println(n);
			if(n.toString().contains("org.eclipse.help")) {
				System.out.println();
			}
			GraphConnection[] nodeConnections = getConnectionsForNode(n, g.getConnections(), true);
			int lowestDep = -1;
			for( int j = 0; j < nodeConnections.length; j++ ) {
				int cRow = findRowNumber(nodeConnections[j].getDestination(), map);
				if( lowestDep == -1 ) lowestDep = cRow;
				if( cRow < lowestDep)
					lowestDep = cRow;
			}
			// My lowest dep is in row 'lowestDep' so i must belong in row one below that
			int myRow = findRowNumber(n, map);
			if( myRow < lowestDep -1 ) {
				// I'm too high in the map! I need to move down
				int currentY = findYPosition(n, map);
				int newRow = lowestDep -1 >= 0 ? lowestDep-1 : 0; // dn't go negative
				ArrayList<Integer> tmp = new ArrayList<Integer>(map.keySet());
				Collections.sort(tmp);
				int newYPos = tmp.get(newRow);
				map.get(currentY).remove(n);
				map.get(newYPos).add(n);
			}
		}
	}
	
	public static HashMap<Integer, ArrayList<GraphNode>> getCurrentPositionMap(Graph g) {
		HashMap<Integer, ArrayList<GraphNode>> map = new HashMap<Integer, ArrayList<GraphNode>>();
		List nodes = g.getNodes();
		Iterator it = nodes.iterator();
		while(it.hasNext()) {
			GraphNode n = ((GraphNode)it.next());
			int height = n.getLocation().y;
			ArrayList<GraphNode> list = map.get(new Integer(height));
			if( list == null ) {
				list = new ArrayList<GraphNode>();
				map.put(new Integer(height), list);
			}
			list.add(n);
		}
		return map;
	}
	public static void spaceOutLevels(final Graph g) {
		HashMap<Integer, ArrayList<GraphNode>> map = getCurrentPositionMap(g);
		spaceOutLevels(g, map);
	}
	
	public static void spaceOutLevels(final Graph g,HashMap<Integer, ArrayList<GraphNode>> map) {
		// We have a map of each 'level' and a bunch on that level.
		Set<Integer> s = map.keySet();
		ArrayList<Integer> yPositions = new ArrayList<Integer>(s);
		Collections.sort(yPositions);
		Iterator<Integer> yIt = yPositions.iterator();
		int newYVal = 70;
		int yValIncrement = 100;
		while(yIt.hasNext()) {
			Integer yVal = yIt.next();
			ArrayList<GraphNode> oneRow = map.get(yVal);
			GraphNode[] oneRowArray = (GraphNode[]) oneRow.toArray(new GraphNode[oneRow.size()]);
			setOneRowsPosition(oneRowArray, g, newYVal);
			newYVal += yValIncrement;
		}
	}
	
	public static void setOneRowsPosition(GraphNode[] oneRowArray, Graph g, int yVal) {
		// Set one row
		int maxWidth = g.getBounds().width;
		int oneSlotWidth = maxWidth / oneRowArray.length;
		int pos = 0;
		for( int i = 0; i < oneRowArray.length; i++ ) {
			int thisSlotMiddle = pos + (oneSlotWidth/2);
			int thisNodeWidth = oneRowArray[i].getSize().width;
			int newX = thisSlotMiddle - (thisNodeWidth/2);
			oneRowArray[i].setLocation(newX, yVal);
			pos += oneSlotWidth;
		}
	}
	
	public static void findIsolatedNodes(final Graph g) {
		List nodel = g.getNodes();
		List nodelClone = new ArrayList();
		nodelClone.addAll(nodel);
		
		List connectionl = g.getConnections();
		List connectionClone = new ArrayList();
		connectionClone.addAll(connectionl);
				
		int nextX = 10;
		int nextY = 0;
		
		GraphNode[] isolated = findNodesWithGroupsOfSize(nodelClone, connectionClone, 1);
		for( int i = 0; i < isolated.length; i++ ) {
			GraphNode n = isolated[i];
			n.setLocation(nextX, nextY);
			nextX = n.getLocation().x + n.getSize().width + 10; 
			nodelClone.remove(n);
		}
	}
	
	public static GraphNode[] findNodesWithGroupsOfSize(List<GraphNode> nodes, List connections, int size) {
		ArrayList<GraphNode> ret = new ArrayList<GraphNode>();
		ArrayList<GraphNode> matched = new ArrayList<GraphNode>();
		
		Iterator i = nodes.iterator();
		while(i.hasNext()) {
			GraphNode n = ((GraphNode)i.next());
			if( !matched.contains(n)) {
				GraphNode[] grouped =findGroupedNodes(n, connections, size); 
				if( grouped != null ) {
					ret.add(n);
					matched.addAll(Arrays.asList(grouped));
				}
			}
		}
		return (GraphNode[]) ret.toArray(new GraphNode[ret.size()]);
	}
	
	public static GraphNode[] findGroupedNodes(GraphNode n, List connections, int groupSize) {
		GraphConnection[] all = getConnectionsForNode(n, connections);
		GraphNode[] opposites = findConnectedNodes(n, connections,all);
		ArrayList<GraphNode> possibleNodesAsList = new ArrayList<GraphNode>();
		possibleNodesAsList.addAll(Arrays.asList(opposites));
		possibleNodesAsList.add(n);
		if( opposites.length > groupSize-1)
			return null;
		if( opposites.length == groupSize-1 ) {
			for( int i = 0; i < opposites.length; i++ ) {
				if( hasOtherLinks(opposites[i], connections, possibleNodesAsList)) {
					return null;
				}
			}
		}
		return (GraphNode[]) possibleNodesAsList
				.toArray(new GraphNode[possibleNodesAsList.size()]);
	}
	
	public static boolean hasOtherLinks(GraphNode node, List connections, ArrayList<GraphNode> approvedLinks) {
		ArrayList<GraphNode> links = findConnectedNodesAsList(node, connections);
		links.removeAll(approvedLinks);
		return links.size() > 0;
	}
	
	public static GraphNode[] findConnectedNodes(GraphNode n, List connections) {
		GraphConnection[] cons = getConnectionsForNode(n, connections);
		return findConnectedNodes(n, connections, cons);
	}
	public static ArrayList<GraphNode> findConnectedNodesAsList(GraphNode n, List connections) {
		GraphConnection[] cons = getConnectionsForNode(n, connections);
		GraphNode[] nodes = findConnectedNodes(n, connections, cons);
		ArrayList<GraphNode> ret = new ArrayList<GraphNode>();
		ret.addAll(Arrays.asList(nodes));
		return ret;
	}
	
	public static GraphNode[] findConnectedNodes(GraphNode n, 
			List connections, GraphConnection[] cons) {
		ArrayList<GraphNode> l = new ArrayList<GraphNode>();
		for( int i = 0; i < cons.length; i++ ) {
			GraphNode opposite = cons[i].getSource().equals(n) ? cons[i].getDestination() : cons[i].getSource();
			if( !l.contains(opposite))
				l.add(opposite);
		}
		return (GraphNode[]) l.toArray(new GraphNode[l.size()]);
	}
	
	public static GraphConnection[] getConnectionsForNode(GraphNode n, List connections, boolean source) {
		ArrayList<GraphConnection> ret = new ArrayList<GraphConnection>();
		GraphConnection[] all = getConnectionsForNode(n, connections);
		for( int i = 0; i < all.length; i++ ) {
			if( source && all[i].getSource().equals(n))
				ret.add(all[i]);
			else if( !source && all[i].getDestination().equals(n))
				ret.add(all[i]);
		}
		return (GraphConnection[]) ret.toArray(new GraphConnection[ret.size()]);
	}
	
	public static GraphConnection[] getConnectionsForNode(GraphNode n, List connections) {
		ArrayList<GraphConnection> ret = new ArrayList<GraphConnection>();
		Iterator i = connections.iterator();
		while(i.hasNext()) {
			GraphConnection gc = ((GraphConnection)i.next());
			if( gc.getSource().equals(n) || gc.getDestination().equals(n)) {
				if( gc.getSource().equals(gc.getDestination()))
					continue; // ignore this
				ret.add(gc);
			}
		}
		return (GraphConnection[]) ret.toArray(new GraphConnection[ret.size()]);
	}
	
	public static boolean isNodeStandalone(GraphNode n, List connections) {
		return getConnectionsForNode(n, connections).length == 0;
	}
}
