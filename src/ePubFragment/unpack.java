package ePubFragment;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.*;
import java.util.Properties;
import java.util.Scanner; 
import java.util.concurrent.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;

import java.util.Arrays;
import java.net.URL; 
import org.apache.commons.codec.binary.Base64;

public class unpack {
	
	
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
		  
		String mongohost = prop.getProperty("mongohost").toString();
		String mongodbname = prop.getProperty("dbname").toString();
		String ePubPath = prop.getProperty("ePubPath");
	    
		
		String bookFolder = argv[0];
		String ePubFilePath = ePubPath + "/"+bookFolder + "/OEBPS/"; 
		
		File fXmlFile = new File(ePubFilePath + "content.opf");
if (!fXmlFile.exists())
	fXmlFile = new File(ePubFilePath + "package.opf");
if (!fXmlFile.exists())
{
	System.out.println("Files " + (ePubFilePath + "content.opf") + " and " + (ePubFilePath + "package.opf") + " both missing.");
	return;
}
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		Mongo m = new Mongo( mongohost , 27017 );
		DB db = m.getDB( mongodbname );
		
		NodeList nodeList;
		Element node;
		
		nodeList = doc.getElementsByTagName("dc:title"); 
		node = (Element)nodeList.item(0);
		String title = node.getTextContent(); 
		
		nodeList = doc.getElementsByTagName("dc:creator"); 
		node = (Element)nodeList.item(0);
		String creator = node.getTextContent(); 
		
		nodeList = doc.getElementsByTagName("dc:identifier"); 
		node = (Element)nodeList.item(0);
		String bookid = node.getTextContent(); 
		
		nodeList = doc.getElementsByTagName("dc:publisher"); 
		node = (Element)nodeList.item(0);
		String publisher = node.getTextContent(); 
		
		// nodeList = doc.getElementsByTagName("dc:description"); 
		// node = (Element)nodeList.item(0);
		// String description = node.getTextContent(); 
		
		System.out.println ("title= " + title + " BookID= "+ bookid + "Author = "+ creator);
		
		String collectionName = bookid.replaceAll(":", "_");
		DBCollection coll = db.getCollection(collectionName);
		
	    System.out.println ("collection name= " + collectionName);
		BasicDBObject bookMetaDoc = new BasicDBObject("name", "meta_data");
		BasicDBObject bookMetaDocDetail = new BasicDBObject();
	    bookMetaDocDetail.put("title", title); 
	    bookMetaDocDetail.put("bookid", bookid);
	    bookMetaDocDetail.put("author", creator);
	    bookMetaDocDetail.put("publisher", publisher);
	    
	    // bookMetaDoc.put("description", description);
	    BasicDBObject metaIndex = new BasicDBObject();	
	
		//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
		NodeList nList = doc.getElementsByTagName("item");
		// System.out.println("-----------------------");
 
		ExecutorService pool = Executors.newFixedThreadPool(10);
		for (int temp = 0; temp < nList.getLength(); temp++) {
		   
		   Node nNode = nList.item(temp);
		   if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			   
			 
			   Element e = (Element) nNode;   
			   String ID = e.getAttribute("id");		   
			   String mediatype = e.getAttribute("media-type");
			   String refName = e.getAttribute("href");
			  
			   if (ID.contains("css") || ID.contains("font") )
			   {
					   if (mediatype.contains ("css")){
						   
						   bookMetaDocDetail.put ("css", refName);
						   System.out.println("css= " + refName);
					   }
					   else {
						   bookMetaDocDetail.put("font", refName);
						   System.out.println("font= " + refName);
					   }
			   }
			   
			   metaIndex.put(ID,refName);
			   
				   
			   if (mediatype.contains("application/xhtml+xml"))
			   {
				   System.out.println("id: " + ID + " href: " + refName);
				  
				   pool.execute (new htmlHandler(ID, ePubFilePath, refName, coll));  
			   }

		   	}
		}
		
		bookMetaDoc.put("detail", bookMetaDocDetail);
		bookMetaDoc.put("table", metaIndex);
		coll.insert(bookMetaDoc);
		// coll.insert(metaIndex);
		
pool.shutdown();
	  } 
	  catch (Exception e) {
		e.printStackTrace();
	  } 
	}
}
 
class htmlHandler implements Runnable {
	
	private final String fragID, filePath, filename;
	private final DBCollection coll;
	htmlHandler (String ID, String path, String name, DBCollection coll) {
		this.fragID = ID;
		this.filePath = path;
		this.filename = name;
		this.coll = coll;
	}
	public void run () {
		File fXmlFile = new File(filePath+filename);
		System.out.println (fXmlFile);
	
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			NodeList imgList = doc.getElementsByTagName("img");
			
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
				  
				   // Scanner sc = new Scanner(new File (filePath + "/" +  imgName));
			       String imageFile = filePath + imgName; 
			       System.out.println ("image source = " + imageFile);
				   File file = new File(imageFile);
				   FileInputStream imageInFile = new FileInputStream(file);
			       
		            byte imageData[] = new byte[(int) file.length()];
		            imageInFile.read(imageData);
		 
imageInFile.close();

		            // Converting Image byte array into Base64 String
		           
				   String base64Data = Base64.encodeBase64String (imageData);
				   e.setAttribute ("src", "data:"+ mediaType+ ";base64," + base64Data);	
				}
			
		       DOMSource domSource = new DOMSource(doc);
		       StringWriter writer = new StringWriter();
		       StreamResult result = new StreamResult(writer);
		       TransformerFactory tf = TransformerFactory.newInstance();
		       Transformer transformer = tf.newTransformer();
		       transformer.transform(domSource, result);
		       
		       
		       BasicDBObject fragmentdoc = new BasicDBObject();
		       fragmentdoc.put("fragId", fragID);
		       fragmentdoc.put("frags", writer.toString());
		       coll.insert(fragmentdoc);
		       //System.out.println (writer.toString());
			
		}
		catch (Exception docE) { 
			docE.printStackTrace();
		}
	}
}
