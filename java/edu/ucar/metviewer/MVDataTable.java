package edu.ucar.metviewer;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Mutable data structure that stores data and facilitates subsetting of the rows to create individual data sets.  After the MVDataTable is loaded, subsets of
 * rows can be built using the subset command.
 */
public class MVDataTable {

  protected ArrayList _listFields = new ArrayList();
  protected ArrayList _listRows = new ArrayList();

  /**
   * Create a MVDataTable which contains the contents of the input {@ ResultSet}.
   *
   * @param res The results of a database query to be loaded
   */
  public MVDataTable(ResultSet res) {
    try {
      ResultSetMetaData met = res.getMetaData();

      for (int i = 1; i <= met.getColumnCount(); i++) {
        addField(met.getColumnLabel(i));
      }

      String[] listFields = (String[]) (_listFields.toArray(new String[]{}));
      while (res.next()) {
        MVOrderedMap row = new MVOrderedMap();
        for (String listField : listFields) {
          row.put(listField, res.getString(listField));
        }
        addRow(row);
      }
    } catch (Exception e) {
      System.out.println("  **  ERROR: Caught " + e.getClass() + " in MVDataTable(): " + e.getMessage());
      _listFields = new ArrayList();
      _listRows = new ArrayList();
    }
  }

  /**
   * Create a MVDataTable with the input row as the single row in the table and initialize the field names to the keys in the input row.
   *
   * @param row New row which contains field information for the table
   */
  public MVDataTable(MVOrderedMap row) {
    addRow(row);
  }

  /**
   * Create an empty MVDataTable whose list of fields is set to the input list.
   *
   * @param fields The initial list of fields
   */
  public MVDataTable(String[] fields) {
    _listFields.addAll(Arrays.asList(fields));
  }

  /**
   * Create an empty MVDataTable
   */
  public MVDataTable() {
  }

  /**
   * Copy constructor
   */
  public MVDataTable(MVDataTable tab) {
    for (int i = 0; i < tab._listFields.size(); i++) {
      _listFields.add(tab._listFields.get(i));
    }
    MVOrderedMap[] listRows = tab.getRows();
    for (MVOrderedMap listRow : listRows) {
      _listRows.add(new MVOrderedMap(listRow));
    }
  }

  /**
   * Accessor for the MVDataTable fields
   */
  public String[] getFields() {
    return (String[]) _listFields.toArray(new String[]{});
  }

  /**
   * Accessor for the MVDataTable rows.  Each hastable is indexed by the field names.
   */
  public MVOrderedMap[] getRows() {
    return (MVOrderedMap[]) _listRows.toArray(new MVOrderedMap[]{});
  }

  /**
   * Builds a new MVDataTable by applying the input {@link MVRowComp} equals() function to each row of this MVDataTable.  Each row is a {@link MVOrderedMap} of
   * values, stored by field names.
   *
   * @param c Discriminates which rows are included into the subset, using the equals() function
   * @return A new MVDataTable containing the appropriate subset of rows
   */
  public MVDataTable getRows(MVRowComp c) {
    MVDataTable ret = new MVDataTable();

    ret._listFields = (ArrayList) _listFields.clone();
    MVOrderedMap[] rows = (MVOrderedMap[]) _listRows.toArray(new MVOrderedMap[]{});
    for (MVOrderedMap row : rows) {
      //if( c.equals(rows[i]) ){ ret._listRows.add(new MVOrderedMap(rows[i])); }
      if (c.equals(row)) {
        ret._listRows.add(row);
      }
    }

    return ret;
  }

  public MVOrderedMap getRow(int row) {
    if (0 > row || _listRows.size() - 1 < row) {
      return null;
    }
    return (MVOrderedMap) _listRows.get(row);
  }

  public String getStr(String field, int row) {
    if (0 > row || _listRows.size() - 1 < row || !_listFields.contains(field)) {
      return null;
    }
    return ((MVOrderedMap) _listRows.get(row)).getStr(field);
  }

  public double getDbl(String field, int row) {
    return Double.parseDouble(getStr(field, row));
  }

  public double getInt(String field, int row) {
    return Integer.parseInt(getStr(field, row));
  }

  /**
   * Accessor for individual columns
   */
  public String[] getColumn(String field) {
    if (!_listFields.contains(field)) {
      return null;
    }
    String[] listRet = new String[_listRows.size()];
    for (int i = 0; i < _listRows.size(); i++) {
      listRet[i] = ((MVOrderedMap) _listRows.get(i)).getStr(field);
    }
    return listRet;
  }

  /**
   * Accessor for individual columns, cast as an integer
   */
  public int[] getIntColumn(String field) {
    if (!_listFields.contains(field)) {
      return null;
    }
    int[] listRet = new int[_listRows.size()];
    for (int i = 0; i < _listRows.size(); i++) {
      listRet[i] = ((MVOrderedMap) _listRows.get(i)).getInt(field);
    }
    return listRet;
  }

  /**
   * Accessor for individual columns, cast as a double
   */
  public double[] getDoubleColumn(String field) {
    if (!_listFields.contains(field)) {
      return null;
    }
    double[] listRet = new double[_listRows.size()];
    for (int i = 0; i < _listRows.size(); i++) {
      listRet[i] = ((MVOrderedMap) _listRows.get(i)).getDouble(field);
    }
    return listRet;
  }

  /**
   * The number of rows in the table
   */
  public int getNumRows() {
    return _listRows.size();
  }

  /**
   * The number of fields in the table
   */
  public int getNumFields() {
    return _listFields.size();
  }

  /**
   * Add and initialize a new field in the MVDataTable at the specified index
   *
   * @param field The name of the new field
   * @param val   An optional initial value for the new field in all rows
   * @param index An optional (zero-based) index of the new field
   */
  public void addField(String field, String val, int index) {
    if (!_listFields.contains(field)) {
      _listFields.add(index, field);
    }
    for (Object _listRow : _listRows) {
      MVOrderedMap row = (MVOrderedMap) _listRow;
      row.put(field, val, index);
    }
  }

  public void addField(String field, String val) {
    addField(field, val, _listFields.size());
  }

  public void addField(String field) {
    addField(field, "");
  }

  public void addFields(String[] fields) {
    for (String field : fields) {
      addField(field);
    }
  }

  /**
   * Remove the field and all associated data from the MVDataTable.  The MVDataTable is not affected if the field is not found.
   *
   * @param field Name of the field to remove, along with accompanying data
   */
  public void removeField(String field) {
    if (!_listFields.contains(field)) {
      return;
    }

    _listFields.remove(field);
    MVOrderedMap[] rows = (MVOrderedMap[]) _listRows.toArray(new MVOrderedMap[]{});
    for (MVOrderedMap row : rows) {
      row.remove(field);
    }
  }

  /**
   * Indicates whether or not the input field is present among the fields in the able
   *
   * @param field Name of the field to test
   * @return true if the input field is present among this table's field names, false otherwise
   */
  public boolean containsField(String field) {
    return _listFields.contains(field);
  }

  /**
   * Replace a field name, specified by the index field, with the new name.
   *
   * @param field zero-based index of the field to replace
   * @param name  replacement name
   */
  public void setFieldName(int field, String name) {
    if (0 > field || _listFields.size() <= field) {
      return;
    }

    String strOrig = (String) _listFields.get(field);
    _listFields.remove(field);
    _listFields.add(field, name);

    for (Object _listRow : _listRows) {
      MVOrderedMap row = (MVOrderedMap) _listRow;
      String strVal = (String) row.get(strOrig);
      row.put(name, strVal);
    }
  }

  /**
   * Add a row to the MVDataTable.  If fields are missing from the input {@link MVOrderedMap}, they are initialized to "".  Extra data in the row structure is
   * removed.  If the MVDataTable is empty when this function is called, the keys of the input row are set as the fields.
   *
   * @param row   Contains the values for the row, mapped by the MVDataTable field names
   * @param index The (zero-based) index of the new field
   */
  public void addRow(MVOrderedMap row, int index) {
    Map.Entry[] vals = (Map.Entry[]) row.entrySet().toArray(new Map.Entry[]{});

    //  if the MVDataTable is currently empty, set the fields to the row keys
    if (1 > _listFields.size()) {
      String[] listKeys = row.getKeyList();
      _listFields.addAll(Arrays.asList(listKeys));
    }

    //  remove inappropriate values from the input row
    for (Map.Entry val : vals) {
      if (!_listFields.contains(val.getKey())) {
        System.out.println("  **  WARNING: MVDataTable.addRow() removed key " + val.getKey());
        row.remove(val.getKey());
      }
    }

    //  set default values for unspecified fields
    for (Object _listField : _listFields) {
      String strField = (String) _listField;
      if (!row.containsKey(strField)) {
        row.put(strField, "");
      }
    }

    _listRows.add(row);
  }

  public void addRow(MVOrderedMap row) {
    addRow(row, _listRows.size());
  }

  public void addRows(MVOrderedMap[] rows) {
    for (int i = 0; null != rows && i < rows.length; i++) {
      addRow(rows[i]);
    }
  }

  public void removeRow(int row) {
    if (0 > row || _listRows.size() - 1 < row) {
      return;
    }
    _listRows.remove(row);
  }

  /**
   * Set a particular element in the MVDataTable to the value specified.  If the specified row or field is not valid, no element is set.
   *
   * @param field Name of the field to set
   * @param row   Index (zero-based) of the row to set
   * @param val   The value to set
   */
  public void set(String field, int row, String val) {
    if (!_listFields.contains(field) || 0 > row || _listRows.size() <= row) {
      return;
    }

    MVOrderedMap tableRow = (MVOrderedMap) _listRows.get(row);
    tableRow.put(field, val);
  }

  /**
   * Set a group of elements, speicified by input {@link MVRowComp} c, to the value specified. The equals() function of input c is used to determine which rows
   * have their field value set to val.
   *
   * @param field Name of the field to set
   * @param c     Indicates which rows to set, via the equals() function
   * @param val   The value to set
   */
  public void set(String field, MVRowComp c, String val) {
    if (!_listFields.contains(field)) {
      return;
    }

    MVOrderedMap[] rows = (MVOrderedMap[]) _listRows.toArray(new MVOrderedMap[]{});
    for (MVOrderedMap row : rows) {
      if (c.equals(row)) {
        row.put(field, val);
      }
    }
  }

  /**
   * Set a group of elements in a particular row of the MVDataTable.  The field/value pairs are specified by input {@link MVOrderedMap} vals.  If the
   * (zero-based) row index is not valid, no elements are set.
   *
   * @param vals Contains the field/value pairs to set
   * @param row  Index (zero-based) of the row to set
   */
  public void set(MVOrderedMap vals, int row) {
    if (0 > row || _listRows.size() <= row) {
      return;
    }

    Map.Entry[] listVals = (Map.Entry[]) vals.entrySet().toArray(new Map.Entry[]{});
    for (Map.Entry listVal : listVals) {
      set((String) listVal.getKey(), row, (String) listVal.getValue());
    }
  }

  /**
   * Set a group of elements in rows specified by input {@link MVRowComp} c. The field/value pairs are specified by input {@link MVOrderedMap} vals. The
   * equals() function of input c is used to determine which rows have their field values set.
   *
   * @param vals Contains the field/value pairs to set
   * @param c    Inidcates which rows to set, via the equals() function
   */
  public void set(MVOrderedMap vals, MVRowComp c) {
    Map.Entry[] listVals = (Map.Entry[]) vals.entrySet().toArray(new Map.Entry[]{});
    for (Map.Entry listVal : listVals) {
      set((String) listVal.getKey(), c, (String) listVal.getValue());
    }
  }

  /**
   * Set a column of data, specified by input field, to an array of values, specified by input vals.  If the input array has a different length than the number
   * of rows in the table, the values are either partially used or recycled.
   *
   * @param field The field name of the column to set
   * @param vals  Values to set the column to
   */
  public void setColumn(String field, String[] vals) {
    if (null == vals || 1 > vals.length) {
      return;
    }
    int intValIndex = 0;
    for (int i = 0; i < getNumRows(); i++) {
      set(field, i, vals[intValIndex]);
      intValIndex = (intValIndex == vals.length - 1 ? 0 : intValIndex + 1);
    }
  }

  /**
   * Return the maximum field name/value length in characters for the specified field
   *
   * @param field The field name of the column to examine
   * @return Size in characters of the max field name/value length
   */
  public int getMaxFieldLength(String field) {
    if (!_listFields.contains(field)) {
      return 0;
    }
    int intLength = field.length();
    MVOrderedMap[] rows = (MVOrderedMap[]) _listRows.toArray(new MVOrderedMap[]{});
    for (MVOrderedMap row : rows) {
      String strVal = (String) row.get(field);
      if (intLength < strVal.length()) {
        intLength = strVal.length();
      }
    }
    return intLength;
  }

  public void clear() {
    for (Object _listRow : _listRows) {
      ((MVOrderedMap) _listRow).clear();
    }
    _listRows.clear();
  }

  public void sort(String field, boolean desc) {
    final String strField = field;
    final boolean boolDesc = desc;
    MVOrderedMap[] listRows = getRows();
    Arrays.sort(listRows, new MVRowComp() {
      public int compare(MVOrderedMap row1, MVOrderedMap row2) {
        if (boolDesc) {
          return row1.getStr(strField).compareTo(row2.getStr(strField));
        } else {
          return row2.getStr(strField).compareTo(row1.getStr(strField));
        }
      }
    });
    _listRows.clear();
    _listRows.addAll(Arrays.asList(listRows));
  }

  public void sort(String field) {
    sort(field, true);
  }
}
