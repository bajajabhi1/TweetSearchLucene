package edu.columbia.watson.project3;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BingTopicParser {
	
	public static final String QUERY_NUM_TAG = "num";
	public static final String QUERY_TAG = "query";
	public static final String QUERY_TIME_TAG = "querytime";
	public static final String QUERY_TWEET_TIME_TAG = "querytweettime";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length != 1)
		{
			System.out.println("Please provide the topic file");
			System.exit(-1);
		}

	}

	public List<QueryBean> parseTrecTopics(String fileName)
	{
		List<QueryBean> queries = null;
		try
		{
			//File file = new File(fileName);
			BufferedReader bf1 = new BufferedReader ( new FileReader ( fileName)) ;
			queries = new LinkedList<QueryBean>();
			String line  ; 
			int lineCount = 0 ; 
			String queryNum = "" ; 
			String query = "" ;
			String queryTweetTime = "" ; 
			while ((line  = bf1.readLine()) != null) {
				
				lineCount ++ ; 
				String lines[] = line.split(",");
				queryNum = lines[0] ; 
				queryTweetTime = lines[1] ; 
				query = lines[3] ;
				queries.add(new QueryBean(queryNum, query, queryTweetTime));
										
			}
			bf1.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}		
		return queries;		
	}
	
}
