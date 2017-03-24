package nz.ac.vuw.ecs.kcassell.similarity;

import java.io.IOException;
import java.util.ArrayList;

import nz.ac.vuw.ecs.kcassell.utils.IdentifierParser;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

/**
 * This use the GoogleDistanceCalculator to calculate the
 * Normalized Google Distance between member identifiers.
 */

public class IdentifierGoogleDistanceCalculator
implements DistanceCalculatorIfc<String> {
	
	GoogleDistanceCalculator googleCalculator = null;

	public IdentifierGoogleDistanceCalculator()
	throws NumberFormatException, IOException {
		googleCalculator =
			new GoogleDistanceCalculator();
	}
	
	
	/**
	 * Calculates the normalized Google Distance (NGD) between the two identifiers
	 * specified.  NOTE: this number can change between runs, because it is
	 * based on the number of web pages found by Google, which changes.
	 * @return a number from 0 (minimally distant) to 1 (maximally distant),
	 *   unless an exception occurs in which case, it is negative
	 *   (RefactoringConstants.UNKNOWN_DISTANCE)
	 */
	public Double calculateDistance(String id1, String id2) {
		double distance = RefactoringConstants.UNKNOWN_DISTANCE.doubleValue();
		String searchTerm1 = buildSearchTerm(id1);
		String searchTerm2 = buildSearchTerm(id2);
		distance = googleCalculator.calculateDistance(searchTerm1, searchTerm2);
		return distance;
	}


	/**
	 * Creates a search term for a search engine by parsing a
	 * camel case identifier into its components, e.g. "StringBuffer"
	 * becomes "String Buffer"
	 * @param id the identifier to parse
	 * @return
	 */
	private String buildSearchTerm(String id) {
		ArrayList<String> parts =
			IdentifierParser.parseCamelCaseIdentifier(id);
		StringBuffer buf = new StringBuffer();
		for (String part : parts) {
			buf.append(part).append(" ");
		}
		buf.delete(buf.length() - 1, buf.length());
		String searchTerm = buf.toString();
		return searchTerm;
	}


	public DistanceCalculatorEnum getType() {
		return DistanceCalculatorEnum.GoogleDistance;
	}
	
	public void clearCache() {
		googleCalculator.clearCache();
	}

}
