package edu.ucar.metviewer;

import java.sql.*;
import java.util.*;

/**
 * Mutable data structure that stores data and facilitates subsetting of the rows to create individual
 * data sets.  After the MVDataTable is loaded, subsets of rows can be built using the subset command. 
 */
public class MVDataTable{
	public ArrayList _listFields = new ArrayList();
	public ArrayList _listRows = new ArrayList();

	/**
	 * Create a MVDataTable which contains the contents of the input {@ ResultSet}.
	 * @param res The results of a database query to be loaded
	 */
	public MVDataTable(ResultSet res){
		try{
			ResultSetMetaData met = res.getMetaData();
	
			for(int i=1; i <= met.getColumnCount(); i++){
				addField(met.getColumnLabel(i));
			}
			
			String[] listFields = (String[])(_listFields.toArray(new String[]{}));
			while( res.next() ){
				MVOrderedMap row = new MVOrderedMap();
				for(int i=0; i < listFields.length; i++){
					row.put(listFields[i], res.getString(listFields[i]));
				}
				addRow(row);
			}
		}catch(Exception e){
			System.out.println("  **  ERROR: Caught " + e.getClass() + " in MVDataTable(): " + e.getMessage());
			_listFields = new ArrayList();
			_listRows = new ArrayList();
		}
	}
	
	/**
	 * Create a MVDataTable with the input row as the single row in the table and initialize the 
	 * field names to the keys in the input row.
	 * @param row New row which contains field information for the table
	 */
	public MVDataTable(MVOrderedMap row){
		addRow(row);
	}
	
	/**
	 * Create an empty MVDataTable whose list of fields is set to the input list.
	 * @param fields The initial list of fields
	 */
	public MVDataTable(String[] fields){
		_listFields.addAll( Arrays.asList(fields) );
	}
	
	/**
	 * Create an empty MVDataTable
	 */
	public MVDataTable(){}
	
	/**
	 * Accessor for the MVDataTable fields
	 */
	public String[] getFields(){ return (String[])_listFields.toArray(new String[]{}); }
	
	/**
	 * Accessor for the MVDataTable rows.  Each hastable is indexed by the field names.
	 */
	public MVOrderedMap[] getRows(){ return (MVOrderedMap[])_listRows.toArray(new MVOrderedMap[]{}); }
	
	/**
	 * The number of rows in the table
	 */
	public int getNumRows(){ return _listRows.size(); }
	
	/**
	 * The number of fields in the table
	 */
	public int getNumFields(){ return _listFields.size(); } 
	
	/**
	 * Add and initialize a new field in the MVDataTable at the specified index
	 * @param field The name of the new field
	 * @param val An optional initial value for the new field in all rows
	 * @param index An optional (zero-based) index of the new field
	 */
	public void addField(String field, String val, int index){
		if( !_listFields.contains(field) ){ _listFields.add(index, field); }
		for(int i=0; i < _listRows.size(); i++){
			MVOrderedMap row = (MVOrderedMap)_listRows.get(i);
			row.put(field, val, index);
		}
	}
	public void addField(String field, String val){ addField(field, val, _listFields.size()); }
	public void addField(String field)			  { addField(field, ""); 					  }
	
	/**
	 * Remove the field and all associated data from the MVDataTable.  The MVDataTable is not affected
	 * if the field is not found. 
	 * @param field Name of the field to remove, along with accompanying data
	 */
	public void removeField(String field){
		if( !_listFields.contains(field) ){ return; }
		
		_listFields.remove(field);
		MVOrderedMap[] rows = (MVOrderedMap[])_listRows.toArray(new MVOrderedMap[]{});
		for(int i=0; i < rows.length; i++){
			rows[i].remove(field);
		}
	}
	
	/**
	 * Replace a field name, specified by the index field, with the new name.
	 * @param field zero-based index of the field to replace
	 * @param name replacement name
	 */
	public void setFieldName(int field, String name){
		if( 0 > field || _listFields.size() <= field ){ return; }
		
		String strOrig = (String)_listFields.get(field);
		_listFields.remove(field);
		_listFields.add(field, name);
		
		for(int i=0; i < _listRows.size(); i++){
			MVOrderedMap row = (MVOrderedMap)_listRows.get(i);
			String strVal = (String)row.get(strOrig);
			row.put(name, strVal);
		}
	}
	
	/**
	 * Add a row to the MVDataTable.  If fields are missing from the input {@link MVOrderedMap}, they
	 * are initialized to "".  Extra data in the row structure is removed.  If the MVDataTable is
	 * empty when this function is called, the keys of the input row are set as the fields.
	 * @param row Contains the values for the row, mapped by the MVDataTable field names
	 * @param index The (zero-based) index of the new field  
	 */
	public void addRow(MVOrderedMap row, int index){
		Map.Entry[] vals = (Map.Entry[])row.entrySet().toArray(new Map.Entry[]{});
		
		//  if the MVDataTable is currently empty, set the fields to the row keys
		if( 1 > _listFields.size() ){
			String[] listKeys = {};
			if( row instanceof MVOrderedMap ){
				listKeys = ((MVOrderedMap)row).keyList();
			} else {
				listKeys = (String[])row.keySet().toArray(new String[]{});
			}
			_listFields.addAll( Arrays.asList(listKeys) );
		}
		
		//  remove inappropriate values from the input row
		for(int i=0; i < vals.length; i++){
			if( !_listFields.contains(vals[i].getKey()) ){
				System.out.println("  **  WARNING: MVDataTable.addRow() removed key " + vals[i].getKey());
				row.remove(vals[i].getKey());
				continue;
			}
		}
		
		//  set default values for unspecified fields
		for(int i=0; i < _listFields.size(); i++){
			String strField = (String)_listFields.get(i);
			if( !row.containsKey( strField ) ){
				row.put(strField, "");
			}
		}
		
		_listRows.add(row);
	}
	public void addRow(MVOrderedMap row){ addRow(row, _listRows.size()); }

	/**
	 * Set a particular element in the MVDataTable to the value specified.  If the specified 
	 * row or field is not valid, no element is set.    
	 * @param field Name of the field to set
	 * @param row Index (zero-based) of the row to set
	 * @param val The value to set
	 */
	public void set(String field, int row, String val){		
		if( !_listFields.contains(field) || 0 > row || _listRows.size() <= row ){ return; }
		
		MVOrderedMap tableRow = (MVOrderedMap)_listRows.get(row);
		tableRow.put(field, val);
	}
	
	/**
	 * Set a group of elements, speicified by input {@link MVRowComp} c, to the value specified.  
	 * The equals() function of input c is used to determine which rows have their field value 
	 * set to val.
	 * @param field Name of the field to set
	 * @param c Indicates which rows to set, via the equals() function
	 * @param val The value to set
	 */
	public void set(String field, MVRowComp c, String val){
		if( !_listFields.contains(field) ){ return; }
		
		MVOrderedMap[] rows = (MVOrderedMap[])_listRows.toArray(new MVOrderedMap[]{});
		for(int i=0; i < rows.length; i++){
			if( c.equals(rows[i]) ){
				rows[i].put(field, val);
			}
		}
	}
	
	/**
	 * Set a group of elements in a particular row of the MVDataTable.  The field/value pairs
	 * are specified by input {@link MVOrderedMap} vals.  If the (zero-based) row index is not 
	 * valid, no elements are set. 
	 * @param vals Contains the field/value pairs to set
	 * @param row Index (zero-based) of the row to set  
	 */
	public void set(MVOrderedMap vals, int row){
		if( 0 > row || _listRows.size() <= row ){ return; }
		
		Map.Entry[] listVals = (Map.Entry[])vals.entrySet().toArray(new Map.Entry[]{});
		for(int i=0; i < listVals.length; i++){
			set((String)listVals[i].getKey(), row, (String)listVals[i].getValue());
		}
	}
	
	/**
	 * Set a group of elements in rows specified by input {@link MVRowComp} c.  The field/value
	 * pairs are specified by input {@link MVOrderedMap} vals.  The equals() function of input 
	 * c is used to determine which rows have their field values set. 
	 * @param vals Contains the field/value pairs to set
	 * @param c Inidcates which rows to set, via the equals() function
	 */
	public void set(MVOrderedMap vals, MVRowComp c){
		Map.Entry[] listVals = (Map.Entry[])vals.entrySet().toArray(new Map.Entry[]{});
		for(int i=0; i < listVals.length; i++){
			set((String)listVals[i].getKey(), c, (String)listVals[i].getValue());
		}
	}
	
	/**
	 * Builds a new MVDataTable by applying the input {@link MVRowComp} equals() function to each row
	 * of this MVDataTable.  Each row is a {@link MVOrderedMap} of values, stored by field names.
	 * @param c Discriminates which rows are included into the subset, using the equals() function
	 * @return A new MVDataTable containing the appropriate subset of rows
	 */
	public MVDataTable subset(MVRowComp c){
		MVDataTable ret = new MVDataTable();
		
		ret._listFields = (ArrayList)_listFields.clone();
		MVOrderedMap[] rows = (MVOrderedMap[])_listRows.toArray(new MVOrderedMap[]{});
		for(int i=0; i < rows.length; i++){
			//if( c.equals(rows[i]) ){ ret._listRows.add(new MVOrderedMap(rows[i])); }
			if( c.equals(rows[i]) ){ ret._listRows.add(rows[i]); }
		}
		
		return ret;
	}
	
	/**
	 * Return the maximum field name/value length in characters for the specified field
	 * @param field The field name of the column to examine
	 * @return Size in characters of the max field name/value length
	 */
	public int getMaxFieldLength(String field){
		if( !_listFields.contains(field) ){ return 0; }
		int intLength = field.length();
		MVOrderedMap[] rows = (MVOrderedMap[])_listRows.toArray(new MVOrderedMap[]{});
		for(int i=0; i < rows.length; i++){
			String strVal = (String)rows[i].get(field);
			if( intLength < strVal.length() ){ intLength = strVal.length(); }
		}		
		return intLength;
	}
	
	public void clear(){
		for(int i=0; i < _listRows.size(); i++){ ((MVOrderedMap)_listRows.get(i)).clear(); }
		_listRows.clear();
	}
}
