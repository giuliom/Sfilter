/**
 * Spam filter project for "Introduction to Machine Learning" class 2011 
 * at University of Bristol
 * @author Giulio Muntoni
 */

import java.util.Hashtable;
import java.util.Scanner;
import java.util.Vector;
import java.io.*;

/**
 * @NOTE
 * My original idea was to use a graph of words to recreate the structure of a spam mail.
 * I was creating a node for every word, and the arcs were the occurrencies of two words 
 * in the same email, so the complexity was huge, but I managed to filter the data to have only
 * the most important connection. I stored only a data in the arc: (#spam connection - #ham connnection)
 * and I keeped the arcs with the higest |arc value|
 * In testing, probably because I had problems with the algorithm tuning/implementation or because 
 * the algorithm is not as good as I thought, I haven't seen big improvements yet, but seems to work a bit.
 * Also I had no time to try to optmize it more.
 */




/**
 * Main class of the spam filter
 * @author giulio
 *
 */

public class filter{

/**
* Class used for training the filter
*/
static Training trainModule;

static FileReader testfr;
static File trainf;


/**
 * Set Stats Variables to be read in a file
 */
protected static int hamcount = 0; // #of Docs ham
protected static int spamcount = 0; // #of Docs spam
protected static int wordsnumber = 0; // #of words in the training set
protected static int hamwords = 0; // #of ham words
protected static int spamwords = 0; // #of spam words

//HASHTABLES

/**
 * Training data hashtable for p(w|ham)
 */
protected static Hashtable<String,Double> ham;

/**
 * Training data hashtable for p(w|spam)
 */
protected static Hashtable<String,Double> spam;

/**
 * Training data hashtable for arcs
 * 
 */
protected static Hashtable<String, ArcSer> arcs;

/**
 * Arc feature. weight =0  it's disabled 
 * 
 */
protected static double linkw = 1;


/**
 *  CLASSIFICATION FUNCTION
 * @param testfr
 * @return 0="ham" || 1="spam" 
 */
public static double classification(FileReader testfr)
{
	/**
	 * Read the mail, removes useless charachters, write it on a temporary file
	 */
	Vector<String> mailvoc = new Vector<String>();
	BufferedWriter bw = null;
	try {
		bw = new BufferedWriter(new FileWriter("output"));
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	
	BufferedReader br = new BufferedReader(testfr);
	Scanner scan = new Scanner(br); 
	String mail = null;	

	while (scan.hasNext())
	{
		String s = scan.next();
		if (mail == null){ mail = s;} else {mail = new String(mail+" "+s);}
	}	

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
	 * Read the temporary file and adds the words contained in the training data 
	 * to the list of words which are going to be used for Naive Bayes
	 */
	BufferedReader outr = null;
	try {
		outr = new BufferedReader(new FileReader("output"));
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	Scanner scan2 = new Scanner(outr);
	
while (scan2.hasNext())
{
	String s = scan2.next();
	if (ham.get(s) != null || spam.get(s) != null )
		{
			mailvoc.add(s);
		}
	}
	
	
	
/**
 * Spam Probability calc, using Naive Bayes
 */
	double cspam = 0;
	double arcspam = 0;
	
	for (int i = 0; i<mailvoc.size();i++)
	{
		double pwspam =0;
		double awspam =0;
		if (spam.get(mailvoc.get(i)) !=null)
		{
			pwspam = spam.get(mailvoc.get(i));
			pwspam = Math.log(pwspam);
			cspam +=pwspam;
			
			/**
			 * Calculate the Arc features spam. 
			 * 
			 */
			ArcSer links= arcs.get(mailvoc.get(i));
			if (links !=null)
			{
				for (int j =0;j<links.size();j++)
				{
					String s = links.get(j);
					if (links.get(s) >0) awspam += links.get(s);
				}
				
				arcspam += awspam;
			}
		}
	}
	
	
	//features to be added here
	cspam += Math.log((double)spamcount/(double)(spamcount+hamcount));
	cspam += Math.log(arcspam)*linkw;
	
	
	
	
	/**
	 * Ham Probability calc using Naive Bayes
	 */
	double cham = 0;
	double archam = 0;
	
	for (int i = 0; i<mailvoc.size();i++)
	{
		double pwham =0;
		double awham =0;
		
		if (ham.get(mailvoc.get(i)) !=null)
		{
			pwham = ham.get(mailvoc.get(i));
			pwham = Math.log(pwham);
			cham += pwham;
			
			/**
			 * Calculate the Arc features ham
			 * 
			 */
			ArcSer links = arcs.get(mailvoc.get(i));
			
			if (links !=null)
			{
				for (int j =0;j<links.size();j++)
				{
					String s = links.get(j);
					if (links.get(s) <0) awham += -links.get(s);
				}
				
				archam += awham;
			}
		}
	}
	
	//features to be added here
	cham += Math.log((double)hamcount/((double)spamcount+hamcount));
	cham += Math.log(archam)*linkw;
	
	
	File f = new File("output");
	if (f.exists()) f.delete();
	
	double result =0;
	
	/**
	 * Compares the two results and returns the classification of the mail.
	 */
	if (cspam <cham) { result =1;}
		else { result=0; }
	return result;
}



/**
 * MAIN
 * @param args
 * @throws FileNotFoundException
 */
@SuppressWarnings("unchecked")
public static void main(String[] args) throws FileNotFoundException {

if (args.length < 1) return;



/**
 * Training
 */

if (args[0].compareTo("-tr") ==0){

trainModule = new Training();

trainModule.preProcessFiles(args);
return;
}

/**
 * Read the training data from these files
 * - hamdb
 * - spamdb
 * - arcdb
 * - stats
 */
{
	FileInputStream hos = null;
	try {
		hos = new FileInputStream("hamdb");
		ObjectInputStream hout = null;
		try {
			hout = new ObjectInputStream(hos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			HashSer hamser = null;
			try {
				hamser = (HashSer)hout.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ham = hamser.hash;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		hout.close();
		hos.close();
		
		FileInputStream spos = new FileInputStream("spamdb");
		ObjectInputStream spout = new ObjectInputStream(spos);
		HashSer spamser = (HashSer)spout.readObject();
		spam = spamser.hash;
		spout.close();
		spos.close();
		
		/**
		 * 
		 *Arc Feature db
		 */
		
		FileInputStream aros = new FileInputStream("arcdb");
		ObjectInputStream arout = new ObjectInputStream(aros);
		arcs = (Hashtable<String,ArcSer>)arout.readObject();
		arout.close();
		aros.close();
		
		
		FileReader stats = new FileReader("stats");
		BufferedReader bf = new BufferedReader(stats);
		hamcount = bf.read();
		spamcount= bf.read();
		wordsnumber = bf.read();
		hamwords = bf.read();
		spamwords = bf.read();
		bf.close();
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}

double errors =0;


/**
 * Results:
 */

for (int a=0; a<args.length;a++)
{
File fr = new File(args[a]);
if (fr.isDirectory())
{
	String filename[] = fr.list();
	for(int i=0;i< filename.length;i++)
	{
	FileReader reader =null;
	reader = new FileReader(fr+"/"+filename[i]);
	double result = classification(reader);
	


	if (filename[i].contains("ham") && result !=0){ errors++;System.out.print(filename[i]+" result: ham\n");}
	if (filename[i].contains("spam") && result !=1){ errors++;System.out.print(filename[i]+"result: spam\n");}

	}
	
}
else
{
	FileReader reader =null;
	reader = new FileReader(fr);
	double result = classification(reader);
	if (result ==0) System.out.print("ham\n"); else System.out.print("spam\n");
	
	
}

//System.out.println("Result:"+errors+"/"+fr.list().length+" = "+errors/(double)fr.list().length);
}








}

/**
 * Removes useless charachters for classification from the mail
 * @param mail
 * @return
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
