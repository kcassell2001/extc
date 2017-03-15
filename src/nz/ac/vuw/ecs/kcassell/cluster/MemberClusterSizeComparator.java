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

import java.util.Comparator;

/**
 * By default, this comparator compares the size of the clusters. The default
 * ordering of clusters will have the smallest clusters first. This can be switched
 * by setting the isAscending flag to false. If sizes are equal, the names are
 * compared.
 * 
 * @author kcassell
 */
public class MemberClusterSizeComparator implements Comparator<Object> {
	/**
	 * Toggles the meaning of the comparison. If ascending, then clusters with a
	 * smaller size come first, e.g. compare(lowSizeNode, highSizeNode) < 0 If
	 * not ascending, then clusters with a larger size come first, e.g.
	 * compare(lowSizeNode, highSizeNode) > 0
	 */
	boolean isAscending = true;

	public void setAscending(boolean isAscending) {
		this.isAscending = isAscending;
	}

	/**
	 * The returned value is based on the cluster sizes. If the sizes are the
	 * same, then the returned value is based on the names.
	 */
	// @Override
	public int compare(Object object1, Object object2) {
		int result = 0;

		if (object1 == null && object2 == null) {
			result = 0;
		} else if (object1 == null) {
			result = -1;
		} else if (object2 == null) {
			result = 1;
		} else { // normal case
			MemberCluster cluster1 = null;
			MemberCluster cluster2 = null;
			
			if (object1 instanceof MemberCluster) {
				cluster1 = (MemberCluster)object1;
			}
			if (object2 instanceof MemberCluster) {
				cluster2 = (MemberCluster)object2;
			}
			
			// Both nodes are non-clusters (size 1)
			if (cluster1 == null && cluster2 == null) {
				result = 0;
			} else if (cluster1 == null && cluster2 != null) {
				result = 1 - cluster2.getElementCount();
			} else if (cluster1 != null && cluster2 == null) {
				result = cluster1.getElementCount() - 1;
			} else {
				result = cluster1.getElementCount() - cluster2.getElementCount();
			}
			
			if (result == 0) {
				result = compareNames(object1, object2);
			}

		}
		
		// Switch the sign of the result if ascending order is desired
		if (!isAscending) {
			result *= -1;
		}
		return result;
	}

	protected int compareNames(Object object1, Object object2) {
		int result;
		String name1 = (object1 instanceof MemberCluster) ? ((MemberCluster) object1)
				.getClusterName() : object1.toString();
		String name2 = (object2 instanceof MemberCluster) ? ((MemberCluster) object2)
				.getClusterName() : object2.toString();

		result = name1.compareTo(name2);
		return result;
	}

}
