package edu.ucar.metviewer.test;

public interface TestDBManager {
    public int getNumberOfRows(String lineDataType) throws Exception;
    public void loadData(String fileName, String database) throws Exception;
    public void checkCreateDatabase(String host, String userName,
                                                String password,
                                                String database);
}
