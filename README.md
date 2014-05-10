# **Twitter semantic search based on distributional semantics**

This project emerged while reflecting on search and [this ted talk](http://www.ted.com/talks/eli_pariser_beware_online_filter_bubbles). We started thinking, that perhaps our lack of understanding of the semantic in our interest is related. We are trying to figure that out. We are first in the process of exploring different search approaches, and their effectiveness on Twitter data. 

This wiki for the project documents the following experiments:
* Our baseline: A simple [Lucene](http://lucene.apache.org/) Keyword search on the corpus
* [Query expansion using results from a commercial search engine](https://github.com/bajajabhi1/SearchQueryExpansion)
* Query expansion using the [JoBimText base API](http://maggie.lt.informatik.tu-darmstadt.de/jobimtext/web-demo/api-and-demo-documentation/)
* Query expansion using bi-gram Twitter model developer by the JoBimText team
* Query expansion from a tri-gram model using a Distributional Thesaurus calculated on the Twitter corpus

As a collateral for our project we have produced the following document that develops a how-to plus tips and tricks on [Running JoBimText on Amazon EMR](https://docs.google.com/document/d/1ltCxQBkWx9qSuHKNbzw9IkmZKTK9MGNTIFmEKw1VIL4/edit?usp=sharing).

##Documentation
Our [wiki](https://github.com/bajajabhi1/TweetSearchLucene/wiki) contains the detailed documentation for the project.
