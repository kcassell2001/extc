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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.util.SettableTransformer;
import edu.uci.ics.jung.io.graphml.EdgeMetadata;
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;

public class CallGraphLink implements Comparable<CallGraphLink> {
	private static final String GRAPHML_SCORE = "score";
	private static final String GRAPHML_LABEL = "label";

	/** A collection of various weights. */
	protected HashMap<String, Number> weights = new HashMap<String, Number>();

//	private double weight;
	protected static int generatedId = 0;
	protected int id = generatedId++;

	/** Controls whether the toString method produces a nonempty string. */
	protected boolean showToString = true;
	
	private String label;

	/** The type of weight to use as part of a node label. */
	protected String weightTypeForLabel = ScoreType.BASIC;

	protected CallGraphLink() {
	}

	public Number getWeight() {
		return getWeight(weightTypeForLabel);
	}

	public void setWeight(Number weight) {
		setWeight(weightTypeForLabel, weight);
	}

	public Number getWeight(String String) {
		Number weightN = weights.get(String);
		return weightN;
	}

	public void setWeight(String String, Number weight) {
		weights.put(String, weight);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the weightTypeForLabel
	 */
	public String getWeightTypeForLabel() {
		return weightTypeForLabel;
	}

	/**
	 * @param weightTypeForLabel the weightTypeForLabel to set
	 */
	public void setWeightTypeForLabel(String weightTypeForLabel) {
		this.weightTypeForLabel = weightTypeForLabel;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();

		if (showToString) {
			Number weightN = getWeight(weightTypeForLabel);
			Double weight = (weightN == null) ? null : weightN.doubleValue();
			if ((weight != null) && !Double.isNaN(weight)) {
				buf.append(String.format("%.1f", weight));
			}
		}
		return buf.toString();
	}

	public static class CallGraphLinkFactory implements Factory<CallGraphLink> {
		private static int id = 0;

		public CallGraphLink create() {
			CallGraphLink link = new CallGraphLink();
			link.setId(id);
			link.setLabel("CGLink" + id++);
			return link;
		}
	} // CalGraphLinkFactory

	/**
	 * This class is used for reading GraphML
	 * 
	 * @author kcassell
	 */
	public static class EdgeMetadataTransformer implements
			Transformer<EdgeMetadata, CallGraphLink> {
		int n = 100;

		public CallGraphLink transform(EdgeMetadata metadata) {
			CallGraphLink link = new CallGraphLink();
			Map<String, String> properties = metadata.getProperties();
			String label = properties.get(GRAPHML_LABEL);
			link.setLabel((label != null) ? label : ("CGLink" + n++));
			String sScore = properties.get(GRAPHML_SCORE);
			double weight = 0.0;
			try {
				weight = Double.parseDouble(sScore);
			} catch (Exception e) {
			}
			link.setWeight(weight);
			return link;
		}
	} // EdgeMetadataTransformer

	/**
	 * This class is used for reading GraphML
	 * 
	 * @author kcassell
	 */
	public static class HyperEdgeMetadataTransformer implements
			Transformer<HyperEdgeMetadata, CallGraphLink> {
		int n = 0;

		public CallGraphLink transform(HyperEdgeMetadata metadata) {
			CallGraphLink link = new CallGraphLink();
			Map<String, String> properties = metadata.getProperties();
			String label = properties.get(GRAPHML_LABEL);
			link.setLabel((label != null) ? label : ("CGLink" + n++));
			String sScore = properties.get(GRAPHML_SCORE);
			double weight = 0.0;
			try {
				weight = Double.parseDouble(sScore);
			} catch (Exception e) {
			}
			link.setWeight(weight);
			return link;
		}
	} // HyperEdgeMetadataTransformer

	/**
	 * Emits the weight of the link. Used by PajekNetWriter
	 * 
	 * @author kcassell
	 */
	public static class LinkWeightTransformer implements
			Transformer<CallGraphLink, Number> {
		/** Returns the link's weight. */
		public Number transform(CallGraphLink link) {
			Number weight = link.getWeight();
			return weight;
		}
	} // LinkWeightTransformer

	/**
	 * Emits the weight of the link. Used by PajekNetWriter
	 * 
	 * @author kcassell
	 */
	public static class LinkWeightTransformerDouble implements
			Transformer<CallGraphLink, Double> {
		/** Returns the link's weight. */
		public Double transform(CallGraphLink link) {
			Number weightN = link.getWeight();
			Double weight = (weightN == null)? null : weightN.doubleValue();
			return weight;
		}
	} // LinkWeightTransformer

	/**
	 * Establishes the weight of the link. Used by PajekNetReader
	 * 
	 * @author kcassell
	 */
	public static class SettableLinkWeightTransformer implements
			SettableTransformer<CallGraphLink, Number> {
		/** Returns the link's weight. */
		public Number transform(CallGraphLink link) {
			Number weight = link.getWeight();
			return weight;
		}

		/** Returns the link's weight. */
		public void set(CallGraphLink link, Number num) {
			double weight = num.doubleValue();
			link.setWeight(weight);
		}
	} // LinkWeightTransformer

	
	/**
	 * Compares two links based on their weights. 
	 */
	public int compareTo(CallGraphLink other) {
		int result = 0;

		if (other == null) {
			result = 1;
		}
		else {
			Number weight = getWeight(weightTypeForLabel);
			Number otherWeight = other.getWeight(weightTypeForLabel);
			
			if ((weight == null) && (otherWeight == null)) {
				result = id - other.getId();
			}
			else if (weight == null) {
				result = -1;
			} else {
				Double weightD = weight.doubleValue();
				
				if (otherWeight == null) {
					result = 1;
				}
				else {
					Double otherWeightD = otherWeight.doubleValue();
					result = weightD.compareTo(otherWeightD);
				}
			}
		}
		return result;
	}

}