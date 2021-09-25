package dev.vini2003.kute.util

import org.jsoup.Jsoup

fun getVideoIdByScrapedSearch(name: String): String {
	val doc = Jsoup.connect("https://www.youtube.com/results?search_query=" + name.replace(" ", "%2C").replace("#", "%23")) .get()
	
	return doc.body().toString().substringAfter("\"videoRenderer\":{\"videoId\":\"").substringBefore("\"")
}