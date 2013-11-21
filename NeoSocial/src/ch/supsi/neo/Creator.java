package ch.supsi.neo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

public class Creator 
{	
	private BatchInserter inserter;
	private BatchInserterIndexProvider indexProvider;
	private BatchInserterIndex geoIndex;
	private BatchInserterIndex peopleIndex;

	private Label cantonLabel = DynamicLabel.label("Canton");
	private Label regionLabel = DynamicLabel.label("Region");
	private Label municiLabel = DynamicLabel.label("Municipality");
	private Label personLabel = DynamicLabel.label("Person");
	private Label maleLabel   = DynamicLabel.label("Male");
	private Label femaleLabel = DynamicLabel.label("Female");

	private RelationshipType isIn     = DynamicRelationshipType.withName("IS_IN");
	private RelationshipType livesIn  = DynamicRelationshipType.withName("LIVES_IN");
	private RelationshipType friendOf = DynamicRelationshipType.withName("FRIEND_OF");

	private long switzerland;	
	private int cantonsAdded = 0;
	private int regionsAdded = 0;
	private HashMap<Long, Integer> municipalities = new HashMap<Long, Integer>();
	private int inhabitantsAdded = 0;
	private List<Long> socialInhabitants = new ArrayList<Long>();

	private Random rand = new Random();
	private String[][] names = Utilities.getNames();
	private int friendsAdded = 0;

	private long lStartTime;
	private long lEndTime;

	
	
	
	/**
	 * Adds cantons, regions and municipalities.
	 */
	public void addGeographicalInfo()
	{							
		inserter = BatchInserters.inserter(Main.dbPath);
		indexProvider = new LuceneBatchInserterIndexProvider(inserter);
		
		geoIndex = indexProvider.nodeIndex("geoIndex", MapUtil.stringMap("type", "exact"));
		geoIndex.setCacheCapacity("name", 3000);
		
		addSwitzerland();
		addGeo();
	}

	/**
	 * Adds the main node.
	 */
	private void addSwitzerland()
	{
		Map<String, Object> properties = new HashMap<>();	
		properties.put("name", "Switzerland");
		switzerland = inserter.createNode(properties);
	}

	/**
	 * Adds the additional geographic nodes.
	 */
	private void addGeo()
	{
		System.out.println("Adding geographical information...");

		lStartTime = System.currentTimeMillis();

		List<String[]> m = Utilities.splitFile(Main.filesPath + "geo.csv");

		long currentCanton = -1;
		long currentRegion = -1;

		for (String[] s : m) 
		{			
			if(s[0].startsWith("- "))
			{
				Map<String, Object> properties = new HashMap<>();
				properties.put("name", s[0].substring(2));

				currentCanton = inserter.createNode(properties, cantonLabel);

				inserter.createRelationship(currentCanton, switzerland, isIn, null );

				geoIndex.add(currentCanton, properties);

				cantonsAdded++;
			}
			if(s[0].startsWith(">> "))
			{
				Map<String, Object> properties = new HashMap<>();
				properties.put("name", s[0].substring(3));

				currentRegion = inserter.createNode(properties, regionLabel);

				if(currentCanton != -1)
					inserter.createRelationship(currentRegion, currentCanton, isIn, null );

				geoIndex.add(currentRegion, properties);

				regionsAdded++;
			}
			if(s[0].startsWith("......"))
			{
				String name = s[0].substring(11);
				int people  = Integer.valueOf(s[1].trim().replace(" ", ""));

				Map<String, Object> properties = new HashMap<>();
				properties.put("name", name);

				long currentMunicipality = inserter.createNode(properties, municiLabel);

				municipalities.put(currentMunicipality, people);

				if(currentRegion != -1)
					inserter.createRelationship(currentMunicipality, currentRegion, isIn, null );

				geoIndex.add(currentMunicipality, properties);
			}
		}

		indexProvider.shutdown();
		inserter.shutdown();

		lEndTime = System.currentTimeMillis();

		System.out.println(String.format("Added %d cantons,  %d regions, %d municipalities in %.2f seconds", cantonsAdded, regionsAdded, municipalities.size(), (lEndTime - lStartTime)/1000.0));
	}

	
	
	
	/**
	 * Adds some inhabitants to the graph.
	 * @param node
	 * @param no
	 */
	public void addInhabitants()
	{
		System.out.println("Adding inhabitants...");

		lStartTime = System.currentTimeMillis();
		
		inserter = BatchInserters.inserter(Main.dbPath);
		indexProvider = new LuceneBatchInserterIndexProvider(inserter);
		
		peopleIndex = indexProvider.nodeIndex("peopleIndex", MapUtil.stringMap("type", "exact"));
		peopleIndex.setCacheCapacity("first_name", 8000000);
		peopleIndex.setCacheCapacity("last_name", 8000000);

		inhabitantsAdded = 0;

		int i = 0;

		for (Entry<Long, Integer> entry : municipalities.entrySet())
		{
			if(i % 10 == 0)
				System.out.println(String.format("%d / %d - %.2f %% -  %d / %d municipalities", inhabitantsAdded, Main.totalInhabitants, ((float)inhabitantsAdded/(float)Main.totalInhabitants)*100f, i, municipalities.size()));

			addInhabitantsInMunicipality(entry.getKey(), entry.getValue());

			i++;
		}

		indexProvider.shutdown();
		inserter.shutdown();

		lEndTime = System.currentTimeMillis();

		System.out.println(String.format("Added %d inhabitants (social %d) in %.2f seconds", inhabitantsAdded, socialInhabitants.size(), (lEndTime - lStartTime) / 1000.0));
	}

	/**
	 * Adds the inhabitants of a particular municipality.
	 * @param municipality
	 * @param no
	 */
	private void addInhabitantsInMunicipality(long municipality, int no)
	{                                                
		for (int i = 0; i < no; i++)
		{        
			// male of female (http://www.bfs.admin.ch/bfs/portal/en/index/themen/01/02/blank/key/alter/nach_geschlecht.html)
			int maleOrFemale = rand.nextInt(1000);

			String firstName = "";
			String lastName  = "";
			boolean male;

			if(maleOrFemale < 494)
			{
				int rm = rand.nextInt(names[1].length);
				firstName = names[1][rm];
				male = true;
			}
			else
			{
				int rf = rand.nextInt(names[0].length);
				firstName = names[0][rf];
				male = false;    
			}

			int rl = rand.nextInt(names[2].length);
			lastName = names[2][rl];

			Map<String, Object> properties = new HashMap<>();
			properties.put("first_name", firstName);
			properties.put("last_name",  lastName);

			long inhabitant = inserter.createNode(properties, personLabel, (male) ? maleLabel : femaleLabel);

			inserter.createRelationship(inhabitant, municipality, livesIn, null );
			
			peopleIndex.add(inhabitant, properties);

			// social (http://en.wikipedia.org/wiki/Facebook_statistics)
			int social = rand.nextInt(1000);

			if(social < 385)
				socialInhabitants.add(inhabitant);

			inhabitantsAdded++;
		}
	}

	/**
	 * Adds some friends to the inhabitants.
	 */
	public void addFriends()
	{                
		System.out.println("Adding friends...");

		lStartTime = System.currentTimeMillis();

		friendsAdded = 0;

		int added = 0;

		for (long inhabitant : socialInhabitants) 
		{
			//  the average friend count is 90 
			double n = rand.nextGaussian()*9 + 90;

			addFriendsToInhabitant(inhabitant, (int)n);

			if(added % 500 == 0)
				System.out.println(String.format("%d / %d inhabitants - %.2f %%", added, socialInhabitants.size(), ((float)added/(float)socialInhabitants.size())*100f));

			added++;
		}

		lEndTime = System.currentTimeMillis();

		System.out.println(String.format("Added %d friends in %.2f seconds - avg. friends %.2f", friendsAdded, (lEndTime - lStartTime) / 1000.0, (float)friendsAdded/(float)socialInhabitants.size()));
	}

	private void addFriendsToInhabitant(long inhabitant, int no)
	{                
		for (int i = 0; i < no; i++)
		{                                        
			int r;

			while(true)
			{
				r = rand.nextInt(inhabitantsAdded);

				if(r!=inhabitant)
					break;
			}
			
			long friend = socialInhabitants.get(r);

			inserter.createRelationship(inhabitant, friend, friendOf, null );

			friendsAdded++;
		}
	}

	public void printStats()
	{
		String s = "";
		s += String.format("TOT. nodes: %d\n", 1+1+cantonsAdded+regionsAdded+municipalities.size()+inhabitantsAdded);
		s += String.format("social inhabitants: %d (%.2f %%)\n", socialInhabitants.size(), ((float)socialInhabitants.size()/(float)inhabitantsAdded)*100f);
		s += String.format("TOT. relations: %d", friendsAdded);

		System.out.println(s);
	}
}


