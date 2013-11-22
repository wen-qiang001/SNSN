package ch.supsi.snsn.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class GeographicalInserter 
{
	private BatchInserter inserter;
	private BatchInserterIndexProvider indexProvider;
	private BatchInserterIndex geoIndex;

	private Label cantonLabel = DynamicLabel.label("Canton");
	private Label regionLabel = DynamicLabel.label("Region");
	private Label municiLabel = DynamicLabel.label("Municipality");

	private RelationshipType isIn = DynamicRelationshipType.withName("IS_IN");

	private long switzerland;	
	private int cantonsAdded = 0;
	private int regionsAdded = 0;
	private HashMap<Long, Integer> municipalities = new HashMap<Long, Integer>();

	private long lStartTime;
	private long lEndTime;

	/**
	 * Adds Switzerland, cantons, regions and municipalities.
	 */
	public HashMap<Long, Integer> addGeographicalInfo()
	{		
		lStartTime = System.currentTimeMillis();
		
		System.out.println("Adding geographical information...");
		
		inserter = BatchInserters.inserter(Main.dbPath);
		indexProvider = new LuceneBatchInserterIndexProvider(inserter);

		geoIndex = indexProvider.nodeIndex("geoIndex", MapUtil.stringMap("type", "exact"));
		geoIndex.setCacheCapacity("name", 3000);

		addSwitzerland();
		addSwissGeoNodes();
		
		indexProvider.shutdown();
		inserter.shutdown();

		lEndTime = System.currentTimeMillis();

		System.out.println(String.format("Added %d cantons,  %d regions, %d municipalities in %.2f seconds", cantonsAdded, regionsAdded, municipalities.size(), (lEndTime - lStartTime)/1000.0));
	
		return municipalities;
	}

	/**
	 * Adds Switzerland as main node.
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
	private void addSwissGeoNodes()
	{
		List<String[]> m = Utilities.splitFile(Main.filesPath + Main.geoFile);

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
	}
}
