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
            Runtime.getRuntime().exec("cmd /c ffmpeg -i " + filename + " -c:v rawvideo -pix_fmt rgb24 raw.yuv");
            
            //Get audio
            Runtime.getRuntime().exec("cmd /c ffmpeg -i "+filename + " vidAu.mp3");
            try {
                // Store Resolution
                p = Runtime.getRuntime().exec("cmd /c \"ffprobe -v error -select_streams v -of default=noprint_wrappers=1:nokey=1 -show_entries stream=r_frame_rate " + filename);         
                br = new BufferedReader(new InputStreamReader(p.getInputStream())); 
                fps = br.readLine();
                br.close();
                
                
                //Store fps
                p = Runtime.getRuntime().exec("cmd /c ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 "+ filename);
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
     * 
     */
    public void toMP4() {
        try {
            //raw to mp4
            Runtime.getRuntime().exec("cmd /c start cmd.exe /k ffmpeg -f rawvideo -pix_fmt rgb24 -s:v "+res+" -i raw.yuv -i vidAu.mp3 -c:v libx264 -crf 0 stegged.mp4");
        } catch (IOException ex) {
            Logger.getLogger(vidHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
   
    
    /**
     * 
     */
    public void cleanUp(String path){
        try {      
            Runtime.getRuntime().exec("cmd /c   Del test_files\\testingVid\\image-*.png ");
            Runtime.getRuntime().exec("cmd /c   Del test_files\\testingVid\\audio.mp3 ");
            Runtime.getRuntime().exec("cmd /c   Del test_files\\testingVid\\tmp.txt ");
            sleep(3000);
        } catch (Exception e) {
            
        }
        
    }
    public void test() {
        toRaw("D:\\Users\\patri\\Documents\\GitHub\\openstego_capstone\\test_files\\testingVid\\test2.mp4");
        System.out.println(res);
        System.out.println(fps);
        toMP4();

    }
}
