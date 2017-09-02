/**
 * Spam filter project for "Introduction to Machine Learning" class 2011 
 * at University of Bristol
 * @author Giulio Muntoni
 */

import java.util.Hashtable;

/**
 * This class contains all the info needed about a word to process the training data
 * 
 *
 */
public class Word{

/**
 *  Number of times the word is in a ham email (counts repeated words)
 */
protected int ham =0;
/**
 *  Number of times the word is in a spam email (counts repeated words)
 */
protected int spam = 0;

/**
 *  Number of times the word is in a ham email 
 */
public int emailham =0;
/**
 *  Number of times the word is in a ham email 
 */
public int emailspam =0;

/**
 * The word
 */
protected String name;

/**
 * An hashtable which stores all the links with other words and a value which is the sum
 * of the links in a spam email minus the sum of the links in a ham email
 * 
 */
public Hashtable<String, Double> arcs;


public Word(String s){
	this.name = s;
	arcs = new Hashtable<String, Double>();
}


public void addHam(){
	this.ham++;
}

public void addSpam(){
	this.spam++;
}

public void addHamConnection(String s){
	// TO BE FINISHED
}

public void addSpamConnection(String s){
	//TO BE FINISHED
}

public int getHam(){ return ham;}
public int getSpam() { return spam;}

}
