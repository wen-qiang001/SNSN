package ch.supsi.neo;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class Main 
{	
	public static int totalInhabitants  = 8039060;
	
//	public static boolean recreateFiles  = false;
//	public static String outputPath      = "/Users/galliva/Desktop/social/output/";
//	public static String outputSeparator = ";"; 
//	public static String outputGFile     = "geo.csv";
//	public static String outputIFile     = "inhabitants.csv";

	public static boolean startStopService = true;
	public static boolean removePrevious   = true;
	public static String neoBin    = "/Users/galliva/Desktop/social/neo4j-community-2.0.0-RC1/bin/neo4j";
	public static String dbPath    = "/Users/galliva/Desktop/social/social_graph.db";
	public static String filesPath = "/Users/galliva/Desktop/social/data/";
	
	public static String maleNamesFile   = "swiss_names_M.csv";
	public static String femaleNamesFile = "swiss_names_F.csv";
	public static String lastnamesFile   = "swiss_lastnames.csv";
	
	public static void main(String[] args) 
	{		
		try 
		{
			if(startStopService)
				Runtime.getRuntime().exec(neoBin + " stop").waitFor();
			
			if(removePrevious)
				FileUtils.deleteDirectory(new File(dbPath));
						
			Creator c = new Creator();
			c.addGeographicalInfo();
			c.addInhabitants();
			
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
