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

public class GetWordsTaskTestBed {
	
	GetWordsTaskTestBed()
	{
		
	}
	
	static String[] existingIsbnList1 = { "isbn_9780545368759", "isbn_9780545391832" };
	static String[] newIsbnList1 = { "isbn_9780545292757", "isbn_9780545367771" };

	static String[] existingIsbnList2 = { "isbn_9780545368759" };
	static String[] newIsbnList2 = { "isbn_9780545391832", "isbn_9780545292757", "isbn_9780545367771" };

	static String[] existingIsbnList3 = { "isbn_9780545368759", "isbn_9780545391832", "isbn_9780545292757" };
	static String[] newIsbnList3 = { "isbn_9780545367771" };

	static String[] existingIsbnList4 = { "isbn_9780545367771", "isbn_9780545391832", "isbn_9780545292757" };
	static String[] newIsbnList4 = { "isbn_9780545368759" };

	static String[] existingIsbnList5 = { "isbn_9780545368759", "isbn_9780545391832", "isbn_9780545367771" };
	static String[] newIsbnList5 = { "isbn_9780545368759" };

	// Over 512 items in this list
	static String[] existingIsbnList6 = { "isbn_9780545368759", "isbn_9780545391832", "isbn_9780545367771","A","ACTION","AN","AND","ANY","ARISING","AS","AS","AUTHORS","Accepting","Additional","Alternatively","Apache","Appendix","BASIS,","BE","BUT","CLAIM,","CONDITIONS","CONNECTION","CONTRACT,","COPYRIGHT","Contribution","Contributor","Copyright","DAMAGES","DEALINGS","DISTRIBUTION","December","Definitions.","Derivative","Developer","Disclaimer","Do","Documentation,","END","EVENT","EXPRESS","Entity","Evil.","FITNESS","FOR","FROM,","For","GCJ","Good,","Grant","HOLDERS","However,","IMPLIED,","IN","INCLUDING","IS","If","In","Instructions","JBoss","JSON","JSON.org:","Java","KIND,","LIABILITY,","LIABLE","LIMITED","Latest","Legal","Liability.","License","Licensor","Limitation","Linux","MERCHANTABILITY,","NO","NONINFRINGEMENT.","NOT","NOTICE","News:","Not","Note:","Notes,","Notwithstanding","OF","OR","OS","OTHER","OTHERWISE,","OUT","Object","On","Other","PARTICULAR","PROVIDED","PURPOSE","Patent","Permission","REPRODUCTION,","Redistribution.","Release","SHALL","SOFTWARE","Sections","Source","Studio","Subject","Submission","Such","TERMS","THE","TITLE,","TO","TORT","The","This","Trademarks.","USE","Unless","WARRANTIES","WARRANTY","WHETHER","WITH","WITHOUT","Warranty","While","Windows","Work","Works,","X","You","Your","a","above","acceptance","accepting","accompanying","act","acting","acts)","add","addendum","additional","additions","advised","against","agree","agreed","agreement","all","alleging","alone","along","alongside","an","and","another","any","appear.","applicable","application","applies","apply","appropriateness","are","arising","as","asserted","associated","assume","at","attached","attribution","authorized","authorship.","available","based","be","been","behalf","below).","beneficial","bind","but","by","can","cannot","carry","cause","cd","certain,","changed","character","charge","choose","claims","clicking","code","combination","command","commercial","common","communication","compiled","complies","computer","conditions","configuration","consequential","consistent","conspicuously","constitutes","construed","contained","containing","content","contents","contract","contributory","control","controlled","conversions","copies","copy","copyright","counterclaim","crash.","cross-claim","customary","damages","date","deal","default","defend,","defined","definition,","deliberate","derived","describing","designated","desktop.","determining","different","direct","direction","directory","discussing","display","distribute,","distributed","distribution,","do","document.","documentation","does","each","eclipse","editorial","either","elaborations,","electronic","entities","entity","eveloper","even","event","example","except","excluding","executed","exercise","exercising","explicitly","express","failure","fee","fifty","file","files","following","follows","for","form","from","furnished","further","generated","give","goodwill,","grant","granted","granting","grants","grossly","harmless","has","herein","hold","icon","identified","if","implied,","import,","improving","in","inability","incidental,","include","included","includes","including","inclusion","incorporated","incorporated","incurred","indemnify,","indicated","indirect,","individual","informational","infringed","infringement,","installed","institute","intentionally","intentionally","interfaces","irrevocable","is","issue","it","its","java","jbdevstudio","launch","law","least","legal","liability","liable","licensable","license","licenses","lieu","limitation","limited","line","link","lists,","litigation","loss","losses),","made","mailing","make,","making","malfunction,","managed","management","marked","marks,","may","mean","means","mechanical","media","medium,","meet","menu","merely","merge,","modifications","modified","modify","modifying","more","must","names","names,","necessarily","negligence),","negligent","no","no-charge,","non-exclusive,","normally","not","nothing","notice","notices","o","object","obligations","obtaining","of","offer","offer,","on","one","only","options.","or","origin","original","other","otherwise","out","outstanding","own","owner","ownership","part","patent","percent","perform,","permission","permissions","permit","perpetual,","person","persons","pertain","places:","portions","possibility","power,","preferred","prepare","processing","product","program","prominent","provide","provided","provides","publicly","publish,","purpose","purposes","readable","reason","reasonable","received","recipients","redistributing","regarding","remain","represent,","representatives,","reproduce","reproducing","reproduction,","equired","required","responsibility,","responsible","restriction,","result","resulting","retain,","revisions,","rights","risks","royalty-free,","section)","sell","sent","separable","separate","service","shall","shares,","so,","software","sole","solely","source","special,","start","state","stated","statement","stating","stoppage,","subject","sublicense,","submit","submitted","subsequently","substantial","such","supersede","support,","systems","systems,","terminate","terms","text","that","the","their","then","theory,","thereof","third-party","this","those","through","to","tort","tracking","trade","trademark,","trademarks,","transfer","transformation","translation","types.","under","union","unless","use","used","using","verbal,","version","via","vm","warranties","warranty","was","where","wherever","whether","which","whole,","whom","will","with","within","without","work","works","worldwide,","would","writing","written","you","your"};
	static String[] newIsbnList6 = { "isbn_9780545368759" };

	static String[] existingIsbnList7 =	{ "isbn_9780545368759" }; 
	// Over 512 items in this list
	static String[] newIsbnList7 = { "isbn_9780545368759", "isbn_9780545391832", "isbn_9780545367771","A","ACTION","AN","AND","ANY","ARISING","AS","AS","AUTHORS","Accepting","Additional","Alternatively","Apache","Appendix","BASIS,","BE","BUT","CLAIM,","CONDITIONS","CONNECTION","CONTRACT,","COPYRIGHT","Contribution","Contributor","Copyright","DAMAGES","DEALINGS","DISTRIBUTION","December","Definitions.","Derivative","Developer","Disclaimer","Do","Documentation,","END","EVENT","EXPRESS","Entity","Evil.","FITNESS","FOR","FROM,","For","GCJ","Good,","Grant","HOLDERS","However,","IMPLIED,","IN","INCLUDING","IS","If","In","Instructions","JBoss","JSON","JSON.org:","Java","KIND,","LIABILITY,","LIABLE","LIMITED","Latest","Legal","Liability.","License","Licensor","Limitation","Linux","MERCHANTABILITY,","NO","NONINFRINGEMENT.","NOT","NOTICE","News:","Not","Note:","Notes,","Notwithstanding","OF","OR","OS","OTHER","OTHERWISE,","OUT","Object","On","Other","PARTICULAR","PROVIDED","PURPOSE","Patent","Permission","REPRODUCTION,","Redistribution.","Release","SHALL","SOFTWARE","Sections","Source","Studio","Subject","Submission","Such","TERMS","THE","TITLE,","TO","TORT","The","This","Trademarks.","USE","Unless","WARRANTIES","WARRANTY","WHETHER","WITH","WITHOUT","Warranty","While","Windows","Work","Works,","X","You","Your","a","above","acceptance","accepting","accompanying","act","acting","acts)","add","addendum","additional","additions","advised","against","agree","agreed","agreement","all","alleging","alone","along","alongside","an","and","another","any","appear.","applicable","application","applies","apply","appropriateness","are","arising","as","asserted","associated","assume","at","attached","attribution","authorized","authorship.","available","based","be","been","behalf","below).","beneficial","bind","but","by","can","cannot","carry","cause","cd","certain,","changed","character","charge","choose","claims","clicking","code","combination","command","commercial","common","communication","compiled","complies","computer","conditions","configuration","consequential","consistent","conspicuously","constitutes","construed","contained","containing","content","contents","contract","contributory","control","controlled","conversions","copies","copy","copyright","counterclaim","crash.","cross-claim","customary","damages","date","deal","default","defend,","defined","definition,","deliberate","derived","describing","designated","desktop.","determining","different","direct","direction","directory","discussing","display","distribute,","distributed","distribution,","do","document.","documentation","does","each","eclipse","editorial","either","elaborations,","electronic","entities","entity","eveloper","even","event","example","except","excluding","executed","exercise","exercising","explicitly","express","failure","fee","fifty","file","files","following","follows","for","form","from","furnished","further","generated","give","goodwill,","grant","granted","granting","grants","grossly","harmless","has","herein","hold","icon","identified","if","implied,","import,","improving","in","inability","incidental,","include","included","includes","including","inclusion","incorporated","incorporated","incurred","indemnify,","indicated","indirect,","individual","informational","infringed","infringement,","installed","institute","intentionally","intentionally","interfaces","irrevocable","is","issue","it","its","java","jbdevstudio","launch","law","least","legal","liability","liable","licensable","license","licenses","lieu","limitation","limited","line","link","lists,","litigation","loss","losses),","made","mailing","make,","making","malfunction,","managed","management","marked","marks,","may","mean","means","mechanical","media","medium,","meet","menu","merely","merge,","modifications","modified","modify","modifying","more","must","names","names,","necessarily","negligence),","negligent","no","no-charge,","non-exclusive,","normally","not","nothing","notice","notices","o","object","obligations","obtaining","of","offer","offer,","on","one","only","options.","or","origin","original","other","otherwise","out","outstanding","own","owner","ownership","part","patent","percent","perform,","permission","permissions","permit","perpetual,","person","persons","pertain","places:","portions","possibility","power,","preferred","prepare","processing","product","program","prominent","provide","provided","provides","publicly","publish,","purpose","purposes","readable","reason","reasonable","received","recipients","redistributing","regarding","remain","represent,","representatives,","reproduce","reproducing","reproduction,","equired","required","responsibility,","responsible","restriction,","result","resulting","retain,","revisions,","rights","risks","royalty-free,","section)","sell","sent","separable","separate","service","shall","shares,","so,","software","sole","solely","source","special,","start","state","stated","statement","stating","stoppage,","subject","sublicense,","submit","submitted","subsequently","substantial","such","supersede","support,","systems","systems,","terminate","terms","text","that","the","their","then","theory,","thereof","third-party","this","those","through","to","tort","tracking","trade","trademark,","trademarks,","transfer","transformation","translation","types.","under","union","unless","use","used","using","verbal,","version","via","vm","warranties","warranty","was","where","wherever","whether","which","whole,","whom","will","with","within","without","work","works","worldwide,","would","writing","written","you","your"}; 
	
	static String[] existingIsbnList8 = { "isbn_world_history" };
	static String[] newIsbnList8 = { "isbn_9780545292757", "isbn_9780545368759", "isbn_9780545391832", "isbn_9780545367771" };

	static String[] existingIsbnList9 = { "isbn_9780545292757", "isbn_9780545368759", "isbn_9780545391832", "isbn_9780545367771" };
	static String[] newIsbnList9 = { "isbn_world_history" };

	static String[] existingIsbnList10 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028" };
	static String[] newIsbnList10 = { "isbn_world_history" };

	static String[] existingIsbnList11 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_world_history" };
	static String[] newIsbnList11 = { "isbn_9780545594028" };

	static String[] existingIsbnList12 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545594028","isbn_world_history" };
	static String[] newIsbnList12 = { "isbn_9780545592567" };

	static String[] existingIsbnList13 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545592567","isbn_9780545594028","isbn_world_history" };
	static String[] newIsbnList13 = { "isbn_9780545506052" };

	static String[] existingIsbnList14 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028","isbn_world_history" };
	static String[] newIsbnList14 = { "isbn_9780545391832" };

	static String[] existingIsbnList15 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028","isbn_world_history" };
	static String[] newIsbnList15 = { "isbn_9780545368759" };

	static String[] existingIsbnList16 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028","isbn_world_history" };
	static String[] newIsbnList16 = { "isbn_9780545367771" };

	static String[] existingIsbnList17 = { "isbn_9780545035255","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028","isbn_world_history" };
	static String[] newIsbnList17 = { "isbn_9780545292757" };

	static String[] existingIsbnList18 = { "isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028","isbn_world_history" };
	static String[] newIsbnList18 = { "isbn_9780545035255" };

	static String[] existingIsbnList19 = { "isbn_world_history","isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052" };
	static String[] newIsbnList19 = { "isbn_9780545592567","isbn_9780545594028" };

	static String[] existingIsbnList20 = { "isbn_world_history","isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545592567" };
	static String[] newIsbnList20 = { "isbn_9780545506052","isbn_9780545594028" };

	static String[] existingIsbnList21 = { "isbn_world_history","isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545506052","isbn_9780545592567" };
	static String[] newIsbnList21 = { "isbn_9780545391832","isbn_9780545594028" };

	static String[] existingIsbnList22 = { "isbn_world_history","isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567" };
	static String[] newIsbnList22 = { "isbn_9780545368759","isbn_9780545594028" };

	static String[] existingIsbnList23 = { "isbn_world_history","isbn_9780545035255","isbn_9780545292757","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567" };
	static String[] newIsbnList23 = { "isbn_9780545367771","isbn_9780545594028" };

	static String[] existingIsbnList24 = { "isbn_world_history","isbn_9780545035255","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567" };
	static String[] newIsbnList24 = { "isbn_9780545292757","isbn_9780545594028" };

	static String[] existingIsbnList25 = { "isbn_world_history","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567" };
	static String[] newIsbnList25 = { "isbn_9780545035255","isbn_9780545594028" };

	static String[] existingIsbnList26 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567" };
	static String[] newIsbnList26 = { "isbn_world_history","isbn_9780545594028" };

	static String[] existingIsbnList27 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545594028" };
	static String[] newIsbnList27 = { "isbn_9780545592567","isbn_world_history" };

	static String[] existingIsbnList28 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545592567","isbn_9780545594028" };
	static String[] newIsbnList28 = { "isbn_9780545506052","isbn_world_history" };

	static String[] existingIsbnList29 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028" };
	static String[] newIsbnList29 = { "isbn_9780545391832","isbn_world_history" };

	static String[] existingIsbnList30 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028" };
	static String[] newIsbnList30 = { "isbn_9780545368759","isbn_world_history" };

	static String[] existingIsbnList31 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028" };
	static String[] newIsbnList31 = { "isbn_9780545367771","isbn_world_history" };

	static String[] existingIsbnList32 = { "isbn_9780545035255","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028" };
	static String[] newIsbnList32 = { "isbn_9780545292757","isbn_world_history" };

	static String[] existingIsbnList33 = { "isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028" };
	static String[] newIsbnList33 = { "isbn_9780545035255","isbn_world_history" };

	static String[] existingIsbnList34 = { "isbn_9780545035255","isbn_9780545292757" };
	static String[] newIsbnList34 = { "isbn_9780545367771","isbn_9780545368759","isbn_world_history","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028" };

	static String[] existingIsbnList35 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771" };
	static String[] newIsbnList35 = { "isbn_9780545368759","isbn_world_history","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028" };

	static String[] existingIsbnList36 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759" };
	static String[] newIsbnList36 = { "isbn_world_history","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028" };

	static String[] existingIsbnList37 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_world_history" };
	static String[] newIsbnList37 = { "isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028" };

	static String[] existingIsbnList38 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_world_history","isbn_9780545391832" };
	static String[] newIsbnList38 = { "isbn_9780545506052","isbn_9780545592567","isbn_9780545594028" };

	static String[] existingIsbnList39 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_world_history","isbn_9780545391832","isbn_9780545506052" };
	static String[] newIsbnList39 = { "isbn_9780545592567","isbn_9780545594028" };

	static String[] existingIsbnList40 = { "isbn_9780545592567","isbn_9780545594028" };
	static String[] newIsbnList40 = { "isbn_9780545367771","isbn_world_history" };

	static String[] existingIsbnList41 = { "isbn_9780545506052","isbn_9780545391832" };
	static String[] newIsbnList41 = { "isbn_9780545592567","isbn_9780545035255" };

	static String[] existingIsbnList42 = { "isbn_9780545391832","isbn_9780545594028" };
	static String[] newIsbnList42 = { "isbn_9780545292757","isbn_world_history" };

	static String[] existingIsbnList43 = { "isbn_9780545368759","isbn_9780545368759" };
	static String[] newIsbnList43 = { "isbn_9780545035255","isbn_9780545391832" };

	static String[] existingIsbnList44 = { "isbn_9780545367771","isbn_9780545594028" };
	static String[] newIsbnList44 = { "isbn_9780545506052","isbn_9780545292757" };

	static String[] existingIsbnList45 = { "isbn_world_history","isbn_9780545367771" };
	static String[] newIsbnList45 = { "isbn_9780545368759","isbn_world_history" };

	static String[] existingIsbnList46 = { "isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028" };
	static String[] newIsbnList46 = { "isbn_igp_epub_unleashed_01" };

	static String[] existingIsbnList47 = { "isbn_world_history","isbn_9780545367771" };
	static String[] newIsbnList47 = { "isbn_igp_epub_unleashed_01" };

	static String[] existingIsbnList48 = { "isbn_igp_epub_unleashed_01" };
	static String[] newIsbnList48 = { "isbn_9780545506052","isbn_9780545592567","isbn_9780545594028" };

	static String[] existingIsbnList49 = { "isbn_igp_epub_unleashed_01","isbn_9780545035255","isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052" };
	static String[] newIsbnList49 = { "isbn_9780545592567","isbn_9780545594028" };

	static String[] existingIsbnList50 = { "isbn_world_history","isbn_igp_epub_unleashed_01" };
	static String[] newIsbnList50 = { "isbn_9780545292757","isbn_9780545367771","isbn_9780545368759","isbn_9780545391832","isbn_9780545506052","isbn_9780545592567","isbn_9780545594028" };

	
	static String[][] existingIsbnLists = {
		existingIsbnList1,
		existingIsbnList2,
		existingIsbnList3,
		existingIsbnList4,
		existingIsbnList5,
		existingIsbnList6,
		existingIsbnList7,
		existingIsbnList8,
		existingIsbnList9,
		existingIsbnList10,
		existingIsbnList11,
		existingIsbnList12,
		existingIsbnList13,
		existingIsbnList14,
		existingIsbnList15,
		existingIsbnList16,
		existingIsbnList17,
		existingIsbnList18,
		existingIsbnList19,
		existingIsbnList20,
		existingIsbnList21,
		existingIsbnList22,
		existingIsbnList23,
		existingIsbnList24,
		existingIsbnList25,
		existingIsbnList26,
		existingIsbnList27,
		existingIsbnList28,
		existingIsbnList29,
		existingIsbnList30,
		existingIsbnList31,
		existingIsbnList32,
		existingIsbnList33,
		existingIsbnList34,
		existingIsbnList35,
		existingIsbnList36,
		existingIsbnList37,
		existingIsbnList38,
		existingIsbnList39,
		existingIsbnList40,
		existingIsbnList41,
		existingIsbnList42,
		existingIsbnList43,
		existingIsbnList44,
		existingIsbnList45,
		existingIsbnList46,
		existingIsbnList47,
		existingIsbnList48,
		existingIsbnList49,
		existingIsbnList50
	};
	static String[][] newIsbnLists = {
		newIsbnList1,
		newIsbnList2,
		newIsbnList3,
		newIsbnList4,
		newIsbnList5,
		newIsbnList6,
		newIsbnList7,
		newIsbnList8,
		newIsbnList9,
		newIsbnList10,
		newIsbnList11,
		newIsbnList12,
		newIsbnList13,
		newIsbnList14,
		newIsbnList15,
		newIsbnList16,
		newIsbnList17,
		newIsbnList18,
		newIsbnList19,
		newIsbnList20,
		newIsbnList21,
		newIsbnList22,
		newIsbnList23,
		newIsbnList24,
		newIsbnList25,
		newIsbnList26,
		newIsbnList27,
		newIsbnList28,
		newIsbnList29,
		newIsbnList30,
		newIsbnList31,
		newIsbnList32,
		newIsbnList33,
		newIsbnList34,
		newIsbnList35,
		newIsbnList36,
		newIsbnList37,
		newIsbnList38,
		newIsbnList39,
		newIsbnList40,
		newIsbnList41,
		newIsbnList42,
		newIsbnList43,
		newIsbnList44,
		newIsbnList45,
		newIsbnList46,
		newIsbnList47,
		newIsbnList48,
		newIsbnList49,
		newIsbnList50
	};
	
	
	public static void main(String argv[]) {
 
		try {
	  
			GetWordsTask gwTask = new GetWordsTask();
			Class[] argClasses = { existingIsbnList1.getClass(), newIsbnList1.getClass() };

			for (int meth = 0; meth < 5; meth++)
			//for (int meth = 4; meth >= 0; meth--)
			{
				String methodName = "getNewWords" + meth;
				System.out.println("****** " + methodName);
				
				long tt = 0;
				int loopCount = 2;
				for (int loop = 0; loop < loopCount; loop++)
				{
					for (int set = 0; set < existingIsbnLists.length; set++)
					{
						
						// Time 
						long t1, t2;
						String[] newWords;
			
						t1 = Calendar.getInstance().getTimeInMillis();
						newWords = (String[])gwTask.getClass().getMethod(methodName, argClasses).invoke(gwTask, existingIsbnLists[set], newIsbnLists[set]);
						// newWords = GetWordsTask.getNewWordsMethod4(existingIsbnLists[set], newIsbnLists[set]);
						t2 = Calendar.getInstance().getTimeInMillis();
						// System.out.println(methodName + " set["+ (set+1) + "] = " + (t2 - t1) + " milliseconds, " + (newWords!=null ? newWords.length : 0) + " words");
						tt += (t2 - t1);
					}
				}
				System.out.println(methodName + " total = " + tt + " milliseconds ");
			}
	  } 
	  catch (Exception e) {
		  System.out.println(e.toString());
	  } 
	}
 
}
