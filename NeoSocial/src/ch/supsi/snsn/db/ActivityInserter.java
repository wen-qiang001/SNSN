package ch.supsi.snsn.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import ch.supsi.snsn.Main;
import ch.supsi.snsn.Utilities;

public class ActivityInserter 
{
	private BatchInserter inserter;
	
	private Label activityLabel = DynamicLabel.label("Activity");
	
	private RelationshipType interestedIn = DynamicRelationshipType.withName("INTERESTED_IN");
	
	private List<Long> activities = new ArrayList<Long>();
	
	private Random rand = new Random();
	private String[] activitiesList = Utilities.getInterests();
	
	private long lStartTime;
	private long lEndTime;
	
	private List<Long> socialInhabitants = new ArrayList<Long>();
	
	/**
	 * Adds the activities and relates them as interests to the inhabitants.
	 * @param si
	 */
	public void addAndRelateActivities(List<Long> si)
	{
		lStartTime = System.currentTimeMillis();
		
		System.out.println("Adding activities...");
		
		inserter = BatchInserters.inserter(Main.dbPath);

		addActivities();
		
		System.out.println("Shutting down...");
		
		inserter.shutdown();
		
		
		System.out.println("Relating activities to the inhabitants...");
		
		inserter = BatchInserters.inserter(Main.dbPath);
		
		this.socialInhabitants = si;
		
		for (long inhabitant : socialInhabitants) 
			addInterests(inhabitant);
		
		System.out.println("Shutting down...");
		
		inserter.shutdown();
		
		lEndTime = System.currentTimeMillis();

		System.out.println(String.format("Added %d activities in %.2f seconds", activities, (lEndTime - lStartTime) / 1000.0));
	}
	
	/**
	 * Adds the activities.
	 */
	private void addActivities()
	{
		for (int i = 0; i < activitiesList.length; i++)
		{
			Map<String, Object> properties = new HashMap<>();
			properties.put("name", activitiesList[i]);

			activities.add(inserter.createNode(properties, activityLabel));
		}
	}
	
	/**
	 * Relates each social inhabitant to some activities.
	 * @param inhabitant
	 */
	private void addInterests(long inhabitant)
	{
		//  the average interests count is 15 
		int no = (int) (rand.nextGaussian()*5) + 15;
		
		for (int i = 0; i < no; i++) 
		{
			int r = rand.nextInt(activities.size());
			
			long interest = activities.get(r);
			
			inserter.createRelationship(inhabitant, interest, interestedIn, null);
		}
	}
}