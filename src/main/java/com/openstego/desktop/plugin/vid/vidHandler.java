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
    public String fps, res;
    String tmpPath = "D:\\Users\\patri\\Documents\\GitHub\\openstego_capstone\\test_files\\";
    
    public void toRaw(String filename) {   
        BufferedReader br;
        Process p;
        
        try {
            //Video to raw
            Runtime.getRuntime().exec("cmd /c ffmpeg -i " + filename + " -c:v rawvideo -pix_fmt yuv420p raw.yuv");
            
            //Get audio
            Runtime.getRuntime().exec("cmd /c ffmpeg -i "+filename + " vidAu.mp3");
            try {
                // Store fps
                p = Runtime.getRuntime().exec("cmd /c \"ffprobe -v error -select_streams v -of default=noprint_wrappers=1:nokey=1 -show_entries stream=r_frame_rate " + filename);         
                br = new BufferedReader(new InputStreamReader(p.getInputStream())); 
                fps = br.readLine();
                br.close();
                
                
                //Store resolution
                p = Runtime.getRuntime().exec("cmd /c ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 "+ filename);
                br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                res = br.readLine();
                System.out.println(res);
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
     * 
     * @param stegoFileName 
     */
    public void toMP4(String stegoFileName) {
        try {
            //raw to mp4
            Runtime.getRuntime().exec("cmd /c start cmd.exe /k ffmpeg -f rawvideo -pix_fmt yuv420p -s:v "+res+" -i "+stegoFileName+".yuv -i vidAu.mp3 -c:v libx264 -preset veryslow -crf 0 "+stegoFileName);
        } catch (IOException ex) {
            Logger.getLogger(vidHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
   
    
    /**
     * 
     * @param stegoFileName 
     */
    public void cleanUp(String stegoFileName){
        try {      
            Runtime.getRuntime().exec("cmd /c   Del raw.yuv ");
            Runtime.getRuntime().exec("cmd /c   Del vidAu.mp3 ");
            Runtime.getRuntime().exec("cmd /c   Del "+stegoFileName+".yuv");
            //sleep(3000);
        } catch (Exception e) {
            
        }
        
    }
    public void test() {
        toRaw("D:\\Users\\patri\\Documents\\GitHub\\openstego_capstone\\test_files\\testingVid\\test2.mp4");
        System.out.println(res);
        System.out.println(fps);
        //toMP4();

    }
}
