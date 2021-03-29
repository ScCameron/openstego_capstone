/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openstego.desktop.plugin.vid;

import java.io.*;
import java.lang.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handle the ffmpeg parts of VideoStego
 * @author Scott Cameron and Patrick Martel
 */
public class vidHandler {
    public String fps, res;

    /**
     * convert a given video file to raw yuv420p format
     * @param filename The file to convert
     * @param ffmpegLocation the path to the folder containing ffmpeg
     */
    public void toRaw(String filename, String ffmpegLocation) {   
        BufferedReader br;
        Process p;
        File f;
        
        try {
            // Check if ffmpeg exists
            f = new File(ffmpegLocation+"\\ffmpeg.exe");
            if(!(f.exists() && !f.isDirectory())) { 
                System.out.println("You must have ffmpeg and ffprobe installed and provide the correct path with the -ff flag to use the VideoStego plugin");
                java.lang.System.exit(0);
            }
            // Convert to raw video file
            Runtime.getRuntime().exec("cmd /c "+ffmpegLocation+"\\ffmpeg -y -i " + filename + " -c:v rawvideo -pix_fmt yuv420p rawVideoToBeDeleted.yuv");
            
            //Get audio
            Runtime.getRuntime().exec("cmd /c "+ffmpegLocation+"\\ffmpeg -y -i "+filename + " vidAudioToBeDeleted");
            try {
                // Store fps
                p = Runtime.getRuntime().exec("cmd /c "+ffmpegLocation+"\\ffprobe -v error -select_streams v -of default=noprint_wrappers=1:nokey=1 -show_entries stream=r_frame_rate " + filename);         
                br = new BufferedReader(new InputStreamReader(p.getInputStream())); 
                fps = br.readLine();
                br.close();
                
                
                //Store resolution
                p = Runtime.getRuntime().exec("cmd /c "+ffmpegLocation+"\\ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 "+ filename);
                br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                res = br.readLine();
                res = res.replace('x', ':');
                br.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(vidHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
     
    }
    
    /**
     * Convert a raw video to mp4 using ffmpeg
     * @param stegoFileName the raw video to convert
     * @param ffmpegLocation the path to the folder containing ffmpeg
     */
    public void toMP4(String stegoFileName, String ffmpegLocation) {
        File f;
        try {
            ProcessBuilder pb;
            System.out.println("Converting back to compressed video. This may take a moment...");
            
            // Check if ffmpeg exists
            f = new File(ffmpegLocation+"\\ffmpeg.exe");
            if(!(f.exists() && !f.isDirectory())) { 
                System.out.println("You must have ffmpeg and ffprobe installed and provide the correct path to use the VideoStego plugin");
                java.lang.System.exit(0);
            }
            
            // include audio if it existed in the original
            f = new File("vidAudioToBeDeleted");
            if(f.exists() && !f.isDirectory()) { 
                pb = new ProcessBuilder("powershell", "-Command", ""+ffmpegLocation+"\\ffmpeg -y -f rawvideo -pix_fmt yuv420p -s:v "+res+" -i "+stegoFileName+"ToBeDeleted.yuv -i vidAudioToBeDeleted -c:v libx264 -preset veryslow -crf 0 "+stegoFileName+" 2>$null");
            }
            else{
                pb = new ProcessBuilder("powershell", "-Command", ""+ffmpegLocation+"\\ffmpeg -y -f rawvideo -pix_fmt yuv420p -s:v "+res+" -i "+stegoFileName+"ToBeDeleted.yuv -c:v libx264 -preset veryslow -crf 0 "+stegoFileName+" 2>$null");
            }
            
            // print output from ffmpeg
            Process p = pb.start();
            BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            while ((line = output.readLine()) != null) {
                System.out.println(line);
            }
            while ((line = stdError.readLine()) != null) {
                System.out.println(line);
            }
            
            // wait for conversion to complete before continuing
            p.waitFor();

            System.out.println("Video compressed successfully");

            

        } catch (IOException ex) {
            Logger.getLogger(vidHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex){
            ex.printStackTrace();
        }
        
    }
    
   
    
    /**
     * Clean up the temporary files created in the video steganography process
     * @param stegoFileName The name of the produced stego file
     */
    public void cleanUp(String stegoFileName){
        try {      
            Runtime.getRuntime().exec("cmd /c   Del rawVideoToBeDeleted.yuv ");
            Runtime.getRuntime().exec("cmd /c   Del vidAudioToBeDeleted ");
            Runtime.getRuntime().exec("cmd /c   Del "+stegoFileName+"ToBeDeleted.yuv");
        } catch (Exception e) {
            
        }
        
    }

}
