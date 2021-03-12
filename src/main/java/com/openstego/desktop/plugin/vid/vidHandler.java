/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openstego.desktop.plugin.vid;

import java.io.*;
import java.lang.*;
import java.io.Console;
import static java.lang.Thread.sleep;
import java.nio.file.Path;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 * @author 
 */
public class vidHandler {
    public String line, frame;
    
    public void unpackVid(String filename) {       
         
        try {
            
            //Get the fps, so the recombined video looks normal
            Runtime.getRuntime().exec("cmd /c   \"ffprobe -v error -select_streams v -of default=noprint_wrappers=1:nokey=1 -show_entries stream=r_frame_rate test_files\\testingVid\\" + filename+ " > test_files\\testingVid\\tmp.txt \"");
            sleep(3000); // sleep is neccessary so that we can find the file
            try {
                // create a reader instance
                BufferedReader br = new BufferedReader(new FileReader("test_files\\testingVid\\tmp.txt "));
                // read until end of file  
                while ((line = br.readLine()) != null) {
                    frame = line;
                }
                // close the reader
                br.close();
        
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            //Images
            Runtime.getRuntime().exec("cmd /c FFmpeg -i test_files\\testingVid\\" + filename+ " -q:v 2 -f image2 test_files\\testingVid\\image-%d.png"); 
            //audio
            Runtime.getRuntime().exec("cmd /c FFmpeg -i  test_files\\testingVid\\"+filename+" test_files\\testingVid\\audio.mp3"); 
            
        } catch (Exception e) {
            System.out.println("Somethings has gone wrong "); 
            e.printStackTrace(); 
        }
        
     
    }
    
    /**
     * 
     */
    public void packVid() {
        try {
            sleep(3000);
            System.out.println(frame);
            //Images
            Runtime.getRuntime().exec("cmd /c  /MIN start cmd.exe /K  "
                    + "\" FFmpeg -r " +frame+" -i \"test_files\\testingVid\\image-%d.png\" -i test_files\\testingVid\\audio.mp3 test_files\\testingVid\\result.avi\""); 
            
            
                   
            
        } catch (Exception e) {
            System.out.println("Somethings has gone wrong "); 
            e.printStackTrace(); 
        }    
    }
    byte[] getFile(String examine){
    String path = "test_files\\testingVid\\"+examine;
    FileInputStream fis = null;
    File file;
    
    byte[] cover;
    
    file = new File(path);
    cover = new byte[(int) file.length()];
    
    try{
        fis = new FileInputStream(file);
        fis.read(cover);
        fis.close();    
    }catch(IOException ioExp){
        ioExp.printStackTrace();
    }
    return cover;
    }
    
    /**
     * 
     * @param msg 
     */
    public void hideData(byte[] msg) {
        String userDirectory = System.getProperty("user.dir");//for full implenentation
        String testDir = "test_files\\testingVid"; //where we're testing
        String keyWord = "image"; //find the files for hiding
        
        int index = 0; //where in the message are we
        int j, i = 0;
        int targInd = 0;
        
        byte[] toHide;
        byte[] cover;
        byte[] result;
        byte insertBit;
                
        boolean first = true;
                
        FileInputStream fis = null;
        File file;

        //We want to get all the images to steg them
        //Creating a File object for directory
        File directoryPath = new File(testDir);
        //List of all files and directories
        String contents[] = directoryPath.list();
        ArrayList<String> aList = new ArrayList<String>(); 
        for(j = 0; j < contents.length; j++)
            aList.add(contents[j]);
        aList.sort(Comparator.comparing(String::length).thenComparing(Comparator.naturalOrder()));
        
        // the message length
        long data = msg.length;     
        //Store the length in a byte array
        byte[] messLenArray ={
            (byte)((data >> 24) & 0xff),
            (byte)((data >> 16) & 0xff),
            (byte)((data >> 8) & 0xff),
            (byte)(data & 0xff),
        };
        
        while(index < msg.length-1) {
            //For each item, if it is an image steg it
            //System.out.println(i);
                if(aList.get(i).contains(keyWord)){
                    
                    //If an image, open it
                    file = new File("test_files\\testingVid\\"+aList.get(i));
                    cover= getFile(contents[i]);
                    System.out.println("working on " + aList.get(i));

                    //Now that we have our image, steg it
                    if(first) {
                        targInd = 9;
                        // If first, insert the size
        
                        //Copy each byte of the message size into the file bit by bit
                        for(j = 0; j < 4; j++){ // fixed 4 bytes for message size
                            for(int k = 0; k < 8; k++){
                                insertBit = (byte) (messLenArray[j] & (byte) 1);
                                cover[targInd] = (byte) ((cover[targInd] & 254) | insertBit);
                                messLenArray[j] = (byte) (messLenArray[j] >> 1);
                                targInd += 1;
                            }
                        }
            
                        
                        overWrite("test_files\\testingVid\\"+aList.get(i), cover);
                        first = false;
                        i++;
                    }
                    else {
                        toHide = new byte[((cover.length-500)/8)]; //temporary cover
                        //Copy into temporary cover
                        for(int k = 0; k < toHide.length-1; k++){
                            toHide[k] = msg[index];
                            index++;
                            if (index >= msg.length-1)
                                break;
                        }
                        result = hideBytes(cover, toHide);
                        overWrite("test_files\\testingVid\\"+aList.get(i), result);  
                    }
                    
                    
                }//end if
                else {
                    i++;
                }

          //i++;  
        }//end while
    }
    
    /**
     * 
     * @param cover
     * @param toHide
     * @param first
     * @return 
     */
    byte[] hideBytes(byte[] cover, byte[] toHide){
        int targInd = 9; //using png, 8 byte header
        int byteSpread = 2;
        int messInd = 0;
        byte insertBit;
        byte messageByte;
        

        
        
            while(messInd < toHide.length){
                messageByte = toHide[messInd];
                messInd++;

                // For every bit in the message byte, insert it into the
                // resulting file starting from the smallest bit and working to
                // the largest
                for(int i = 0; i < 8; i++){
                    // get the next bit of the message
                    insertBit = (byte) (messageByte & 1); //0x00000001

                    // prepare the massage byte to extract the next bit
                    messageByte = (byte) (messageByte >> 1);
                    
                    cover[targInd] = (byte) ((cover[targInd] & 254) | insertBit);
                    targInd += 1;
                } 
            }        
            
        
        return cover;
    }
    
    /**
     * 
     * @param name
     * @param data 
     */
    public void overWrite(String name, byte[] data){
        try {
 		File file = new File("test_files\\testingVid\\"+name);
		if (file.exists()) {
			FileOutputStream fos = new FileOutputStream(file, false);
			fos.write(data);
			fos.close();
		}   
        } catch(Exception e) {
        
        }
    }
    
    public byte[] retrieve(String cover){
        byte[] msg;
        byte[] stegoData = new byte[1000];
        byte[] result;
        byte extractedByte, messageByte;
                
        int messInd = 0;
        int targInd = 0;
        int tempByte;
        int sizeByte = 0;
        int size = 0;
        int i = 0;   
        int index = 0;
        
        String keyWord = "image";
        String testDir = "test_files\\testingVid";
        
        boolean first = true;
        
        File file;
        
        //All files listed
        File directoryPath = new File(testDir);
        String contents[] = directoryPath.list();
        ArrayList<String> aList = new ArrayList<String>();
        
        for(int j = 0; j < contents.length; j++)
            aList.add(contents[j]);
        aList.sort(Comparator.comparing(String::length).thenComparing(Comparator.naturalOrder()));
        
        //We need to get the length
        if(first) {
            
            while(first) {      
                if(aList.get(i).contains(keyWord)) {
                    System.out.println("Working on "+aList.get(i));
                    targInd = 9;
                    for(int j = 3; j >=0; j--){
                        for(int k = 0; k <8; k++){
                            // if the current stego file byte is not a quality byte, jump randomly until you find a quality byte

                            extractedByte = stegoData[targInd];
                            tempByte = (extractedByte & 1);
                            tempByte = (tempByte << k);
                            sizeByte = (tempByte | sizeByte);
                            targInd += 1;
                        }
                        size = size | (Math.abs(sizeByte) << (j*8));
                    }
                    System.out.println(size);
                    first = false;
                    i++;
                }
                else{
                    i++;
                }
            }//end while
        }//end if 
      
        msg = new byte[size];  
        while(index < msg.length-1) {

            System.out.println("Processing " + aList.get(i));
            if(contents[i].contains(keyWord)){
               System.out.println("Processing " + aList.get(i));

                //If an image, open it
                file = new File("test_files\\testingVid\\"+aList.get(i));
                stegoData = getFile(aList.get(i));
                   
                if(!first) {
                    messInd = 9;
                    while((targInd < (stegoData.length-500)/8)){
                            // reconstruct the message bit by bit
                            messageByte = 0; // 0x00000000

                            // reconstruct 1 message byte out of 8 cover file bytes
                            for(int j = 0; j <8; j++){

                                extractedByte = stegoData[targInd];
                                tempByte = (byte) (extractedByte & (byte) 1);
                                tempByte = (byte) (tempByte << i);
                                messageByte = (byte) (tempByte | messageByte);

                                targInd += 1;
                            }
                            msg[messInd] = messageByte;
                            messInd++;
                    }//end while
                }
            }//end if
            else {
                i++;
            }

        i++;  
        }//end while
        return msg;
    }
    
    public byte[] extractBytes(){
    byte[] toRet = new byte[1];
    
    return toRet;
    }
    /**
     * 
     */
    public void cleanUp(){
        try {      
            Runtime.getRuntime().exec("cmd /c   Del test_files\\testingVid\\image-*.png ");
            Runtime.getRuntime().exec("cmd /c   Del test_files\\testingVid\\audio.mp3 ");
            Runtime.getRuntime().exec("cmd /c   Del test_files\\testingVid\\tmp.txt ");
            sleep(3000);
        } catch (Exception e) {
            
        }
        
    }
    public void test() {
        System.out.println("Testing start...");
        File file = new File("test_files\\testingVid\\msg2.txt");
        byte[] bArray = new byte[(int) file.length()];
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();    
        }catch(IOException ioExp){
            ioExp.printStackTrace();
        }
        cleanUp();
        //unpackVid("test.mp4");
        //hideData(bArray);
        //String coverVid;
        //byte[] tmp = retrieve("test.mp4");
        Path path = Paths.get("test_files\\testingVid\\result.txt");
        try {
            //Files.write(path, tmp);
            System.out.println(" ");
        } catch (Exception ex) {
            Logger.getLogger(vidHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
        File directoryPath = new File("test_files\\testingVid");
        String contents[] = directoryPath.list();
        System.out.println("Sorting...");
        ArrayList<String> aList = new ArrayList<String>();
        
        for(int j = 0; j < contents.length; j++)
            aList.add(contents[j]);
        aList.sort(Comparator.comparing(String::length).thenComparing(Comparator.naturalOrder()));
        try {
            for(int i = 0; i < contents.length; i++)
                System.out.println(aList.get(i));
        } catch (Exception ex) {
            Logger.getLogger(vidHandler.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        //unpackVid("test.mp4");
        //cleanUp();

        //packVid();
        return;
    }
}
