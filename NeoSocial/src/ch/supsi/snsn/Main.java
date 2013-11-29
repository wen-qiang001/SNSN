package ch.supsi.snsn;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

import ch.supsi.snsn.db.GeographicalInserter;
import ch.supsi.snsn.db.ResidentialInserter;
import ch.supsi.snsn.db.ActivityInserter;
import ch.supsi.snsn.db.SocialInserter;

public class Main 
{	
	public static int totalInhabitants      = 8039060;
	
	private static boolean startStopService = false;
	private static boolean removePrevious   = false;
	private static boolean addInhabitants   = true;
	private static boolean addFriends  	    = true;
	private static boolean addActivities    = true;
	
//	public static String neoBin     = "/Users/galliva/Desktop/SNSN/neo4j-community-2.0.0-RC1/bin/neo4j";
//	public static String dbPath     = "/Users/galliva/Desktop/SNSN/social_graph.db";
//	public static String filesPath  = "/Users/galliva/Desktop/SNSN/data/";
//	public static String outputPath = "/Users/galliva/Desktop/SNSN/output/";
	public static String neoBin     = "/home/azureuser/neo4j-community-2.0.0-RC1/bin/neo4j";
	public static String dbPath     = "/home/azureuser/social_graph.db";
	public static String filesPath  = "/home/azureuser/data/";
	public static String outputPath = "/home/azureuser/output/";
	
	public static String activitiesFile  = "activities.csv";
	public static String geoFile         = "geo_subset.csv";
	public static String lastnamesFile   = "lastnames.csv";
	public static String femaleNamesFile = "names_F.csv";
	public static String maleNamesFile   = "names_M.csv";
	
	public static String snOutputFile    = "sn.out";

	public static void main(String[] args) 
	{		
		GeographicalInserter g = new GeographicalInserter();
		ResidentialInserter i = new ResidentialInserter();
		SocialInserter s = new SocialInserter();
		ActivityInserter in = new ActivityInserter();
		
		try 
		{
			if(startStopService)
				Runtime.getRuntime().exec(neoBin + " stop").waitFor();
			
			if(removePrevious)
				FileUtils.deleteDirectory(new File(dbPath));
		
			List<Long> socialInhabitants = (addInhabitants) ? i.addInhabitants(g.addGeographicalInfo()) : Utilities.loadSocialInhabitants();
					
			if(addFriends)
				s.addFriends(socialInhabitants);
			
			if(addActivities)
				in.addAndRelateActivities(socialInhabitants);
						
			if(startStopService)
				Runtime.getRuntime().exec(neoBin + " start-no-wait").waitFor();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Error: " + e);
		}

		System.out.println("Done!");
	}
}
