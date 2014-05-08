package edu.columbia.watson.project3.jobimApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.columbia.watson.project3.QueryBean;

public class SimilarTermsRequestSender
{
	public static final String BASE_URL = "http://maggie.lt.informatik.tu-darmstadt.de:10080/jobim/ws/api/stanford/jo/similar/";
	public static final String SEPARATOR = " ";
	public static final String COMMA_SEPARATOR = ",";
	public static final String UNDERSCORE_SEPARATOR = "_";
	public static final String QUERY_FILE = "2012.topics.MB51-110.xml";
	public static final String OUTPUT_SIMILAR_FILE = "SimilarityFileJobimAPI";
	public static final String OUTPUT_SIMILAR_DIR = "API-SIMILAR_TERMS";

	public static void createSimilarTermsOutputFile(QueryBean queryBean, Map<String,String> queryPosMap)
	{
		PrintWriter writer = null;
		try 
		{
			writer = new PrintWriter(new FileWriter(OUTPUT_SIMILAR_FILE,true));
			String str = getSimilarTermsJson(queryBean.getQueryNum(), queryPosMap);
			writer.print(str);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if(writer!=null)
				writer.close();
		}
	}

	public static String getSimilarTermsJson(final String queryId,  Map<String,String> queryPosMap)
	{
		String queryOutputString = queryId;
		String expandedList = "";
		int countOfExpansions = 0;
		//List<SimilarTermVO> simTermList = new LinkedList<SimilarTermVO>();
		File baseDir = new File(OUTPUT_SIMILAR_DIR);
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				String parts[] = name.split(UNDERSCORE_SEPARATOR);
				if(parts[0].equals(queryId))
					return true;
				else
					return false;
			}
		};
		File fileList[] = baseDir.listFiles(filter);
		for(File file : fileList)
		{
			System.out.println(file.getAbsolutePath());
			String queryTerm = file.getName().split(UNDERSCORE_SEPARATOR)[1].split("#")[0];
			List<SimilarTermVO> termList = parseSimilarTermJson(file);
			countOfExpansions = countOfExpansions + termList.size();
			Collections.sort(termList,descComp);
			for(SimilarTermVO vo: termList)
			{
				if(queryPosMap.get(queryTerm) == null)
				{
					expandedList = expandedList + queryTerm + COMMA_SEPARATOR + vo.toString() + "\n";
				}
				else 
				{
					expandedList = expandedList + queryPosMap.get(queryTerm) + COMMA_SEPARATOR + vo.toString() + "\n";
				}
			}
			//simTermList.addAll(termList);
		}
		queryOutputString = queryOutputString + COMMA_SEPARATOR + countOfExpansions + "\n";
		queryOutputString = queryOutputString + expandedList;
		return queryOutputString;
	}

	public static List<SimilarTermVO> parseSimilarTermJson(File jsonFile)
	{
		String jsonStr = getJsonString(jsonFile);
		List<SimilarTermVO> simTermList = new LinkedList<SimilarTermVO>();
		JSONParser parser = new JSONParser();
		JSONObject jsonObject;
		SimilarTermVO simTermVo = null;
		try 
		{
			jsonObject = (JSONObject) parser.parse(jsonStr);
			// Getting JSON Array node
			JSONArray holings = (JSONArray) jsonObject.get("results");
			Iterator<JSONObject> iterator = holings.iterator();
			while (iterator.hasNext()) {
				//System.out.println("==================");
				JSONObject inner = iterator.next();
				Double score = (Double)inner.get("score");
				// key contains POS here
				String term = (String)inner.get("key");
				term = term.substring(0,term.indexOf('#'));
				simTermVo = new SimilarTermVO(term.trim(), score);				
				simTermList.add(simTermVo);
				//System.out.println(term + "," + score);
			}
		} 
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		return simTermList;
	}

	public static String getJsonString(File jsonFile)
	{
		StringBuffer jsonStr = new StringBuffer();
		BufferedReader in = null;
		try 
		{
			System.out.println("Loading file : " + jsonFile.getAbsolutePath());

			in = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile)));
			String inputLine = null;
			while ((inputLine = in.readLine()) != null)
			{
				jsonStr.append(inputLine);
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
		return jsonStr.toString();
	}
	/**
	 * This method sends the query to Jobim API and gets the similar terms json and saves it local file
	 * @param queryId
	 * @param searchQuery
	 */
	public static void getSimilarWords(String queryId, String searchQuery)
	{
		PrintWriter writer = null;
		try
		{
			String json = sendRequest(searchQuery);		
			// replacing special characters so that the file name can be created
			if(searchQuery.contains("'") || searchQuery.contains("#") || searchQuery.contains(" "))
			{
				searchQuery = searchQuery.replace("'", "_").replace(" ", "_").replace("`", "_").replace("\"", "_");
			}
			File holdingOutputDir = new File(OUTPUT_SIMILAR_DIR);			
			if(!holdingOutputDir.exists())
				holdingOutputDir.mkdirs();
			File output = new File(holdingOutputDir, queryId + UNDERSCORE_SEPARATOR + searchQuery);
			writer = new PrintWriter(output, "UTF-8");
			writer.println(json);
		}
		catch(FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if(writer!=null)
				writer.close();
		}
	}

	public static String sendRequest(String searchQuery)
	{
		StringBuffer response = new StringBuffer();
		BufferedReader in = null;
		try 
		{
			String urlStr = BASE_URL + replaceSeparator(replaceSeparator(replaceSeparator(replaceSeparator(searchQuery, " "),"#"),"`"),"\"");
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

	public static void main(String args[]) throws UnsupportedEncodingException
	{
		//String urlStr = "http://maggie.lt.informatik.tu-darmstadt.de:10080/jobim/ws/api/stanford/jo/similar/\"%23``";
		//String encodedUrl = URLEncoder.encode(urlStr, "UTF-8");
		//System.out.println(encodedUrl);
		//System.out.println(getSimilarTermsJson("MB051"));
		//createSimilarTermsOutputFile();

	}

	public static String replaceSeparator(String input, String separator)
	{
		switch(separator)
		{
		case " " : return input.replace(separator, "%20");
		case "#" : return input.replace(separator, "%23");
		case "`" : return input.replace(separator, "%60");
		case "'" : return input.replace(separator, "%27");
		case "\"" : return input.replace(separator, "%25");
		default : return input;		
		}
	}

	static Comparator<SimilarTermVO> descComp = new Comparator<SimilarTermVO>() {

		@Override
		public int compare(SimilarTermVO o1, SimilarTermVO o2) {
			// TODO Auto-generated method stub
			return (int) Math.round(o2.getScore()-o1.getScore());
		}
	};
}
class SimilarTermVO
{
	String term = null;
	Double score = null;
	public SimilarTermVO(String term, Double score)
	{
		this.term = term;
		this.score = score;
	}
	
	public String getTerm() {
		return term;
	}
	
	public void setTerm(String term) {
		this.term = term;
	}
	
	public Double getScore() {
		return score;
	}
	
	public void setScore(Double score) {
		this.score = score;
	}

	public String toString()
	{
		return term.toLowerCase() + "," + score.toString().trim();
	}
}
