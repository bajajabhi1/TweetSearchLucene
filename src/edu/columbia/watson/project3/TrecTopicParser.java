package edu.columbia.watson.project3;

import java.io.File;
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

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element elem = (Element) node;
					
					// Get the value of all sub-elements.
					String queryNum = elem.getElementsByTagName(QUERY_NUM_TAG).item(0)
							.getChildNodes().item(0).getNodeValue();
					queryNum = queryNum.substring(queryNum.indexOf(':')+2).trim();
					String query = elem.getElementsByTagName(QUERY_TAG).item(0)
							.getChildNodes().item(0).getNodeValue();
					//String queryTime = elem.getElementsByTagName(QUERY_TIME_TAG).item(0)
					//		.getChildNodes().item(0).getNodeValue();
					String queryTweetTime = elem.getElementsByTagName(QUERY_TWEET_TIME_TAG).item(0)
							.getChildNodes().item(0).getNodeValue();
					queryTweetTime = queryTweetTime.trim();
					queries.add(new QueryBean(queryNum, query, queryTweetTime));
				}
			}

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
		return queries;
	}

}
