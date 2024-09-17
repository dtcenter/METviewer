package edu.ucar.metviewer.test.mvutil;

import edu.ucar.metviewer.MVUtil;
import org.junit.Test;

import org.junit.runner.notification.Failure;
import org.w3c.dom.Document;



import static org.junit.Assert.*;


public class TestMVUtil {
    @Test
    public void testCreateDocument(){
        try{
            System.out.println("Testing CreateDocument...");
            assertTrue(MVUtil.createDocument() instanceof Document );
            assertTrue(MVUtil.createDocument() != null);

        }catch (javax.xml.parsers.ParserConfigurationException e){
            fail("ParserConfigurationException was raised while creating a document");
        }
    }




}
