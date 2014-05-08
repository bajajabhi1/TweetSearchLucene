package edu.columbia.watson.project3;

import java.util.Comparator;

public class WeightedTweetComparator implements Comparator<WeightedTweetId>{
	

	@Override
	public int compare(WeightedTweetId arg0, WeightedTweetId arg1) {
		// TODO Auto-generated method stub
		if (arg0.score > arg1.score )
			return 1 ; 
		else if ( arg0.score < arg1.score)
			return -1 ; 
		else
			return 0;
	}

}
