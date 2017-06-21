package edu.ucar.metviewer;

import java.util.Map;

/**
 * An instantiable (default) instance of the Map.Entry class for manipulating Map.Entry[] structures
 *
 * @author : tatiana $
 * @version : 1.0 : 31/05/17 12:29 $
 */
public class MVMapEntry implements Map.Entry {

  Object key = null;
  Object value = null;

  public MVMapEntry(Object k, Object v) {
    key = k;
    value = v;
  }

  public boolean equals(Object o) {
    return
      o instanceof MVMapEntry &&
        (null == ((MVMapEntry) o).getKey() ? null == key : ((MVMapEntry) o).getKey().equals(key)) &&
        (null == ((MVMapEntry) o).getValue() ? null == value : ((MVMapEntry) o).getValue().equals(value));
  }

  public Object getKey() {
    return key;
  }

  public Object getValue() {
    return value;
  }

  public int hashCode() {
    return (null == key ? 0 : key.hashCode()) ^ (null == value ? 0 : value.hashCode());
  }

  public Object setValue(Object o) {
    Object old = value;
    value = o;
    return old;
  }

  public String toString() {
    return key.toString() + "=" + value.toString();
  }
}


