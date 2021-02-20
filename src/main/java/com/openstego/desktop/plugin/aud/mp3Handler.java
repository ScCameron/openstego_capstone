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
/**
 *
 * @author Patrick
 * Citation: mp3Stegz: https://sourceforge.net/projects/mp3stegz/
 */
public class mp3Handler {
            
    //How many bytes to skip
    public int spacing = 8;
    
    /**
     * Look through the file 4 bytes at a time, 
     * searching for the bytes that indicate a frame header
     * @param audioFile - the byte array of a mp3 file
     * @return the index in the array as an integer
     */
    public int findFirstHeader(byte[] audioFile){
        int pos, results;
        
        pos = 0;
        results = -1; //If we failed to find the first frame
        //Loop through the file looking for the end of the header
        while(pos < audioFile.length-1){
                if(((audioFile[pos] == (byte)0xff)) 
                    && ((audioFile[pos+1] == (byte)0xfb) 
                    || (audioFile[pos+1] == (byte)0xfa))){
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
     int posNow, result;
     posNow = prevHeadPos+4;//Skip this header
     result = -1; //If we failed to find the next frame
     //Loop through looking for the next frame
     while(posNow < audioFile.length-1){
            if(((audioFile[posNow] == (byte)0xff)) 
                    && ((audioFile[posNow+1] == (byte)0xfb) 
                    || (audioFile[posNow+1] == (byte)0xfa))){
                result = posNow;
                break; 
            }
            posNow++;    
     }

     return result;
    }

    /**
     * Simple check for if a frame is the first, unused
     * @param audioFile byte array of an mp3
     * @param pos the index of the header
     * @return true/false
     */
    boolean isFirstFrame(byte[] audioFile, int pos){
        return pos == (findFirstHeader(audioFile));
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
            //System.out.println(posNext - pos);//test
            //System.out.println(posNext);//test
            pos = posNext;
            count = count+1;
            posNext = findNextFrame(audioFile, posNext);
        }
        //System.out.println("Frames");//test
        //System.out.println(count);//test
        result = count;
        return result;
    }
    
    /**
     * Counts the number of bytes in the frames, unused
     * @param audioFile byte array of an mp3
     * @return the number of bytes as an int
     */
    int byteCount(byte[] audioFile){
        int pos, posNext, count;

        pos = findFirstHeader(audioFile);
        count = 0;
        posNext = pos;
        while(posNext != -1){
            count += (posNext - pos);   
            pos = posNext;
            posNext = findNextFrame(audioFile, posNext);
        }
        return count;
    }

    /**
     * Hides a given byte array within another
     * @param audioFile mp3 file as a byte array
     * @param toHide a file as a byte array
     * @return the modified byte array
     */
    byte[] hideFile(byte[] audioFile, byte[]toHide){
        int pos, posNext, i, messInd, targInd;
        byte[] stegged = new byte[audioFile.length];
        byte insertBit, messageByte;
        long data = toHide.length;
        boolean firstTime = true;

        
        //Store the length in a byte array
        byte[] messLenArray ={
            (byte)((data >> 24) & 0xff),
            (byte)((data >> 16) & 0xff),
            (byte)((data >> 8) & 0xff),
            (byte)(data & 0xff),
        };
        
        //Check if message is too long
        if(byteCount(audioFile)/(8*spacing) < toHide.length){
            System.out.println("Cover file too small to hide data");
            java.lang.System.exit(0);
        }
        
        //Copy cover data
        for(i = 0; i < audioFile.length-1; i++)
            stegged[i] = audioFile[i];
        
        //find the first frame, second frame
        pos = findFirstHeader(stegged);
        posNext = findNextFrame(stegged, pos);
        
        messInd = 0; //Where in the message are we
        
        //Loop unitl we run out of frames
        while((posNext != -1) && messInd < toHide.length){
            targInd = pos + 4;
            //Loop unitl the end of frame is reached
            while(targInd < posNext){
                //We need to copy the message length, only once
                if(firstTime) {
                    //Copy each byte of the message size into the file bit by bit
                    for(int j = 0; j < 4; j++){ // fixed 4 bytes for message size
                        for(int k = 0; k < 8; k++){
                            insertBit = (byte) (messLenArray[j] & (byte) 1);
                            stegged[targInd] = (byte) ((stegged[targInd] & 254) | insertBit);
                            messLenArray[j] = (byte) (messLenArray[j] >> 1);
                            targInd += spacing;
                        }
                    }   
                    firstTime = false;
                }
                //If its not the first time
                if(messInd >= toHide.length)
                    break;
                
                messageByte = toHide[messInd];
                messInd++;

                // For every bit in the message byte, insert it into the
                // resulting file starting from the smallest bit and working to
                // the largest
                for(i = 0; i <8; i++){
                    // get the next bit of the message
                    insertBit = (byte) (messageByte & 1); //0x00000001
                    //System.out.println(targInd);
                    // prepare the massage byte to extract the next bit
                    messageByte = (byte) (messageByte >> 1);

                    // insert the first 7 bits of the original file + 1 bit of the message
                    stegged[targInd] = (byte) ((stegged[targInd] & 254) | insertBit);
                    targInd += spacing;
                } 
            }
            pos = posNext;
            posNext = findNextFrame(stegged, pos);
        }
        return stegged;
    }

    /**
     * Finds the hidden contents withing an mp3
     * @param stegged a mp3 byte array
     * @return a byte array containing the hidden data
     */
    byte[] revealFile(byte[] stegged) {
        int pos, posNext, i, messInd, targInd, size, tempByte, sizeByte;
        byte extractedByte, messageByte;
        boolean firstTime = true;
        byte[] rev = new byte[1];

        messInd = 0;
        size = 0;
        sizeByte = 0;
        
        pos = findFirstHeader(stegged);
        posNext = findNextFrame(stegged, pos);
        

        //while we still have frames
        while((posNext != -1)) {
            targInd = pos + 4;//4 byte header
            //while we're in a frame
            while(targInd < posNext) {
                //retrieve the length once
                if(firstTime){
                    // get the size of the message
                    for(int j = 3; j >=0; j--){
                        for(int k = 0; k <8; k++){

                            extractedByte = stegged[targInd];
                            tempByte = (extractedByte & 1);
                            tempByte = (tempByte << k);
                            sizeByte = (tempByte | sizeByte);
                            targInd += spacing;
                        }
                        size = size | (Math.abs(sizeByte) << (j*8));
                    }
                    firstTime = false;
                    rev = new byte[size];
                    System.out.println(size);
                }
                //If its not the first time
                
                messageByte = 0; // 0x00000000
                //targInd += spacing;            
                // reconstruct 1 message byte out of 8 cover file bytes
                for(i = 0; i <8; i++){
                    extractedByte = stegged[targInd];
                    tempByte = (byte) (extractedByte & (byte) 1);
                    tempByte = (byte) (tempByte << i);
                    messageByte = (byte) (tempByte | messageByte);

                    targInd += spacing;
                }
                if(messInd >= size)
                    break;
                rev[messInd] = messageByte;
                messInd++;
            }
            pos = posNext;
            posNext = findNextFrame(stegged, pos);
        }
        return rev;
    }
    
    
    //Just my testing function
    public void test(){
        int pos = 0;
        int nextPos = 0;
        byte[] head = new byte[4];
        int tot = 0;
        try {
            byte[] testFile = Files.readAllBytes(Paths.get("test_files\\testInput.mp3"));
            byte[] hide = Files.readAllBytes(Paths.get("test_files\\testText.txt"));
            byte[] res = new byte[testFile.length];
            /*pos = findFirstHeader(testFile);
            nextPos = findNextFrame(testFile, pos);
            while(nextPos != -1){
                for(int i = 0; i<4; i++){
                    head[i] = testFile[nextPos];
                    nextPos++;
                }
                nextPos = findNextFrame(testFile, nextPos-4);
                int padding = (head[2] >> 2) & (byte) 0x01;
                tot++;
                System.out.println(padding);
               
            }
System.out.println(tot);*/
            res = hideFile(testFile, hide);
            Files.write(Paths.get("test_files\\mp3Res.mp3"), testFile);
            byte[] rev = revealFile(res);
            Files.write(Paths.get("test_files\\results.txt"), rev);
        } catch (IOException ex) {
            Logger.getLogger(mp3Handler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
}
