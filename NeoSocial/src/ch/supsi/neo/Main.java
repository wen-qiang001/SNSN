package ch.supsi.neo;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class Main 
{
	public static String neoBin    = "/Users/galliva/Desktop/social/neo4j-community-2.0.0-M06/bin/neo4j";
	public static String dbPath    = "/Users/galliva/Desktop/social/social_graph.db";
	public static String filesPath = "/Users/galliva/Desktop/social/data/";
	
	public static String maleNamesFile   = "swiss_names_M.csv";
	public static String femaleNamesFile = "swiss_names_F.csv";
	public static String lastnamesFile   = "swiss_lastnames.csv";
	
	public static String municipalitiesIndex = "municipalities";
	public static String inhabitantsIndex    = "inhabitants";
	
	public static int totalInhabitants  = 8039060;
	public static int transactionsSize  = 1000;
	public static int maxMunicipalities = 9999;
	
	public static void main(String[] args) 
	{		
		try 
		{
			Process p = Runtime.getRuntime().exec(neoBin + " stop");
			p.waitFor();
			
			File f = new File(dbPath);
			FileUtils.deleteDirectory(f);
			
			Creator creator = new Creator();
			creator.openOrCreateDB();
			creator.addGeographicalInfo();
			creator.addInhabitants();
			creator.addFriends();
			creator.printStats();
			creator.closeDB();
												
			p = Runtime.getRuntime().exec(neoBin + " start-no-wait");
			p.waitFor();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Error: " + e);
		}

		System.out.println("Done!");
	}
}
