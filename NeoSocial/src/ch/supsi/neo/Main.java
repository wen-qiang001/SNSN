package ch.supsi.neo;

import java.io.File;

import org.apache.commons.io.FileUtils;

import ch.supsi.neo.db.GeoManager;
import ch.supsi.neo.db.InhabitantsManager;
import ch.supsi.neo.db.SocialManager;

public class Main 
{	
	public static int totalInhabitants  = 8039060;
	
	public static boolean startStopService = false;
	public static boolean removePrevious   = false;
	public static String neoBin    = "/Users/galliva/Desktop/social/neo4j-community-2.0.0-RC1/bin/neo4j";
	//public static String dbPath    = "/Users/galliva/Desktop/social/social_graph.db";
	//public static String filesPath = "/Users/galliva/Desktop/social/data/";
	public static String dbPath    = "C:\\Users\\Install\\Desktop\\social_graph.db";
	public static String filesPath = "C:\\Users\\Install\\Desktop\\data\\";	
	
	public static String geoFile         = "geo.csv";
	public static String lastnamesFile   = "lastnames.csv";
	public static String femaleNamesFile = "names_F.csv";
	public static String maleNamesFile   = "names_M.csv";

	public static void main(String[] args) 
	{		
		try 
		{
			if(startStopService)
				Runtime.getRuntime().exec(neoBin + " stop").waitFor();
			
			if(removePrevious)
				FileUtils.deleteDirectory(new File(dbPath));
						
			GeoManager g = new GeoManager();
			InhabitantsManager i = new InhabitantsManager();
			SocialManager s = new SocialManager();
			
			s.addFriends(i.addInhabitants(g.addGeographicalInfo()));
			
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
