package edu.columbia.watson.project3;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.search.spans.* ;
public class TweetSearch {

	public static final String RUN_ID = "project3";
	public static final String RANGE_START_TWEET_ID = "0";

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
		String expandString = null;
		String expandFile = null ; 
		int hitsPerPage = 5000;
		
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
			else if ("-expandSingle".equals(args[i])){
				expandString =  args[i] ;
			}
			else if ("-expandFile".equals(args[i])){
				expandFile = args[i+1];
				i++;
			}
			
		}

		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		runSearch(searcher, queryFile, expandFile, queryString, expandString , outputFile, hitsPerPage);

		reader.close();
	}
	public static void createWordExpansionList(List<String> allQueries , List<ArrayList<String> > wordExps , int i  , String queryStr) {
		if ( i >= wordExps.size() ){
			queryStr = queryStr.trim() ;
			allQueries.add(queryStr);
			//return 0 ; 
		}
		for (int k = 0 ; k < wordExps.get(i).size() ; k ++){
			 createWordExpansionList(allQueries,wordExps,i+1,queryStr + " " + wordExps.get(i).get(k));
			 
		}
		//return 0 ;
	}
	public static String expandQuery(QueryBean q, int method, int wordMax, float minScore , int queryMax, double queryScore, List<String> expandedQueryList, List<Double> expandedQueryScoreList){
		/*Method 1 : add it to original query, wordMax words with score >= minScore expanded for each word in query
		 *Method 2 : original query 
		 *Method 3 : just hashtag on expansion side 
		 */
		String expanded = null;
		
		String query = q.getQuery() ;
		query = query.trim() ; 
		String[] queryW = query.split(" ");
		double totalWordsAdded = 0 ; 
		double scoreYet = 0 ; 
		
		switch ( method){
		case 1:		expanded = query;
					for ( int i =0 ; i < queryW.length ; i ++ ){
						int expansionPerWord = 0 ;
						for ( int j = 1 ; j < q.expandedList.size() && expansionPerWord < wordMax ; j++ ){
								 if (q.expandedList.get(j).word.equals(queryW[i]) &&  q.expandedList.get(j).score >= minScore){
									 expansionPerWord ++;
									 totalWordsAdded ++ ; 
									 scoreYet += q.expandedList.get(j).score ;
									 expanded = expanded + " " + q.expandedList.get(j).expansion ;
								 }
				 
						}
					}
					queryScore = scoreYet / totalWordsAdded ;
					break;
		case 2:		expanded = query ; 
					queryScore= 1;
					break ;
					
		case 3:		System.out.println("method 3 ") ; 
					totalWordsAdded = 0 ; 
					scoreYet = 0 ; 
					expanded = query;
					for ( int i =0 ; i < queryW.length ; i ++ ){
						int expansionPerWord = 0 ;
						for ( int j = 0 ; j < q.expandedList.size() && expansionPerWord < wordMax ; j++ ){
								 if (q.expandedList.get(j).word.equals(queryW[i]) &&  q.expandedList.get(j).score >= minScore && q.expandedList.get(j).word.contains("#")){
									 expansionPerWord ++;
									 totalWordsAdded ++ ; 
									 scoreYet += q.expandedList.get(j).score ;
									 expanded = expanded + q.expandedList.get(j).expansion ;
								 }
				 
						}
					}
					queryScore = scoreYet / totalWordsAdded ;
					break;
		case 4:		expanded = query ; 
					System.out.println ( "Method 4") ; 
					Map<String, Double> wordPairScore = new HashMap<String,Double>() ; 
					for (int i = 0 ; i < queryW.length ; i ++){
						int expansionPerWord = 0 ; 
						for ( int j = 0 ; j < q.expandedList.size() && expansionPerWord < wordMax ; j ++){
							if (q.expandedList.get(j).word.equals(queryW[i]) &&  q.expandedList.get(j).score >= minScore){
								 expansionPerWord ++;
								 String key = q.expandedList.get(j).word + "%" + q.expandedList.get(j).expansion;
								 wordPairScore.put(key, q.expandedList.get(j).score);
							 }
						}
					}
					List<ArrayList<String>> wordExps = new ArrayList <  ArrayList < String > >() ; 
					
					for ( int i = 0 ; i < queryW.length ; i ++){
						
						ArrayList<String> exps = new ArrayList<String>()   ;
						int expansionPerWord = 0 ; 
						for ( int j = 0 ; j < q.expandedList.size() && expansionPerWord < wordMax ; j++ ){
							if (q.expandedList.get(j).word.equals(queryW[i]) ){
								 expansionPerWord ++;
								 System.out.println("Fuck you " + queryW[i]) ;
								 exps.add(q.expandedList.get(j).expansion) ; 
								  
							 }
						}
						//System.out.println(expansionPerWord) ;
						if ( exps.size() > 0)
							wordExps.add( exps);
						
					}
					System.out.println(wordExps.size());
					List<String> allQueries = null ; 
					
					createWordExpansionList(allQueries, wordExps, 0, "");
					List<Double> allQueriesScore = null; 
					for (int i =0 ; i < allQueries.size() ; i ++){
						String[] generatedQWords = allQueries.get(i).split(" ");
						double TempScore = 0 ;
						for ( int j =0 ; j < generatedQWords.length ; j ++){
							TempScore += wordPairScore.get(queryW[j] + "%" + generatedQWords[j]);
							
						}
						allQueriesScore.add(TempScore);
					}
					
		}
		return expanded ;
	}
	public static void runSearch(IndexSearcher searcher, String queryFile, String expandFile, String queryStr, String expandString, String outputFile, int hitsPerPage)
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
				List<QueryBean> queryList = topicParser.parseTrecTopics(queryFile,expandFile);
				for(QueryBean queryBean : queryList)
				{	
					double searchQueryPower = -1;
					List<String> expandedQueryList = new ArrayList<String>() ;
					List< Double>expandedQueryScoreList = new ArrayList<Double>();
					String searchQuery = expandQuery(queryBean, 4, 5, 0 , 100, searchQueryPower, expandedQueryList, expandedQueryScoreList);
					
					//searchQuery = searchQuery ; 
					//String searchQuery = queryBean.getQuery();
					String[] searchQueryW = searchQuery.split(" ");
					
					Query query = null	;
					try{
						 query = parser.parse(searchQuery);
					}
					catch(Exception E){
						
					}
					System.out.println("\nSearching for: " + queryBean.getQueryNum() + " :: " + queryBean.getQuery() + " :: " + searchQuery);
					
					//search here
					
					collector = TopScoreDocCollector.create(hitsPerPage, true);
					System.out.println("Searching for range '" + 0 + " to " + queryBean.getQueryTweetTime() + "' using RangeQuery");
																		
					Query rangeQuery = TermRangeQuery.newStringRange(TweetIndexer.TWEET_ID, RANGE_START_TWEET_ID, queryBean.getQueryTweetTime(), true,true);
					SpanQuery[] nullVal= new SpanQuery[searchQueryW.length] ;
					for ( int queryExpWords = 0 ; queryExpWords < searchQueryW.length ; queryExpWords ++) {
						  nullVal[queryExpWords] = new SpanTermQuery (new Term(TweetIndexer.SEARCH_FIELD , searchQueryW[queryExpWords] )) ;
						  
					}
					
					SpanNearQuery spanNear = new SpanNearQuery(nullVal, 10, false) ;
					//SpanNearQuery spanNearFinal = new SpanNearQuery(null, hitsPerPage, true) ;
					
					
					BooleanQuery booleanQuery = new BooleanQuery();
					booleanQuery.add(query, BooleanClause.Occur.MUST);
					booleanQuery.add(rangeQuery, BooleanClause.Occur.MUST);
					System.out.println("Query: " + booleanQuery.toString());
					
					searcher.search(booleanQuery, collector);
					
					ScoreDoc[] hits = collector.topDocs().scoreDocs;
					//writer.println(queryLine);
					for(int hitCount=0;hitCount<hits.length;++hitCount)
					{
						//System.out.println("in It" ) ;
						int docId = hits[hitCount].doc;
						Document d = searcher.doc(docId);
						//System.out.println(d.get(ID) + " " + d.get(SEARCH_FIELD) + " " + hits[hitCount].score);
						writer.println(queryBean.getQueryNum() + " " + d.get(TweetIndexer.TWEET_ID) + " " + hits[hitCount].score + " " + RUN_ID); 
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
				for (int hitCount=0;hitCount<hits.length;++hitCount)
				{
					int docId = hits[hitCount].doc;
					Document d = searcher.doc(docId);
					writer.println(queryStr + " " + d.get(TweetIndexer.TWEET_ID) + " " + hits[hitCount].score + " " + RUN_ID);
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
