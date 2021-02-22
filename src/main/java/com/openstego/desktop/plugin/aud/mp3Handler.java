/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openstego.desktop.plugin.aud;

//Imports
import java.io.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.*;
import  java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Patrick
 * Citation: mp3Stegz: https://sourceforge.net/projects/mp3stegz/
 */
public class mp3Handler {       
    //How many bytes to skip
    public int spacing = 8;
    //Maybe try treating as different endian
    
    /**
     * Look through the file 
     * searching for the bytes that indicate a frame header
     * @param audioFile - the byte array of a mp3 file
     * @return the index in the array as an integer
     */
    public int findFirstHeader(byte[] audioFile){
        int pos, results;
        
        pos = 0;
        results = -1; //If we failed to find the first frame
        //Loop through the file looking for the end of the header
        while(pos < audioFile.length){
            //Headers start with ff
            if ((audioFile[pos] == (byte)0xFF) && 
                ((audioFile[pos+1] == (byte)0xFB) || 
                        (audioFile[pos+1] == (byte)0xFA))) {
                results = pos;
                break; 
            }
            pos++;
        }
            
        return results;
    }
    
    /**
     * Finds the next frame header in an mp3
     * @param audioFile byte array of an mp3
     * @param prevHeadPos the index of the last header
     * @return the index of the next header as an int
     */
    public int findNextFrame(byte[] audioFile, int prevHeadPos) {
     int pos, result;
     pos = prevHeadPos+4;//Skip this header
     result = -1; //If we failed to find the next frame
     //int count = 0;
     //Loop through looking for the next frame
     while(pos < audioFile.length){
         
         
        // TESTING: currently have hard coded the header for the test file which is 
        // usually FF E3 XX 64
        if ((audioFile[pos] == (byte)0xFF) && 
            ((audioFile[pos+1] == (byte)0xE3)) && 
            (audioFile[pos+3] == (byte)0x64)) {
            result = pos;
            //count++;
            break; 
            }
        
        pos++;    
     }
     //System.out.printf("count of frames is %d\n", count);

     return result;
    }

    /**
     * Counts the number of frames, unused. 
     * @param audioFile byte array of an mp3
     * @return the number of frames as an integer
     */
    int frameCount(byte[] audioFile){
        int pos, posNext, count, result;
        
        result = -1; 
        pos = findFirstHeader(audioFile);
        count = 0;
        posNext = pos;
        while(posNext != -1){
            count = count+1;
            posNext = findNextFrame(audioFile, posNext);
        }
        result = count;
        return result;
    }

    byte[] hideFileAlt(byte[] cover, byte[] toHide){
        int pos, posNext, i, messInd, targInd;
        byte insertBit, messageByte;
        long data = toHide.length;
        
        //Check if the data will fit in the cover
        if((toHide.length*8)+32> frameCount(cover)){
            System.out.println("File too long to hide");
            java.lang.System.exit(0);
        }
        //Store the length in a byte array
        byte[] messLenArray = {
            (byte)((data >> 24) & 0xff),
            (byte)((data >> 16) & 0xff),
            (byte)((data >> 8) & 0xff),
            (byte)(data & 0xff),
        };
        targInd = 0; //Where in the message are we
        
        //pos is the position of a frame header, 
        //We hide a bit in the 3rd byte of a frame header
        pos = findFirstHeader(cover);
        
        //Copy each byte of the message size into the file bit by bit
        for(int j = 0; j < 4; j++){ // fixed 4 bytes for message size
            for(int k = 0; k < 8; k++){
                insertBit = (byte) (messLenArray[j] & (byte) 1);
                cover[pos+2] = (byte) ((cover[pos+2] & 254) | (insertBit)); //hide
                messLenArray[j] = (byte) (messLenArray[j] >> 1);
                pos = findNextFrame(cover, pos); //next frame header
            }
            
        }

        while(targInd <toHide.length){
            messageByte = toHide[targInd];
            for(i=0;i<8;i++){
                // get the next bit of the message
                insertBit = (byte) (messageByte & 1); //0x00000001

                // prepare the massage byte to extract the next bit
                messageByte = (byte) (messageByte >> 1);

                // insert the first 7 bits of the original file + 1 bit of the message
                cover[pos+2] = (byte) ((cover[pos+2] & 254) | (insertBit)); //hide
                pos = findNextFrame(cover, pos); //next frame header

            }
            targInd++;
        }
        return cover;        
    }
    
    byte[] unHideFileAlt(byte[] cover) {
        int pos, sizeByte, tempByte, messInd, i;
        byte extractedByte, messageByte;
        long size;
        
        sizeByte = 0;
        size = 0;
        messInd = 0;
        
        //pos is the position of a frame header, 
        //We hid a bit in the 3rd byte of a frame header
        pos = findFirstHeader(cover);

        // get the size of the message
        for(int j = 3; j >=0; j--){
            for(int k = 0; k <8; k++){
                extractedByte = cover[pos+2];
                tempByte = (extractedByte & 1);
                tempByte = (tempByte << k);
                sizeByte = (tempByte | sizeByte);
                pos = findNextFrame(cover, pos); //next frame header
                //System.out.println( Math.abs(sizeByte) << (j*8));
            }
            size = size | (Math.abs(sizeByte) << (j*8));
            //sizeByte = 0;
            //System.out.printf("size is %s\n", size);

        }       
        byte[] output = new byte[(int) size];
        System.out.println(size);
        //size = 0;//kill here
        while(messInd < size){
            //messageOutput.seek(messInd);

            //coverFile.seek(targInd);
            messageByte = 0; // 0x00000000

            // reconstruct 1 message byte out of 8 cover file bytes
            for(i = 0; i <8; i++){
                //System.out.printf("%x\n", pos);
                extractedByte = cover[pos+2];
                tempByte = (byte) (extractedByte & (byte) 1);
                tempByte = (byte) (tempByte << i);
                messageByte = (byte) (tempByte | messageByte);

                pos = findNextFrame(cover, pos); //next frame header
            }
            //messageOutput.write(messageByte);
            output[messInd] = messageByte;
            //System.out.printf("output byte is %s\n", output);
            messInd++;
        }

        return output;
    }

    //Just my testing function
    public void test(){
        int pos = 0;
        int nextPos = 0;
        byte[] head = new byte[4];
        int tot = 0;
        BitSet bit = new BitSet();
        
        try {
            byte[] testFile = Files.readAllBytes(Paths.get("test_files\\testInput.mp3"));
            byte[] hide = Files.readAllBytes(Paths.get("test_files\\testTextLonger.txt"));
            
          
            testFile = hideFileAlt(testFile, hide); 
          
            Files.write(Paths.get("test_files\\mp3Res.mp3"), testFile);
            
            byte[] result = unHideFileAlt(testFile);
            Files.write(Paths.get("test_files\\resultingMessage.txt"), result);
            
        } catch (IOException ex) {
            Logger.getLogger(mp3Handler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
}
