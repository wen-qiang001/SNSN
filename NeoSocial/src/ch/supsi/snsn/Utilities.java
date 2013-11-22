package ch.supsi.snsn;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Utilities 
{
	@SuppressWarnings({ "unchecked", "resource" })
	public static List<Long> loadSocialInhabitants()
	{
		List<Long> socialInhabitants = null;
		
		try 
		{
            FileInputStream fileIn = new FileInputStream(Main.outputPath  + Main.snOutputFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            socialInhabitants = (List<Long>)in.readObject(); 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return socialInhabitants;
	}
	
	public static String[][] getNames()
	{
		String path1 = Main.filesPath + Main.femaleNamesFile;
		String path2 = Main.filesPath + Main.maleNamesFile;
		String path3 = Main.filesPath + Main.lastnamesFile;
		
		String[][] res = {getData(path1), getData(path2), getData(path3)};
				
		return res;
	}
	
	public static String[] getInterests()
	{
		throw new NotImplementedException();
	}
	
	private static String[] getData(String path)
	{
		ArrayList<String> result = new ArrayList<String>();
				
		for (String[] s : splitFile(path)) 
			result.add(s[0]);
		
		return result.toArray(new String[0]);
	}
	
	public static List<String[]> splitFile(String fileName)
	{
		List<String[]> result = new ArrayList<String[]>();

		BufferedReader br;

		try
		{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "ISO-8859-1"));
			
			String line;
			while ((line = br.readLine()) != null)
			{
				String[] splitted = line.split(";");
				result.add(splitted);
			}

			br.close();
		}
		catch (FileNotFoundException e)
		{
		}
		catch (IOException e)
		{
		}

		return result;
	}
}
