package edu.ucar.metviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Mutable data structure that stores data and facilitates subsetting of the rows to create individual data sets.  After the MVDataTable is loaded, subsets of
 * rows can be built using the subset command.
 */
public class MVDataTable {

  protected List _listFields = new ArrayList();
  protected List _listRows = new ArrayList();



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
    super();
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



  public String getStr(String field, int row) {
    if (0 > row || _listRows.size() - 1 < row || !_listFields.contains(field)) {
      return null;
    }
    return ((MVOrderedMap) _listRows.get(row)).getStr(field);
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
    if (_listFields.isEmpty()) {
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
