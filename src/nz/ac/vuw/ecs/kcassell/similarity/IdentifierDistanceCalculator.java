/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
Copyright (c) 2010, Keith Cassell
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following 
      disclaimer in the documentation and/or other materials
      provided with the distribution.
    * Neither the name of the Victoria University of Wellington
      nor the names of its contributors may be used to endorse or
      promote products derived from this software without specific
      prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package nz.ac.vuw.ecs.kcassell.similarity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import nz.ac.vuw.ecs.kcassell.utils.ApplicationParameters;
import nz.ac.vuw.ecs.kcassell.utils.IdentifierParser;
import nz.ac.vuw.ecs.kcassell.utils.ParameterConstants;
import nz.ac.vuw.ecs.kcassell.utils.Stemmer;

/**
 * This is a Jaccard calculator where the property set of a class
 * member (field or method) consists of the subcomponents of the
 * identifier, e.g. the property set for "calculateDistance" would
 * be two elements: ["calculate", "distance"]
 * @author Keith
 *
 */
public class IdentifierDistanceCalculator
extends JaccardCalculator
implements DistanceCalculatorIfc<String>
{
	/** A list of tokens that shouldn't be considered in the properties. */
	protected ArrayList<String> toIgnore = new ArrayList<String>();
	
	/** A "stemmer" that removes common suffixes from words. */
	protected Stemmer stemmer = new Stemmer();

	public IdentifierDistanceCalculator() {
		createToIgnoreList();
	}

	/**
	 * This provides the property set for a method or attribute. The property
	 * set of a class member (field or method) consists of the subcomponents of
	 * the identifier.  These subcomponents are normalized by converting to
	 * lower case and stripping suffixes,
	 * e.g. the property set for "calculatingDistances" would
	 * be two elements: ["calcul", "distance"].
	 * @param identifier the member identifier, e.g."calculateDistance"
	 * @return The property set
	 */
	public Set<String> getProperties(String member) {
		ArrayList<String> parts =
			IdentifierParser.parseCamelCaseIdentifier(member);
		
		// eliminate case differences
		HashSet<String> properties = new HashSet<String>();
		for (String part : parts) {
			String lowerCase = part.toLowerCase();
			stemmer.add(lowerCase.toCharArray(), lowerCase.length());
			stemmer.stem();
			String partStem = stemmer.toString();
			properties.add(partStem);
		}
		return properties;
	}    

    /** Calculate the distance between the identifiers 
     * @param id1 the first identifier
     * @param id1 the second identifier */
	public Double calculateDistance(String id1, String id2) {
		Double distance = null;
		Set<String> properties1 = getProperties(id1);
		properties1.removeAll(toIgnore);
		Set<String> properties2 = getProperties(id2);
		properties2.removeAll(toIgnore);
		distance = calculateDistance(properties1, properties2);
		return distance;
	}

	/**
	 * Creates a list of tokens that shouldn't be considered when comparing
	 * the closeness of two identifiers.  The created list is based on
	 * the value retrieved from 
	 * ApplicationParameterConstants.IDENTIFIER_PARTS_TO_IGNORE_KEY
	 */
	protected void createToIgnoreList() {
		toIgnore = new ArrayList<String>();
		String sToIgnore = ApplicationParameters.getSingleton().getParameter(
				ParameterConstants.IDENTIFIER_PARTS_TO_IGNORE_KEY, "");
		if (sToIgnore != null) {
			StringTokenizer tokenizer = new StringTokenizer(sToIgnore,
					" ,\t\n\r\f");
			while (tokenizer.hasMoreTokens()) {
				toIgnore.add(tokenizer.nextToken());
			}
		}
	}

	public DistanceCalculatorEnum getType() {
		return DistanceCalculatorEnum.Identifier;
	}

}
