package ch.supsi.neo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.Index;

public class Creator 
{	
	private GraphDatabaseService db;
	private Node switzerland;
	
	private Transaction tx;
	private Index<Node> municipalitiesIndex;	
	private Index<Node> inhabitantsIndex;	
	
	private Random rand = new Random();
	private String[][] names = Utilities.getNames();
	
	private int cantonsAdded = 0;
	private int regionsAdded = 0;
	private HashMap<String, Integer> municipalities = new HashMap<String, Integer>();
	private int inhabitantsAdded = 0;
	private List<Integer> socialInhabitants = new ArrayList<Integer>();
	private int friendsAdded = 0;
	
	private long lStartTime;
	private long lEndTime;
	
	/**
	 * Open or creates the DB.
	 */
	@SuppressWarnings("deprecation")
	public void openOrCreateDB()
	{
		db = new GraphDatabaseFactory().
				newEmbeddedDatabaseBuilder(Main.dbPath)
				.setConfig(GraphDatabaseSettings.node_keys_indexable, "name")
				//.setConfig(GraphDatabaseSettings.relationship_keys_indexable, "IS_IN, LIVES_IN, FRIEND_OF")
				.setConfig(GraphDatabaseSettings.node_auto_indexing, "true")
				//.setConfig(GraphDatabaseSettings.relationship_auto_indexing, "true")
				.newGraphDatabase();		

		registerShutdownHook(db);		
		
		tx = db.beginTx();
		municipalitiesIndex = db.index().forNodes(Main.municipalitiesIndex);
		inhabitantsIndex    = db.index().forNodes(Main.inhabitantsIndex);
		tx.success();
		tx.finish();
	}
	
	/**
	 * Closes the DB.
	 */
	public void closeDB()
	{
		db.shutdown();	
		System.gc();
	}
	
	/**
	 * Adds cantons, regions and municipalities.
	 */
	public void addGeographicalInfo()
	{	
		tx = db.beginTx();
		
		try
		{						
			addSwitzerland();
			addGeo();
			
			tx.success();
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
			tx.failure();
		}
		finally
		{
			tx.close();
		}
	}

	private void addSwitzerland()
	{
		switzerland = db.createNode();
		switzerland.setProperty("name", "Switzerland");
	}

	private void addGeo()
	{
		System.out.println("Adding geographical information...");
		
		lStartTime = System.currentTimeMillis();
		
		List<String[]> m = Utilities.splitFile(Main.filesPath + "geo.csv");
		
		Node currentCanton = null;
		Node currentRegion = null;
		
		for (String[] s : m) 
		{			
			if(s[0].startsWith("- "))
			{
				Node n = db.createNode();
				n.setProperty("name",  s[0].substring(2));

				n.createRelationshipTo(switzerland, Relation.RelTypes.IS_IN);

				currentCanton = n;
				
				cantonsAdded++;
			}

			if(s[0].startsWith(">> "))
			{
				Node n = db.createNode();
				n.setProperty("name" , s[0].substring(3));

				if(currentCanton != null)
					n.createRelationshipTo(currentCanton, Relation.RelTypes.IS_IN);

				currentRegion = n;
				
				regionsAdded++;
			}

			if(s[0].startsWith("......"))
			{
				String id   = s[0].substring(6, 10);
				String name = s[0].substring(11);
				int people  = Integer.valueOf(s[1].trim().replace(" ", ""));
				
				municipalities.put(id, people);
								
				Node n = db.createNode();
				n.setProperty("name",  name);

				municipalitiesIndex.add(n, "id", id);
				
				if(currentRegion != null)
					n.createRelationshipTo(currentRegion, Relation.RelTypes.IS_IN);
				
				if(municipalities.size() >= Main.maxMunicipalities)
					break;
			}
		}
		
		lEndTime = System.currentTimeMillis();
		
		System.out.println(String.format("Added %d cantons,  %d regions, %d municipalities in %.2f seconds", cantonsAdded, regionsAdded, municipalities.size(), (lEndTime - lStartTime)/1000.0));
	}

	/**
	 * Adds some inhabitants to the municipalities.
	 * @param node
	 * @param no
	 */
	public void addInhabitants()
	{
		System.out.println("Adding inhabitants...");
		
		lStartTime = System.currentTimeMillis();
		
		inhabitantsAdded = 0;
				
		int i = 0;
		
		for (Entry<String, Integer> entry : municipalities.entrySet())
		{
			if(i % 10 == 0)
				System.out.println(String.format("%d / %d - %.2f %% -  %d / %d municipalities", inhabitantsAdded, Main.totalInhabitants, ((float)inhabitantsAdded/(float)Main.totalInhabitants)*100f, i, municipalities.size()));
			
			addInhabitantsInMunicipality(entry.getKey(), entry.getValue());
									
			i++;
		}
		
		lEndTime = System.currentTimeMillis();
		
		System.out.println(String.format("Added %d inhabitants (social %d) in %.2f seconds", inhabitantsAdded, socialInhabitants.size(), (lEndTime - lStartTime) / 1000.0));
	}
	
	private void addInhabitantsInMunicipality(String municipality, int no)
	{						
		try
		{		
			tx = db.beginTx();
			Node found = municipalitiesIndex.get("id", municipality).getSingle();
			tx.success();
			tx.close();
			
			tx = db.beginTx();
			
			if(found != null)
			{				
				for (int i = 0; i < no; i++)
				{					
					Node person = db.createNode();
				
					String personId = String.format("p%d", ++inhabitantsAdded);
					
					// male of female (http://www.bfs.admin.ch/bfs/portal/en/index/themen/01/02/blank/key/alter/nach_geschlecht.html)
					int maleOrFemale = rand.nextInt(1000);
					
					String name = "";

					if(maleOrFemale < 494)
					{
						int rm = rand.nextInt(names[1].length);
						name = names[1][rm];
						person.setProperty("gender",  "M");
					}
					else
					{
						int rf = rand.nextInt(names[0].length);
						name = names[0][rf];
						person.setProperty("gender",  "F");
					}

					int rl = rand.nextInt(names[2].length);
					
					name = name + " " + names[2][rl];
					
					person.setProperty("name",  name);
					
					// social (http://en.wikipedia.org/wiki/Facebook_statistics)
					int social = rand.nextInt(1000);
					
					if(social < 385)
					{
						person.setProperty("social",  "Y");
						socialInhabitants.add(inhabitantsAdded);
					}	
					else
						person.setProperty("social",  "N");
						
					inhabitantsIndex.add(person, "id", personId);
					person.createRelationshipTo(found, Relation.RelTypes.LIVES_IN);
					
					if(i > 0 && i % Main.transactionsSize == 0)
					{
						tx.success();
						tx.close();
						tx = db.beginTx();
					}
				}	
				
				found = null;
			}
			
			tx.success();
			tx.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println(e.toString());
			tx.failure();
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
		
		for (int inhabitant : socialInhabitants) 
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
	
	private void addFriendsToInhabitant(int inhabitant, int no)
	{		
		try
		{					
			String personId = String.format("p%d", inhabitant);
			
			tx = db.beginTx();
			
			Node found = inhabitantsIndex.get("id", personId).getSingle();
						
			if(found != null)
			{
				Node friend;
				
				for (int i = 0; i < no; i++)
				{					
					int r;
					
					while(true)
					{
						r = rand.nextInt(inhabitantsAdded) + 1;
						
						if(r!=inhabitant)
							break;
					}
										
					friend = inhabitantsIndex.get("id", "p" + r).next();
					
					if(friend != null)
					{
						found.createRelationshipTo(friend, Relation.RelTypes.FRIEND_OF);
						friendsAdded++;
					}
				}
			}
			
			tx.success();
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
			tx.failure();
		}
		finally
		{
			tx.close();
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
	
	private static void registerShutdownHook(final GraphDatabaseService graphDb)
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				graphDb.shutdown();
			}
		});
	}
}
