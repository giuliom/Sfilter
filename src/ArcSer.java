/**
 * Spam filter project for "Introduction to Machine Learning" class 2011 
 * at University of Bristol
 * @author Giulio Muntoni
 */

import java.io.*;
import java.util.*;

/**
 * A class used to implements Serializable interface to
 * easyly save the Arcs
 * I know it's not the smartest implementation but I needed a quick to made version of it
 * to test the Graph feature
 */
public class ArcSer implements Serializable{

	
	private static final long serialVersionUID = 1L;
	
	/**
	 *  Arcs destination name vector
	 */
	public Vector<String> str = new Vector<String>();
	
	/**
	 * Arc value vector
	 */
	public Vector<Double> dbl = new Vector<Double>();
	
	public ArcSer(){
		
	
	}
	
	
	public void put(String key,Double value){
		str.add(key);
		dbl.add(value);
	}
	
	
	public double get(String key){
		for (int i = 0; i < str.size(); i++) 
		{
			if (str.get(i).equals(key)) return dbl.get(i);
		}
		return -1;
	}
	
	
	public String get(int index){
		return str.get(index);
	}
	
	
	public int findIndex(String key){
		for (int i = 0; i < str.size(); i++) 
		{
			if (str.get(i).equals(key)) return i;
		}
		return -1;
		
	}
	
	
	public void remove(String key){
		for (int i = 0; i < str.size(); i++) 
		{
			if (str.get(i).equals(key)) 
			{
				str.remove(i);
				dbl.remove(i);
			}
		}
		
	}
	
	
	public int size(){
		return str.size();
	}
	
	
}
