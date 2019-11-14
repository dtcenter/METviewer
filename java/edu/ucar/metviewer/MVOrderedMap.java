package edu.ucar.metviewer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Extension of Hashtable that returns the set of entries in the order that they were inserted into the table, via the method getOrderedEntries().
 */
public final class MVOrderedMap extends Hashtable {
  private static final long serialVersionUID = 1L;
  private final List listKeys = new ArrayList();

  /**
   * Default constructor creates an empty map
   */
  public MVOrderedMap() {
    super();
  }


  /**
   * Copy constructor
   *
   * @param copy Map to copy
   */
  public MVOrderedMap(final MVOrderedMap copy) {
    super();
    listKeys.addAll(copy.listKeys);
    Map.Entry[] listEntries = copy.getOrderedEntries();
    for (Map.Entry listEntry : listEntries) {
      put(listEntry.getKey(), listEntry.getValue());
    }
  }

  public MVOrderedMap(final MVOrderedMap copy, String s) {
    super();
    listKeys.addAll(copy.listKeys);
    Map.Entry[] listEntries = copy.getOrderedEntriesSeries();
    for (int i = 0; i < listEntries.length; i++) {
      super.put(i, listEntries[i].getValue());
    }
  }

  public List getListKeys() {
    return listKeys;
  }

  /**
   * Pseudo-override of the Hashtable get() method to cast the return value as a string.
   *
   * @param key Symbol (String) used as the index for the value
   * @return The inserted value, cast as a String
   */
  public String getStr(final String key) {
    return (String) get(key);
  }


  /**
   * Inserts the key/value pair into the table, putting the key into the ordered list at the position specified by index
   *
   * @param key   Symbol used as the index for value
   * @param value Data to store, indexed by key field
   * @param index An optional (zero-based) index for the key
   * @return The inserted value
   */
  public Object put(final Object key, final Object value, final int index) {
    if (!listKeys.contains(key)) {
      listKeys.add(index, key);
    }
    return super.put(key, value);
  }

  public void putSeries(final Object key, final Object value, final int index) {
    listKeys.add(index, key);
    super.put(index, value);
  }

  /**
   * Overrides put() in {@link Hashtable} to add the key/value pair to the table and the key at the end of the ordered list
   */
  public Object put(final Object key, final Object value) {
    return put(key, value, listKeys.size());
  }


  /**
   * Pseudo-overrides to ensure that the map input has type String
   */
  public Object putStr(final Object key, final Object value) {
    return put(key, value.toString());
  }


  /**
   * Overrides remove() in {@link Hashtable} to remove the key/value pair from the table
   */
  public Object remove(final Object key) {
    listKeys.remove(key);
    return super.remove(key);
  }

  public void clear() {
    listKeys.clear();
    super.clear();
  }

  /**
   * Accessor for the ordered keys contained in the table
   *
   * @return The ordered keys
   */
  public String[] getKeyList() {
    return (String[]) listKeys.toArray(new String[listKeys.size()]);
  }

  /**
   * Returns a {@link Map.Entry} array with the entries in the same order that they were inserted into the table.
   *
   * @return The ordered list of table entries
   */
  public Map.Entry[] getOrderedEntries() {
    Map.Entry[] ret = new Map.Entry[size()];
    for (Object o : entrySet()) {
      Map.Entry entry = (Map.Entry) o;
      ret[listKeys.indexOf(entry.getKey())] = entry;
    }
    return ret;
  }

  public Map.Entry[] getOrderedEntriesSeries() {
    Map.Entry[] ret = new Map.Entry[size()];
    for (Object o : entrySet()) {
      Map.Entry entry = (Map.Entry) o;
      ret[(int) entry.getKey()] = entry;
    }
    return ret;
  }

  public MVOrderedMap convertFromSeriesMap() {
    MVOrderedMap simpleMap = new MVOrderedMap();
    for (int i = 0; i < listKeys.size(); i++) {
      if (simpleMap.containsKey(listKeys.get(i))) {
        Object o = simpleMap.get(listKeys.get(i));
        String[] oldArr = new String[0];
        if (o instanceof String[]) {
          oldArr = (String[]) o;
        } else if (o instanceof String) {
          oldArr = new String[]{(String) o};
        }

        String[] curArr = new String[0];
        int newSize = oldArr.length;

        if (this.get(i) instanceof String) {
          curArr = new String[]{(String) this.get(i)};
        } else if (this.get(i) instanceof String[]) {
          curArr = (String[]) this.get(i);
        }
        newSize = newSize + curArr.length;
        String[] newArr = new String[newSize];
        System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
        System.arraycopy(curArr, 0, newArr, oldArr.length, curArr.length);


        simpleMap.put(listKeys.get(i), newArr);
      } else {
        simpleMap.put(listKeys.get(i), this.get(i));
      }
    }
    return simpleMap;
  }

  public Map.Entry[] getOrderedEntriesForSqlSeries() {
    MVOrderedMap simpleMap = convertFromSeriesMap();

    Map.Entry[] ret = new Map.Entry[simpleMap.size()];
    for (Object o : simpleMap.entrySet()) {
      Map.Entry entry = (Map.Entry) o;
      ret[simpleMap.listKeys.indexOf(entry.getKey())] = entry;
    }
    return ret;
  }

  /**
   * Returns a string representation of the MVOrderedMap in R declaration syntax
   */
  public String getRDecl() {
    return MVUtil.getRDecl(this);
  }

  public Map<String, Object> getYamlDecl() {
    return MVUtil.getYamlDecl(this);
  }

  public String getRDeclSeries() {
    return MVUtil.getRDecl(convertFromSeriesMap());
  }
  public Map<String, Object> getYamlDeclSeries() {
    return MVUtil.getYamlDecl(convertFromSeriesMap());
  }
}

