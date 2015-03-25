package ePubFragment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.google.gson.*;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class unpackDict {

	public static final String TABLE_DICTIONARY = "dictionary";
	public static final String TABLE_WORD_FORM = "dictform";
	
	public static final String COL_WORD_FORM = "wordform";
	public static final String COL_DICT_ID = "dictId";
	public static final String COL_WORD = "word";
	public static final String COL_YO = "yo";
	public static final String COL_DEF = "def";

	public static final String VERSION_YOUNG = "YD";
	public static final String VERSION_OLD = "OD";
	
	public static final String JSON_CALLBACK_WRAPPER = "JsonDefinitionCallback";
	
	private int wordCount = 0;
	private int formCount = 0;
	private String inFilename;
	private FileReader inFile;
	private DBCollection dictColl, dictFormColl;
	private String dictIdStr, wordFormStr, wordStr, yoStr, defStr;

	private Mongo m = null;
	private DB db = null;
	private String mongoHost = null;
	private int mongoPort = 27017;
	private String mongoDBname = null;
	
	unpackDict(String mongoHost, int mongoPort, String mongoDBname) {
		this.mongoHost = mongoHost;
		this.mongoPort = mongoPort;
		this.mongoDBname = mongoDBname; 
	}

	public int getWordCount()
	{
		return wordCount;
	}

	public int getFormCount()
	{
		return formCount;
	}

	private void dbCreateDictIndexes()
	{
		dictColl.createIndex(new BasicDBObject(COL_DICT_ID, 1).append(COL_YO, 1));
		dictColl.createIndex(new BasicDBObject(COL_WORD, 1).append(COL_YO, 1));
	}
	
	private void fileAddWordDef(String outputDir)
	{
		String outFilename = outputDir + this.yoStr + "/" + this.dictIdStr + ".html"; 
		try
		{
			File file = new File(outFilename);
			FileWriter outFile = new FileWriter(file);
			
			HashMap<String, String> defMap = new HashMap<String,String>();

			defMap.put("id", this.dictIdStr);
			defMap.put("word", this.wordStr);
			defMap.put("version", this.yoStr);
			defMap.put("definition", this.defStr);

			Gson gson = new GsonBuilder().disableHtmlEscaping().create();
			
			outFile.write(/*JSON_CALLBACK_WRAPPER*/ "Json" + this.dictIdStr + this.yoStr + "Callback" + "(" + gson.toJson(defMap) + ")");
			outFile.flush();
			outFile.close();
System.out.println("\t fileAddWordDef Created html file for word: [" + wordStr + "]" + yoStr + " as " + outFilename);
		}
		catch (Exception e)
		{
			System.out.println("\t fileAddWordDef ERROR Creating html file for word: [" + wordStr + "]" + yoStr + ": " + e.toString());
		}
	}
	
	private void dbAddWordDef()
	{
		// Avoid duplicates 
		BasicDBObject key = new BasicDBObject(COL_DICT_ID, dictIdStr).append(COL_YO, yoStr);
		//BasicDBObject key = new BasicDBObject(COL_WORD, wordStr).append(COL_YO, yoStr);
		DBCursor dbCur = dictColl.find(key);
		if (dbCur != null && dbCur.hasNext())
		{
			System.out.println("\t dbAddWordDef Removing old definition for word: [" + wordStr + "]" + yoStr);
			dictColl.remove(key);
		}
		dbCur.close();

		BasicDBObject dictDef = new BasicDBObject(COL_DICT_ID, dictIdStr)
									.append(COL_WORD, wordStr)
									.append(COL_YO, yoStr)
									.append(COL_DEF, defStr);
		dictColl.insert(dictDef);
		System.out.println("\t dbAddWordDef Added definition for word: [" + wordStr + "]" + yoStr);
	}
	

	private void dbCreateWordFormIndexes()
	{
		dictFormColl.createIndex(new BasicDBObject(COL_WORD_FORM, 1).append(COL_YO, 1));
	}
	
	private void dbAddWordForm()
	{
		// Avoid duplicates 
		BasicDBObject key = new BasicDBObject(COL_WORD_FORM, wordFormStr).append(COL_YO, yoStr);
		DBCursor dbCur = dictFormColl.find(key);
		if (dbCur != null && dbCur.hasNext())
		{
			System.out.println("\tdbAddWordForm Removing old wordform: [" + wordFormStr +  "] for word: [" + wordStr + "]" + yoStr);
			dictFormColl.remove(key);
		}
		dbCur.close();

		BasicDBObject wordForm = new BasicDBObject(COL_WORD_FORM, wordFormStr)
									.append(COL_WORD, wordStr)
									.append(COL_DICT_ID, dictIdStr)
									.append(COL_YO, yoStr);
		dictFormColl.insert(wordForm);
		System.out.println("\tdbAddWordForm Added new wordform: [" + wordFormStr +  "] for word: [" + wordStr + "]" + yoStr);
	}
	

	public void buildWordForms(String wordFormfile)
	{
		this.inFilename = wordFormfile;
		
		try {
			if (!openInputFile())
				return;

			if (!dbConnect())
				return;

			formCount = 0;
			while (inFile.ready())
			{
				String record = readLine();
				if (record == null || record.isEmpty())
					break;

				formCount++;
				
				if (!parseWordFormColumns(record))
					continue;	// Bad record
				
				System.out.println("WordForm Record [" + formCount + 
						"] wordFormStr=" + wordFormStr + ",wordStr=" + wordStr  + ",dictIdStr=" + dictIdStr + ",yoStr=" + yoStr);

				// Insert into database
				if (yoStr.compareTo("ALL") != 0)
				{
					dbAddWordForm();
				}
				else
				{
					yoStr = VERSION_YOUNG;
					dbAddWordForm();
					yoStr = VERSION_OLD;
					dbAddWordForm();
				}
				
				if (formCount == 1)
				{
					dbCreateWordFormIndexes();
				}
			}
			
			
		}
		catch (Exception e) { 
			System.out.println("ERROR: " + e.toString());
		}
		finally {
			dbDisconnect();
			closeInputFile();
		}
		return;
		
	}
	
	private boolean parseWordFormColumns(String record)
	{
		String columns[] = record.split("\t");
		if (columns == null || columns.length != 5)
		{
			System.out.println("Invalid WordForm Record Format at Record [" + formCount + "], column count = " + (columns!=null?columns.length:0)); 
			return false;
		}
		
		// String xStr = columns[0];				// Always 'X'; don't save
		wordFormStr = columns[1].toUpperCase();		// Uppercase word forms
		wordStr = columns[2];
		dictIdStr = columns[3];
		yoStr = columns[4];
		
		if (wordFormStr == null || wordStr == null || dictIdStr == null || yoStr == null)
		{
			System.out.println("Invalid WordForm Record Format at Record [" + formCount + "], contains null columns: " 
					+ "wordForm=[" 	+ wordFormStr 	+ "]"
					+ "wordStr=[" 	+ wordStr 		+ "]"
					+ "dictIdStr=["	+ dictIdStr 	+ "]"
					+ "yoStr=[" 	+ yoStr 		+ "]"
					); 
			return false;
		}
			
		return true;
	}
	
	
	// Flat file expected to be in this FORMAT:
	// 		x<tab>dictId<tab>word<tab>young_old<tab>definition
	// e.g.
	// 		X	4045405	abracadabra	YD	<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"></meta><link rel="stylesheet" href="YoungDictionary.css" type="text/css"></link></head><body><div class="entry YD"><span class="hw">abracadabra</span><span class="pronGrp"><span class="pron"> (<em>ab</em>-ruh-kuh-<strong>dab</strong>-ruh) </span></span><table class="YD"><tr><td><span class="sense"><span class="def"> <span class="deftext"><strong>Abracadabra</strong> is a word you say when you do a magic trick.</span> <div class="ex">When a magician pulls a rabbit out of his hat, he says “<strong>Abracadabra!</strong>”</div> </span></span></td></tr></table></div></body></html>
	public int buildDictionary(String filename, String imagesDir, String audioDir, String cssDir, String outputDir) 
	{
		this.inFilename = filename;
		
		try {
			if (!openInputFile())
				return 0;

			if (!dbConnect())
				return 0;

			wordCount = 0;
			while (inFile.ready())
			{
				String record = readLine();
				if (record == null || record.isEmpty())
					break;
				wordCount++;

				if (!parseDictionaryColumns(record))
					continue;	// Bad record

				// The following methods will inline/embed the media in the definition (defStr).  
				// We may want to additionally save the original, smaller definition.  
				inlineDefinitionImages(imagesDir);
				inlineDefinitionAudioPronounce(audioDir);
				inlineCSS(cssDir);

				// System.out.println("Record [" + wordCount +"] xStr=[" +	xStr + "],dictIdStr=[" + dictIdStr + "],wordStr=[" + wordStr + "],yoStr=[" + yoStr + "],defStr=[" + defStr + "]");
				
				// Create flat file
				fileAddWordDef(outputDir);
				// Insert into database
				dbAddWordDef();
				
				if (wordCount == 1)
				{
					dbCreateDictIndexes();
				}
			}
			
			
		}
		catch (Exception e) { 
			System.out.println("ERROR: " + e.toString());
		}
		finally {
			dbDisconnect();
			closeInputFile();
		}
		return wordCount;
	}

	
	boolean dbConnect()
	{
		try 
		{
			m = new Mongo(mongoHost , mongoPort);
			db = m.getDB(mongoDBname);
			dictColl = db.getCollection(TABLE_DICTIONARY);
			dictFormColl = db.getCollection(TABLE_WORD_FORM);

			return true;
		}
		catch (Exception e)
		{
			System.out.println("Error connecting to host=" + mongoHost + ", mongoPort=" + mongoPort + ", db=" + mongoDBname + ", ERROR=" + e.toString());
			return false;
		}
	}

	void dbDisconnect()
	{
		if (m != null){
			m.close();
			m = null;
		}
	}

	private boolean openInputFile()
	{
		File file = new File(inFilename);

		inFile = null;
		try
		{
			inFile = new FileReader(file);
		}
		catch (Exception e)
		{
			System.out.println("Exception opening input file " + file.getName() + ": " + e.toString());
			inFile = null;
			return false;
		}
		return true;
	}
	
	private void closeInputFile()
	{
		if (inFile == null)
			return;
		try
		{
			inFile.close();
		}
		catch (Exception e)
		{
			System.out.println("Exception closing input file: " + e.toString());
		}
		inFile = null;
	}
	
	
	private String readLine()
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			int i;
			while ((i = inFile.read()) != -1)
			{
				if (i == '\n')
					break;
				sb.append((char)i);
			}
		}
		catch (Exception e)
		{
			System.out.println("Error reading line: " + e.toString());
		}
		return sb.toString().trim();
	}

	
	private boolean parseDictionaryColumns(String record)
	{
		String columns[] = record.split("\t");
		if (columns == null || columns.length != 5)
		{
			System.out.println("Invalid Definition Record Format at Record [" + wordCount + "], column count = " + (columns!=null?columns.length:0)); 
			return false;
		}
		
		// String xStr = columns[0];	// Always 'X'; don't save
		dictIdStr = columns[1];
		wordStr = columns[2];
		yoStr = columns[3];
		defStr = columns[4];

		if (dictIdStr == null || wordStr == null || yoStr == null || defStr == null)
		{
			System.out.println("Invalid WordForm Record Format at Record [" + formCount + "], contains null columns: " 
					+ "dictIdStr=[" + dictIdStr	+ "]"
					+ "wordStr=[" 	+ wordStr 	+ "]"
					+ "yoStr=["		+ yoStr 	+ "]"
					+ "defStr=[" 	+ defStr 	+ "]"
					); 
			return false;
		}
			
		
		return true;
	}
	
	
	// Inlines images in the defStr variable.
	// Code taken from original unpack.java 
	private boolean inlineDefinitionImages(String imagesDir)
	{
		// System.out.println("Word = " + this.wordStr + ", Old Def = " + this.defStr);
		
		// <img alt="animal: elephant" src="elephant_phl_CORBIS1-00169084-001.jpg"></img>
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new ByteArrayInputStream(this.defStr.getBytes()));
			doc.getDocumentElement().normalize();
			NodeList imgList = doc.getElementsByTagName("img");
			if (imgList == null || imgList.getLength() == 0)
				return true;
			for (int i = 0; i < imgList.getLength(); i++) { 
				   Node nNode = imgList.item(i);
				   Element e = (Element) nNode;  
				   String imgName = e.getAttribute("src");
				   String mediaType = "image/";
				   if (imgName.contains("jpg"))
				        mediaType += "jpg";
				   else if (imgName.contains("png"))
					   	mediaType += "jpg";
				   else if (imgName.contains("gif")) 
					    mediaType += "gif";
				   
				   //encode image to base64 code and attach to the HTML document
			       String imageFile = imagesDir + imgName; 
			       
			       System.out.println ("\t\tinlining image source = " + imageFile);
			       
				   File file = new File(imageFile);
				   FileInputStream imageInFile = new FileInputStream(file);
			       
		            byte imageData[] = new byte[(int) file.length()];
		            imageInFile.read(imageData);
		            imageInFile.close();

				   String base64Data = Base64.encodeBase64String (imageData);
				   e.setAttribute ("src", "data:"+ mediaType+ ";base64," + base64Data);	
				}
			
		       DOMSource domSource = new DOMSource(doc);
		       StringWriter writer = new StringWriter();
		       StreamResult result = new StreamResult(writer);
		       TransformerFactory tf = TransformerFactory.newInstance();
		       Transformer transformer = tf.newTransformer();
		       transformer.transform(domSource, result);

		       this.defStr = writer.toString();
			   // System.out.println("Word = " + this.wordStr + ", New Def = " + this.defStr);
		}
		catch (Exception docE) { 
			docE.printStackTrace();
		}
		
		return true;
	}
	
	
/*
	// Inlines images in the defStr variable.
	private boolean inlineDefinitionAudioPronounce(String audioPronounceDir)
	{
		// System.out.println("Word = " + this.wordStr + ", Old Def = " + this.defStr);
		// data:audio/mpeg;base64,//M4xAAUc(etc...)
		
		// <audio id="dicAudio" src="/dictionary/Pronunciation/pron_animal.mp3" autoplay="true">
		// <img alt="animal: elephant" src="elephant_phl_CORBIS1-00169084-001.jpg"></img>
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new ByteArrayInputStream(this.defStr.getBytes()));
			doc.getDocumentElement().normalize();
			NodeList audioList = doc.getElementsByTagName("audio");
			if (audioList == null || audioList.getLength() == 0)
				return true;
			for (int i = 0; i < audioList.getLength(); i++) { 
				   Node nNode = audioList.item(i);
				   Element e = (Element) nNode;  
				   String audioName = e.getAttribute("src");
				   String id = e.getAttribute("id");
				   if (id == null || id.compareTo("dicAudio") != 0)
					   continue;
				   String mediaType = "audio/";
				   if (audioName.contains("mp3"))
				        mediaType += "mpeg";
				   else
					   continue;	// Unsupported audio
				   
				   //encode image to base64 code and attach to the HTML document
			       String imageFile = audioPronounceDir + audioName; 
			       
			       System.out.println ("\t\tinlining image source = " + imageFile);
			       
				   File file = new File(imageFile);
				   FileInputStream imageInFile = new FileInputStream(file);
			       
		            byte imageData[] = new byte[(int) file.length()];
		            imageInFile.read(imageData);
		            imageInFile.close();

				   String base64Data = Base64.encodeBase64String (imageData);
				   e.setAttribute ("src", "data:"+ mediaType+ ";base64," + base64Data);	
				}
			
		       DOMSource domSource = new DOMSource(doc);
		       StringWriter writer = new StringWriter();
		       StreamResult result = new StreamResult(writer);
		       TransformerFactory tf = TransformerFactory.newInstance();
		       Transformer transformer = tf.newTransformer();
		       transformer.transform(domSource, result);

		       this.defStr = writer.toString();
			   // System.out.println("Word = " + this.wordStr + ", New Def = " + this.defStr);
		}
		catch (Exception docE) { 
			docE.printStackTrace();
		}
		
		return true;
	}
*/
	
	
	// Inlines images in the defStr variable.
	// HACK / BRUTE FORCE METHOD
	private boolean inlineDefinitionAudioPronounce(String audioPronounceDir)
	{
		// System.out.println("Word = " + this.wordStr + ", Old Def = " + this.defStr);
		// e.g.		<audio id="dicAudio" src="data:audio/mpeg;base64,//M4xAAUc" /><script>document.querySelectorAll('img.pron-icon')[0].setAttribute ('onclick', 'playsound()');function playsound(){document.getElementById('dicAudio').play();}</script>
		try {
			String audioFile = audioPronounceDir + "pron_" + this.wordStr + ".mp3";
			File file = null;
			String searchStr = "pron-icon";
			int index = this.defStr.indexOf(searchStr);
			if (index >= 0)
			{
				try
				{
					file = new File(audioFile);
					if (!file.exists())
					{
						System.out.println("Audio file does not exist: " + audioFile);
						return true; 
					}
				}
				catch (Exception e)
				{
					System.out.println("Cannot find Audio file: " + audioFile);
				}
			}
			else
			{
				// No pron button
				return true;
			}
			
			searchStr = "</body>";
			index = this.defStr.indexOf(searchStr);
			if (index >= 0 && file != null)
			{
				System.out.println ("\t\tinlining audio file = " + audioFile);
				FileInputStream audioInFile = new FileInputStream(file);
			    byte audioBytes[] = new byte[(int) file.length()];
			    audioInFile.read(audioBytes);
			    audioInFile.close();
				String base64AudioData = Base64.encodeBase64String(audioBytes);
				
				String replacement = "<audio id=\"dicAudio\" src=\"data:audio/mpeg;base64," 
							+ base64AudioData + "\" /><script>document.querySelectorAll('img.pron-icon')[0].setAttribute ('onclick', 'playsound()');function playsound(){document.getElementById('dicAudio').play();}</script>"
							+ searchStr;						
						
				this.defStr = this.defStr.replace(searchStr, replacement);
			}
		}
		catch (Exception docE) { 
			docE.printStackTrace();
		}
		
		return true;
	}
	
	
/*	
	// Inlines css in the defStr variable.
	private boolean inlineCSS(String cssDir)
	{
 System.out.println("Word = " + this.wordStr + ", Old Def = " + this.defStr);
 		// <head>...<link href="OldDictionary.css" rel="stylesheet" type="text/css">...</head>
 		// <head>...<link href="YoungDictionary.css" rel="stylesheet" type="text/css">...<head>
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new ByteArrayInputStream(this.defStr.getBytes()));
			doc.getDocumentElement().normalize();
			NodeList headList = doc.getElementsByTagName("head");
			if (headList == null || headList.getLength() == 0)
				return true;
			Node hNode = headList.item(0);
			Element hElem = (Element)hNode;  
			
			NodeList linkList = hElem.getElementsByTagName("link");
			if (linkList == null || linkList.getLength() == 0)
				return true;
			Node lNode = linkList.item(0);
			Element lElem = (Element)lNode;  
		    String mediaType = lElem.getAttribute("type");
		    if (mediaType.compareTo("text/css") == 0)
			{
		    	String hrefName = lElem.getAttribute("href");
				String cssFile = cssDir + hrefName; 
				       
				System.out.println ("\t\tinlining css file = " + cssFile);
				       
				File file = new File(cssFile);
				FileInputStream cssInFile = new FileInputStream(file);
				       
			    byte cssBytes[] = new byte[(int) file.length()];
			    cssInFile.read(cssBytes);
			    cssInFile.close();
				String cssData = new String(cssBytes);

				Element cssElem = doc.createElement("style");
				cssElem.setTextContent("body {background: #FFFFFF; margin: 0px; padding: 0px;overflow:auto;}");
				
				// doc.replaceChild(cssElem, lNode);
				hElem.replaceChild(cssElem, lNode);
				
				DOMSource domSource = new DOMSource(doc);
				StringWriter writer = new StringWriter();
				StreamResult result = new StreamResult(writer);
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer = tf.newTransformer();
				transformer.transform(domSource, result);

				this.defStr = writer.toString();
			}
System.out.println("Word = " + this.wordStr + ", New Def = " + this.defStr);
		}
		catch (Exception docE) { 
			docE.printStackTrace();
		}
		
		return true;
	}
*/

	// Inlines css in the defStr variable.
	// HACK / BRUTE FORCE METHOD
	private boolean inlineCSS(String cssDir)
	{
 // System.out.println("Word = " + this.wordStr + ", Old Def = " + this.defStr);
 		// <head>...<link rel="stylesheet" href="OldDictionary.css" type="text/css"></link>...</head>
 		// <head>...<link href="OldDictionary.css" rel="stylesheet" type="text/css">...</head>
 		// <head>...<link rel="stylesheet" href="YoungDictionary.css" type="text/css"></link>...<head>
 		// <head>...<link href="YoungDictionary.css" rel="stylesheet" type="text/css">...<head>
		try {
			String cssFilename = "OldDictionary.css";
			String searchStr = "<link rel=\"stylesheet\" href=\"" + cssFilename + "\" type=\"text/css\"></link>";
			int index = this.defStr.indexOf(searchStr);
			if (index < 0)
			{
				searchStr = "<link href=\"" + cssFilename + "\" rel=\"stylesheet\" type=\"text/css\">";
				index = this.defStr.indexOf(searchStr);
			}
			if (index < 0)
			{
				cssFilename = "YoungDictionary.css";
				searchStr = "<link rel=\"stylesheet\" href=\"" + cssFilename + "\" type=\"text/css\"></link>";
				index = this.defStr.indexOf(searchStr);
			}
			if (index < 0)
			{
				searchStr = "<link href=\"" + cssFilename + "\" rel=\"stylesheet\" type=\"text/css\">";
				index = this.defStr.indexOf(searchStr);
			}
			if (index >= 0)
			{
				String cssFile = cssDir + cssFilename; 
			       
				System.out.println ("\t\tinlining css file = " + cssFile);
				       
				File file = new File(cssFile);
				FileInputStream cssInFile = new FileInputStream(file);
			    byte cssBytes[] = new byte[(int) file.length()];
			    cssInFile.read(cssBytes);
			    cssInFile.close();
				String cssData = new String(cssBytes);
				this.defStr = this.defStr.replace(searchStr, cssData);
			}
else
{
System.out.println("*****************FAILED TO INLINE CSS!***********************");	
}
// System.out.println("Word = " + this.wordStr + ", New Def = " + this.defStr);
		}
		catch (Exception docE) { 
			docE.printStackTrace();
		}
		
		return true;
	}

	
	public static void main(String argv[]) {
 
		String dictFlatfile = null, wordFormfile = null;
		try {
			if (argv.length < 2)
			{
				System.out.println("Usage: unpackDict flat_file_directory config.properties_location" );
				return;
			}
			if (argv[0].isEmpty()) 
			{
				System.out.println("need the flat_file_directory" );
				return;
			}
			if (argv[1].isEmpty()) 
			{
				System.out.println("need the config.properties_location" );
				return;
			}

			//   /EBOOK/ePub/EntryTable.txt
			dictFlatfile = argv[0] + "EntryTable.txt";
			wordFormfile = argv[0] + "WordFormTable.txt";

			//   /EBOOK/workspace/ePubETL/src/ePubFragment 
			String configDir = argv[1].toString();	
			Properties prop = new Properties();
			prop.load (new FileInputStream (configDir +"/config.properties"));

			String metadata_directory = argv[0];	

			//   /EBOOK/ePub/images/
			String imagesDir = metadata_directory + "images/";

			//   /EBOOK/ePub/audio/
			String audioDir = metadata_directory + "audio/dictionary/Pronunciation/";
			
			//   /EBOOK/ePub/css/
			String cssDir = metadata_directory + "css/";
			
			String outputDir =  metadata_directory + "definitions/";
			
			String mongohost = prop.getProperty("mongohost").toString();
			String mongodbname = prop.getProperty("dbname").toString();
			int mongoport = 27017;
			
			long t1 = Calendar.getInstance().getTimeInMillis();
			
			unpackDict upDict = new unpackDict(mongohost, mongoport, mongodbname);
			upDict.buildWordForms(wordFormfile);
			upDict.buildDictionary(dictFlatfile, imagesDir, audioDir, cssDir, outputDir);
		
			long t2 = Calendar.getInstance().getTimeInMillis();
				
			System.out.println("Processed  " + upDict.getWordCount() + " defintion records, and " + upDict.getFormCount() + " wordform records in "+ (t2 - t1) + " milliseconds");
		} 
		catch (Exception e) {
			System.out.println("Error Processing file [" + (dictFlatfile!=null?dictFlatfile:"NULL") + "]: " + e.toString());
		} 
	}
}
