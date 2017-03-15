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

package nz.ac.vuw.ecs.kcassell.persistence;

/**
 * A data container for a measurement on some Java element.
 * @author kcassell
 *
 */
public class SoftwareMeasurement {
	
	/** Constant for Conceptual Cohesion of a Class (C3)
	 * using LSA. */
	public final static String C3 = "C3";
	
	/** Constant for Conceptual Cohesion of a Class (C3)
	 * using a VectorSpaceModel. */
	public final static String C3V = "C3V";
	
	/** The Eclipse handle for a java element */
	private String handle;
	
	/** The metric ID used in the database.  Many of these can be found in
	 * net.sourceforge.metrics.core.Constants */
	private String metricId;
	
	/** The measurement value. */
	private Double measurement;
	
	/** The foreign key into the preferences table. */
	private Integer prefKey;

	
	public SoftwareMeasurement(String handle, String metricId,
			Double measurement, Integer prefKey) {
		super();
		this.handle = handle;
		this.metricId = metricId;
		this.measurement = measurement;
		this.prefKey = prefKey;
	}

	public String getHandle() {
		return handle;
	}

	public String getMetricId() {
		return metricId;
	}

	public Double getMeasurement() {
		return measurement;
	}

	public Integer getPrefKey() {
		return prefKey;
	}
	
	

}
