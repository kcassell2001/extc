package nz.ac.vuw.ecs.kcassell.similarity;

import java.util.Set;

import nz.ac.vuw.ecs.kcassell.cluster.MemberCluster;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

/**
 * A collection of utilities to calculate distances between groups.
 * In most cases, it will be more efficient to calculate distances
 * once and store them for later access rather than to recalculate
 * using these utilities.
 * @author Keith Cassell
 */
public class ClusterDistanceUtils {

	private static final double MAX_CLUSTER_DISTANCE = 1.0;

	/**
	 * Returns the smallest distance between any element in cluster1
	 * and any element in cluster2
	 * @param cluster1 a collection of elements
	 * @param cluster2 a collection of elements
	 * @param calc calculates the distance between elements
	 * @return the smallest distance
	 */
	public static double singleLinkDistance(MemberCluster cluster1,
			MemberCluster cluster2,
			DistanceCalculatorIfc<String> calc) {
		double min = MAX_CLUSTER_DISTANCE;
		Set<String> elements1 = cluster1.getElements();
		Set<String> elements2 = cluster2.getElements();
		
		for (String element1 : elements1) {
			for (String element2 : elements2) {
				Number distance = calc.calculateDistance(element1, element2);
				if (!RefactoringConstants.UNKNOWN_DISTANCE.equals(distance)) {
					min = Math.min(min, distance.doubleValue());
				}
			}
		}
		return min;
	}

	/**
	 * Returns the smallest distance between any element in cluster1
	 * and any element in cluster2
	 * @param cluster1 a collection of elements
	 * @param cluster2 a collection of elements
	 * @param matrix contains the distances between elements
	 * @return the smallest distance
	 */
	public static double singleLinkDistance(MemberCluster cluster1,
			MemberCluster cluster2,
			DistanceMatrix<String> matrix,
			DistanceCalculatorIfc<String> calc) {
		double min = 1.0;
		Set<String> elements1 = cluster1.getElements();
		Set<String> elements2 = cluster2.getElements();
		
		for (String element1 : elements1) {
			for (String element2 : elements2) {
				Number distance = RefactoringConstants.UNKNOWN_DISTANCE;
				if (matrix != null) {
					distance = matrix.getDistance(element1, element2);
				}
				if (RefactoringConstants.UNKNOWN_DISTANCE.equals(distance)) {
					distance = calc.calculateDistance(element1, element2);
				}
				min = Math.min(min, distance.doubleValue());
			}
		}
		return min;
	}

	/**
	 * Returns the smallest distance between element1 and any element in cluster2
	 * @param element1
	 * @param cluster a collection of elements
	 * @param matrix contains the distances between elements
	 * @return the smallest distance
	 */
	public static double singleLinkDistance(String element1,
			MemberCluster cluster, DistanceMatrix<String> matrix,
			DistanceCalculatorIfc<String> calc) {
		double min = 1.0;
		Set<String> clusterElements = cluster.getElements();

		for (String element2 : clusterElements) {
			Number distance = RefactoringConstants.UNKNOWN_DISTANCE;
			if (matrix != null) {
				distance = matrix.getDistance(element1, element2);
			}
			if (RefactoringConstants.UNKNOWN_DISTANCE.equals(distance)) {
				distance = calc.calculateDistance(element1, element2);
			}
			min = Math.min(min, distance.doubleValue());
		}
		return min;
	}

	/**
	 * Returns the largest distance between any element in cluster1
	 * and any element in cluster2
	 * @param cluster1 a collection of elements
	 * @param cluster2 a collection of elements
	 * @param calc calculates the distance between elements
	 * @return the largest distance
	 */
	public static double completeLinkDistance(MemberCluster cluster1,
			MemberCluster cluster2,
			DistanceCalculatorIfc<String> calc) {
		double max = 0.0;
		Set<String> elements1 = cluster1.getElements();
		Set<String> elements2 = cluster2.getElements();
		
		for (String element1 : elements1) {
			for (String element2 : elements2) {
				Number distance = calc.calculateDistance(element1, element2);
				if (!RefactoringConstants.UNKNOWN_DISTANCE.equals(distance)) {
					max = Math.max(max, distance.doubleValue());
				}
			}
		}
		return max;
	}

	/**
	 * Returns the largest distance between any element in cluster1
	 * and any element in cluster2
	 * @param cluster1 a collection of elements
	 * @param cluster2 a collection of elements
	 * @param matrix contains the distances between elements
	 * @return the largest distance
	 */
	public static double completeLinkDistance(MemberCluster cluster1,
			MemberCluster cluster2,
			DistanceMatrix<String> matrix,
			DistanceCalculatorIfc<String> calc) {
		double max = 0.0;
		Set<String> elements1 = cluster1.getElements();
		Set<String> elements2 = cluster2.getElements();
		
		for (String element1 : elements1) {
			for (String element2 : elements2) {
				Number distance = RefactoringConstants.UNKNOWN_DISTANCE;
				if (matrix != null) {
					distance = matrix.getDistance(element1, element2);
				}
				if (RefactoringConstants.UNKNOWN_DISTANCE.equals(distance)) {
					distance = calc.calculateDistance(element1, element2);
				}
				max = Math.max(max, distance.doubleValue());
			}
		}
		return max;
	}

	/**
	 * Returns the largest distance between element1 and any element in cluster2
	 * @param element1
	 * @param cluster a collection of elements
	 * @param matrix contains the distances between elements
	 * @return the largest distance
	 */
	public static double completeLinkDistance(String element1,
			MemberCluster cluster, DistanceMatrix<String> matrix,
			DistanceCalculatorIfc<String> calc) {
		double max = 0.0;
		Set<String> clusterElements = cluster.getElements();

		for (String element2 : clusterElements) {
			Number distance = RefactoringConstants.UNKNOWN_DISTANCE;
			if (matrix != null) {
				distance = matrix.getDistance(element1, element2);
			}
			if (RefactoringConstants.UNKNOWN_DISTANCE.equals(distance)) {
				distance = calc.calculateDistance(element1, element2);
			}
			max = Math.max(max, distance.doubleValue());
		}
		return max;
	}

	/**
	 * Returns the average distance between the elements in cluster1
	 * and the elements in cluster2
	 * @param cluster1 a collection of elements
	 * @param cluster2 a collection of elements
	 * @param calc calculates the distance between elements
	 * @return the average distance
	 */
	public static double averageLinkDistance(MemberCluster cluster1,
			MemberCluster cluster2,
			DistanceCalculatorIfc<String> calc) {
		double sum = 0.0;
		int i = 0;
		Set<String> elements1 = cluster1.getElements();
		Set<String> elements2 = cluster2.getElements();
		
		for (String element1 : elements1) {
			for (String element2 : elements2) {
				Number distance = calc.calculateDistance(element1, element2);
				if (!RefactoringConstants.UNKNOWN_DISTANCE.equals(distance)) {
					sum += distance.doubleValue();
					i++;
				}
			}
		}
		double average = sum/i;
		return average;
	}

	/**
	 * Returns the average distance between the elements in cluster1
	 * and the elements in cluster2
	 * @param cluster1 a collection of elements
	 * @param cluster2 a collection of elements
	 * @param matrix contains the distances between elements
	 * @return the average distance
	 */
	public static double averageLinkDistance(MemberCluster cluster1,
			MemberCluster cluster2,
			DistanceMatrix<String> matrix,
			DistanceCalculatorIfc<String> calc) {
		double sum = 0.0;
		int i = 0;
		Set<String> elements1 = cluster1.getElements();
		Set<String> elements2 = cluster2.getElements();
		
		for (String element1 : elements1) {
			for (String element2 : elements2) {
				Number distance = RefactoringConstants.UNKNOWN_DISTANCE;
				if (matrix != null) {
					distance = matrix.getDistance(element1, element2);
				}
				if (RefactoringConstants.UNKNOWN_DISTANCE.equals(distance)) {
					distance = calc.calculateDistance(element1, element2);
				}
				sum += distance.doubleValue();
				i++;
			}
		}
		double average = sum/i;
		return average;
	}

	/**
	 * Returns the average distance between the element1
	 * and the elements in cluster2
	 * @param element1
	 * @param cluster a collection of elements
	 * @param matrix contains the distances between elements
	 * @return the average distance
	 */
	public static double averageLinkDistance(String element1,
			MemberCluster cluster, DistanceMatrix<String> matrix,
			DistanceCalculatorIfc<String> calc) {
		double sum = 0.0;
		int i = 0;
		Set<String> elements2 = cluster.getElements();

		for (String element2 : elements2) {
			Number distance = RefactoringConstants.UNKNOWN_DISTANCE;
			if (matrix != null) {
				distance = matrix.getDistance(element1, element2);
			}
			if (RefactoringConstants.UNKNOWN_DISTANCE.equals(distance)) {
				distance = calc.calculateDistance(element1, element2);
			}
			sum += distance.doubleValue();
			i++;
		}
		double average = sum / i;
		return average;
	}
}
