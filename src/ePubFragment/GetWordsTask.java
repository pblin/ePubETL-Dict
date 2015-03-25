package ePubFragment;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.QueryBuilder;

public class GetWordsTask {
	
	private DB db;
	private Mongo m;

	GetWordsTask()
	{
		
	}

	
	public String[] getNewWords(String existingIsbnList[], String newIsbnList[])
	{
		// Best performing method0 thru method5: 
		// 		currently method0 (each isbn has separate collection)
		return getNewWords0(existingIsbnList, newIsbnList);
	}
	

	public String[] getNewWords0(String existingIsbnList[], String newIsbnList[])
	{
		if (!dbInit())	// TODO? Leave connections open in connection pool?
			return null;
		
		String[] result = getNewWordsMethod0(existingIsbnList, newIsbnList);
		
		dbClose();
		
		return result;
	}
	
	public String[] getNewWords1(String existingIsbnList[], String newIsbnList[])
	{
		if (!dbInit())	// TODO? Leave connections open in connection pool?
			return null;
		
		String[] result = getNewWordsMethod1(existingIsbnList, newIsbnList);
		
		dbClose();
		
		return result;
	}
	
	public String[] getNewWords2(String existingIsbnList[], String newIsbnList[])
	{
		if (!dbInit())	// TODO? Leave connections open in connection pool?
			return null;
		
		String[] result = getNewWordsMethod2(existingIsbnList, newIsbnList);
		
		dbClose();
		
		return result;
	}
	
	public String[] getNewWords3(String existingIsbnList[], String newIsbnList[])
	{
		if (!dbInit())	// TODO? Leave connections open in connection pool?
			return null;
		
		String[] result = getNewWordsMethod3(existingIsbnList, newIsbnList);
		
		dbClose();
		
		return result;
	}
	
	public String[] getNewWords4(String existingIsbnList[], String newIsbnList[])
	{
		if (!dbInit())	// TODO? Leave connections open in connection pool?
			return null;
		
		String[] result = getNewWordsMethod4(existingIsbnList, newIsbnList);
		
		dbClose();
		
		return result;
	}
	
	private boolean dbInit()
	{
		try
		{
			// TODO: Fix this hard coded value
			String dir = "/EBOOK/workspace/ePubETL/src/ePubFragment";
			Properties prop = new Properties();
		    prop.load (new FileInputStream (dir +"/config.properties"));
			  
			String mongohost = prop.getProperty("mongohost").toString();
			String mongodbname = prop.getProperty("dbname").toString();
			
			m = new Mongo( mongohost , 27017 );
			db = m.getDB( mongodbname );
			return true;
		}
		catch (Exception e)
		{
			System.out.println("Failed to initialize the Database: " + e.toString());
			return false;
		}
	}

	private void dbClose()
	{
		if (m != null)
			m.close();
	}
	

	private String[] getNewWordsMethod0(String existingIsbnList[], String newIsbnList[])
	{
		HashMap<String, String> existingWords = new HashMap<String, String>();
		for (int e = 0; existingIsbnList != null && e < existingIsbnList.length; e++)
		{
			DBCollection isbnColl;
			String collectionName = existingIsbnList[e] + "_words";
			try
			{
				isbnColl = db.getCollection(collectionName);
			}
			catch (Exception ex)
			{
				System.out.println("No word table for existingIsbn = " + existingIsbnList[e]);
				continue;
			}
			DBCursor dbCur = isbnColl.find();
			while (dbCur.hasNext()) {
				BasicDBObject obj = (BasicDBObject) dbCur.next();
				String word = obj.getString("word");
				if (!existingWords.containsKey(word))
					existingWords.put(word, null);
			}
			dbCur.close();
		}

		HashMap<String, String> newWords = new HashMap<String, String>();
		for (int n = 0; newIsbnList != null && n < newIsbnList.length; n++)
		{
			DBCollection isbnColl;
			String collectionName = newIsbnList[n] + "_words";
			try
			{
				isbnColl = db.getCollection(collectionName);
			}
			catch (Exception ex)
			{
				System.out.println("No word table for newIsbn = " + newIsbnList[n]);
				continue;
			}

			DBCursor dbCur = isbnColl.find();
			while (dbCur.hasNext()) {
				BasicDBObject obj = (BasicDBObject) dbCur.next();
				String word = obj.getString("word");
				if (!existingWords.containsKey(word) && !newWords.containsKey(word))
					newWords.put(word, null);
			}
			dbCur.close();
		}
		
		return makeArray(newWords.keySet());
	}
	

	private String[] getNewWordsMethod1(String existingIbsnList[], String newIsbnList[])
	{
		HashMap<String, String> existingWords = new HashMap<String, String>();
		HashMap<String, String> newWords = new HashMap<String, String>();
		
		DBCollection isbnColl = db.getCollection("words");
		DBCursor dbCur;
		
		if (existingIbsnList != null)
		{
			DBObject eIsbns = new QueryBuilder().put("isbn").in(existingIbsnList).get();
			dbCur = isbnColl.find(eIsbns);	
			while (dbCur.hasNext()) {
				BasicDBObject obj = (BasicDBObject) dbCur.next();
				String word = obj.getString("word");
				if (!existingWords.containsKey(word))
					existingWords.put(word, word);
			}
			dbCur.close();
		}

		DBObject nIsbns = new QueryBuilder().put("isbn").in(newIsbnList).get();
		dbCur = isbnColl.find(nIsbns);
		while (dbCur.hasNext()) {
			BasicDBObject obj = (BasicDBObject) dbCur.next();
			String word = obj.getString("word");
			if (!existingWords.containsKey(word))
				newWords.put(word,  word);
		}
		dbCur.close();
		
		return makeArray(newWords.keySet());
	}
	

	private String[] getNewWordsMethod2(String existingIbsnList[], String newIsbnList[])
	{
		HashMap<String, String> existingWords = new HashMap<String, String>();
		HashMap<String, String> newWords = new HashMap<String, String>();

		DBCollection isbnColl = db.getCollection("words");
		DBCursor dbCur;
		
		if (existingIbsnList != null)
		{
			DBObject eIsbns = new QueryBuilder().put("isbn").in(existingIbsnList).get();
			dbCur = isbnColl.find(eIsbns);	
			while (dbCur.hasNext()) {
				BasicDBObject obj = (BasicDBObject) dbCur.next();
				String word = obj.getString("word");
				if (!existingWords.containsKey(word))
					existingWords.put(word, word);
			}
			dbCur.close();
		}
		
		DBObject nIsbns = new QueryBuilder().put("isbn").in(newIsbnList).get();
		dbCur = isbnColl.find(nIsbns);
		while (dbCur.hasNext()) {
			BasicDBObject obj = (BasicDBObject) dbCur.next();
			String word = obj.getString("word");
			if (!newWords.containsKey(word))
				newWords.put(word, word);
		}
		dbCur.close();
		
		Set<String> eSet = existingWords.keySet();
		Set<String> nSet = newWords.keySet();
		
		nSet.removeAll(eSet);
		
		return makeArray(nSet);
	}
	

	private String[] getNewWordsMethod3(String existingIbsnList[], String newIsbnList[])
	{
		HashMap<String, String> existingWords = new HashMap<String, String>();
		HashMap<String, String> newWords = new HashMap<String, String>();

		DBCollection isbnColl = db.getCollection("words");

		ArrayList<BasicDBObject> eIsbnList = new ArrayList<BasicDBObject>();
		for (int i = 0; i < existingIbsnList.length; i++)
			eIsbnList.add(new BasicDBObject("isbn", existingIbsnList[i]));
		BasicDBObject query = new BasicDBObject("$or", eIsbnList);
		DBCursor dbCur = isbnColl.find(query);	
		while (dbCur.hasNext()) {
			BasicDBObject obj = (BasicDBObject) dbCur.next();
			String word = obj.getString("word");
			if (!existingWords.containsKey(word))
				existingWords.put(word, word);
		}
		dbCur.close();

		ArrayList<BasicDBObject> nIsbnList = new ArrayList<BasicDBObject>();
		for (int i = 0; i < newIsbnList.length; i++)
			nIsbnList.add(new BasicDBObject("isbn", newIsbnList[i]));
		query = new BasicDBObject("$or", nIsbnList);
		dbCur = isbnColl.find(query);
		while (dbCur.hasNext()) {
			BasicDBObject obj = (BasicDBObject) dbCur.next();
			String word = obj.getString("word");
			if (!existingWords.containsKey(word))
				newWords.put(word,  word);
		}
		dbCur.close();
		
		return makeArray(newWords.keySet());
	}
	
	
	private String[] getNewWordsMethod4(String existingIbsnList[], String newIsbnList[])
	{
		// MONGO "in" tested to at least 512 elements (i.e. existing or new IsbnList.length > 512)
		DBCollection isbnColl = db.getCollection("words");
		
		List<String> eList = null;
		if (existingIbsnList != null)
		{
			DBObject eIsbns = new QueryBuilder().put("isbn").in(existingIbsnList).get();
			eList = isbnColl.distinct("word", eIsbns);	
		}

		DBObject nIsbns = new QueryBuilder().put("isbn").in(newIsbnList).get();
		List<String> nList = isbnColl.distinct("word", nIsbns);

		if (eList != null)
			nList.removeAll(eList);
		
		String sArray[] = new String[nList.size()];
		return nList.toArray(sArray);
	}
		

	private String[] makeArray(Set<String> newWords)
	{
		if (newWords.isEmpty())
			return null;
		
		String words[] = new String[newWords.size()];
		Iterator<String> it = newWords.iterator();
		int w = 0;
		while (it.hasNext())
		{
			words[w++] = (String)it.next();
		}
		return words;
	}
	
	public static void main(String argv[]) {
 
		GetWordsTask gwTask = new GetWordsTask();
		
		String[] existingIsbnList = { "isbn_9780545391832","isbn_9780545594028","isbn_9780545292757" };
		String[] newIsbnList = { "isbn_world_history" };
		
		String newWordsList[] = gwTask.getNewWords(existingIsbnList, newIsbnList);
		System.out.println("New Words Count " + (newWordsList!=null ? newWordsList.length : 0) );
		for (int w = 0; newWordsList != null && w < newWordsList.length; w++)
			System.out.println("\t" + (w+1) + " = " + newWordsList[w]);
	}
 
}
