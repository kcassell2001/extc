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

package nz.ac.vuw.ecs.kcassell.callgraph;

import java.util.Comparator;

/**
 * By default, this comparator compares the links' weights, such that the default
 * ordering of links will have the lowest weights first. This can be switched by
 * setting the isAscending flag to false. If weights are equal, the link IDs are
 * compared.
 * 
 * @author kcassell
 */
public class WeightComparator implements Comparator<CallGraphLink> {
	/**
	 * Toggles the meaning of the comparison. If ascending, then links with a
	 * lower weight come first, e.g. compare(lowWeightLink, highWeightLink) < 0 If
	 * not ascending, then links with a higher weight come first, e.g.
	 * compare(lowWeightLink, highWeightLink) > 0
	 */
	boolean isAscending = true;

	/**
	 * A link may have multiple weights. This field specifies which of those
	 * weights will be used in the comparison.
	 */
	String weightBeingCompared = ScoreType.BASIC;

	public void setAscending(boolean isAscending) {
		this.isAscending = isAscending;
	}

	public String getWeightBeingCompared() {
		return weightBeingCompared;
	}

	public void setWeightBeingCompared(String weightBeingCompared) {
		this.weightBeingCompared = weightBeingCompared;
	}

	/**
	 * The returned value is based on the weights. If the weights are the same,
	 * then the returned value is based on the IDs.
	 */
	// @Override
	public int compare(CallGraphLink link1, CallGraphLink link2) {
		int result = 0;
		Number weight1 = link1.getWeight(weightBeingCompared);
		Number weight2 = link2.getWeight(weightBeingCompared);
		int id1 = link1.getId();
		int id2 = link2.getId();

		if ((weight1 == null) && (weight2 == null))

		{
			result = id1 - id2;
		} else if (weight1 == null) {
			result = -1;
		} else if (weight2 == null) {
			result = 1;
		} else {
			result = (int) Math.signum(weight1.doubleValue()
					- weight2.doubleValue());

			if (result == 0) {
				result = id1 - id2;
			}
		}

		if (!isAscending) {
			result *= -1;
		}
		return result;
	}

}
