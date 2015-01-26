/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.*;

/**
 * @author crazywizard
 */
public class Mine extends MIDlet {

    public void startApp() {
        Form f = new Form("Object DB");
        RecordsReader reader = new RecordsReader("mine", true);
        //reader.deleteRecStore();
        Vector results = null;
        try {
            Hashtable attr = new Hashtable();                        
            attr.put("name", "Caroline");
            //Image img = Image.createImage("/ngugi.png");
            InputStream in = getClass().getResourceAsStream("/ngugi.png");
            byte[] data = new byte[in.available()];
            in.read(data, 0, in.available());
            //reader.saveRecord(attr, "Member");            
            //results = reader.readRecords(data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Hashtable attr = new Hashtable();
        attr.put("developer", "crazywizard");
        results = reader.readRecords(null, "name" , RecordsReader.ATTR_FIELD | RecordsReader.INT);
        Enumeration results_enum = results.elements();
        while(results_enum.hasMoreElements()){
            Hashtable result = (Hashtable) results_enum.nextElement();
            System.out.println(result.toString());
        }
        test();
        Display.getDisplay(this).setCurrent(f);
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
    
    private void test(){
        String status = null;
        int status_value = (status==null)?-1:Integer.parseInt(status);
        System.out.println(status_value);
    }
}
