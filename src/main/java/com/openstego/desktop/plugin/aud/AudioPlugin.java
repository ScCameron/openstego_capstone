/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) Samir Vaidya
 */
package com.openstego.desktop.plugin.aud;



import com.openstego.desktop.OpenStegoConfig;
import com.openstego.desktop.OpenStegoException;
import com.openstego.desktop.OpenStegoPlugin;
import com.openstego.desktop.plugin.lsb.LSBConfig;
import com.openstego.desktop.plugin.lsb.LSBEmbedOptionsUI;
import com.openstego.desktop.ui.OpenStegoUI;
import com.openstego.desktop.ui.PluginEmbedOptionsUI;
import com.openstego.desktop.util.LabelUtil;
import com.openstego.desktop.util.cmd.CmdLineOptions;

import java.util.List;
import java.util.ArrayList;


/**
 *
 * @author Patrick and Scott
 */
public class AudioPlugin extends OpenStegoPlugin {
    /**
     * Constant for Namespace to use for this plugin
     */
    public final static String NAMESPACE = "AudioLSB";
    int byteSpread = 2; // number of bytes between each insert. 1 generates strange noises
    int targInd = 112; // the byte start point of the message in the cover file
    
    
    /**
     * LabelUtil instance to retrieve labels
     */
    private final static LabelUtil labelUtil = LabelUtil.getInstance(NAMESPACE);
    
    /**
     * Default constructor
     */
    public AudioPlugin() {
        LabelUtil.addNamespace(NAMESPACE, "i18n.AudioPluginLabels");
    }
    
    /**
     * Gives the name of the plugin
     *
     * @return Name of the plugin
     */
    @Override
    public String getName(){
        return "AudioLSB";
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
        return "TODO";
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
     * @throws OpenStegoException
     */
    @Override
    public byte[] embedData(byte[] msg, String msgFileName, byte[] cover, String coverFileName, String stegoFileName) {
        int messInd = 0;

        if(cover.length < msg.length  * 8 * byteSpread ){
            System.out.println("Message too long");
            java.lang.System.exit(0);
        }
        byte messageByte, insertBit;

        /*Insert the message length*/
        long data = msg.length;
        System.out.println(msg.length);
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

        // For every byte in the message
        while(messInd < msg.length){
            //rafR.seek(messInd);
            messageByte = msg[messInd];
            messInd++;

            // For every bit in the message byte, insert it into the
            // resulting file starting from the smallest bit and working to
            // the largest
            for(int i = 0; i <8; i++){
                // get the next bit of the message
                insertBit = (byte) (messageByte & 1); //0x00000001

                // prepare the massage byte to extract the next bit
                messageByte = (byte) (messageByte >> 1);

                // insert the first 7 bits of the original file + 1 bit of the message
                cover[targInd] = (byte) ((cover[targInd] & 254) | insertBit);
                targInd += byteSpread;
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
        return "testOutput.mp3";
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
        int messInd = 0;
        
        byte extractedByte, messageByte;
        

        int size = 0;
        int tempByte;
        int sizeByte = 0;
        // get the size of the message
        for(int j = 3; j >=0; j--){
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

        while(messInd < size){
            //messageOutput.seek(messInd);

            //coverFile.seek(targInd);
            messageByte = 0; // 0x00000000

            // reconstruct 1 message byte out of 8 cover file bytes
            for(int i = 0; i <8; i++){
                extractedByte = stegoData[targInd];
                tempByte = (byte) (extractedByte & (byte) 1);
                tempByte = (byte) (tempByte << i);
                messageByte = (byte) (tempByte | messageByte);

                targInd += byteSpread;
            }
            //messageOutput.write(messageByte);
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
        // dummy code to fill the method
        byte[] b = new byte[1];
        return b;
    }



    /**
     * Method to check the correlation between original signature and the extracted watermark
     *
     * @param origSigData Original signature data
     * @param watermarkData Extracted watermark data
     * @return Correlation
     * @throws OpenStegoException
     */
    public double getWatermarkCorrelation(byte[] origSigData, byte[] watermarkData) throws OpenStegoException {
        return 0.0;
    }

    /**
     * Method to get correlation value which above which it can be considered that watermark strength is high
     *
     * @return High watermark
     * @throws OpenStegoException
     */
    public double getHighWatermarkLevel() throws OpenStegoException {
        return 0.0;
    }

    /**
     * Method to get correlation value which below which it can be considered that watermark strength is low
     *
     * @return Low watermark
     * @throws OpenStegoException
     */
    public double getLowWatermarkLevel() throws OpenStegoException {
        return 0.0;
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
    public byte[] getDiff(byte[] stegoData, String stegoFileName, byte[] coverData, String coverFileName, String diffFileName) {
        // dummy code to fill the method
        byte[] b = new byte[1];
        return b;
    }

    /**
     * Method to find out whether given stego data can be handled by this plugin or not
     *
     * @param stegoData Stego data containing the message
     * @return Boolean indicating whether the stego data can be handled by this plugin or not
     */
    public boolean canHandle(byte[] stegoData) {
        // dummy code to fill the method
        return true;
    }

    /**
     * Method to get the list of supported file extensions for reading
     *
     * @return List of supported file extensions for reading
     * @throws OpenStegoException
     */
    public List<String> getReadableFileExtensions() throws OpenStegoException {
        // dummy code to fill the method
        List<String> dummy = new ArrayList<String>();
        dummy.add("dummy0");
        return dummy;
    }

    /**
     * Method to get the list of supported file extensions for writing
     *
     * @return List of supported file extensions for writing
     * @throws OpenStegoException
     */
    public List<String> getWritableFileExtensions() throws OpenStegoException {
        // dummy code to fill the method
        List<String> dummy = new ArrayList<String>();
        dummy.add("dummy0");
        return dummy;        
    }

    // ------------- Command-line Related Methods -------------

    /**
     * Method to populate the standard command-line options used by this plugin
     *
     * @param options Existing command-line options. Plugin-specific options will get added to this list
     * @throws OpenStegoException
     */
    public void populateStdCmdLineOptions(CmdLineOptions options) throws OpenStegoException {
    }

    /**
     * Method to get the usage details of the plugin
     *
     * @return Usage details of the plugin
     * @throws OpenStegoException
     */
    public String getUsage() throws OpenStegoException {
        return "dummy usage message";
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
    public PluginEmbedOptionsUI getEmbedOptionsUI(OpenStegoUI stegoUI) throws OpenStegoException {
         // dummy code to fill the method
        return new LSBEmbedOptionsUI(stegoUI);
    }

    // ------------- Other Methods -------------

    /**
     * Method to get the configuration class specific to this plugin
     *
     * @return Configuration class specific to this plugin
     */
    public Class<? extends OpenStegoConfig> getConfigClass() {
        // dummy code to fill the method
        return LSBConfig.class;
    }

}