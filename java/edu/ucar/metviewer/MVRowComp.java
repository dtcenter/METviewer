package edu.ucar.metviewer;

import java.util.Comparator;

/**
 * Wraps {@link Comparator} for use in discriminating rows of a {@link MVDataTable}.  One or both of the functions with {@link MVOrderedMap} arguments should be
 * implemented.  Default implementations are provided, so it not necessary to override both.  Also, most member functions of MVDataTable only require one or the
 * other (compare() or equals()).  The Comparator member functions are overridden to cast the Object arguments.
 */
public abstract class MVRowComp implements Comparator {

  /**
   * Compares the two input {@link MVDataTable} rows
   *
   * @param row1 First MVDataTable row in comparison
   * @param row2 Second MVDataTable row in comparison
   * @return A comparison metric value specifying the "difference" between the input rows
   */
  public int compare(MVOrderedMap row1, MVOrderedMap row2) {
    return 0;
  }

  /**
   * Overrides the compare() function of {@link Comparator} to cast the input Objects as {@link MVOrderedMap}s and pass them to the compare() function above.
   */
  public int compare(Object o1, Object o2) {
    return compare((MVOrderedMap) o1, (MVOrderedMap) o2);
  }

  /**
   * Indicates if the input row is "equal" to this comparator
   *
   * @param row MVDataTable row to consider
   * @return true if the input row is equal, false otherwise
   */
  public boolean equals(MVOrderedMap row) {
    return true;
  }

  /**
   * Overrides the equals() function of {@link Comparator} to cast the input Object as a {@link MVOrderedMap} and passes it to the equals() function above.
   */
  public boolean equals(Object obj) {
    return equals((MVOrderedMap) obj);
  }
}