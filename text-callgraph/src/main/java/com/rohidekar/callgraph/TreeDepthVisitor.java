package com.rohidekar.callgraph;

import java.util.HashMap;
import java.util.Map;

public class TreeDepthVisitor {
	private Map<String, GraphNode> visited = new HashMap<String, GraphNode>();

	public void visit(GraphNode iParent) {
		visited.put(iParent.toString(), iParent);
	}

	public boolean isVisited(GraphNode iParent) {
		return visited.keySet().contains(iParent.toString());
	}
}
