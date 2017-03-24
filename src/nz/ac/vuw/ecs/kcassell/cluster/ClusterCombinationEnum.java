package nz.ac.vuw.ecs.kcassell.cluster;

/** Values to indicate how groups/clusters shoud be combined.
 * @see Jain, Murphy, and Flynn, "Data clustering: a review",
 * ACM Computing Surveys, 1999	 */
public enum ClusterCombinationEnum {
	AVERAGE_LINK,
	COMPLETE_LINK,
	SINGLE_LINK
}
