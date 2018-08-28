package edu.ucar.metviewer.test;

public interface TestDBManager {
    public int getNumberOfRows(String lineDataType) throws Exception;
    public void loadData(String fileName, String database) throws Exception;
}
