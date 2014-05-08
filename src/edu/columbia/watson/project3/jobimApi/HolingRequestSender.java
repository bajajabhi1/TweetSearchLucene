package edu.columbia.watson.project3.jobimApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.columbia.watson.project3.QueryBean;
import edu.columbia.watson.project3.TrecTopicParser;

public class HolingRequestSender
{
	public static final String BASE_URL = "http://maggie.lt.informatik.tu-darmstadt.de:10080/jobim/ws/holing?s=";
	public static final String SEPARATOR = " ";
	public static final String QUERY_FILE = "E:/Watson-Project-Data/2012/2012.topics.MB51-110.xml";
	public static final String OUTPUT_HOLING_DIR = "E:/Watson-Project-Data/2012/API-HOLING";

	public static void main(String args[])
	{
		TrecTopicParser topicParser = new TrecTopicParser();
		List<QueryBean> queryList = topicParser.parseTrecTopics(QUERY_FILE);

		for(QueryBean queryBean : queryList)
		{
			//System.out.println(queryBean.getQuery().trim());
			//Map<String,String> map = getPOS(queryBean.getQuery().trim(), queryBean.getQueryNum());
			System.out.println("Query == " + queryBean.getQuery());
			Map<String,String> queryPosMap = getPOS(queryBean);
			//Map<String,String> map = getPOS(input, "NOTEST");
			for(String queryPart : queryPosMap.keySet())
			{
				System.out.println(queryPart + " => " + queryPosMap.get(queryPart));
				//SimilarTermsRequestSender.getSimilarWords(queryBean.getQueryNum(), queryPosMap.get(queryPart));
				SimilarTermsRequestSender.createSimilarTermsOutputFile(queryBean, queryPosMap);
			}
			System.out.println("=====================");
		}
	}

	/**
	 * This method load the local json file and parses and get the unique "key" field having the POS
	 * @param queryId
	 * @return the set of pos tags
	 */
	public static Map<String,String> getPOS(QueryBean queryBean)
	{
		List<String> posList = parseJson(queryBean.getQueryNum(), loadLocalFile(queryBean.getQueryNum()));
		for(String pos:posList)
		{
			System.out.println("POS - " + pos);
		}
		Set<String> posListNotFound = new HashSet<String>();
		Set<String> queryPartNotFound = new HashSet<String>();
		String inputParts[] = queryBean.getQuery().trim().toLowerCase().split(SEPARATOR);
		Map<String,String> hollingMap = new HashMap<String,String>();
		for(String part : inputParts)
		{
			boolean found = false;
			for(String pos : posList)
			{
				String pos_q[] = pos.split("#");
				if(!pos_q[0].equals("") && !pos_q[0].equals("\"") && !pos_q[0].equals("\'"))
				{
				if(part.equalsIgnoreCase(pos_q[0]) || part.indexOf(pos_q[0])>=0 || part.indexOf(pos_q[0].substring(0,pos_q[0].length()-1))>=0)
				{
					hollingMap.put(pos_q[0], part);
					found = true;
					break;
				}
				/*if(part.indexOf(pos_q[0].substring(0,pos_q[0].length()-1))>=0)
				{
					hollingMap.put(part, pos);
					found = true;
					break;
				}*/
				}
				queryPartNotFound.add(part);
			}
			if(!found)
			{
				queryPartNotFound.add(part);
				System.out.println("Query part not found - " + part);
			}
		}
		if(!queryPartNotFound.isEmpty())
		{for(String pos : posList)
		{
			boolean found = false;
			for(String part : inputParts)
			{
				String pos_q[] = pos.split("#");
				if(part.equalsIgnoreCase(pos_q[0]) || part.indexOf(pos_q[0])>=0 || part.indexOf(pos_q[0].substring(0,pos_q[0].length()-1))>=0)
				{
					found = true;
					break;
				}
			}
			if(!found)
			{
				posListNotFound.add(pos);
				System.out.println("POS not found - " + pos);
			}
		}}
		Set<String> posSet = new LinkedHashSet<String>(posList);
		return hollingMap;
	}
	
	/**
	 * This method get the holing json from Jobim API and
	 * parses on the assumption that the query terms are present in returned holing json but thats not true
	 * It also saves the json to local file
	 * @param query
	 * @param queryId
	 * @return the map of query term and pos tag
	 */
	public static Map<String,String> getPOS(String query, String queryId)
	{
		Map<String,String> map = new HashMap<String,String>();
		
		List<String> posString = parseJson(queryId, sendRequest(queryId,query));
		//List<String> posString = parseJson(queryId, loadLocalFile(queryId));
		String inputParts[] = query.split(SEPARATOR);
		for(String part : inputParts)
		{
			for(String pos : posString)
			{
				//System.out.println(pos);
				String word[] = pos.split("#");
				//System.out.println(word[0]);
				if(word[0].equalsIgnoreCase(part))
				{
					//System.out.println("Found");
					if(map.get(part) == null)
					{
						map.put(part, pos);	
					}
				}
			}
		}
		return map;
	}

	public static String loadLocalFile(String queryId)
	{
		File file = new File(OUTPUT_HOLING_DIR + "/" + queryId);
		StringBuffer response = new StringBuffer();
		BufferedReader in = null;
		try 
		{
			System.out.println("Loading file : " + file.getAbsolutePath());

			in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String inputLine = null;
			while ((inputLine = in.readLine()) != null)
			{
				response.append(inputLine);
			}
			in.close();
		}
		catch(IOException ioEx)
		{
			ioEx.printStackTrace();
		}
		finally
		{
			if(in!=null)
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
		}
		//System.out.println(response.toString());
		return response.toString();
	}
	
	public static String sendRequest(String queryId, String searchQuery)
	{
		StringBuffer response = new StringBuffer();
		BufferedReader in = null;
		PrintWriter writer = null;
		try 
		{
			String urlStr = BASE_URL + replaceSeparator(replaceSeparator(searchQuery, " "),"#");
			URL urlObj = new URL(urlStr);
			HttpURLConnection con = (HttpURLConnection) urlObj.openConnection(); 
			System.out.println("\nSending 'GET' request to URL : " + urlStr);

			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine = null;
			while ((inputLine = in.readLine()) != null)
			{
				response.append(inputLine);
			}
			in.close();
			File holdingOutputDir = new File(OUTPUT_HOLING_DIR);			
			if(!holdingOutputDir.exists())
				holdingOutputDir.mkdirs();
			File output = new File(holdingOutputDir, queryId);
			writer = new PrintWriter(output, "UTF-8");
			writer.println(response.toString());
		}
		catch(IOException ioEx)
		{
			ioEx.printStackTrace();
		}
		finally
		{
			if(in!=null)
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			if(writer!=null)
				writer.close();
		}
		//System.out.println(response.toString());
		return response.toString();
	}

	public static List<String> parseJson(String queryId, String json)
	{
		List<String> posString = new LinkedList<String>();
		JSONParser parser = new JSONParser();
		JSONObject jsonObject;
		try 
		{
			jsonObject = (JSONObject) parser.parse(json);
			// Getting JSON Array node
			JSONArray holings = (JSONArray) jsonObject.get("holings");
			Iterator<JSONObject> iterator = holings.iterator();
			while (iterator.hasNext())
			{
				JSONObject inner = iterator.next();
				JSONObject keys = (JSONObject) inner.get("key");
				String key = (String)keys.get("key");
				posString.add(key);
			}			
		} 
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		return posString;
	}

	public static String replaceSeparator(String input, String separator)
	{
		switch(separator)
		{
		case " " : return input.replace(separator, "%20");
		case "#" : return input.replace(separator, "%23");
		default : return input;		
		}
	}

}
