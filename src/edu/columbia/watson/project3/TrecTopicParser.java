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

public class TrecTopicParser {
	
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
			File file = new File(fileName);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			// Load the input XML document, parse it and return an instance of the
			// Document class.
			Document document = builder.parse(file);
			queries = new LinkedList<QueryBean>();
			
			NodeList nodeList = document.getDocumentElement().getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element elem = (Element) node;					
					// Get the value of all sub-elements.
					String queryNum = elem.getElementsByTagName(QUERY_NUM_TAG).item(0)
							.getChildNodes().item(0).getNodeValue();
					queryNum = queryNum.substring(queryNum.indexOf(':')+2).trim();
					String query = elem.getElementsByTagName(QUERY_TAG).item(0)
							.getChildNodes().item(0).getNodeValue();
					query = query.trim();
					String queryTime = elem.getElementsByTagName(QUERY_TIME_TAG).item(0)
							.getChildNodes().item(0).getNodeValue();
					String queryTweetTime = elem.getElementsByTagName(QUERY_TWEET_TIME_TAG).item(0)
							.getChildNodes().item(0).getNodeValue();
					queryTweetTime = queryTweetTime.trim();
					queries.add(new QueryBean(queryNum, query, queryTweetTime));					
				}
			}
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return queries;		
	}

	public List<QueryBean> parseTrecTopics(String fileName,String expandFile, String bingFile)
	{
		List<QueryBean> queries = null;
		try
		{
			File file = new File(fileName);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			//PrintWriter writer = new PrintWriter (new FileOutputStream(new File(bingFile),true)) ; 
			//writer = new PrintWriter(bingFile,"UTF-8");
			
			// Load the input XML document, parse it and return an instance of the
			// Document class.
			Document document = builder.parse(file);
			queries = new LinkedList<QueryBean>();
			
			NodeList nodeList = document.getDocumentElement().getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				
				Node node = nodeList.item(i);
				//System.out.println(node.getNodeName());
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element elem = (Element) node;
					
					// Get the value of all sub-elements.
					String queryNum = elem.getElementsByTagName(QUERY_NUM_TAG).item(0)
							.getChildNodes().item(0).getNodeValue();
					queryNum = queryNum.substring(queryNum.indexOf(':')+2).trim();
					String query = elem.getElementsByTagName(QUERY_TAG).item(0)
							.getChildNodes().item(0).getNodeValue();
					String queryTime = elem.getElementsByTagName(QUERY_TIME_TAG).item(0)
							.getChildNodes().item(0).getNodeValue();
					String queryTweetTime = elem.getElementsByTagName(QUERY_TWEET_TIME_TAG).item(0)
							.getChildNodes().item(0).getNodeValue();
					queryTweetTime = queryTweetTime.trim();
					queries.add(new QueryBean(queryNum, query, queryTweetTime));
					/*
					writer.println(queryNum);
					writer.println(queryTweetTime) ;
					queryTime = queryTime.trim() ;
					String [] queryTimeElem = queryTime.split(" ");
					String expandedQuery = queryTimeElem[1] + " " + queryTimeElem[5] + query ;
					writer.println(expandedQuery);
					*/
					
				}
			}
			//writer.close();
			// Print all employees.
			//for (QueryBean query : queries)
			//	System.out.println(query.toString());
		}
		catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";
	 
		try {
			   
			br = new BufferedReader(new FileReader(expandFile));
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] query = line.split(csvSplitBy);
				System.out.println(line);
				String queryId = query[0] ;
				int lines = Integer.parseInt(query[1]) ;
				
				for(QueryBean expandingQueryId : queries){
					if (expandingQueryId.getQueryNum().equals(queryId)){
						for ( int nLines = 0 ; nLines < lines ; nLines ++ ) {
							
							line = br.readLine() ;
							//System.out.println(line);
							String[] wordExp = line.split(csvSplitBy);
							expandingQueryId.addExpansion(wordExp[0], wordExp[1], Double.parseDouble(wordExp[2]) );
						}
						break;
					}
				}
				
	 
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
				/*for ( int spitQueries = 0 ; spitQueries < queries.size() ; spitQueries ++){
			QueryBean qq = queries.get(spitQueries) ;
			String qqId = qq.getQueryNum() ;
			String qqDate = qq.getQueryDate().toString() ;
			String qqTweetTime = qq.getQueryTweetTime() ;
			String qqQuery = qq.getQuery() ;
		}*/
		
		
		return queries;
		
	}

}
