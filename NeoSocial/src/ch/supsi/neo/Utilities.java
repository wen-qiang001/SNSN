package ch.supsi.neo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utilities 
{
	
	public static String[][] getNames()
	{
		String path = Main.filesPath + "names.csv";
		List<String[]> splitted = splitFile(path);
		
		String[] femaleNames = new String[1000];
		String[] maleNames   = new String[1000];
		
		int i = 0;
		for (String[] s : splitted) 
		{
			femaleNames[i] = s[1];
			maleNames[i] = s[3];
			i++;
		}
		
		
		path = Main.filesPath + "lastnames.csv";		
		splitted = splitFile(path);
		
		String[] lastNames   = new String[1000];
		
		i = 0;
		for (String[] s : splitted) 
		{
			lastNames[i] = s[0];
			i++;
		}
		
		String[][] res = {femaleNames, maleNames, lastNames};
				
		return res;
	}
	
	public static List<String[]> splitFile(String fileName)
	{
		List<String[]> result = new ArrayList<String[]>();

		BufferedReader br;

		try
		{
			br = new BufferedReader(new FileReader(fileName));

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
