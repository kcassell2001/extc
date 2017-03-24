package nz.ac.vuw.ecs.kcassell.similarity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import nz.ac.vuw.ecs.kcassell.callgraph.CallGraphNode;
import nz.ac.vuw.ecs.kcassell.callgraph.JavaCallGraph;

/**
 * Uses each of a collection of calculators to calculate a
 * distance matrix between the members represented by a callGraph.
 * @author kcassell
 *
 */
public class DistanceCollector {
	
	/** The key is a distance calculator; the value is the distance matrix
	 * computed by that distance calculator for a dependency graph.
	 */
	HashMap<DistanceCalculatorIfc<String>, DistanceMatrix<String>> allDistances =
		new HashMap<DistanceCalculatorIfc<String>, DistanceMatrix<String>>();

	/** The calculators to use for collecting distances. */
	private Collection<DistanceCalculatorIfc<String>> calculators =
		new ArrayList<DistanceCalculatorIfc<String>>();

	
	public DistanceCollector(
			Collection<DistanceCalculatorIfc<String>> calculators) {
		this.calculators = calculators;
	}
	
	/**
	 * Uses each of a collection of calculators to calculate a
	 * distance matrix between the members represented by the callGraph.
	 * @param callGraph a dependency graph between class members
	 */
	public void collectDistances(JavaCallGraph callGraph) {
		List<CallGraphNode> nodes = callGraph.getNodes();
		List<String> memberNames = getMemberNames(nodes);
		
		for (DistanceCalculatorIfc<String> calc : calculators) {
			collectDistances(memberNames, calc);
		}
	}

	/**
	 * Uses a calculator to calculate a
	 * distance matrix between class members.
	 * @param memberNames the names of class members
	 * @param calc the distance calculator to use
	 * @return the distance matrix
	 */
	public DistanceMatrix<String> collectDistances(List<String> memberNames,
			DistanceCalculatorIfc<String> calc) {
		DistanceMatrix<String> matrix =
			new DistanceMatrix<String>(memberNames);
		matrix.fillMatrix(calc);
		allDistances.put(calc, matrix);
		return matrix;
	}

	/**
	 * Collects the member names from the graph's nodes.
	 * @param nodes
	 * @return the list of names
	 */
	protected List<String> getMemberNames(List<CallGraphNode> nodes) {
		List<String> memberNames = new ArrayList<String>();
		
		for (CallGraphNode node : nodes) {
			memberNames.add(node.getSimpleName());
		}
		Collections.sort(memberNames);
		return memberNames;
	}

	public HashMap<DistanceCalculatorIfc<String>, DistanceMatrix<String>> getAllDistances() {
		return allDistances;
	}
}
