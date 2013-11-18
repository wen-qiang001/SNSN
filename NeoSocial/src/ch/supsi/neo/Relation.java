package ch.supsi.neo;

import org.neo4j.graphdb.RelationshipType;

public class Relation 
{
	public static enum RelTypes implements RelationshipType
	{
	    IS_IN,
	    LIVES_IN,
	    FRIEND_OF
	}
}
