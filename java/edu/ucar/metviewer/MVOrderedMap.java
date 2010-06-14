package edu.ucar.metviewer;

import java.util.*;
import java.util.regex.*;

/**
 * Extension of Hashtable that returns the set of entries in the order that they were inserted 
 * into the table, via the method getOrderedEntries().
 */
public class MVOrderedMap extends Hashtable {
	private static final long serialVersionUID = 1L;
	
	public ArrayList _listKeys = new ArrayList();
	
	public static void main(String[] args){
		System.out.println("----  MVOrderedMap Test  ----");

		String strMap = 
			"map(\n" +
			"  key1=list(val1_1,val1_2, val1_3);\n" +
			"  key2=val2_1;\n" +
			"  key3=val3_1;\n" +
			")\n";
		System.out.println("input:\n" + strMap + "\n");
		
		MVOrderedMap mapParsed = parseMap(strMap);
		System.out.println("output:\n" + mapParsed.getRDecl() + "\n");
		
		System.out.println("----  MVOrderedMap Test Complete  ----");
	}
	
	/**
	 * Default constructor creates an empty map
	 */
	public MVOrderedMap(){
		super();
	}
	
	/**
	 * Copy constructor
	 * @param copy Map to copy
	 */
	public MVOrderedMap(MVOrderedMap copy){
		super();
		_listKeys.addAll(copy._listKeys);
		Map.Entry[] listEntries = copy.getOrderedEntries();
		for(int i=0; i < listEntries.length; i++){
			put( listEntries[i].getKey(), listEntries[i].getValue() );
		}
	}
	
	/**
	 * Pseudo-override of the Hashtable get() method to cast the return value as a string.
	 * @param key Symbol (String) used as the index for the value
	 * @return The inserted value, cast as a String
	 */
	public String getStr(String key){ return (String)get(key); }
	
	/**
	 * Pseudo-override of the Hashtable get() method to cast the return value as an int.
	 * @param key Symbol (String) used as the index for the value
	 * @return The inserted value, cast as an int
	 */
	public int getInt(String key){ return Integer.parseInt(get(key).toString()); }
	
	/**
	 * Pseudo-override of the Hashtable get() method to cast the return value as an double.
	 * @param key Symbol (String) used as the index for the value
	 * @return The inserted value, cast as a double
	 */
	public double getDouble(String key){ return Double.parseDouble(get(key).toString()); }
	
	/**
	 * Pseudo-override of the Hashtable get() method to use the input as a pattern, using
	 * the * character as a wildcard, which returns a list of matched values
	 * @param key Pattern used to match keys for the value(s)
	 * @return List of matched values
	 */
	public String[] getStrPattern(String key){
		ArrayList listRet = new ArrayList();
		String strPat = key.replace(".", "\\.").replace("*", ".*").replace("(", "\\(").replace(")", "\\)");
		Pattern patKey = Pattern.compile(strPat);
		for(int i=0; i < _listKeys.size(); i++){
			String strKey = _listKeys.get(i).toString();
			Matcher matKey = patKey.matcher(strKey);
			if( matKey.matches() ){ listRet.add( getStr(strKey) ); }
		}
		return (String[])listRet.toArray(new String[]{});
	}
	
	/**
	 * Inserts the key/value pair into the table, putting the key into the ordered list at the
	 * position specified by index
	 * @param key Symbol used as the index for value 
	 * @param value Data to store, indexed by key field
	 * @param index An optional (zero-based) index for the key
	 * @return The inserted value
	 */
	public Object put(Object key, Object value, int index){
		if( !_listKeys.contains(key) ){ _listKeys.add(index, key); }
		return super.put(key, value);
	}
	
	/**
	 * Overrides put() in {@link Hashtable} to add the key/value pair to the table and the
	 * key at the end of the ordered list
	 */
	public Object put(Object key, Object value){ return put(key, value, _listKeys.size()); }
	
	/**
	 * Pseudo-override to ease the process of inserting integers into the map
	 */
	public Object putInt(Object key, int value){ return put(key, new Integer(value)); }

	/**
	 * Pseudo-override to ease the process of inserting doubles into the map
	 */
	public Object putDbl(Object key, double value){ return put(key, new Double(value)); }
	
	/**
	 * Pseudo-overrides to ensure that the map input has type String
	 */
	public Object putStr(Object key, Object value){ return put(key, value.toString()); }
	public Object putStr(Object key, int value)   { return put(key, "" + value);       }
	public Object putStr(Object key, double value){ return put(key, "" + value);       }

	/**
	 * Overrides remove() in {@link Hashtable} to remove the key/value pair from the table
	 */
	public Object remove(Object key){
		_listKeys.remove(key);
		return super.remove(key);
	}

	/**
	 * Accessor for the ordered keys contained in the table
	 * @return The ordered keys
	 */
	public String[] keyList(){
		return (String[])_listKeys.toArray(new String[]{});
	}
	
	/**
	 * Returns a {@link Map.Entry} array with the entries in the same order that they were 
	 * inserted into the table. 
	 * @return The ordered list of table entries
	 */
	public Map.Entry[] getOrderedEntries(){
		Map.Entry[] ret = new Map.Entry[size()];
		for(Iterator iterEntries = entrySet().iterator(); iterEntries.hasNext();){
			Map.Entry entry = (Map.Entry)iterEntries.next();
			ret[ _listKeys.indexOf(entry.getKey()) ] = entry; 
		}
		return ret;
	}
	
	/**
	 * Returns a string representation of the MVOrderedMap in R declaration syntax
	 */
	public String getRDecl(){
		return MVUtil.getRDecl(this);
	}
	
	public static final Pattern _patMap = Pattern.compile("(?s)map\\((.*)\\)");
	public static final Pattern _patList = Pattern.compile("(?s)list\\((.*)\\)");
	public static final Pattern _patPair = Pattern.compile("(?s)^\\s*([\\w\\d]+)\\s*=\\s*(.*)\\s*$");
	public static MVOrderedMap parseMap(String map){
		MVOrderedMap ret = new MVOrderedMap();

		Matcher matMap = _patMap.matcher(map.replace("\n", ""));
		if( !matMap.matches() ){
			return null;
		}
		
		String strData  = matMap.group(1);
		String[] listMembers = strData.split(";");
		for(int i=0; i < listMembers.length; i++){
			Matcher matPair = _patPair.matcher(listMembers[i]);
			if( !matPair.matches() ){
				return null;
			}
			
			String strKey = matPair.group(1);
			String strVal = matPair.group(2);
			
			String[] listVal = parseList(strVal);
			MVOrderedMap mapVal = parseMap(strVal);			
			Object objVal = strVal;
			if     ( null != listVal ){ objVal = listVal; }
			else if( null != mapVal  ){ objVal = mapVal;  }
			
			ret.put(strKey, objVal);
		}
		
		return ret;
	}
	
	public static String[] parseList(String list){
		String[] listData = null;
		Matcher matList = _patList.matcher(list);
		if( matList.matches() ){
			listData = matList.group(1).split(",");
			for(int i=0; i < listData.length; i++){ listData[i] = listData[i].trim(); }
		}
		return listData;
	}
}

