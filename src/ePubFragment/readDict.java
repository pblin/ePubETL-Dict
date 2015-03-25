package ePubFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Calendar;
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
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.QueryBuilder;

public class readDict {

	private Mongo m = null;
	private DB db = null;
	private String mongoHost = "localhost";
	private int mongoPort = 27017;
	private String mongoDBname = "ePub";
	private DBCollection dictColl;
	

	public void getDefinitions(String words[], String yo){
		if (words != null)
		{
			DBObject eIsbns = new QueryBuilder().put("word").in(words).get();
			DBCursor dbCur = dictColl.find(eIsbns);	
			while (dbCur.hasNext()) {
				BasicDBObject obj = (BasicDBObject) dbCur.next();
				String word = obj.getString("word");
			}
			dbCur.close();
		}
		
	}
	
	public String[] getDefinition(String word, String yo){
		String id = null, def = null;
		String result[] = { id, word, def };
		if (!dbConnect())
			return result;

		BasicDBObject key = new BasicDBObject(unpackDict.COL_WORD, word).append(unpackDict.COL_YO, yo);
		DBCursor dbCur = dictColl.find(key);
		if (dbCur != null && dbCur.hasNext())
		{
			BasicDBObject obj = (BasicDBObject) dbCur.next();
			id = obj.getString(unpackDict.COL_DICT_ID);
			def = obj.getString(unpackDict.COL_DEF);
		}
		dbCur.close();
		dbDisconnect();
		
		result[0] = id;
		result[2] = def;
		
		return result;
	}
	
	private boolean dbConnect()
	{
		try 
		{
			m = new Mongo(mongoHost , mongoPort);
			db = m.getDB(mongoDBname);
			dictColl = db.getCollection("dict");

			return true;
		}
		catch (Exception e)
		{
			System.out.println("Error connecting to host=" + mongoHost + ", mongoPort=" + mongoPort + ", db=" + mongoDBname + ", ERROR=" + e.toString());
			return false;
		}
	}

	private void dbDisconnect()
	{
		if (m != null){
			m.close();
			m = null;
		}
	}

	
	public static void main(String argv[]) {
 
		try {
			readDict dictonary = new readDict();
			
			System.out.println("\nTest Lookup:");
			String ODtestwords[] = { "work", "workshop", "zillion", "programmer", "programming" };
			
			for (int t = 0; t < ODtestwords.length; t++)
			{
				String id_word_def[] = dictonary.getDefinition(ODtestwords[t], "OD");
				if (id_word_def != null && id_word_def.length == 3)
				{
					System.out.println("\n\nword (OD) = " + id_word_def[1]);
					System.out.println("id = " + id_word_def[0]);
					System.out.println("def = " + id_word_def[2]);
				}
				
			}
		} 
		catch (Exception e) {
			System.out.println("Error Looking up test word in dictionary: " + e.toString());
		} 
	}
}
