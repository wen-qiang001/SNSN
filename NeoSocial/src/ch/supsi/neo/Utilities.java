package ch.supsi.neo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Utilities 
{
	
	public static String[][] getNames()
	{
		String path1 = Main.filesPath + Main.femaleNamesFile;
		String path2 = Main.filesPath + Main.maleNamesFile;
		String path3 = Main.filesPath + Main.lastnamesFile;
		
		String[][] res = {getData(path1), getData(path2), getData(path3)};
				
		return res;
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
