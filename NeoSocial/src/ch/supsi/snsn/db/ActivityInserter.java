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
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import ch.supsi.snsn.Main;
import ch.supsi.snsn.Utilities;

public class ActivityInserter 
{
	private BatchInserter inserter;
	private BatchInserterIndexProvider indexProvider;
	private BatchInserterIndex activityIndex;
	
	private Label activityLabel = DynamicLabel.label("Activity");
	
	private RelationshipType interestedIn = DynamicRelationshipType.withName("INTERESTED_IN");
	
	private List<Long> activities = new ArrayList<Long>();
	
	private Random rand = new Random();
	
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
		indexProvider = new LuceneBatchInserterIndexProvider(inserter);

		activityIndex = indexProvider.nodeIndex("activityIndex", MapUtil.stringMap("type", "exact"));
		activityIndex.setCacheCapacity("name", 1000);

		addActivities();
		
		System.out.println("Shutting down...");
		
		indexProvider.shutdown();
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
		List<String[]> acts = Utilities.splitFile(Main.filesPath + Main.activitiesFile);
		
		for (int i = 0; i < acts.size(); i++)
		{
			Map<String, Object> properties = new HashMap<>();
			properties.put("name", acts.get(i));

			long a = inserter.createNode(properties, activityLabel);
			
			activityIndex.add(a, properties);
			
			activities.add(a);
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