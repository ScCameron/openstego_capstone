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
        int pos = 0;

        while(pos < audioFile.length){
            // Check specific bits to see if it is a mp3 header
            // help with header algorith from https://www.allegro.cc/forums/thread/591512/674023#target
            if(audioFile[pos] == (byte)0xff && ((audioFile[pos+1]>>5)&(byte)0x7) == (byte)0x7 &&
            ((audioFile[pos+1]>>1)&(byte)0x3) != (byte)0 && ((audioFile[pos+2]>>4)&(byte)0xf) != (byte)0xf &&
            ((audioFile[pos+2]>>2)&(byte)0x3) != (byte)0x3) 
            {
                // store the second byte of the first header as it helps with finding future headers
                headByte2 = audioFile[pos+1];
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
        pos = prevHeadPos+4; //Skip this header
        result = -1; //If we failed to find the next frame (reach end of file)
        
        //Loop through looking for the next frame
        while(pos < audioFile.length){
            // Check specific bits to see if it is a mp3 header
            // help with header algorith from https://www.allegro.cc/forums/thread/591512/674023#target
            if(audioFile[pos] == (byte)0xff && ((audioFile[pos+1]>>5)&(byte)0x7) == (byte)0x7 &&
            ((audioFile[pos+1]>>1)&(byte)0x3) != (byte)0 && ((audioFile[pos+2]>>4)&(byte)0xf) != (byte)0xf &&
            ((audioFile[pos+2]>>2)&(byte)0x3) != (byte)0x3) 
            {
                result = pos;
                // check against our stored byte 2 from findFirstHeader() to see it its a header
                if(audioFile[pos+1] == headByte2){
                    break; 
                }
            }
        pos++;    
     }
     return result;
    }

    /**
     * Counts the number of frames in an mp3 file. 
     * @param audioFile byte array of an mp3
     * @return the number of frames as an integer
     */
    int frameCount(byte[] audioFile){
        int pos, posNext, count;
        pos = findFirstHeader(audioFile);
        count = 0;
        posNext = pos;
        while(posNext != -1){
            count++;
            posNext = findNextFrame(audioFile, posNext);
        }
        return count;
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
        int pos, posNext, i, messInd, targInd, pass, shift, posMod, coverMask, frames;
        byte insertBit, messageByte;
        long data = toHide.length;
        messageByte = 0;
        
        // how far in the the bit is from the left that we are modifying
        shift = 0;
        // which byte we are modifying in the header
        posMod = 2;
        // mask to clear the bit we're modifying
        coverMask = 0b11111110;
                    
        frames = frameCount(cover);
        System.out.printf("Message size is %d. Bytes available is %d\n",toHide.length, frames*3/8);

        
        //Check if the data will fit in the cover
        if((toHide.length*8)+(32*8)> frames*3){
            System.out.println("File too long to hide");
            java.lang.System.exit(0);
        }
        //Store the message length in a byte array
        byte[] messLenArray = {
            (byte)((data >> 24) & 0xff),
            (byte)((data >> 16) & 0xff),
            (byte)((data >> 8) & 0xff),
            (byte)(data & 0xff),
        };
        messInd = 0; //Where in the message are we
        
        //pos is the position of a frame header, 
        //We hide 3 bits per frame header
        pos = findFirstHeader(cover);
        
        //Copy each byte of the message size into the file bit by bit
        for(int j = 0; j < 4; j++){ // fixed 4 bytes for message size
            for(int k = 0; k < 8; k++){
                insertBit = (byte) (messLenArray[j] & (byte) 1);
                cover[pos+posMod] = (byte) ((cover[pos+posMod] & coverMask) | (insertBit)<<shift); //hide
                messLenArray[j] = (byte) (messLenArray[j] >> 1);
                pos = findNextFrame(cover, pos);
            }
        }
        
        i = 0;
        // pass over the data 3 times to hide our data. each time we use a different bit
        for(pass = 1; pass <= 3; pass++){
            switch(pass){
                case 1:
                    shift = 0; // first bit from the right
                    posMod = 2; // third bit in the header
                    coverMask = 0b11111110;
                    break;
                case 2:
                    pos = findFirstHeader(cover);
                    shift = 2; // third bit from the right
                    posMod = 3; //last byte of header
                    coverMask = 0b11111011;
                    break;
                case 3:
                    pos = findFirstHeader(cover);
                    shift = 3; // fourth bit from the right
                    posMod = 3; //last byte of header
                    coverMask = 0b11110111;
                    break;
            }
            
            // hide each bit into the approtriate slot in each header
            // if you reach the end of the cover file, restart passing over a different bit
            while(messInd < toHide.length){
                // this makes sure we dont corrupt any data when 
                // we start new pass in the middle of a byte,
                if(i == 0){
                    messageByte = toHide[messInd];
                }
                while(i < 8){
                    // get the next bit of the message
                    insertBit = (byte) (messageByte & 1);

                    // prepare the massage byte to extract the next bit
                    messageByte = (byte) (messageByte >> 1);

                    // insert the first 7 bits of the original file + 1 bit of the message
                    cover[pos+posMod] = (byte) ((cover[pos+posMod] & coverMask) | ((insertBit)<<shift));
                    pos = findNextFrame(cover, pos);
                    i++;
                    // break if we reach the end of the cover file
                    if(pos == -1){
                        break;
                    }
                }
                if(pos == -1){
                    break;
                }
                i = 0;

                messInd++;
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
        
        // the current byte of the message being constructed
        messageByte = 0;
        
        // how far in the the bit is from the left that we are modifying
        shift = 0;
        // which byte we are modifying in the header
        posMod = 2;
        
        // used for building the size of the message to be constructed
        sizeByte = 0;
        size = 0;
        
        // the index of the message we are constructing
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

                pos = findNextFrame(cover, pos);
            }
            size = size | (Math.abs(sizeByte) << (j*8));
        }   
        
        i = 0;
        byte[] output = new byte[(int) size];
        // pass over the data 3 times to hide our data. each time we use a different bit
        for(pass = 1; pass <= 3; pass++){
            switch(pass){
                case 1:
                    shift = 0; // first bit from the right
                    posMod = 2; // third byte in the header
                    break;
                case 2:
                    pos = findFirstHeader(cover);
                    shift = 2; // third bit from the right
                    posMod = 3; //last byte of header
                    break;
                case 3:
                    pos = findFirstHeader(cover);
                    shift = 3; // fourth bit from the right
                    posMod = 3; //last byte of header
                    break;
            }

            // find each bit into the approtriate slot in each header.
            // if you reach the end of the cover file, restart passing over a different bit
            while(messInd < size){
                // this makes sure we dont corrupt any data when 
                // we start new pass in the middle of a byte,
                if(i == 0){
                    messageByte = 0;
                }
                while(i < 8){
                    extractedByte = cover[pos+posMod];
                    tempByte = (byte) (extractedByte & (byte) 1<<shift);
                    tempByte = (byte) (tempByte >>shift);//we want to move the bit to the end
                    tempByte = (byte) (tempByte << i);
                    messageByte = (byte) (tempByte | messageByte);
                    pos = findNextFrame(cover, pos);
                    i++;
                    if(pos == -1){
                        break;
                    }
                }
                if(pos == -1){
                    break;
                }
                i = 0;
                output[messInd] = messageByte;
                messInd++;
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
