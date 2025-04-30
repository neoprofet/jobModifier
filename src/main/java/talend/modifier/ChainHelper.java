package talend.modifier;

import java.util.*;

/**
 * Utility class for analyzing component connection chains in Talend jobs.
 * <p>
 * Provides methods to determine reachability between components and to find the last component
 * in a chain starting from a given node.
 */
public class ChainHelper {

    /**
     * Finds the last node in a linear chain of components starting from the specified node.
     * <p>
     * This method assumes that the graph represents a directed sequence of Talend components
     * connected one after another (e.g., via OnComponentOk). It traverses the graph by always
     * taking the last listed connection from each node until no further nodes are found.
     *
     * @param start the name of the starting component node
     * @param graph a directed graph where keys are component names and values are lists of connected components
     * @return the name of the last node in the chain starting from {@code start}
     */
    public static String findLastNodeInChain(String start, Map<String, List<String>> graph) {
        String current = start;
        while (graph.containsKey(current) && !graph.get(current).isEmpty()) {
            current = graph.get(current).get(graph.get(current).size() - 1);
        }
        return current;
    }

    /**
     * Determines if a target node is reachable from a start node in the component connection graph.
     * <p>
     * This method performs a depth-first search (DFS) to check whether there is a path from {@code start}
     * to {@code target}. It prevents revisiting nodes by tracking them in a visited set.
     *
     * @param start  the name of the starting component node
     * @param target the name of the target component node to find
     * @param graph  a directed graph where keys are component names and values are lists of connected components
     * @return {@code true} if {@code target} is reachable from {@code start}, otherwise {@code false}
     */
    public static boolean isNodeReachable(String start, String target, Map<String, List<String>> graph) {
        Set<String> visited = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (!visited.add(current)) continue;

            if (target.equals(current)) return true;

            for (String neighbor : graph.getOrDefault(current, Collections.emptyList())) {
                stack.push(neighbor);
            }
        }
        return false;
    }
}
