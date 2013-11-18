package ch.supsi.neo;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class Main 
{
	public static String neoBin    = "/Users/galliva/Desktop/social/neo4j-community-2.0.0-M06/bin/neo4j";
	public static String dbPath    = "/Users/galliva/Desktop/social/social_graph.db";
	public static String filesPath = "/Users/galliva/Desktop/social/data/";
	
	public static String municipalitiesIndex = "municipalities";
	public static String inhabitantsIndex    = "inhabitants";
	
	public static int maxMunicipalities = 20;
	
	public static void main(String[] args) 
	{	
		try 
		{
			Process p = Runtime.getRuntime().exec(neoBin + " stop");
			p.waitFor();
			
			File f = new File(dbPath);
			FileUtils.deleteDirectory(f);
			
			Creator creator = new Creator();
			creator.addGeographicalInfo();
			creator.addInhabitants();
			creator.addFriends();
			creator.printStats();
												
			p = Runtime.getRuntime().exec(neoBin + " start-no-wait");
			p.waitFor();
		} 
		catch (Exception e) 
		{
			System.out.println("Error: " + e.toString());
		}

		System.out.println("Done!");
	}
}
