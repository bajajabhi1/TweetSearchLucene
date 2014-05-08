package edu.columbia.watson.project3.twitter2012BigramAPI;

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
import java.util.LinkedList;
import java.util.List;

import edu.columbia.watson.project3.QueryBean;
import edu.columbia.watson.project3.TrecTopicParser;

public class SimilarTermsTwitterRequestSender
{
	public static final String BASE_URL = "http://maggie.lt.informatik.tu-darmstadt.de:10080/jobim/ws/api/twitter2012Bigram/jo/similar/";
	public static final String BASE_URL_END = "?numberOfEntries=50&format=tsv";
	public static final String SEPARATOR = " ";
	public static final String COMMA_SEPARATOR = ",";
	public static final String UNDERSCORE_SEPARATOR = "_";
	public static final String QUERY_FILE = "2012.topics.MB51-110.xml";
	public static final String OUTPUT_SIMILAR_FILE = "SimilarityFileTwitterJobimAPI";
	public static final String OUTPUT_SIMILAR_DIR = "JOBIM_TWITTER_API";

	public static void createSimilarTermsOutputFile()
	{
		PrintWriter writer = null;
		try 
		{
			writer = new PrintWriter(new FileWriter(OUTPUT_SIMILAR_FILE,true));
			TrecTopicParser topicParser = new TrecTopicParser();
			List<QueryBean> queryList = topicParser.parseTrecTopics(QUERY_FILE);
			for (QueryBean queryBean: queryList)
			{
				String str = getSimilarTermsFromLocal(queryBean.getQueryNum());
				writer.print(str);	
			}			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			if(writer!=null)
				writer.close();
		}

	}

	public static String getSimilarTermsFromLocal(final String queryId)
	{
		String queryOutputString = queryId;
		String expandedList = "";
		int countOfExpansions = 0;
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
			String queryTerm = file.getName().split(UNDERSCORE_SEPARATOR)[1];
			List<SimilarTermVO> termList = getSimilarTermsFromSimilarityFile(file);
			countOfExpansions = countOfExpansions + termList.size();
			Collections.sort(termList,descComp);
			for(SimilarTermVO vo: termList)
			{
				expandedList = expandedList + queryTerm.toLowerCase() + COMMA_SEPARATOR + vo.toString() + "\n";
			}
		}
		queryOutputString = queryOutputString + COMMA_SEPARATOR + countOfExpansions + "\n";
		queryOutputString = queryOutputString + expandedList;
		return queryOutputString;
	}

	public static List<SimilarTermVO> getSimilarTermsFromSimilarityFile(File similarityTsvFile)
	{
		BufferedReader in = null;
		List<SimilarTermVO> simTermList = new LinkedList<SimilarTermVO>();
		SimilarTermVO simTermVo = null;
		String inputLine = null;
		try 
		{
			System.out.println("Loading file : " + similarityTsvFile.getAbsolutePath());
			in = new BufferedReader(new InputStreamReader(new FileInputStream(similarityTsvFile)));
			int count = 0;
			while ((inputLine = in.readLine()) != null)
			{
				count++;
				if(count<5) // Skip the first four lines
					continue;

				if(!inputLine.isEmpty() && inputLine!="" && inputLine!="\\r\\n" && inputLine!="\\n")
				{
					String lineParts[] = inputLine.split("\t");
					simTermVo = new SimilarTermVO(lineParts[0].trim(), new Double(lineParts[1].trim()));				
					simTermList.add(simTermVo);
				}
			}
			in.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
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
		return simTermList;
	}

	/**
	 * This method downloads the similarity results, for each query term in topic file, from JoBim Twitter API and saves it to a local file
	 */
	public static void downloadSimilarTermsTwitterJoBimAPI()
	{
		TrecTopicParser topicParser = new TrecTopicParser();
		List<QueryBean> queryList = topicParser.parseTrecTopics(QUERY_FILE);
		for (QueryBean bean: queryList)
		{
			String query = bean.getQuery();
			String queryParts[] = query.split(SEPARATOR);
			for(String queryPart : queryParts)
			{
				getSimilarWords(bean.getQueryNum(), queryPart.trim());
			}
		}
	}

	/**
	 * This method sends the query to Jobim Twitter API and gets the similar terms tsv and saves it local file
	 * @param queryId
	 * @param searchQuery
	 */
	public static void getSimilarWords(String queryId, String searchQuery)
	{
		PrintWriter writer = null;
		try
		{
			String tsvResult = sendRequest(searchQuery);		
			// replacing special characters so that the file name can be created
			searchQuery = replaceSpecChar(searchQuery);
			File holdingOutputDir = new File(OUTPUT_SIMILAR_DIR);			
			if(!holdingOutputDir.exists())
				holdingOutputDir.mkdirs();
			File output = new File(holdingOutputDir, queryId + UNDERSCORE_SEPARATOR + searchQuery);
			writer = new PrintWriter(output, "UTF-8");
			writer.println(tsvResult);
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
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
			String urlStr = BASE_URL + replaceSpecChar(searchQuery) + BASE_URL_END;
			URL urlObj = new URL(urlStr);
			HttpURLConnection con = (HttpURLConnection) urlObj.openConnection(); 
			System.out.println("\nSending 'GET' request to URL : " + urlStr);

			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine = null;
			while ((inputLine = in.readLine()) != null)
			{
				response.append(inputLine + "\n");
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
		return response.toString();
	}

	
	public static void main(String args[]) throws UnsupportedEncodingException
	{
		//downloadSimilarTermsTwitterJoBimAPI();
		createSimilarTermsOutputFile();
	}

	public static String replaceSpecChar(String input)
	{
		return input.replace(" ", "").replace("#", "").replace("'", "").replace("\"","").replace("`", "");
	}

	static Comparator<SimilarTermVO> descComp = new Comparator<SimilarTermVO>() {

		@Override
		public int compare(SimilarTermVO o1, SimilarTermVO o2) {
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
