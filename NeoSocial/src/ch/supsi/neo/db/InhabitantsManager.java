package ch.supsi.neo.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

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

import ch.supsi.neo.Main;
import ch.supsi.neo.Utilities;

public class InhabitantsManager 
{	
	private BatchInserter inserter;
	private BatchInserterIndexProvider indexProvider;
	private BatchInserterIndex peopleIndex;
	
	private Label personLabel = DynamicLabel.label("Person");
	private Label maleLabel   = DynamicLabel.label("Male");
	private Label femaleLabel = DynamicLabel.label("Female");

	private RelationshipType livesIn  = DynamicRelationshipType.withName("LIVES_IN");
	
	private int inhabitantsAdded = 0;
	private List<Long> socialInhabitants = new ArrayList<Long>();
	
	private Random rand = new Random();
	private String[][] names = Utilities.getNames();

	private long lStartTime;
	private long lEndTime;
	
	private HashMap<Long, Integer> municipalities = new HashMap<Long, Integer>();
	
	/**
	 * Adds some inhabitants to the graph.
	 * @param node
	 * @param no
	 */
	public List<Long> addInhabitants(HashMap<Long, Integer> m)
	{
		System.out.println("Adding inhabitants...");

		lStartTime = System.currentTimeMillis();
		
		this.municipalities = m;
		
		inserter = BatchInserters.inserter(Main.dbPath);
		indexProvider = new LuceneBatchInserterIndexProvider(inserter);
		
		peopleIndex = indexProvider.nodeIndex("peopleIndex", MapUtil.stringMap("type", "exact"));
		peopleIndex.setCacheCapacity("first_name", 8000000);
		peopleIndex.setCacheCapacity("last_name", 8000000);

		inhabitantsAdded = 0;

		int i = 0;

		for (Entry<Long, Integer> entry : municipalities.entrySet())
		{
			if(i % 100 == 0)
				System.out.println(String.format("%d / %d - %.2f %% -  %d / %d municipalities", inhabitantsAdded, Main.totalInhabitants, ((float)inhabitantsAdded/(float)Main.totalInhabitants)*100f, i, municipalities.size()));

			addInhabitantsInMunicipality(entry.getKey(), entry.getValue());

			i++;
		}

		System.out.println("Shutting down...");
		
		indexProvider.shutdown();
		inserter.shutdown();

		lEndTime = System.currentTimeMillis();

		System.out.println(String.format("Added %d inhabitants (social %d) in %.2f seconds", inhabitantsAdded, socialInhabitants.size(), (lEndTime - lStartTime) / 1000.0));
	
		return socialInhabitants;
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
}
