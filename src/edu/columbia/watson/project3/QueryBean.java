package edu.columbia.watson.project3;

import java.util.Date;

public class QueryBean {

	private String queryNum;
	private String query;
	private Date queryDate;
	private String queryTweetTime;
	
	public QueryBean(String queryNum, String query, String queryTweetTime)
	{
		this.queryNum  = queryNum;
		this.query = query;
		this.queryTweetTime = queryTweetTime;
	}
	
	public String getQueryNum() {
		return queryNum;
	}
	public void setQueryNum(String queryNum) {
		this.queryNum = queryNum;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public Date getQueryDate() {
		return queryDate;
	}
	public void setQueryDate(Date queryDate) {
		this.queryDate = queryDate;
	}
	public String getQueryTweetTime() {
		return queryTweetTime;
	}
	public void setQueryTweetTime(String queryTweetTime) {
		this.queryTweetTime = queryTweetTime;
	}
	
	public String toString()
	{
		return this.queryNum = "," + this.query + "," + this.queryTweetTime;
	}
	
	public static void main(String args[])
	{
		String str = "31748468477267968,Hah? Bsk kuliah";
		char tweetIdIndex = "31748468477267968,Hah? Bsk kuliah".charAt(17);
		if(str.substring(0,16).matches("\\d+"))
		{
			System.out.println("True");
		}
		System.out.println(tweetIdIndex);
	}
}
