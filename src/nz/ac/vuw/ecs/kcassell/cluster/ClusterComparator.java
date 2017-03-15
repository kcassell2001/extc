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
 * By default, this comparator compares the clusters' names, such that the default
 * ordering of clusters will have the lowest names first. This can be switched by
 * setting the isAscending flag to false. If names are equal, the cluster IDs are
 * compared.
 * 
 * @author kcassell
 */
public class ClusterComparator implements Comparator<Object> {
	/**
	 * Toggles the meaning of the comparison. If ascending, then clusters with a
	 * lower name come first, e.g. compare(lowNameCluster, highNameCluster) < 0 If
	 * not ascending, then clusters with a higher name come first, e.g.
	 * compare(lowNameCluster, highNameCluster) > 0
	 */
	boolean isAscending = true;

	public void setAscending(boolean isAscending) {
		this.isAscending = isAscending;
	}

	/**
	 * The returned value is based on the names.
	 */
	// @Override
	public int compare(Object obj1, Object obj2) {
		int result = 0;
		if ((obj1 == null) && (obj2 == null)) {
			result = 0;
		} else if (obj1 == null) {
			result = -1;
		} else if (obj2 == null) {
			result = 1;
		} else {
			String name1 = getName(obj1);
			String name2 = getName(obj2);
			result = name1.compareTo(name2);
		}
		if (!isAscending) {
			result *= -1;
		}
		return result;
	}

	/**
	 * Return the name of the object
	 * @param obj either a String identifier or a MemberCluster
	 * @return the cluster name for a MemberCluster; obj.toString() otherwise
	 */
	protected String getName(Object obj) {
		String name = null;
		if (obj != null) {
			if (obj instanceof MemberCluster) {
				name = ((MemberCluster) obj).getClusterName();
			} else {
				name = obj.toString();
			}
		}
		return name;
	}

}
