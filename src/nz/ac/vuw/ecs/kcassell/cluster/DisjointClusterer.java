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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import nz.ac.vuw.ecs.kcassell.similarity.DistanceMatrix;
import nz.ac.vuw.ecs.kcassell.utils.EclipseUtils;
import nz.ac.vuw.ecs.kcassell.utils.RefactoringConstants;

/**
 * This clusterer adds objects to one or the other of
 * two seed clusters.
 * @author Keith
 */
public class DisjointClusterer extends MatrixBasedAgglomerativeClusterer {

	/** One of the two major groups. */
	protected MemberCluster cluster1 = null;
	protected MemberCluster seedCluster1 = null;

	/** One of the two major groups. */
	protected MemberCluster cluster2 = null;
	protected MemberCluster seedCluster2 = null;
	
    /**
     * Given a list of existing clusters and a calculator to calculate the
     * distances between them, initialize the clusterer by building
     * the distance matrix.
     * @param clusters a collection of clusters to be further clustered
     * @param classHandle the Eclipse handle for the containing class
     */
	public DisjointClusterer(
			MemberCluster seed1,
			MemberCluster seed2,
			List<MemberCluster> clusters,
			String classHandle) {
		this.cluster1 = seed1;
		seedCluster1 = seed1;
		this.cluster2 = seed2;
		seedCluster2 = seed2;
		try {
			distanceCalculator = setUpSpecifiedCalculator(classHandle);
			List<String> memberHandles =
				EclipseUtils.getFilteredMemberHandles(classHandle);
			originalMatrix = new DistanceMatrix<String>(memberHandles);
			originalMatrix.fillMatrix(distanceCalculator);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			distanceCalculator = setUpLevenshteinClustering();
		}

		ArrayList<String> clusterNames = new ArrayList<String>();

		// Store information about cluster composition
		for (MemberCluster mCluster : clusters) {
			String clusterName = mCluster.getClusterName();
			clusterNames.add(clusterName);
			clusterHistory.put(clusterName, mCluster);
			
			// set up individuals in the clusterHistory
			Set<String> memberNames = mCluster.getElements();
			for (String name : memberNames) {
				clusterHistory.put(name, null);
			}
		}
		buildDistanceMatrix(clusterNames);
		
		separateSeeds();
		logger.info(distanceMatrix.toString());
	}

	public MemberCluster getCluster1() {
		return cluster1;
	}

	public MemberCluster getCluster2() {
		return cluster2;
	}

	/**
	 * Make seed1 and seed2 hard to merge
	 */
	protected void separateSeeds() {
		String seed1Name = this.cluster1.getClusterName();
		String seed2Name = this.cluster2.getClusterName();
		distanceMatrix.setDistance(seed1Name, seed2Name,
				RefactoringConstants.MAX_DISTANCE);
	}

    /**
     * Form clusters by combining nodes.  Two objects should be combined
     * for each iteration until two clusters remain.
     * @return the identifiers for the two clusters
     */
	public Collection<String> cluster() {
		while (continueClustering()) {
			clusterOnce();
			previousIteration++;
		}
		return getClusters();
	}

	/**
	 * Revises the distanceMatrix after a clustering step by removing the rows
	 * and columns for the elements that were merged, and creating a row for
	 * the newly formed cluster
	 * @param cluster
	 * @return
	 */
	protected DistanceMatrix<String> modifyMatrix(MemberCluster cluster) {
		super.modifyMatrix(cluster);
		separateSeeds();
		return distanceMatrix;
	}


	/**
	 * Keep clustering until only two clusters remain.
	 */
	protected boolean continueClustering() {
		List<String> headers = distanceMatrix.getHeaders();
		return headers.size() > 2;
	}
	
	/**
	 * Creates a cluster from two identifiers (each of which may represent
	 * one or more elements).  Resets seed1 or seed2 if necessary.
	 * @param neighbors the two objects
	 * @return the new cluster
	 */
	protected MemberCluster createCluster(Distance<String> neighbors) {
		MemberCluster cluster = new MemberCluster();
		String near1 = neighbors.getFirst();
		String near2 = neighbors.getSecond();
		logger.info("createCluster from " + near1 + ", " + near2);
		addChildToCluster(cluster, near1);
		addChildToCluster(cluster, near2);
		Number distance = neighbors.getDistance();
		cluster.setDistance(distance.doubleValue());
		String comment = "dist. = " + distance;
		cluster.setComment(comment);
		
		// Name the new cluster based on the seed name, if possible
		String seed1Name = cluster1.getClusterName();
		String seed2Name = cluster2.getClusterName();
		String nameBase =
			(seed1Name.equals(near1) || seed2Name.equals(near1)) ? near1 : near2;
		String clusterName = nameCluster(cluster, nameBase);
		clusterHistory.put(clusterName, cluster);
		
		// Reset a seed cluster if necessary
		if (seed1Name.equals(near1) || seed1Name.equals(near2)) {
			cluster1 = cluster;
		} else if (seed2Name.equals(near1) || seed2Name.equals(near2)) {
			cluster2 = cluster;
		}
		return cluster;
	}


}
