package edu.ucar.metviewer;

import edu.ucar.metviewer.db.DatabaseInfo;
import edu.ucar.metviewer.db.LoadDatabaseManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static edu.ucar.metviewer.test.util.TestUtil.FILE_SEPARATOR;
import static edu.ucar.metviewer.test.util.TestUtil.ROOT_DIR;
import static org.junit.Assert.*;

public class MVLoadTest {
    static MVLoad mvLoad;
    static String testDataDir = ROOT_DIR + FILE_SEPARATOR + "met_data";
    static ByteArrayOutputStream log;
    static PrintStream printStream;
    static ByteArrayOutputStream logSql;
    static ByteArrayOutputStream logError;
    static PrintWriter printStreamSql;
    static PrintStream printStreamError;
    static DatabaseInfo databaseInfo;
    static LoadDatabaseManager databaseManager;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void main() {
    }

    @Test
    public void getUsage() {
        List<String> argsList = new ArrayList<>();
        argsList.add("-help");  //invalid option
        MVLoad.main(argsList.toArray(new String[argsList.size()]));
    }

    @Test
    public void processFile() {
    }
}