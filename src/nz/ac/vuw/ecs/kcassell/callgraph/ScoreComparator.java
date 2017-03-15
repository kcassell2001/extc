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
 * By default, this comparator compares the nodes' scores, such that the default
 * ordering of nodes will have the lowest scores first. This can be switched by
 * setting the isAscending flag to false. If scores are equal, the node IDs are
 * compared.
 * 
 * @author kcassell
 */
public class ScoreComparator implements Comparator<CallGraphNode> {
	/**
	 * Toggles the meaning of the comparison. If ascending, then nodes with a
	 * lower score come first, e.g. compare(lowScoreNode, highScoreNode) < 0 If
	 * not ascending, then nodes with a higher score come first, e.g.
	 * compare(lowScoreNode, highScoreNode) > 0
	 */
	boolean isAscending = true;

	/**
	 * A node may have multiple scores. This field specifies which of those
	 * scores will be used in the comparison.
	 */
	String scoreBeingCompared = ScoreType.CENTRALITY;

	public void setAscending(boolean isAscending) {
		this.isAscending = isAscending;
	}

	public String getScoreBeingCompared() {
		return scoreBeingCompared;
	}

	public void setScoreBeingCompared(String scoreBeingCompared) {
		this.scoreBeingCompared = scoreBeingCompared;
	}

	/**
	 * The returned value is based on the scores. If the scores are the same,
	 * then the returned value is based on the IDs.
	 */
	// @Override
	public int compare(CallGraphNode node1, CallGraphNode node2) {
		int result = 0;

		if (node1 == null && node2 == null) {
			result = 0;
		} else if (node1 == null) {
			result = -1;
		} else if (node2 == null) {
			result = 1;
		} else {
			Number score1 = node1.getScore(scoreBeingCompared);
			Number score2 = node2.getScore(scoreBeingCompared);
			int id1 = node1.getId();
			int id2 = node2.getId();

			if ((CallGraphNode.UNKNOWN_SCORE.equals(score1) && CallGraphNode.UNKNOWN_SCORE
					.equals(score2))
					|| ((score1 == null) && (score2 == null)))

			{
				result = id1 - id2;
			} else if (CallGraphNode.UNKNOWN_SCORE.equals(score1)
					|| (score1 == null)) {
				result = -1;
			} else if (CallGraphNode.UNKNOWN_SCORE.equals(score2)
					|| (score2 == null)) {
				result = 1;
			} else {
				result = (int) Math.signum(score1.doubleValue()
						- score2.doubleValue());

				if (result == 0) {
					result = id1 - id2;
				}
			}
		}   // else neither node is null
		if (!isAscending) {
			result *= -1;
		}
		return result;
	}
}
