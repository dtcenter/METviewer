/**
 * StopWatch.java Copyright UCAR (c) 2018. University Corporation for Atmospheric Research (UCAR),
 * National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL), P.O. Box
 * 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2018.
 */

package edu.ucar.metviewer;

/**
 * @author : tatiana $
 * @version : 1.0 : 05/11/18 15:02 $
 */
public class StopWatch {

  private Long startTime;
  private Long stopTime;
  private Long duration;
  private Long totalDuration;



  public void start() {
    startTime = System.nanoTime();
    stopTime = null;

    duration = null;

  }

  public void stop() throws StopWatchException {
    if (startTime == null) {
      throw new StopWatchException("Start time was never set");
    }
    stopTime = System.nanoTime();
    duration = stopTime - startTime;
    if (totalDuration == null) {
      totalDuration = duration;
    } else {
      totalDuration = totalDuration + duration;
    }

  }

  public Long getTotalDuration() {
    return totalDuration;
  }

  public String getFormattedDuration() throws StopWatchException {
    if (duration == null) {
      throw new StopWatchException("Start or stop time was never set");
    }
    return MVUtil.formatTimeSpan(duration / 1000000);
  }

  public String getFormattedTotalDuration() throws StopWatchException {
    if (totalDuration == null) {
      throw new StopWatchException("Start or stop time was never set");
    }
    return MVUtil.formatTimeSpan(totalDuration / 1000000);
  }

}
