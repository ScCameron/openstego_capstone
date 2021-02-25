/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openstego.desktop.plugin.aud;

import java.lang.*;
import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.OpenStegoPlugin;
import com.openstego.desktop.plugin.lsb.LSBConfig;
import com.openstego.desktop.ui.OpenStegoUI;
import com.openstego.desktop.ui.PluginEmbedOptionsUI;
import com.openstego.desktop.util.LabelUtil;
import com.openstego.desktop.util.cmd.CmdLineOptions;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Patrick
 * Citation: mp3Stegz: https://sourceforge.net/projects/mp3stegz/
 */
public class mp3Handler extends OpenStegoPlugin {
    /**
     * Constant for Namespace to use for this plugin
     */
    public final static String NAMESPACE = "mp3Stego";
    //How many bytes to skip
    public int spacing = 8;
    
    private byte headByte2 = 0;
    
    /**
     * LabelUtil instance to retrieve labels
     */
    private final static LabelUtil labelUtil = LabelUtil.getInstance(NAMESPACE);
    
    /**
     * Default constructor
     */
    public mp3Handler() {
        LabelUtil.addNamespace(NAMESPACE, "i18n.AudioPluginLabels");
    }
    
    /**
     * Gives the name of the plugin
     *
     * @return Name of the plugin
     */
    @Override
    public String getName(){
        return "mp3Stego";
    }
    
    /**
     * Gives the purpose(s) of the plugin
     *
     * @return Purpose(s) of the plugin
     */
    @Override
    public List<Purpose> getPurposes(){
        List<Purpose> purposes = new ArrayList<Purpose>();
        purposes.add(Purpose.DATA_HIDING);
        return purposes;
    }
    
    /**
     * Gives a short description of the plugin
     *
     * @return Short description of the plugin
     */
    @Override
    public String getDescription(){
        return "Hide data in mp3 files";
    }
    
    // ------------- Core Stego Methods -------------
    
    
    /**
     * Look through the file 
     * searching for the bytes that indicate a frame header
     * @param audioFile - the byte array of a mp3 file
     * @return the index in the array as an integer
     */
    public int findFirstHeader(byte[] audioFile){

        int pos, result;//, count;
        pos = 0;
        //count = 0;

        while(pos < audioFile.length){
            //System.out.println("f");
            // help with algorith from https://www.allegro.cc/forums/thread/591512/674023#target
            if(audioFile[pos] == (byte)0xff && ((audioFile[pos+1]>>5)&(byte)0x7) == (byte)0x7 &&
              ((audioFile[pos+1]>>1)&(byte)0x3) != (byte)0 && ((audioFile[pos+2]>>4)&(byte)0xf) != (byte)0xf &&
            ((audioFile[pos+2]>>2)&(byte)0x3) != (byte)0x3) {
                result = pos;
                headByte2 = audioFile[pos+1];
                //count++;
                break;

            }
            pos++;
        }
        
        return pos;
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
     int count = 0;
     byte b2 = 0;
    byte b4 = 0; // byte 2 and 4 of the header
    int b2Count, b4Count = 0;
     //Loop through looking for the next frame
     while(pos < audioFile.length){
         
         
        // TESTING: currently have hard coded the header for the test file which is 
        // usually FF E3 XX 64
//        if ((audioFile[pos] == (byte)0xFF) && 
//            ((audioFile[pos+1] >= (byte)0xE2)) && 
//            ((audioFile[pos+3] & (byte) 0x03) == (byte)0x00)) {
//            result = pos;
//            count++;
//            //break; 
//            }


        
        // help with algorith from https://www.allegro.cc/forums/thread/591512/674023#target
        if(audioFile[pos] == (byte)0xff && ((audioFile[pos+1]>>5)&(byte)0x7) == (byte)0x7 &&
          ((audioFile[pos+1]>>1)&(byte)0x3) != (byte)0 && ((audioFile[pos+2]>>4)&(byte)0xf) != (byte)0xf &&
        ((audioFile[pos+2]>>2)&(byte)0x3) != (byte)0x3) {
            result = pos;
            //System.out.printf("%x, %x\n", headByte2, audioFile[pos+1]);
//            if(b2 == 0){
//                b2 = audioFile[pos+1];
//                count++;
//            }
            if(audioFile[pos+1] == headByte2){
                count++;
                //System.out.printf("%x %x %x %x\n", audioFile[pos], audioFile[pos+1], audioFile[pos+2], audioFile[pos+3]);
                break; 
            }
            
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

    /**
     * Method to embed the message into the cover data
     *
     * @param toHide Message to be embedded
     * @param msgFileName Name of the message file. If this value is provided, then the filename should be embedded in
     *        the cover data
     * @param cover Cover data into which message needs to be embedded
     * @param coverFileName Name of the cover file
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the message
     * @throws OpenStegoException
     */
    @Override
    public byte[] embedData(byte[] toHide, String msgFileName, byte[] cover, String coverFileName, String stegoFileName) {
        int pos, posNext, i, messInd, targInd, pass, shift, posMod;
        byte insertBit, messageByte;
        long data = toHide.length;
        System.out.println((toHide.length*8)+32);
        System.out.println(frameCount(cover)*3);
        //Check if the data will fit in the cover
        if((toHide.length*8)+32> frameCount(cover)*3){
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
                cover[pos+2] = (byte) ((cover[pos+2] & 254) | (insertBit)<<0); //hide
                messLenArray[j] = (byte) (messLenArray[j] >> 1);
                pos = findNextFrame(cover, pos); //next frame header
            }
            
        }
        
        //These help determine which bit is set
        pass = 1;
        shift = 0; 
        posMod = 2;
        while(targInd <toHide.length){
            messageByte = toHide[targInd];
            for(i=0;i<8;i++){
                // get the next bit of the message
                insertBit = (byte) (messageByte & 1); //0x00000001

                // prepare the massage byte to extract the next bit
                messageByte = (byte) (messageByte >> 1);

                // insert the first 7 bits of the original file + 1 bit of the message
                cover[pos+posMod] = (byte) ((cover[pos+2] & 254) | (insertBit)<<shift); //hide
                pos = findNextFrame(cover, pos); //next frame header
                //This stuff handles the positioning of the bit
                if(pos == -1){
                    if(pass == 1){
                        pos = findFirstHeader(cover);//Sart at the beginning of the file
                        shift = 2; //leftshift 3, original bit 
                        posMod = 3; //last byte of header
                        pass = 2;
                    }
                    if(pass == 2){
                        pos = findFirstHeader(cover);//Sart at the beginning of the file
                        shift = 3; //leftshift 3, original bit 
                        posMod = 3; //last byte of header
                        pass = 3;
                    }
                    if(pass == 3){
                        java.lang.System.exit(0); //somethings gone wrong, we need to exit
                    }
                }
            }
            targInd++;
            //This stuff handles the positioniong of the bit
            if(pos == -1){
                if(pass == 1){
                    pos = findFirstHeader(cover);//Sart at the beginning of the file
                    shift = 2; //leftshift 3, original bit 
                    posMod = 3; //last byte of header
                    pass = 2;
                }
                if(pass == 2){
                    pos = findFirstHeader(cover);//Sart at the beginning of the file
                    shift = 3; //leftshift 3, original bit 
                    posMod = 3; //last byte of header
                    pass = 3;
                }
                if(pass == 3){
                    java.lang.System.exit(0); //somethings gone wrong, we need to exit
                }
            }
        }
        return cover;        
    }
    
/**
     * Method to extract the message file name from the stego data
     *
     * @param stegoData Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @return Message file name
     * @throws OpenStegoException
     */
    @Override
    public String extractMsgFileName(byte[] stegoData, String stegoFileName) throws OpenStegoException {
        return "output.file";
    }
    
    
    
/**
     * Method to extract the message from the stego data
     *
     * @param cover Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @param origSigData Optional signature data file for watermark
     * @return Extracted message
     * @throws OpenStegoException
     */
    @Override
    public byte[] extractData(byte[] cover, String stegoFileName, byte[] origSigData) throws OpenStegoException {
        int pos, sizeByte, tempByte, messInd, i, pass, shift, posMod;
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
        pass = 1;
        shift = 0;
        posMod = 2;
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
                extractedByte = cover[pos+posMod];
                tempByte = (byte) (extractedByte & (byte) 1<<shift);
                tempByte = (byte) (tempByte >>shift);//we want to move the bit to the end
                tempByte = (byte) (tempByte << i);
                messageByte = (byte) (tempByte | messageByte);
                pos = findNextFrame(cover, pos); //next frame header
                if(pos == -1){
                      if(pass == 1){
                          pos = findFirstHeader(cover);//Sart at the beginning of the file
                          shift = 2; //leftshift 3, original bit 
                          posMod = 3; //last byte of header
                          pass = 2;
                      }
                      if(pass == 2){
                          pos = findFirstHeader(cover);//Sart at the beginning of the file
                          shift = 3; //leftshift 3, original bit 
                          posMod = 3; //last byte of header
                          pass = 3;
                      }
                      if(pass == 3){
                          java.lang.System.exit(0); //somethings gone wrong, we need to exit
                      }
                  }                
            }
            //messageOutput.write(messageByte);
            output[messInd] = messageByte;
            //System.out.printf("output byte is %s\n", output);
            messInd++;
            if(pos == -1){
                 if(pass == 1){
                     pos = findFirstHeader(cover);//Sart at the beginning of the file
                     shift = 2; //leftshift 3, original bit 
                     posMod = 3; //last byte of header
                     pass = 2;
                 }
                 if(pass == 2){
                     pos = findFirstHeader(cover);//Sart at the beginning of the file
                     shift = 3; //leftshift 3, original bit 
                     posMod = 3; //last byte of header
                     pass = 3;
                 }
                 if(pass == 3){
                     java.lang.System.exit(0); //somethings gone wrong, we need to exit
                 }
             }           
        }

        return output;
    }

    /**
     * Method to generate the signature data. This method needs to be implemented only if the purpose of the plugin is
     * Watermarking
     *
     * @return Signature data
     * @throws OpenStegoException
     */
    @Override
    public byte[] generateSignature() throws OpenStegoException {
        return null;
    }
    
    /**
     * Method to check the correlation between original signature and the extracted watermark
     *
     * @param origSigData Original signature data
     * @param watermarkData Extracted watermark data
     * @return Correlation
     * @throws OpenStegoException
     */
    @Override
    public double getWatermarkCorrelation(byte[] origSigData, byte[] watermarkData) throws OpenStegoException {
        return 0;
    }
    
    /**
     * Method to get correlation value which above which it can be considered that watermark strength is high
     *
     * @return High watermark
     * @throws OpenStegoException
     */
    @Override
    public double getHighWatermarkLevel() throws OpenStegoException {
        return 0;
    }
    
    /**
     * Method to get correlation value which below which it can be considered that watermark strength is low
     *
     * @return Low watermark
     * @throws OpenStegoException
     */
    @Override
    public double getLowWatermarkLevel() throws OpenStegoException {
        return 0;
    }
    
    /**
     * Method to get difference between original cover file and the stegged file
     *
     * @param stegoData Stego data containing the embedded data
     * @param stegoFileName Name of the stego file
     * @param coverData Original cover data
     * @param coverFileName Name of the cover file
     * @param diffFileName Name of the output difference file
     * @return Difference data
     * @throws OpenStegoException
     */
    @Override
    public byte[] getDiff(byte[] stegoData, String stegoFileName, byte[] coverData, String coverFileName, String diffFileName) {
        return null;
    }
    
    /**
     * Method to find out whether given stego data can be handled by this plugin or not
     *
     * @param stegoData Stego data containing the message
     * @return Boolean indicating whether the stego data can be handled by this plugin or not
     */
    @Override
    public boolean canHandle(byte[] stegoData) {
        return true;
    }
    
    /**
     * Method to get the list of supported file extensions for reading
     *
     * @return List of supported file extensions for reading
     * @throws OpenStegoException
     */
    @Override
    public List<String> getReadableFileExtensions() throws OpenStegoException {
        List<String> fileTypes = new ArrayList<String>();
        fileTypes.add("mp3");
        return fileTypes;
    }
    
    /**
     * Method to get the list of supported file extensions for writing
     *
     * @return List of supported file extensions for writing
     * @throws OpenStegoException
     */
    @Override
    public List<String> getWritableFileExtensions() throws OpenStegoException {
        List<String> fileTypes = new ArrayList<String>();
        fileTypes.add("mp3");
        return fileTypes;        
    }
    
    // ------------- Command-line Related Methods -------------

    /**
     * Method to populate the standard command-line options used by this plugin
     *
     * @param options Existing command-line options. Plugin-specific options will get added to this list
     * @throws OpenStegoException
     */
    @Override
    public void populateStdCmdLineOptions(CmdLineOptions options) throws OpenStegoException {
    }

    /**
     * Method to get the usage details of the plugin
     *
     * @return Usage details of the plugin
     * @throws OpenStegoException
     */
    @Override
    public String getUsage() throws OpenStegoException {
        return "This plugin uses the default OpenStego command line options";
    }

    // ------------- GUI Related Methods -------------
    
    /**
     * Method to get the UI object for "Embed" action specific to this plugin. This UI object will be embedded inside
     * the main OpenStego GUI
     *
     * @param stegoUI Reference to the parent OpenStegoUI object
     * @return UI object specific to this plugin for "Embed" action
     * @throws OpenStegoException
     */
    @Override
    public PluginEmbedOptionsUI getEmbedOptionsUI(OpenStegoUI stegoUI) throws OpenStegoException {
        return null;
    }

    // ------------- Other Methods -------------

    /**
     * Method to get the configuration class specific to this plugin
     *
     * @return Configuration class specific to this plugin
     */
    @Override
    public Class<? extends OpenStegoConfig> getConfigClass() {
        return LSBConfig.class;
    }

}
