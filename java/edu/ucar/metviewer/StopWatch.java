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

  public static void main(String args[]) throws Exception {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();

    for (int i = 0; i < 10000000; i++) {
      Object obj = new Object();
    }
    stopWatch.stop();
    System.out.println(stopWatch.getFormattedDuration());

    stopWatch.start();
    for (int i = 0; i < 10000000; i++) {
      Object obj = new Object();
    }
    stopWatch.stop();
    System.out.println(stopWatch.getFormattedDuration());
    System.out.println(stopWatch.getFormattedTotalDuration());
  }


  public void start() {
    startTime = System.nanoTime();
    stopTime = null;

    duration = null;

  }

  public void stop() throws Exception {
    if (startTime == null) {
      throw new Exception("Start time was never set");
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

  public String getFormattedDuration() throws Exception {
    if (duration == null) {
      throw new Exception("Start or stop time was never set");
    }
    return MVUtil.formatTimeSpan(duration / 1000000);
  }

  public String getFormattedTotalDuration() throws Exception {
    if (totalDuration == null) {
      throw new Exception("Start or stop time was never set");
    }
    return MVUtil.formatTimeSpan(totalDuration / 1000000);
  }

}
