package ch.supsi.snsn.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import ch.supsi.snsn.Main;

public class SocialInserter 
{
	private BatchInserter inserter;
	
	private RelationshipType friendOf = DynamicRelationshipType.withName("FRIEND_OF");
	
	private int friendsAdded;
	
	private Random rand = new Random();
	
	private long lStartTime;
	private long lEndTime;
	
	private List<Long> socialInhabitants = new ArrayList<Long>();
	
	/**
	 * Adds some friends to the inhabitants.
	 */
	public void addFriends(List<Long> si)
	{            
		lStartTime = System.currentTimeMillis();
		
		System.out.println("Adding friends...");
		
		inserter = BatchInserters.inserter(Main.dbPath);

		this.socialInhabitants = si;
		
		friendsAdded = 0;

		int added = 0;

		for (long inhabitant : socialInhabitants) 
		{
			//  the average friend count is 90 
			double n = rand.nextGaussian()*9 + 90;

			addFriendsToInhabitant(inhabitant, (int)n);

			if(added % 10000 == 0)
				System.out.println(String.format("%d / %d inhabitants - %.2f %%", added, socialInhabitants.size(), ((float)added/(float)socialInhabitants.size())*100f));

			if(added > 0 && added % 10000 == 0)
			{
				System.out.println("Shutting down...");
				inserter.shutdown();
				System.out.println("Restarting...");
				inserter = BatchInserters.inserter(Main.dbPath);
			}
			
			added++;
		}

		System.out.println("Shutting down...");
		
		inserter.shutdown();
		
		lEndTime = System.currentTimeMillis();

		System.out.println(String.format("Added %d friends in %.2f seconds - avg. friends %.2f", friendsAdded, (lEndTime - lStartTime) / 1000.0, (float)friendsAdded/(float)socialInhabitants.size()));
	}

	/**
	 * Adds the friends for a particular inhabitant.
	 * @param inhabitant
	 * @param no
	 */
	private void addFriendsToInhabitant(long inhabitant, int no)
	{                
		for (int i = 0; i < no; i++)
		{                                        
			int r;

			while(true)
			{
				r = rand.nextInt(socialInhabitants.size());

				if(r!=inhabitant)
					break;
			}
			
			long friend = socialInhabitants.get(r);

			inserter.createRelationship(inhabitant, friend, friendOf, null);

			friendsAdded++;
		}
	}
}
