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

package nz.ac.vuw.ecs.kcassell.cluster.frequentitemsets;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator<String> {

	protected Map<String, Double> supportMap;
	
	/**
	 * @param itemSupportList
	 */
	public ValueComparator(Map<String, Double> supportMap) {
		this.supportMap = supportMap;
	}

	/**
	 * We want numerically decreasing values in our list, so this reverses
	 * the usual meaning of compare for numbers.
	 */
	public int compare(String a, String b) {
		int result = 0;
		Double aValue = supportMap.get(a);
		Double bValue = supportMap.get(b);
		
		if ((aValue == null) && (bValue == null)) {
			result = 0;
		} else if (aValue != null) {
			try {
			result = -1 * aValue.compareTo(bValue);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			// If the values are the same, then compare the keys
			if (result == 0) {
				result = a.compareTo(b);
			}
		} else if (bValue != null) {
			result = -1;
		}
		return result;
	}
}