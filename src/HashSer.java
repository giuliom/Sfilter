/**
 * Spam filter project for "Introduction to Machine Learning" class 2011 
 * at University of Bristol
 * @author Giulio Muntoni
 */

import java.io.*;
import java.util.Hashtable;

/**
 * 
 * Class used only to make the Hashtable serializable
 *
 */
public class HashSer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Hashtable<String,Double> hash;
	
	public HashSer(Hashtable<String,Double> hash)
	{
		this.hash = hash;
	}
	
}
