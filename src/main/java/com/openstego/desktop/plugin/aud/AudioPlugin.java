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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
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
    
    /**
     * LabelUtil instance to retrieve labels
     */
    private static LabelUtil labelUtil = LabelUtil.getInstance(NAMESPACE);
    
    
    // test, remove
    public static void printTest(){
        System.out.println("Hello world;");
    }
        public static void TestAudEmbed(){
    String testingPath = "C:\\Users\\patri\\Desktop\\StegoTesting";
        String wav = testingPath + "\\sample.wav";
        String mess = testingPath + "\\testText.txt";
        
        
        byte[] messByte = new byte[1];
        // Get the bytes of our message
        try {
            Path path = Paths.get(mess);
            messByte = Files.readAllBytes(path);
        } catch(IOException e) {
        }
        int totalFramesRead = 0;
        int bytesPerFrame = 0;
        File fileIn = new File(wav);
        // somePathName is a pre-existing string whose value was
        // based on a user selection.
        try {
          AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fileIn);
            bytesPerFrame = audioInputStream.getFormat().getFrameSize();
            if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
            // some audio formats may have unspecified frame size
            // in that case we may read any amount of bytes
            bytesPerFrame = 1;
          } 
        } catch(IOException | UnsupportedAudioFileException e) {
        }
          // Set an arbitrary buffer size of 1024 frames.
          int numBytes = 1024 * bytesPerFrame; 
          byte[] audioBytes = new byte[numBytes];
          try {
            int numBytesRead = 0;
            int numFramesRead = 0;
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fileIn);
            // Try to read numBytes bytes from the file.
            while ((numBytesRead = 
                 audioInputStream.read(audioBytes)) != -1) {
                // Calculate the number of frames actually read.
                numFramesRead = numBytesRead / bytesPerFrame;
                totalFramesRead += numFramesRead;
              
                if(numBytesRead < messByte.length*8 ){
                    System.out.println("Message too long");
                    java.lang.System.exit(0);
                }
                    }               
 
                
                        InputStream is = null;
                        OutputStream os = null;
                        File src = new File(testingPath+"\\sample.wav");
                        File dest = new File(testingPath+"\\stegRes.wav");
                        
                            is = new FileInputStream(src);
                            os = new FileOutputStream(dest);

                            // buffer size 1K
                            byte[] buf = new byte[1024];

                            int bytesRead;
                                while ((bytesRead = is.read(buf)) > 0) {
                                    os.write(buf, 0, bytesRead);
                                }
                                is.close();
                                os.close(); 
                  
                
                
                RandomAccessFile raf = new RandomAccessFile(testingPath+"\\stegRes.wav", "rw");
                RandomAccessFile rafR = new RandomAccessFile(testingPath+"\\testText.txt", "rw");
                int messInd = 0;
                int targInd = 44;
                byte b1, b2, b3, b4;
                while(messInd < rafR.length()*8){
                    b1 = (byte) 1; //0x0000001
                    rafR.seek(messInd);
                    messInd += 1;
                    b2 = rafR.readByte();
                    for(int i = 0; i <7; i++){
                        b3 = (byte) (b2&b1);
                        b1 = (byte) (b1 << 1);
                        raf.seek(targInd);
                        System.out.println("Before Mod");
                        System.out.println(raf.readByte());
                        raf.write(raf.readByte()|b3);
                        System.out.println("After Mod");
                        System.out.println(raf.readByte());
                        targInd +=1;
                        System.out.println("Byte");
                        System.out.println(b3);
                    } 
                    System.out.println("Index");
                        System.out.println(targInd); 
                        System.out.println(messInd); 
                         
                }

                  //raf.seek(targInd);
                        
                        //raf.write(70); // Write byte
                        
                
            }
          catch(IOException | UnsupportedAudioFileException e) {
        }
    
    }

  
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
        // dummy code to fill the method
        
        

            
        byte[] b = new byte[1];
        return b;

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
        // dummy code to fill the method
        return "a";
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
        // dummy code to fill the method
        byte[] b = new byte[1];
        return b;
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