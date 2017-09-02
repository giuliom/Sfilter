/**
 * Spam filter project for "Introduction to Machine Learning" class 2011 
 * at University of Bristol
 * @author Giulio Muntoni
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;
import java.io.*;

/**
 * This class provides the method needed to create training data
 *  for the classification algorithm
 * @author giulio
 */
public class Training{

/**
 * Hashtable which binds a String of text from the email to a word class
 */
protected Hashtable<String, Word> words = new Hashtable<String, Word>();

/**
 * Filter Variables for algorithm tuning
 * They should be stored in an external file to be changed dinamically
 */

/**
 * Gain adjustment
 */
protected double gainAdj = 1; 

/**
 * averageconnection* arcs to select only importants arcs. Parameter
 */
protected double minArcs = 100; 

/**
 * Minimum number of occurrencies of a word to be stored in the training set
 */
protected int minOccurrencies = 2;


/**
 * Set Stats Variables of the training set
 */
protected int hamcount = 0; // #of Docs ham
protected int spamcount = 0; // #of Docs spam
protected int wordsnumber = 0; // #of words in the ts
protected int hamwords = 0; // #of ham words
protected int spamwords = 0; // #of spam words


File folder;


//Constructor
public Training() {
	// TODO Auto-generated constructor stub
}


/**
 * PREPROCESSOR: read the files/folders and creates a training data. It uses various filters
 * @param path
 */
public void preProcessFiles(String[] path){

	
for (int a=1; a<path.length;a++)
{	
folder = new File(path[a]);
String filename[] = folder.list();


for (int i = 0; i < filename.length; i++) {
	
	/*
	 * Create a writer to the temporary output file used to store processed emails
	 */
	BufferedWriter bw = null;
	try {
		bw = new BufferedWriter(new FileWriter("output"));
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}

	FileReader reader = null;
	try {
		reader = new FileReader(folder+"/"+filename[i]);
		} catch (FileNotFoundException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
		}
	BufferedReader br = new BufferedReader(reader);
	Scanner scan = new Scanner(br); 
	
	
	
	String mail = null;
	int n = 0;
	
	/**
	 * scans the email and add all the content to one string
	 */
		while (scan.hasNext())
		{
			
			String s = scan.next();
			
			if (mail == null){ mail = s;} else {mail = new String(mail+" "+s);}
		}
		
		
		/**
		 * Removes "useless" charachters from the mail, then write the string into output file
		 */
		mail = clean(mail);
		
		try {
			bw.write(mail);
			bw.close();
			} catch (IOException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		
		
		
		/**
		 * Add the words from the mail to the wordTable
		 */
		if (filename[i].contains("ham"))
		{
			addToWordTable(0);
			hamcount++;
		}
		else
		{
			addToWordTable(1);
			spamcount++;
		}
	}
}
	
	System.out.println("Words added");

	/**
	 * Starts GainFiltering
	 */
	Gainfiltering();
	System.out.println("Gain Filtering Finished");
	
	
	/**
	 * Create a table of connections(arcs) between words
	 * 
	 */
	createConnectionTable(path);
	System.out.println("ConnectionTable created");
	
	/**
	 * Filter the table of connections
	 * 
	 */
	filterConnections();
	System.out.println("ConnectionTable filtered");

	
	
	/**
	 * Updates Set Stats
	 */
	Collection<Word> wrd = words.values();
	Iterator<Word> it = wrd.iterator();
	while (it.hasNext())
	{
			Word w = (Word) it.next();
			hamwords += w.ham;
			spamwords += w.spam;
	}
	wordsnumber = hamwords+spamwords;
	System.out.println("Words count:"+wrd.size());
	
	/**
	 * Save information the training informations in files
	 */
	save();
	System.out.println("Saved");
	
}


/**
 * Create an hashtable of words reading the words from the temp file output
 * @param mode 0 means "ham", 1 means "spam"
 */
public void addToWordTable(int mode){

	FileReader reader = null;
	try {
		reader = new FileReader("output");
		} catch (FileNotFoundException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
		}
	BufferedReader br = new BufferedReader(reader);
	Scanner scan = new Scanner(br); 
	
	/**
	 * Add the worlds to a vector avoiding duplicates and updating word class values if needed
	 */
	Vector<String> mailw = new Vector<String>();
	while (scan.hasNext())
	{
		String s = scan.next();
		mailw.add(s);
	}
	
	
	
	for (int i =0; i<mailw.size();i++)
	{
		Word obj = null;
		
		if ((obj = words.get(mailw.get(i))) !=null)
		{
			if (mode == 0) 
			{ 
				obj.addHam();
			
				
			}
			else 
			{
				obj.addSpam();
				
			}
		}
		
		else
		{
			obj = new Word(mailw.get(i));
			words.put(mailw.get(i), obj);
			if (mode == 0) 
			{ 
				obj.addHam();
			}
			else 
			{
				obj.addSpam();

			}
		}
	}
	
	Hashtable<String,Double> list = new Hashtable<String,Double>();
	
	//removes duplicates
	
	
	for (int i =0; i<mailw.size();i++)
	{
		if (list.get(mailw.get(i))==null){list.put(mailw.get(i), (double) 0);}
	}
	
	/**
	 * update spamemail and hamemail
	 */
	Enumeration<String> it = list.keys();
	while(it.hasMoreElements())
	{
		String s= it.nextElement();
		if (mode==0) { words.get(s).emailham++;} else {words.get(s).emailspam++;}
	}
	
	File f = new File("output");
	if (f.exists()) f.delete();
	
}


/**
 * GAIN FILTERING: filters the words in the hastable choosing the best one for classification 
 * following different steps:
 * 1- Removes word with less than 2 appareance or with the same occurrencies in ham and spam
 * 2- Deletes the words under the average gain
 */
public void Gainfiltering(){
	
	
	System.out.println("Words before the filter: "+words.size());
	
	double averagegain = 0;
	
	Collection<Word> wrd = words.values();
	
	Vector<String> toremove1 = new Vector<String>();
	
	Iterator<Word> ot = wrd.iterator();
	
	/**
	 * STEP 1: Removes word with less than 2 appareance or with the same occurrencies in ham and spam
	 */
	while (ot.hasNext()){
		Word w = ot.next();
		if ((w.emailham+w.emailspam)<minOccurrencies || w.getHam()==w.getSpam() )
			{
				toremove1.add(w.name);
			}
	}
	for (int k=0;k<toremove1.size();k++){
		words.remove(toremove1.get(k));
	
	}
	
	
	/**
	 * Calculates the average Information Gain of the words
	 */
	Iterator<Word> it = wrd.iterator();
	
	while (it.hasNext()){
		
		Word w =  it.next();
		
		averagegain += calcGain(w);
		//System.out.println(calcGain(w));
		
	}
	
	averagegain = averagegain/(double)wrd.size();
	
	
	
	
	/**
	 * STEP 2: Deletes the words under the average gain
	 */
	Collection<Word> wrd2 = words.values();
	
	Iterator<Word> it2 = wrd2.iterator();
	
	Vector<String> toremove = new Vector<String>();
	
	while (it2.hasNext()){
		
		Word w = (Word) it2.next();
		 if (calcGain(w)< averagegain*gainAdj || w.name.length()<=2 || w.name.length()>12)
		 {
			 
			 toremove.add(w.name);
			 
		 }
	}
	
	for (int k=0;k<toremove.size();k++){
		words.remove(toremove.get(k));
	
	}
	
	
	System.out.println("Words Remaining: " +words.size());
	
}







/**
 * Calculates Information Gain of a word
 * @param w the word
 * @return the information gain value
 */
public double calcGain(Word w){
	double nham = (double)(w.getHam()+1);
	double nspam = (double)(w.getSpam()+1);
	double pword = (double)((w.emailham+w.emailspam)/(double)(hamcount+spamcount));
	double pspam = (double)(w.emailspam/(double)(hamcount+spamcount));
	double pham = (double)(w.emailham/(double)(hamcount+spamcount));
	double purity = pspam * Math.log(pspam+1)+ pham * Math.log(pham+1);
	double notpham = (double)(hamcount-w.emailham)/(double)(hamcount+spamcount);
	double notpspam = (double)(spamcount-w.emailspam)/(double)(hamcount+spamcount);
	double inpurity = notpham * Math.log(notpham+1) + notpspam * Math.log(notpspam+1);
	//System.out.println("pword:"+pword+"  pspam:"+pspam+"  pham:"+pham+"  purity:"+purity);
	return  pword * purity + (1-pword) * inpurity;
	
}


/**
 * Create a table of connections between words and filter them
 * The complexity of this algorithm is huge,but I'm able to filter the amount of information a lot later.
 * I wanted the algorithm to be "unsupervised" so it creates gives importance to links automatically based on the frequence
 * @param path
 */
public void createConnectionTable(String[] path){
	
for (int a=1; a<path.length;a++)
{	
	folder = new File(path[a]);
	String filename[] = folder.list();


	for (int i = 0; i < filename.length; i++) {

		FileReader reader = null;
		try {
			reader = new FileReader(folder+"/"+filename[i]);
			} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			}
		BufferedReader br = new BufferedReader(reader);
		Scanner scan = new Scanner(br); 
		
		//words contained in the doc and wods hashtable
		Vector<String> cont = new Vector<String>();
		
		
		
		while (scan.hasNext())
		{
				
			String s = scan.next();
			if (words.containsKey(s)) cont.add(s);
		}
		
		// For every world to add add an hashtable entry with the links updating their weight
		for (int k =0; k<cont.size();k++)
		{
		
			double val = 0;
			if (filename[i].contains("ham")){ val = -1; }else {val = 1;}
			
			for (int x=0;x<cont.size();x++)
			{
				if (words.get(cont.get(k)).arcs.get(cont.get(x))==null)
				{
				words.get(cont.get(k)).arcs.put(cont.get(x), val);
				}
				else
				{
					Double oldval = words.get(cont.get(k)).arcs.get(cont.get(x));
					Double newval = new Double(val);
					oldval +=  newval;
					words.get(cont.get(k)).arcs.remove(cont.get(x));
					words.get(cont.get(k)).arcs.put(cont.get(x), oldval);
				}
			}
			words.get(cont.get(k)).arcs.remove(cont.get(k));
			
			
			
		}
			
	}
}
}


/**
 *  Filter the arcs.
 *  I had no time to optimize the Arcs Feature.
 */

public void filterConnections(){
	
	double averageminus =0;
	double minuscount = 0;
	
	double averageplus = 0;
	double pluscount = 0;
	
	int conncount = 0;
	
	Collection<Word> wrd = words.values();
	
	
	Iterator<Word> it = wrd.iterator();
	
	while (it.hasNext()){
		
		Word w =  it.next();
		Enumeration<String> str = w.arcs.keys();
		while (str.hasMoreElements())
		{
			String s = str.nextElement();
			if (w.arcs.get(s) < 0){ averageminus += w.arcs.get(s);minuscount++;}
			else {if (w.arcs.get(s) > 0)  averageplus += w.arcs.get(s);pluscount++;}
			
			conncount++;
		}
		
	}
	
	System.out.println("avg-:"+averageminus+"    avg+:"+averageplus+"  arcs:"+conncount);
	
	averageminus = averageminus/minuscount;
	averageplus = averageplus/pluscount;
	
	System.out.println("avg-:"+averageminus+"    avg+:"+averageplus+"  arcs:"+conncount);

	
	conncount = 0;
	
	
	Iterator<Word> it2 = wrd.iterator();
	
	while (it2.hasNext()){
		
		Word w =  it2.next();
		
		Vector<String> todelete = new Vector<String>();
		
		Enumeration<String> str = w.arcs.keys();
		while (str.hasMoreElements())
		{
			String s = str.nextElement();
			double vals = w.arcs.get(s);
			
			if ( vals > averageminus*minArcs && vals <averageplus*minArcs) {todelete.add(s);}
	
		}
		
		for (int i=0;i<todelete.size();i++)
		{
			w.arcs.remove(todelete.get(i));
			
		}
		
		
		//Only for testing
		for(int i =0;i< w.arcs.size();i++)
		{
			conncount++;
			
		}
		
		
		
		
	}
	System.out.println("now counts:"+conncount);

}


/**
 * SAVE the training data on different files
 */
public void save(){
	/**
	 * An hashtable which stores p(w|ham) for each word
	 */
	Hashtable<String, Double> hamfinal = new Hashtable<String, Double>(words.size(), 1);
	
	/**
	 * An hashtable which stores p(w|spam) for each word
	 */
	Hashtable<String, Double> spamfinal =  new Hashtable<String, Double>(words.size(), 1);
	
	/**
	 * Hashtable for the arcs
	 * @deprecated
	 */
	Hashtable<String, ArcSer> arcsfinal = new Hashtable<String,ArcSer>();
	
	/**
	 * Adds the data to the new structures
	 */
	Collection<Word> wrd = words.values();
	Iterator<Word> it = wrd.iterator();
	while (it.hasNext())
	{
			Word w = (Word) it.next();
			
			if(w.ham>0)
			{
			Double pwham = new Double(((double)(w.ham+1))/(double)(hamwords+wordsnumber));
			hamfinal.put(w.name, pwham);
			}
			
			if (w.spam>0)
			{
			Double pwspam = new Double((double)(w.spam+1)/(double)(spamwords+wordsnumber));
			spamfinal.put(w.name,pwspam);
			}
			
			
			//Add the arcs for the word
			if (w.arcs.size() >0)
			{
				ArcSer arclist = new ArcSer();
				
				Enumeration<String> str = w.arcs.keys();
				while (str.hasMoreElements())
				{
					String s = str.nextElement();
					double d = w.arcs.get(s);
					arclist.put(s, d);
				}
				
				arcsfinal.put(w.name,arclist);
			}
	}
	
	HashSer ham = new HashSer(hamfinal);
	HashSer spam = new HashSer(spamfinal);
	
	/**
	 * Writes all the data in their files
	 */
	FileOutputStream hos = null;
	try {
		hos = new FileOutputStream("hamdb");
		ObjectOutputStream hout = null;
		try {
			hout = new ObjectOutputStream(hos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			hout.writeObject(ham);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		hout.close();
		hos.close();
		
		FileOutputStream spos = new FileOutputStream("spamdb");
		ObjectOutputStream spout = new ObjectOutputStream(spos);
		spout.writeObject(spam);
		spout.close();
		spos.close();
		
		
		FileOutputStream aros = new FileOutputStream("arcdb");
		ObjectOutputStream arout = new ObjectOutputStream(aros);
		arout.writeObject(arcsfinal);
		arout.close();
		aros.close();
		
		FileWriter fw = new FileWriter("stats");
		BufferedWriter stats = new BufferedWriter(fw);
		stats.write(hamcount);
		stats.write(spamcount);
		stats.write(wordsnumber);
		stats.write(hamwords);
		stats.write(spamwords);
		stats.close();
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
	
}

/**
 * Removes useless characters for training from a string
 * @param mail
 * @return the cleaned string
 */
public static String clean(String mail)
{
	mail = mail.replace('|', ' ');
		mail =mail.replace('!', ' ');
		mail =mail.replace('"', ' ');
		mail =mail.replace('%', ' ');
		mail =mail.replace('&', ' ');
		mail =mail.replace('/', ' ');
		mail =mail.replace('(', ' ');
		mail =mail.replace(')', ' ');
		mail =mail.replace('=', ' ');
		mail =mail.replace('?', ' ');
		mail =mail.replace('^', ' ');
		mail =mail.replace('*', ' ');
		mail =mail.replace('°', ' ');
		mail =mail.replace(':', ' ');
		mail =mail.replace(';', ' ');
		mail =mail.replace(',', ' ');
		mail =mail.replace('.', ' ');
		mail =mail.replace('-', ' ');
		mail =mail.replace('_', ' ');
		mail =mail.replace('<', ' ');
		mail =mail.replace('>', ' ');
		mail =mail.replace('[', ' ');
		mail =mail.replace(']', ' ');
		mail =mail.replace('{', ' ');
		mail =mail.replace('}', ' ');
		mail =mail.replace('#', ' ');
		mail =mail.replace('+', ' ');
		mail =mail.replace('~', ' ');
	return mail;
}

}
