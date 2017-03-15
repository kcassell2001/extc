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

package nz.ac.vuw.ecs.kcassell.cluster;

import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

/**
 * This class is used to contain a distance indicating some kind of strength
 * of relationship between objects.
 * @author Keith
 * @see edu.uci.ics.jung.algorithms.shortestpath.Distance<V>
 *      edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics
 *      edu.uci.ics.jung.algorithms.scoring.DistanceCentralityScorer<V,E>
 *      edu.uci.ics.jung.algorithms.shortestpath.ShortestPathUtils
 */
public class Distance<V> {

	protected V first = null;
	protected V second = null;
	protected Number distance = RefactoringConstants.UNKNOWN_DISTANCE;

	public Distance(V first, V second, Number distance) {
		super();
		this.first = first;
		this.second = second;
		this.distance = distance;
	}
	
	public V getFirst() {
		return first;
	}

	public V getSecond() {
		return second;
	}

	public Number getDistance() {
		return distance;
	}

	@Override
	public String toString() {
		return "Distance@" + hashCode() + " (" +
				first + " - " + second + "): distance= " + distance;
	}
	

}
