
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordFilter;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author crazywizard
 */
public class RecordsReader {
    private RecordStore rs = null;  //Record Store
    private String REC_STORE;   // Name of record store 
    private int type = -1;
    private boolean flag = true;
    private SearchFilter filter = null;
    private Comparator comp = null;
    
    /* Java Data Types -- powers of 2 for bitwise operations */ 
    static final int INT = 1;
    static final int STRING = 2;
    static final int BYTE = 4;
    static final int SHORT = 8;
    static final int LONG = 16;
    static final int FLOAT = 32;
    static final int DOUBLE = 64;
    static final int CHAR = 128;
    static final int CHARS = 256;    
    static final int BOOLEAN = 512;
        
    /* Extra types */
    static final int HASH = 10;
    
    /* Save Fields */
    static final int TYPE_FIELD = 1024;
    static final int ATTR_FIELD = 2048;
    static final int VALUE_FIELD = 4096;
    
    public RecordsReader(String rec_store){ /* Default Open Store */
        this.REC_STORE = rec_store;        
    }
    
    public RecordsReader(String rec_store, boolean flag){   /* Allow for NON-creation of record store */
        this.REC_STORE = rec_store;
        this.flag = flag;        
    }
    
    public void openRecStore(boolean flag){
        try{
            rs = RecordStore.openRecordStore(REC_STORE, flag);
        }catch (RecordStoreNotFoundException ex){
            System.out.println("Record Store Not Found.");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }//--End of openRecStore(boolean)
    
    public void closeRecStore(){        
        try{
            rs.closeRecordStore();
        }catch(NullPointerException ex){
            System.out.println("RecordStore NOT open.");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }//--End of closeRecStore();
    
    public void deleteRecStore(){
        if(RecordStore.listRecordStores() != null){            
            try{
                //Attempt to close recordstore first
                closeRecStore();
                RecordStore.deleteRecordStore(REC_STORE);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }//--End of deleteRecStore()
    
     /*--------------------------------------------------
     * Create arrays to write to record store
     *-------------------------------------------------*/
    public void saveRecord(Hashtable attr, byte[] data) throws IOException{
        openRecStore(flag);
        
        //Write data into internal byte array
        ByteArrayOutputStream strmBytes = new ByteArrayOutputStream();
        //Write Java data types into the above byte array
        DataOutputStream strmDataType = new DataOutputStream(strmBytes);
        
        byte[] record;
        
        // Check if type isset
        if(type == -1){
            type = 9;
        }
        
        /* Decompose hashtable to key/value string pairs */
        String attr_str = "";
        if(attr != null){            
            Enumeration keys = attr.keys();
            while(keys.hasMoreElements()){
                String key = (String) keys.nextElement();
                String value = attr.get(key).toString();
                attr_str += "|"+key+":"+value;
            }
            attr_str = attr_str.substring(1);System.out.println("Attributes: "+attr_str);
        }
        
        //Write Java data types
        strmDataType.writeInt(type);        
        strmDataType.writeUTF(attr_str.toString());        
        strmDataType.write(data);
        
        //Cleared any buffered data
        strmDataType.flush();
        
        //Get stream data into byte array and write into record
        record = strmBytes.toByteArray();
        int index;
        try {
            index = rs.addRecord(record, 0, record.length);
        } catch (RecordStoreException ex) {
            //Throw an error
            ex.printStackTrace();
        }
        
        //Close buffers
        strmBytes.close();
        strmBytes.close();
        
        closeRecStore();
        
        //Reset type
        type = -1;
        
        
    }//--End of saveRecord()
    
    public void saveRecord(int id, Hashtable attr, byte[] data) throws IOException{
        openRecStore(flag);
        
        //Write data into internal byte array
        ByteArrayOutputStream strmBytes = new ByteArrayOutputStream();
        //Write Java data types into the above byte array
        DataOutputStream strmDataType = new DataOutputStream(strmBytes);
        
        byte[] record;
        
        //check if type isset
        if(type == -1){
            type = 9;   //default setting
        }
        
        /* Decompose hashtable to key/value string pairs */
        String attr_str = "";
        if(attr != null){
            Enumeration keys = attr.keys();
            while(keys.hasMoreElements()){
                String key = (String) keys.nextElement();
                String value = attr.get(key).toString();
                attr_str += "|"+key+":"+value;
            }
            attr_str = attr_str.substring(1);
        }
        
        //Write Java data types
        strmDataType.writeInt(type);
        strmDataType.writeUTF(attr_str.toString());
        strmDataType.write(data);
        
        //Cleared any buffered data
        strmDataType.flush();
        
        //Get stream data into byte array and write into record
        record = strmBytes.toByteArray();
        int index;
        try {
            rs.setRecord(id, record, 0, record.length);
        } catch (InvalidRecordIDException ex) {
            ex.printStackTrace();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
        
        closeRecStore();
    }
    
    public void saveRecord(Hashtable attr, int data) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(data);
        dos.flush();
        
        type = INT;
        saveRecord(attr, bos.toByteArray());        
    }//--End of saveRecord(Hashtable, int)
    
    public void saveRecord(Hashtable attr, String data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeUTF(data);
        dos.flush();
        
        type = STRING;        
        saveRecord(attr, bos.toByteArray());
    }//--End of saveRecord(Hashtable, String)
    
    public void updateRecord(int id, Hashtable attr, int data, boolean overwrite) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(data);
        dos.flush();
        
        type = INT;
        
        /* First read contents of record */
        Hashtable result = readRecord(id);
        
        //Check for attributes
        if(result.containsKey("attr") && overwrite==false){
            Hashtable attribs = (Hashtable) result.get("attr");
            //(over)write new attributes
            Enumeration attr_enum = attr.keys();
            while(attr_enum.hasMoreElements()){
                String key = attr_enum.nextElement().toString();
                String value = attr.get(key).toString();
                attribs.put(key, value);
            }
            //Update changes with new byte data
            saveRecord(id, attribs, bos.toByteArray());
        }else{
            //Update record with new byte data
            saveRecord(id, attr, bos.toByteArray());
        }
        
    }//--End of updateRecord(int, Hashtable, int, boolean)
    
    /* Overload function */
    public void updateRecord(int id, Hashtable attr, int data) throws IOException{
        updateRecord(id, attr, data, false);
    }//--End of update
    
    public void updateRecords(int id, Hashtable attr, String data, boolean overwrite) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeUTF(data);
        dos.flush();
        
        type = STRING;
        
        /* First read contents of record */
        Hashtable result = new Hashtable();
        
        //Check for attributes
        if(result.containsKey("attr") && overwrite==false){
            Hashtable attribs = (Hashtable) result.get("attr");
            //(over)write new attributes
            Enumeration attr_enum = attr.keys();
            while(attr_enum.hasMoreElements()){
                String key = attr_enum.nextElement().toString();
                String value = attr.get(key).toString();
                attribs.put(key, value);
            }
            //Update changes with new byte data
            saveRecord(id, attribs, bos.toByteArray());
        }else{
            //Update record with new byte data
            saveRecord(id, attr, bos.toByteArray());
        }
    }//--End of updateRecords(int, Hashtable, String, boolean)
    
    /* Overload function */
    public void updateRecords(int id, Hashtable attr, String data) throws IOException{
        updateRecords(id, attr, data, false);
    }//--End of updateRecords(int, Hashtable, String)
    
    public void updateRecord(int id, Hashtable attr, byte[] data, boolean overwrite) throws IOException{
        type = INT;
        
        /* First read contents of record */
        Hashtable result = readRecord(id);
        
        //Check for attributes 
        if(result.containsKey("attr") && overwrite==false){
            Hashtable attribs = (Hashtable) result.get("attr");
            //(over)write new attributes
            Enumeration attr_enum = attr.keys();
            while(attr_enum.hasMoreElements()){
                String key = attr_enum.nextElement().toString();
                String value = attr.get(key).toString();
                attribs.put(key, value);
            }
            //Update changes with new byte data
            saveRecord(id, attribs, data);
        }else{
            //Update record with new byte data
            saveRecord(id, attr, data);
        }
    }//--End of updateRecord(int, Hashtable, byte[], boolea)
    
    public void updateRecord(int id, Hashtable attr, byte[] data) throws IOException{
        updateRecord(id, attr, data, false);
    }//--End of updateRecord(int, Hashtable, byte[])
    
    
    public Vector readRecords(){
        openRecStore(flag);
                
        try {
            RecordEnumeration re = rs.enumerateRecords(filter, comp, false);
            //Reset the filter and comparator for future use
            filter = null;
            comp = null;
            
            Vector results = new Vector();
            while(re.hasNextElement()){
                //Get data into the byte array
                int next = re.nextRecordId();
                //Array to hold each record
                byte[] recData = new byte[rs.getRecordSize(next)];
                
                //Read from the specified byte array
                ByteArrayInputStream strmBytes = new ByteArrayInputStream(recData);
                
                //Read Java data types from the above byte array
                DataInputStream strmDataType = new DataInputStream(strmBytes);
                
                if(rs.getRecordSize(next) > recData.length){
                    //Expand byte array
                    recData = new byte[rs.getRecordSize(next)];
                }
                rs.getRecord(next, recData, 0);
                
                //Read back the data types                
                int type = strmDataType.readInt();
                                                
                //Categorise results by type
                switch(type){
                    case INT:{
                        //Create holding Hashtable
                        Hashtable result = new Hashtable();
                        
                        String attr_str = strmDataType.readUTF();
                        if(attr_str.length() > 1){
                            //Reconstruct attrs
                            String[] attr_array = split(attr_str, "|");
                            Hashtable attr = new Hashtable();
                            for(int i=0; i<attr_array.length; i++){
                                String[] attr_parts = split(attr_array[i], ":");                                
                                attr.put(attr_parts[0], attr_parts[1]);
                            }
                            //Add to result
                            result.put("attr", attr);                           
                        }
                                                
                        //Extract Value                        
                        Integer value = new Integer(strmDataType.readInt());
                        
                        //Pack singular result
                        result.put("value", value);
                        result.put("id", new Integer(next));
                        
                        results.addElement(result);
                        
                        break;
                    }case STRING:{
                        //Create holding hashtable
                        Hashtable result = new Hashtable();
                        
                        String attr_str = strmDataType.readUTF();                        
                        if(attr_str.length() > 1){
                            //Reconstruct attrs
                            String[] attr_array = split(attr_str, "|");
                            Hashtable attr = new Hashtable();
                            for(int i=0; i<attr_array.length; i++){
                                String[] attr_parts = split(attr_array[i], ":");
                                attr.put(attr_parts[0], attr_parts[1]);
                            }
                            //Add to result
                            result.put("attr", attr);
                        }
                                                
                        //Extract Value 
                        String value = strmDataType.readUTF();
                        result.put("value", value);
                        result.put("id", new Integer(next));
                        results.addElement(result);
                        
                        break;
                    }case BYTE:{
                        //Create holding hashtable
                        Hashtable result = new Hashtable();
                        
                        String attr_str = strmDataType.readUTF();
                        if(attr_str.length() > 1){
                            //Reconstruct attrs
                            String[] attr_array = split(attr_str, "|");
                            Hashtable attr = new Hashtable();
                            for(int i=0; i<attr_array.length; i++){
                                String[] attr_parts = split(attr_array[i], ":");
                                attr.put(attr_parts[0], attr_parts[1]);
                            }
                            //Add to result
                            result.put("attr", attr);                            
                        }
                        
                        //Extract Value
                        byte[] value = new byte[strmDataType.available()];
                        strmDataType.readFully(value);
                        result.put("value", value);
                        result.put("id", new Integer(next));
                        results.addElement(result);
                        
                        break;
                    }
                    default:
                        //Do nothing?
                        break;
                }
                
                //Reset so read starts at beginning of array
                strmBytes.reset();
                
            }
            closeRecStore();
            return results;
        } catch (RecordStoreNotOpenException ex) {
            ex.printStackTrace();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }        
        
        closeRecStore();
        return null;
    }//--End of readRecords()
    
    /* Overload function */
    public Vector readRecords(String search, Object attrs, int field){
        //Create search filter
        search = (search != null) ? search : "";    //Guard against null
        filter = new SearchFilter(search);
        
        //Create comparator
        comp = new Comparator(field, attrs);
        
        return readRecords();        
    }//--End of readRecords(String)
    
    public Vector readRecords(Hashtable search){
        //Create search filter
        filter = new SearchFilter(search);
        
        return readRecords();
    }//--End of readRecords(Hashtable)
    
    public Vector readRecords(byte[] search){
        //Create search filter
        filter = new SearchFilter(search);
        
        return readRecords();
    }
    
    public Hashtable readRecord(int id){
        openRecStore(flag);
        
        try{
            //Array to hold record
            byte[] recData = new byte[rs.getRecordSize(id)];
            
            //Read from specified byte array
            ByteArrayInputStream strmBytes = new ByteArrayInputStream(recData);
            
            //Read Java data types from the above byte array
            DataInputStream strmDataType = new DataInputStream(strmBytes);
            
            rs.getRecord(id, recData, 0);
            
            //Read back the data 
            int type = strmDataType.readInt();
                        
            //Categorise results by type
            switch(type){
                case INT:{
                    //Create holding hashtable
                    Hashtable result = new Hashtable();
                    
                    String attr_str = strmDataType.readUTF();
                    if(attr_str.length() > 1){
                        //Reconstruct attrs
                        String[] attr_array = split(attr_str, "|");
                        Hashtable attr = new Hashtable();
                        for(int i=0; i<attr_array.length; i++){
                            String[] attr_parts = split(attr_array[i], ":");
                            attr.put(attr_parts[0], attr_parts[1]);
                        }
                        //Add to result
                        result.put("attr", attr);
                    }
                    
                    //Extract value
                    Integer value = new Integer(strmDataType.readInt());
                    
                    //Pack into singular result
                    result.put("value", value);
                    result.put("id", new Integer(id));
                    
                    closeRecStore();
                    return result;                    
                }case STRING:{
                    //
                }case BYTE:{
                    //Create holding hashtable
                    Hashtable result = new Hashtable();
                    
                    String attr_str = strmDataType.readUTF();
                    if(attr_str.length() > 1){
                        //Reconstruct attrs
                        String[] attr_array = split(attr_str, "|");
                        Hashtable attr = new Hashtable();
                        for(int i=0; i<attr_array.length; i++){
                            String[] attr_parts = split(attr_array[i], ":");
                            attr.put(attr_parts[0], attr_parts[1]);
                        }
                        //Add to result
                        result.put("attr", attr);
                    }
                    
                    //Extract value
                    byte[] value = new byte[strmDataType.available()];
                    strmDataType.readFully(value);
                    result.put("value", value);
                    result.put("id", new Integer(id));
                    
                    closeRecStore();
                    return result;
                }
                default:
                    closeRecStore();
                    return null;                    
            }
            
        } catch (RecordStoreNotOpenException ex) {
            ex.printStackTrace();
        } catch (InvalidRecordIDException ex) {System.out.println("That record doesn't exist..");
            ex.printStackTrace();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }     
        closeRecStore();
        
        return null;
    }//--End of readRecord(int)
    
    /* Helper Methods */
    public static int toInt(byte[] bytes){
        int result = 0;
        for(int i=0; i<4; i++){
            result = (result << 8) - Byte.MIN_VALUE + (int)bytes[i];
        }
        
        return result;
    }//--End of toInt(byte[])
    
    public static String[] split(String original, String separator)            
                {
                    if(original.length() < 1)   //Guard against empty strings
                        return new String[0];
                    
                    Vector nodes=new Vector();
                    //Parse nodes into Vector
                    int index=original.indexOf(separator);
                    while(index>=0){
                        nodes.addElement(original.substring(0, index));
                        original=original.substring(index+separator.length());
                        index=original.indexOf(separator);
                    }
                    //Get the last node
                    nodes.addElement(original);
                    //Create split array
                    String[] result=new String[nodes.size()];
                    if(nodes.size()>0){
                        for(int loop=0; loop<nodes.size(); loop++)
                        {
                            result[loop]=(String)nodes.elementAt(loop);

                        }
                    }
                    return result;
             }//--End of split(String, String)
    
    public static boolean equals(byte[] b1, byte[] b2){
        /**
        * compares the two given byte arrays for equality
        * 
        * @param b1 first byte array
        * @param b2 second byte array
        * @returns true if the arrays have the same contents, false otherwise.
        */
        if(b1 == null && b2 == null){
            return true;
        }
        if(b1 == null || b2 == null){
            return false;
        }
        if(b1.length != b2.length){
            return false;
        }
        for(int i=0; i<b1.length; i++){
            if(b1[i] != b2[i]){
                return false;
            }
        }
        return true;
    }//--End of compare(byte[], byte[])
}

//**************************************
// Filter class for searching
//**************************************
class SearchFilter implements RecordFilter{
    int type = -1;    
    private String searchText = null;
    private Hashtable searchHash = null;
    private byte[] searchData = null;
    
    public SearchFilter(String searchText){
        //Text to find
        this.searchText = searchText.toLowerCase();
        this.type = RecordsReader.STRING;
    }
    
    public SearchFilter(Hashtable searchHash){
        //Attributes to search through
        this.searchHash = searchHash;
        this.type = RecordsReader.HASH;
    }
    
    public SearchFilter(byte[] searchData){
        /* Use as last resort to search equivalent byte data */
        this.searchData = searchData;
        this.type = RecordsReader.BYTE;
    }

    public boolean matches(byte[] candidate) {
        /* Categorise search operation by type */
        switch(type){
            case RecordsReader.INT: {
                //
                break;
            }case RecordsReader.STRING: {
            try {                
                //Read from specified byte array
                ByteArrayInputStream strmBytes = new ByteArrayInputStream(candidate);
                
                //Read Java data types from the above byte array
                DataInputStream strmDataType = new DataInputStream(strmBytes);
                
                //Read back the data
                int data_type = strmDataType.readInt();
                strmDataType.readUTF();
                byte[] data = new byte[strmDataType.available()];
                strmDataType.readFully(data);
                String str = new String(data).toLowerCase();
                
                //Does the text exist?
                if(searchText != null && str.indexOf(searchText) != -1 && data_type == RecordsReader.STRING)
                    return true;
                else
                    return false;                
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            }case RecordsReader.HASH:{
            try {
                //Read from specified byte array
                ByteArrayInputStream strmBytes = new ByteArrayInputStream(candidate);
                
                //Read Java data types from the above byte array
                DataInputStream strmDataType = new DataInputStream(strmBytes);
                
                //Read back the data
                strmDataType.readInt();
                String attr_str = strmDataType.readUTF();
                
                //Create holding hashtable
                Hashtable result = new Hashtable();
                if(attr_str.length() > 1){
                    //Reconstruct attrs
                    String[] attr_array = RecordsReader.split(attr_str, "|");
                    Hashtable attr = new Hashtable();
                    for(int i=0; i<attr_array.length; i++){
                        String[] attr_parts = RecordsReader.split(attr_array[i], ":");
                        attr.put(attr_parts[0], attr_parts[1]);                        
                    }
                    Enumeration search_keys = this.searchHash.keys();
                    while(search_keys.hasMoreElements()){
                        Object key = search_keys.nextElement();     //Maintain type                        
                        if(attr.containsKey(key) && attr.contains(this.searchHash.get(key))){System.out.println("Match Found");
                            return true;
                        }
                    }
                    
                    //No match Found
                    return false;
                }                                
            } catch (IOException ex) {
                ex.printStackTrace();                
            }
            break;            
            }case RecordsReader.BYTE:{
                /* Use as last resort to test the byte data equivalent */
                try{
                    //Read from specified byte array
                    ByteArrayInputStream strmBytes = new ByteArrayInputStream(candidate);
                    
                    //Read Java data types from the above byte array
                    DataInputStream strmDataType = new DataInputStream(strmBytes);
                    
                    //Read back the data
                    strmDataType.readInt();
                    strmDataType.readUTF();
                    byte[] data = new byte[strmDataType.available()];                    
                    strmDataType.readFully(data);
                    
                    return RecordsReader.equals(data, this.searchData);
                } catch (IOException ex){
                    ex.printStackTrace();                   
                }
                break;
            }
            default:
                return false;                
        }
        return false;   //Default return for unexpected exit
    }
}

/* Sorting class by comparing two inputs in lexigraphical order */
class Comparator implements RecordComparator{
    private ByteArrayInputStream strmBytes_1 = null;
    private ByteArrayInputStream strmBytes_2 = null;
    private DataInputStream strmDataType_1 = null;
    private DataInputStream strmDataType_2 = null;
    private int FIELD = -1;
    private Object comp_obj;
    
    public Comparator(int FIELD, Object comp_obj){
        this.FIELD = FIELD;
        this.comp_obj = comp_obj;
    }

    public int compare(byte[] rec1, byte[] rec2) {
        try{
            int maxsize = Math.max(rec1.length, rec2.length);
            byte[] recData = new byte[maxsize];
            
            //Read from specified byte array
            strmBytes_1 = new ByteArrayInputStream(rec1);
            strmBytes_2 = new ByteArrayInputStream(rec2);
            
            //Read Java data types from the above byte array
            strmDataType_1 = new DataInputStream(strmBytes_1);
            strmDataType_2 = new DataInputStream(strmBytes_2);
            
                        
            switch(FIELD){
                case RecordsReader.TYPE_FIELD:{
                    //Sort according to type
                    break;
                }
                case RecordsReader.ATTR_FIELD:{
                    /* Sort according to attrs */
                    strmDataType_1.readInt();
                    strmDataType_2.readInt();
                    
                    //Reconstruct attrs                    
                    String[] attr_array_1 = RecordsReader.split(strmDataType_1.readUTF(), "|");
                    String[] attr_array_2 = RecordsReader.split(strmDataType_2.readUTF(), "|");
                    
                    Hashtable attr_1 = new Hashtable();
                    for(int i=0; i<attr_array_1.length; i++){
                        String[] attr_parts = RecordsReader.split(attr_array_1[i], ":");
                        attr_1.put(attr_parts[0], attr_parts[1]);
                    }
                    
                    Hashtable attr_2 = new Hashtable();
                    for(int i=0; i<attr_array_2.length; i++){
                        String[] attr_parts = RecordsReader.split(attr_array_2[i], ":");
                        attr_2.put(attr_parts[0], attr_parts[1]);
                    }
                    
                    //Determine type for sorting
                    if((FIELD & RecordsReader.INT) == 1){
                        //Compare record #1 and #2
                        if(attr_1.containsKey(comp_obj) && attr_2.containsKey(comp_obj)){
                            int x1 = ((Integer)attr_1.get(comp_obj)).intValue();
                            int x2 = ((Integer)attr_2.get(comp_obj)).intValue();
                            if(x1==x2)
                                return RecordComparator.EQUIVALENT;
                            else if(x1<x2)
                                return RecordComparator.PRECEDES;
                            else
                                return RecordComparator.FOLLOWS;
                        }
                    }else{  //Default to String
                        //Compare record #1 and #2
                        if(attr_1.containsKey(comp_obj) && attr_2.containsKey(comp_obj)){
                            String str_1 = (String) attr_1.get(comp_obj);
                            String str_2 = (String) attr_2.get(comp_obj);
                            
                            //Compare record #1 and #2
                            if(str_1.compareTo(str_2) == 0)
                                return RecordComparator.EQUIVALENT;
                            else if(str_1.compareTo(str_2) < 0)
                                return RecordComparator.PRECEDES;
                            else return RecordComparator.FOLLOWS;
                        }
                    }
                    
                    break;
                } case RecordsReader.VALUE_FIELD:{
                    /* Sort according to value */
                    //Read record data type
                    int type_1 = strmDataType_1.readInt();
                    int type_2 = strmDataType_2.readInt();
                                        
                    switch(type_1 & type_2){
                        case RecordsReader.INT:{
                            //Data is integer
                            strmDataType_1.readUTF();
                            strmDataType_2.readUTF();
                            
                            int x1 = strmDataType_1.readInt();
                            int x2 = strmDataType_2.readInt();
                            
                            //Compare record #1 and #2
                            if(x1==x2)
                                return RecordComparator.EQUIVALENT;
                            else if(x1<x2)
                                return RecordComparator.PRECEDES;
                            else
                                return RecordComparator.FOLLOWS;                            
                        }
                        case RecordsReader.STRING:{
                            //Data is String
                            strmDataType_1.readUTF();
                            strmDataType_2.readUTF();
                            
                            String str_1 = strmDataType_1.readUTF();
                            String str_2 = strmDataType_2.readUTF();
                            
                            //Compare record #1 and #2
                            if(str_1.compareTo(str_2) == 0)
                                return RecordComparator.EQUIVALENT;
                            else if(str_1.compareTo(str_2) < 0)
                                return RecordComparator.PRECEDES;
                            else return RecordComparator.FOLLOWS;
                           
                        }
                        default:
                            return RecordComparator.EQUIVALENT;                            
                    }                    
                }
                
            }
        } catch (IOException ex){
            //return RecordComparator.EQUIVALENT;
        }
        return 0;        
    }
    //
}
