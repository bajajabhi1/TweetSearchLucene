package edu.columbia.watson.project3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class TweetIndexer {
	
	public static final int COMMA_SEPERATOR_POS = 17;
	public static final char COMMA_SEPERATOR = ',';
	public static final char LINE_JOIN_CHAR = ' ';
	public static final String SEARCH_FIELD = "text";
	public static final String TWEET_ID = "id";	
	public static final String DIR_CORRECTED_OUPTUT = "home/arpitg1991/Desktop/WatsonData/tweets-Id-corrected";
	
	public static void main(String[] args)
	{		
		String usage = "java edu.columbia.watson.project.IndexFiles [-index INDEX_PATH] [-docs DOCS_PATH]n\n\n"
				+ "This indexes the documents in DOCS_PATH, creating a Lucene index"
				+ "in INDEX_PATH that can be searched with SearchFiles";
		String indexPath = "index";
		String docsPath = null;
		for(int i=0;i<args.length;i++) 
		{
			if ("-index".equals(args[i])) 
			{
				indexPath = args[i+1];
				i++;
			}
			else if ("-docs".equals(args[i]))
			{
				docsPath = args[i+1];
				i++;
			}
		}

		if (docsPath == null)
		{
			System.err.println("Usage: " + usage);
			System.exit(1);
		}

		final File docDir = new File(docsPath);
		final File correcdocDir = new File(DIR_CORRECTED_OUPTUT);
		if (!docDir.exists() || !docDir.canRead())
		{
			System.out.println("Document directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		Date start = new Date();
		try
		{	
			System.out.println("Indexing to directory '" + indexPath + "'...");
			
			Directory dir = FSDirectory.open(new File(indexPath));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_47, analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			
			// Optional: for better indexing performance, if you
			// are indexing many documents, increase the RAM
			// buffer.  But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx512m or -Xmx1g):
			//
			// iwc.setRAMBufferSizeMB(256.0);
			
			IndexWriter writer = new IndexWriter(dir, iwc);
			runIndexer(writer, docDir, correcdocDir);
			
			// NOTE: if you want to maximize search performance,
			// you can optionally call forceMerge here.  This can be
			// a terribly costly operation, so generally it's only
			// worth it when your index is relatively static (ie
			// you're done adding documents to it):
			//
			// writer.forceMerge(1);
			
			writer.close();
			
			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");
			
		} 
		catch (IOException e) 
		{
			System.out.println(" caught a " + e.getClass() +
					"\n with message: " + e.getMessage());
		}
	}

	public static void runIndexer(IndexWriter idxWriter, File docDir, File correcDocDir)
	{
		// do not try to index files that cannot be read
		if (docDir.canRead())
		{
			if (docDir.isDirectory())
			{
				if(!correcDocDir.exists())
					correcDocDir.mkdirs();				
				String[] files = docDir.list();
				// an IO error could occur
				if (files != null)
				{
					for (int i = 0; i < files.length; i++)
					{
						// create the writing directory.
						runIndexer(idxWriter, new File(docDir, files[i]), new File(correcDocDir, files[i]));
					}
				}
			} 
			else
			{
				FileInputStream fis = null;
				PrintWriter correcWriter = null;
				BufferedReader br = null;				
				try
				{
					fis = new FileInputStream(docDir);
					correcWriter = new PrintWriter(correcDocDir, "UTF-8");
					//} 

					//String fileName = docDir.getAbsolutePath() + "/" +  fileEntry.getName() ;   
					System.out.println("File Detected - " + docDir.getName());
					//String tweetLine = null;
					String currTweetLine = null;
					br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

					String tweetText = null;
					String tweetId = null;
					//tweetLine = br.readLine();

					// read current tweet line. If it consists of a new tweet-id then write the prev tweet data
					while((currTweetLine = br.readLine())!=null)
					{
						if(currTweetLine.length()<COMMA_SEPERATOR_POS+1)
						{
							if(tweetText!=null) // precautionary check
							{
								// Join this to tweetText
								//System.out.println("case 1, " + currTweetLine);
								tweetText = tweetText + LINE_JOIN_CHAR +  currTweetLine;	
							}
						}
						else
						{
							char findComma = currTweetLine.charAt(COMMA_SEPERATOR_POS);
							if(findComma == COMMA_SEPERATOR && currTweetLine.substring(0, COMMA_SEPERATOR_POS-1).matches("\\d+"))
							{
								// Found a new tweet, so process the prev tweet in tweetText and tweetId
								if(tweetId != null)
								{
									Document document = new Document();
									Field pathField = new StringField("path", docDir.getPath(), Field.Store.NO);
									document.add(pathField);

									Field idField = new StringField(TWEET_ID, tweetId, Field.Store.YES);
									document.add(idField);
									document.add(new TextField(SEARCH_FIELD, tweetText, Field.Store.YES ));
									idxWriter.addDocument(document);
									correcWriter.println(tweetId + COMMA_SEPERATOR + tweetText);
								}

								// Update the  tweetId and tweetText
								tweetId = currTweetLine.substring(0, COMMA_SEPERATOR_POS) ;
								tweetText = currTweetLine.substring(COMMA_SEPERATOR_POS +1,currTweetLine.length());						

							}
							else
							{
								if(tweetText!=null) // precautionary check
								{
									// Join this to tweetText
									//System.out.println("case 2, " + currTweetLine);
									tweetText = tweetText + LINE_JOIN_CHAR + currTweetLine;	
								}
							}
						}
					}
					// Reading file is finished so write the last tweet
					if(tweetId != null)
					{
						Document document = new Document();
						Field pathField = new StringField("path", docDir.getPath(), Field.Store.NO);
						document.add(pathField);

						Field idField = new StringField(TWEET_ID, tweetId, Field.Store.YES);
						document.add(idField);
						document.add(new TextField(SEARCH_FIELD, tweetText.toLowerCase(), Field.Store.YES ));
						idxWriter.addDocument(document);
						correcWriter.println(tweetId + COMMA_SEPERATOR + tweetText);
					}

					/*while (tweetLine != null)
					{
						//System.out.println(tweetLine);
						if(tweetLine.length()<COMMA_SEPERATOR_POS+1)
						{
							if(tweetText!=null) // precautionary check
							{
								// Join this to tweetText
								System.out.println("case 1, " + tweetLine);
								tweetText = tweetText + tweetLine;	
							}
							tweetLine = br.readLine();
							continue;
						}
						char findComma = tweetLine.charAt(COMMA_SEPERATOR_POS);
						if(findComma == ',' && tweetLine.substring(0, COMMA_SEPERATOR_POS-1).matches("\\d+"))
						{
							// Found a new tweet, so process the prev
							tweetId = tweetLine.substring(0, COMMA_SEPERATOR_POS) ;
							tweetText = tweetLine.substring(COMMA_SEPERATOR_POS +1,tweetLine.length()) ; 								
							Document document = new Document();
							Field pathField = new StringField("path", docDir.getPath(), Field.Store.NO);
							document.add(pathField);

							Field idField = new StringField(ID, tweetId, Field.Store.YES);
							document.add(idField);
							document.add(new TextField(SEARCH_FIELD, tweetText, Field.Store.YES ));
							idxWriter.addDocument(document);
							correcWriter.println(tweetLine);
							// Read the next line
							tweetLine = br.readLine();
						}
						else
						{
							// Join this to tweetText
							System.out.println("case 2, " + tweetLine);
							tweetText = tweetText + tweetLine;
							while((tweetLine = br.readLine())!=null)
							{
								if(tweetLine.length()<COMMA_SEPERATOR_POS+1)
								{
									System.out.println("case 3, " + tweetLine);
									if(tweetText!=null) // precautionary check
									{
										// Join this to tweetText
										tweetText = tweetText + tweetLine;	
									}
									continue;
								}
								char findComma2 = tweetLine.charAt(COMMA_SEPERATOR_POS);
								if(findComma2 == ',' && tweetLine.substring(0, COMMA_SEPERATOR_POS-1).matches("\\d+"))
								{
									// found a new tweet. So break from this loop.
									// outer loop will now process this tweetLine
									break;
								}
								else
								{
									System.out.println("case 4, " + tweetLine);
									tweetText = tweetText + tweetLine;
								}
							}								
						}
					}*/

				} 
				catch (FileNotFoundException fnfe)
				{
					// at least on windows, some temporary files raise this exception with an "access denied" message
					// checking if the file can be read doesn't help
					fnfe.printStackTrace();
					//return;
				}
				catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					//return;
				}
				catch (IOException e)
				{
					e.printStackTrace();
				} 

				finally
				{
					try 
					{
						if (br != null)
							br.close();
						if (fis != null)
							fis.close();
						if (correcWriter != null)
							correcWriter.close();
					} catch (IOException ex)
					{
						ex.printStackTrace();
					}
				}
			}

		}
	}

}
