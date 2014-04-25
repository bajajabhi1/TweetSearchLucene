package edu.columbia.watson.project3;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class TweetSearch {

	public static final String RUN_ID = "project3";

	public static void main(String[] args) throws Exception {
		String usage =
				"Usage:\tedu.columbia.watson.project3.SearchFiles [-index dir] [-queryFile file] [-query string] [-output outputFile] [-hitsPerPage hitsPerPage]";
		if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			System.out.println(usage);
			System.exit(0);
		}

		String index = "index";
		String outputFile = "output.txt";
		String queryFile = null;
		String queryString = null;
		int hitsPerPage = 1000;

		for(int i = 0;i < args.length;i++)
		{
			if ("-index".equals(args[i]))
			{
				index = args[i+1];
				i++;
			} 
			else if ("-queryFile".equals(args[i]))
			{
				queryFile = args[i+1];
				i++;
			} 
			else if ("-query".equals(args[i]))
			{
				queryString = args[i+1];
				i++;
			} 
			else if ("-output".equals(args[i]))
			{
				outputFile = args[i+1];
				i++;
			} 
			else if ("-hitsPerPage".equals(args[i]))
			{
				hitsPerPage = Integer.parseInt(args[i+1]);
				if (hitsPerPage <= 0)
				{
					System.err.println("There must be at least 1 hit per page.");
					System.exit(1);
				}
				i++;
			}
		}

		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		runSearch(searcher, queryFile, queryString, outputFile, hitsPerPage);

		reader.close();
	}

	public static void runSearch(IndexSearcher searcher, String queryFile, String queryStr, String outputFile, int hitsPerPage)
	{
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
		TopScoreDocCollector collector = null;
		//BufferedReader queryFileIS = null;
		PrintWriter writer = null;
		try
		{

			writer = new PrintWriter(outputFile, "UTF-8");
			QueryParser parser = new QueryParser(Version.LUCENE_47, TweetIndexer.SEARCH_FIELD, analyzer);
			// Take the query from arguments otherwise from command line input
			if (queryFile != null)
			{
				TrecTopicParser topicParser = new TrecTopicParser();
				List<QueryBean> queryList = topicParser.parseTrecTopics(queryFile);
				for(QueryBean queryBean : queryList)
				{
					Query query = parser.parse(queryBean.getQuery());
					System.out.println("Searching for: " + query.toString(TweetIndexer.SEARCH_FIELD));

					// search here
					collector = TopScoreDocCollector.create(hitsPerPage, true);
					searcher.search(query, collector);
					ScoreDoc[] hits = collector.topDocs().scoreDocs;
					//writer.println(queryLine);
					for(int hitCount=0;hitCount<hits.length;++hitCount)
					{
						int docId = hits[hitCount].doc;
						Document d = searcher.doc(docId);
						//System.out.println(d.get(ID) + " " + d.get(SEARCH_FIELD) + " " + hits[hitCount].score);
						writer.println(queryBean.getQueryNum() + " " + d.get(TweetIndexer.ID) + " " + hits[hitCount].score + " " + RUN_ID); 
					}
				}
			} 
			else
			{
				if(queryStr == null)
				{
					Scanner  queryScanner = new Scanner(System.in);
					// get the query from user if query is not in arguments
					System.out.println("Enter query: ");
					queryStr = queryScanner.next();
					queryScanner.close();
				}
				Query query = parser.parse(queryStr.trim());
				System.out.println("Searching for: " + query.toString(TweetIndexer.SEARCH_FIELD));

				// search here
				collector = TopScoreDocCollector.create(hitsPerPage, true);
				searcher.search(query, collector);
				ScoreDoc[] hits = collector.topDocs().scoreDocs;
				for(int hitCount=0;hitCount<hits.length;++hitCount)
				{
					int docId = hits[hitCount].doc;
					Document d = searcher.doc(docId);
					writer.println(queryStr + " " + d.get(TweetIndexer.ID) + " " + hits[hitCount].score + " " + RUN_ID);
					writer.println(d.get(TweetIndexer.SEARCH_FIELD));
				}				
			}
			writer.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		} 
		catch (ParseException e)
		{
			e.printStackTrace();
		} 
		finally
		{
			if (writer != null)
				writer.close();
		}
	}
}
