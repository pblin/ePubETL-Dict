package ePubFragment;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

// Finds all the words in an ISBN and saves that information to mongoDB.
// The words in an ISBN are considered "Wordforms".  
// Given a Wordform, we will attempt to lookup up the root form of word and store that. 
//    NOTE: if new definitions are added to the dictionary, 
//          or additional Wordforms are created, we will have to re-process all the books, but pre-processing will   

public class unpackWords {
	public static final String COL_WORD = "word";
	public static final String COL_YO = "yo";
	public static final String COL_ISBN = "isbn";

	private Properties prop;
	private String bookFolder;
	private String bookid;
	private DB db;
	private DBCollection isbnColl, wordColl;

	// Inner class that holds a word and version
	public class definition
	{
		public String word;
		public String yo;
		public definition(String word, String yo)
		{
			this.word = word;
			this.yo = yo;
		}
	}
	
	private HashMap<String, String> newWordforms;
	private HashMap<String, definition> newWords;
	private int wordCount = 0;
	
	unpackWords(Properties prop, String bookFolder) {
		this.prop = prop;
		this.bookFolder = bookFolder;
	}

	public int getWordCount()
	{
		return wordCount;
	}
	
	public boolean unpack()
	{	
		try
		{
			String mongohost = prop.getProperty("mongohost").toString();
			String mongodbname = prop.getProperty("dbname").toString();
			String ePubPath = prop.getProperty("ePubPath");
	
			Mongo m = new Mongo( mongohost , 27017 );
			db = m.getDB( mongodbname );
		    
//System.out.println("Processing " + bookFolder);
			String ePubFilePath = ePubPath + "/" + bookFolder + "/OEBPS/"; 
			
			File fXmlFile = new File(ePubFilePath + "content.opf");
			if (!fXmlFile.exists())
				fXmlFile = new File(ePubFilePath + "package.opf");
			if (!fXmlFile.exists())
			{
				System.out.println("Files " + (ePubFilePath + "content.opf") + " and " + (ePubFilePath + "package.opf") + " both missing.");
				return false;
			}
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			
			NodeList nodeList = doc.getElementsByTagName("dc:identifier"); 
			Element node = (Element)nodeList.item(0);
			bookid = node.getTextContent(); 
			bookid = makeNameV1(bookid);
			
			String collectionName = this.bookid + "_words";		// NOTE appended "_words"
			
			this.isbnColl = db.getCollection(collectionName);
			this.wordColl = db.getCollection("words");
			
			newWordforms = new HashMap<String, String>();
			newWords = new HashMap<String, definition>();

//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("item");
//System.out.println("-----------------------");
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
			   
			   Node nNode = nList.item(temp);
			   if (nNode.getNodeType() == Node.ELEMENT_NODE) {	 
				   Element e = (Element) nNode;   
				   String ID = e.getAttribute("id");		   
				   String mediatype = e.getAttribute("media-type");
				   String refName = e.getAttribute("href");
				  				   
				   if (mediatype.contains("application/xhtml+xml"))
				   {
//System.out.println("id: " + ID + " href: " + refName);
					   buildWordforms(ePubFilePath, refName);
				   }
			   	}
			}
	
//System.out.println("Converting wordforms to words");
			convertWordformsToWords();
//System.out.println("Writing words");
			writeWords();
//System.out.println("Done unpacking " + bookFolder);

		}
		catch (Exception e)
		{
			System.out.println("Error processing " + bookFolder + ": " + e.toString());
			return false;
		}
		return true;
	}

	// To name isbn's, this logic needs to be the same on the client.  
	// If the logic changes, maybe a new version number will help sync with the Client changes.
	public String makeNameV1(String bookid)
	{
		if (bookid == null)
			return null;
		String collectionName = bookid.replaceAll(":", "_");
		collectionName = collectionName.replaceAll("-", "_");
		if (collectionName.indexOf("isbn_") > 0)	// remove urn_ prefix?
			collectionName = collectionName.substring(collectionName.indexOf("isbn_"));
		if (!collectionName.startsWith("isbn_"))
			collectionName = "isbn_" + collectionName;
		return collectionName;
	}
	
	
	public void buildWordforms(String filePath, String filename) {
		File fXmlFile = new File(filePath+filename);
System.out.println("Processing file: " + fXmlFile);
	
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			
			boolean bFound = false;

			// Look for "span" elements with "data-dic" attribute, no attributes, or 
			// e.g. <span ... data-dic="David" ...></span>		
			NodeList spanList = doc.getElementsByTagName("span");
			if (spanList != null && spanList.getLength() > 0) {
				for (int i = 0; i < spanList.getLength(); i++) { 
					Node nNode = spanList.item(i);
					Element e = (Element) nNode;  
					String dicWord = e.getAttribute("data-dic");
					if (dicWord != null && !dicWord.isEmpty()) {
						parseWords(dicWord);
						bFound = true;
					}
					else {
						// Span with no attributes (Sample 6)
						NamedNodeMap nnm = e.getAttributes();
						String className = e.getAttribute("class");
						if (nnm == null || nnm.getLength() == 0 || (className != null && className.startsWith("styleid"))) {
							String WordformList = e.getTextContent();
//							System.out.println("Line found: [" + WordformList + "]");
							parseWords(WordformList);
							bFound = true;
						}
					}
				}
			}
			if (!bFound) {
				// HACK: May not be necessary.

				// Look for "div" elements, or "p" elements 
				// 		Sample 3: w/ class = "text" or "text1"
				//		Sample 2, 4: w/ class = "para"

				NodeList divList = null;
				for (int x = 0; x < 2; x++){
					if (x == 0)
						divList = doc.getElementsByTagName("div");
					else
						divList = doc.getElementsByTagName("p");
					if (divList != null && divList.getLength() > 0) {
						for (int i = 0; i < divList.getLength(); i++) { 
							Node nNode = divList.item(i);
							Element e = (Element) nNode;  
							String className = e.getAttribute("class");
							String id = e.getAttribute("id");
/*
							if ( (className != null && (className.startsWith("text") || className.compareTo("para")==0)) ||
								 (id != null && !id.isEmpty() && id.startsWith("rw-p_")) 	)
*/
							// Since this is a last resort, take anything between <p> and </p> tags.
							{
								String WordformList = e.getTextContent();
//								System.out.println("Line found: [" + WordformList + "]");
								parseWords(WordformList);
								bFound = true;
							}
						}
						if (bFound)	// if you found "div" elements with expected data, don't get "p" elements.
							break;
					}
				}
			}
		}
		catch (Exception docE) { 
			docE.printStackTrace();
		}
	}
	
	private void parseWords(String wordformList)
	{
/*
		char replaceCharsWithSpace[] = {'.',  '?',  '!',  ',',  ';',  ':',  '"',  '“',  '”', '(', ')', '/'};
		for (int r = 0; r < replaceCharsWithSpace.length; r++)
			wordformList = wordformList.replace(replaceCharsWithSpace[r], ' ');
*/

		StringBuffer sb = new StringBuffer("");
		String replaceCharsWithSpace = ".?!,;:\"“”()/";
		int len = wordformList.length();
		for (int i = 0; i < len; i++)
		{
			char c = wordformList.charAt(i);
			if (replaceCharsWithSpace.indexOf(c) >= 0)
				sb.append(' ');
			else
				sb.append(c);
		}
		wordformList = sb.toString();
		
		
		String replaceStringsWithSpace[] = {"<b>", "</b>"};
		for (int r = 0; r < replaceStringsWithSpace.length; r++)
			wordformList = wordformList.replaceAll(replaceStringsWithSpace[r], " ");
		
		StringTokenizer st = new StringTokenizer(wordformList);
		while (st.hasMoreTokens())
		{
			String dicWord = st.nextToken();
			
			dicWord = dicWord.trim().toUpperCase();	// Upper case wordforms 
			if (!dicWord.isEmpty())
			{
				if (!newWordforms.containsKey(dicWord))
					newWordforms.put(dicWord, null);
			}
		}
	}

	public int convertWordformsToWords()
	{
		Set<String> keys = newWordforms.keySet();
		if (keys == null)
			return 0;
		Iterator<String> it = keys.iterator();
		while (it.hasNext())
		{
			String wordform = it.next();
			
			// Look up 
			DBCollection dictFormColl = db.getCollection(unpackDict.TABLE_WORD_FORM);
			BasicDBObject key = new BasicDBObject(unpackDict.COL_WORD_FORM, wordform);
			DBCursor dbCur = dictFormColl.find(key);
			String word = null;
			boolean bYoung = false, bOld = false;
			while (dbCur != null && dbCur.hasNext())
			{
				BasicDBObject obj = (BasicDBObject) dbCur.next();
				String yo = obj.getString(unpackDict.COL_YO);
				if (yo.compareTo(unpackDict.VERSION_YOUNG) == 0)
					bYoung = true;
				else if (yo.compareTo(unpackDict.VERSION_OLD) == 0)
					bOld = true;
				word = obj.getString(unpackDict.COL_WORD);
			}
			dbCur.close();
	
if (!bYoung && !bOld)
{
// TODO: if wordform not found
// 1. modify hashmap code to put original word (not uppercased) in data portion of hashmap (currently null)
// 2. search word/defintion table for original word in both young and old 
}

			if (bYoung)
			{
				String word1 = word + "-" + unpackDict.VERSION_YOUNG;
				if (!newWords.containsKey(word1))
					newWords.put(word1, new definition(word, unpackDict.VERSION_YOUNG));
			}
			if (bOld)
			{
				String word1 = word + "-" + unpackDict.VERSION_OLD;
				if (!newWords.containsKey(word1))
					newWords.put(word1, new definition(word, unpackDict.VERSION_OLD));
			}
		}
		
		wordCount = newWords.size();
		return wordCount;
	}

	
	
	// Writes the words in the ISBN being processed to two separate collections
	// Probably want to eventually choose one collection to write to.
	public void writeWords()
	{
		int count;
		Iterator<definition> it;
		Collection<definition> defs;
		
		//Method1: Each ISBN gets it's own collection/table named isbn_####_words with a list of words
		count = 0;
		defs = newWords.values();
		if (defs == null)
			return;
		it = defs.iterator();
		while (it.hasNext())
		{
			definition def = it.next();
			addIsbnWordCollection(def);
			if (count++ == 0)
			{
				isbnColl.createIndex(new BasicDBObject(COL_WORD, 1).append(COL_YO, 1));
			}
		}

		//Method2: add the list of words (including associated ISBN) to the single words collection/table
		count = 0;
		defs = newWords.values();
		if (defs == null)
			return;
		it = defs.iterator();
		while (it.hasNext())
		{
			definition def = it.next();
			addPair(def);
			if (count++ == 0)
			{
				wordColl.createIndex(new BasicDBObject(COL_ISBN, 1));
				wordColl.createIndex(new BasicDBObject(COL_ISBN, 1).append(COL_WORD, 1).append(COL_YO, 1));
			}
		}
		
		return;
	}
	
	private void addIsbnWordCollection(definition def)
	{	
		String word = def.word;
		String yo = def.yo;
		
		BasicDBObject nameValue = new BasicDBObject(COL_WORD, word).append(COL_YO, yo);
		
		// Avoid duplicate (words,version) pairs in the same isbn
		DBCursor dbCur = isbnColl.find(nameValue);
		if (dbCur == null || !dbCur.hasNext())
		{
			isbnColl.insert(nameValue);
//			System.out.println("\t addIsbnWordCollection Added word: [" + word + "]" + yo);
		}
		else
		{
//			System.out.println("\t addIsbnWordCollection Duplicate word: [" + word + "]" + yo);
		}
	}
	
	private void addPair(definition def)
	{
		String word = def.word;
		String yo = def.yo;
		
		BasicDBObject pair = new BasicDBObject(COL_ISBN, bookid).append(COL_WORD, word).append(COL_YO, yo);
		// Avoid duplicate (word,version) pairs in the same isbn
		DBCursor dbCur = wordColl.find(pair);
		if (dbCur == null || !dbCur.hasNext())
		{
			wordColl.insert(pair);
//			System.out.println("\t addPair Added word: [" + word + "]" + yo);
		}
		else
		{
//			System.out.println("\t addPair Duplicate word: [" + word + "]" + yo);
		}
	}
	
	
	public static void main(String argv[]) {
 
	  try {
		  
		  if (argv.length < 2)
		  {
			  System.out.println("need the book folder name and config.properties location" );
			  return;
		  }
			  
		  
		  if (argv[0].isEmpty()) 
			{
				System.out.println("need the book folder name" );
				return;
			}
		  
		  if (argv[1].isEmpty()) 
			{
				System.out.println("config.properties file location" );
				return;
			}
		  
		//String dir = System.getProperty("user.dir").toString();
		// URL propertiesFile = new URL(root, "config.properties");
		String dir = argv[1].toString();
		Properties prop = new Properties();
	    prop.load (new FileInputStream (dir +"/config.properties"));
		  
for (int s = 1; s <= 12; s++)
{
		// Time 
		long t1, t2;
		t1 = Calendar.getInstance().getTimeInMillis();
	
		//String bookFolder = argv[0];
		String bookFolder = "Sample_" + s;

		unpackWords unpackW = new unpackWords(prop, bookFolder);
		unpackW.unpack();
				
		t2 = Calendar.getInstance().getTimeInMillis();
		System.out.println("Processed  " + bookFolder + " " + unpackW.getWordCount() + " unique words in " + (t2 - t1) + " milliseconds");
}		
		
	  } 
	  catch (Exception e) {
		e.printStackTrace();
	  } 
	}
	
/*
 * 	public static void main(String argv[]) {
 
	  try {
		  
		  if (argv.length < 2)
		  {
			  System.out.println("need the book folder name and config.properties location" );
			  return;
		  }
			  
		  
		  if (argv[0].isEmpty()) 
			{
				System.out.println("need the book folder name" );
				return;
			}
		  
		  if (argv[1].isEmpty()) 
			{
				System.out.println("config.properties file location" );
				return;
			}
		  
		//String dir = System.getProperty("user.dir").toString();
		// URL propertiesFile = new URL(root, "config.properties");
		String dir = argv[1].toString();
		Properties prop = new Properties();
	    prop.load (new FileInputStream (dir +"/config.properties"));
		  
		String mongohost = prop.getProperty("mongohost").toString();
		String mongodbname = prop.getProperty("dbname").toString();
		String ePubPath = prop.getProperty("ePubPath");
	    
for (int s = 1; s <= 11; s++)
{
		// Time 
		long t1, t2;
		t1 = Calendar.getInstance().getTimeInMillis();
	
		//String bookFolder = argv[0];
		String bookFolder = "Sample_" + s;
		System.out.println("Processing " + bookFolder);
		
		File fXmlFile = new File(ePubFilePath + "content.opf");
		if (!fXmlFile.exists())
			fXmlFile = new File(ePubFilePath + "package.opf");
		if (!fXmlFile.exists())
		{
			System.out.println("Files " + (ePubFilePath + "content.opf") + " and " + (ePubFilePath + "package.opf") + " both missing.");
			continue;
		}
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		Mongo m = new Mongo( mongohost , 27017 );
		DB db = m.getDB( mongodbname );
		
		NodeList nodeList;
		Element node;
		
		nodeList = doc.getElementsByTagName("dc:identifier"); 
		node = (Element)nodeList.item(0);
		String bookid = node.getTextContent(); 
				
		//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
		NodeList nList = doc.getElementsByTagName("item");
		// System.out.println("-----------------------");
 
		unpackWords2 unpackW = new unpackWords2(db, bookid);
		
		for (int temp = 0; temp < nList.getLength(); temp++) {
		   
		   Node nNode = nList.item(temp);
		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {	 
			   Element e = (Element) nNode;   
			   String ID = e.getAttribute("id");		   
			   String mediatype = e.getAttribute("media-type");
			   String refName = e.getAttribute("href");
			  				   
			   if (mediatype.contains("application/xhtml+xml"))
			   {
				   // System.out.println("id: " + ID + " href: " + refName);
				   unpackW.buildWordList(ePubFilePath, refName);
			   }
		   	}
		}

		int wordCount = unpackW.writeWordList();

		t2 = Calendar.getInstance().getTimeInMillis();
		System.out.println("Processed  " + bookFolder + " " + wordCount + " unique words (out of " + unpackW.getGrossWordCount() + ") in "+ (t2 - t1) + " milliseconds");
}		
		
	  } 
	  catch (Exception e) {
		e.printStackTrace();
	  } 
	}

 * 	
 */
	
}
 
