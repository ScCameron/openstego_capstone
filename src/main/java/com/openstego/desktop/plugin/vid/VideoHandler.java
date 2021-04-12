/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */
package com.openstego.desktop.plugin.vid;



import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.OpenStegoPlugin;
import com.openstego.desktop.plugin.lsb.LSBConfig;
import com.openstego.desktop.ui.OpenStegoUI;
import com.openstego.desktop.ui.PluginEmbedOptionsUI;
import com.openstego.desktop.util.LabelUtil;
import com.openstego.desktop.util.cmd.CmdLineOption;
import com.openstego.desktop.util.cmd.CmdLineOptions;

import java.util.List;
import java.util.ArrayList;


/**
 * Plugin for video steganography utilizing LSB and lossless compression
 * @author Patrick and Scott
 */
public class VideoHandler extends OpenStegoPlugin {
    /**
     * Constant for Namespace to use for this plugin
     */
    public final static String NAMESPACE = "VideoStego";
    int byteSpread = 1; // number of bytes between each insert
    int startTargInd = 0; // the byte start point of the message in the cover file
    int targInd = startTargInd;
    
    /**
     * LabelUtil instance to retrieve labels
     */
    private final static LabelUtil labelUtil = LabelUtil.getInstance(NAMESPACE);
    
    /**
     * Default constructor
     */
    public VideoHandler() {
        LabelUtil.addNamespace(NAMESPACE, "i18n.VideoPluginLabels");
    }
    
    /**
     * Gives the name of the plugin
     *
     * @return Name of the plugin
     */
    @Override
    public String getName(){
        return "VideoStego";
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
        return "Embed and extract message files from video files";
    }
   
    
    // ------------- Core Stego Methods -------------

    /**
     * Method to embed the message into the cover data
     *
     * @param msg Message to be embedded
     * @param msgFileName Name of the message file. If this value is provided, then the filename should be embedded in
     *        the cover data
     * @param cover Cover data into which message needs to be embedded
     * @param coverFileName Name of the cover file
     * @param stegoFileName Name of the output stego file
     * @return Stego data containing the message
     */
    @Override
    public byte[] embedData(byte[] msg, String msgFileName, byte[] cover, String coverFileName, String stegoFileName) {
        int messInd = 0; // index of current message byte being processed
        int count = cover.length; // number of usable bytes in cover file
        
        System.out.printf("Message size is %d bytes. %d bytes are able to be inserted\n", msg.length, count/8);
        // check for message length
        if(msg.length * 8 > count){
            System.out.println("Message too long");
            java.lang.System.exit(0);
        }
        byte messageByte, insertBit;
        
        // Insert the message length
        long data = msg.length;
        //Store the length in a byte array
        byte[] messLenArray ={
            (byte)((data >> 24) & 0xff),
            (byte)((data >> 16) & 0xff),
            (byte)((data >> 8) & 0xff),
            (byte)(data & 0xff),
        };

        //Copy each byte of the message size into the file bit by bit
        for(int j = 0; j < 4; j++){ // fixed 4 bytes for message size
            for(int k = 0; k < 8; k++){
                insertBit = (byte) (messLenArray[j] & (byte) 1);
                cover[targInd] = (byte) ((cover[targInd] & 254) | insertBit);
                messLenArray[j] = (byte) (messLenArray[j] >> 1);
                targInd += byteSpread;
            }
        }
        
        byteSpread = 1;
        
        // For every byte in the message
        while(messInd < msg.length){
            messageByte = msg[messInd];
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
        System.out.println("Done Embedding");
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
     * @param stegoData Stego data containing the message
     * @param stegoFileName Name of the stego file
     * @param origSigData Optional signature data file for watermark
     * @return Extracted message
     * @throws OpenStegoException
     */
    @Override
    public byte[] extractData(byte[] stegoData, String stegoFileName, byte[] origSigData) throws OpenStegoException {
        // the current message index being processed
        int messInd = 0;
        byte extractedByte, messageByte;
        int size; // the size of the extracted message
        int tempByte;
        int sizeByte; // holds the current byte being processed to get the size
        
        targInd = startTargInd;
        size = 0;
        // reconstruct the 4 bytes that indicate message size
        for(int j = 3; j >=0; j--){
            sizeByte = 0;
            for(int k = 0; k <8; k++){
                extractedByte = stegoData[targInd];
                tempByte = (extractedByte & 1);
                tempByte = (tempByte << k);
                sizeByte = (tempByte | sizeByte);
                targInd += byteSpread;
            }
            size = size | (Math.abs(sizeByte) << (j*8));
        }
        
        byte[] output = new byte[(int) size];
        byteSpread = 1;
        
        // reconstruct the message bit by bit
        while(messInd < size){
            messageByte = 0; // 0x00000000

            // reconstruct 1 message byte out of 8 cover file bytes
            for(int i = 0; i <8; i++){
                extractedByte = stegoData[targInd];
                tempByte = (byte) (extractedByte & (byte) 1);
                tempByte = (byte) (tempByte << i);
                messageByte = (byte) (tempByte | messageByte);
                targInd += 1;
            }
            output[messInd] = messageByte;
            
            messInd++;
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
        List<String> extensions = new ArrayList<String>();
        extensions.add("yuv");
        return extensions;
    }

    /**
     * Method to get the list of supported file extensions for writing
     *
     * @return List of supported file extensions for writing
     * @throws OpenStegoException
     */
    @Override
    public List<String> getWritableFileExtensions() throws OpenStegoException {
        List<String> extensions = new ArrayList<String>();
        extensions.add("yuv");
        return extensions;      
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
        options.add("-ff", "--ffmpegLocation", CmdLineOption.TYPE_OPTION, true);
        options.add("-fc", "--ffmpegCommand", CmdLineOption.TYPE_OPTION, true);
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