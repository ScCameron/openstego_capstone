This folder contains files to test the audio OpenStego plugins.

The built openstego.jar must be placed in this folder to run the tests

The runTests.ps1 file contains several tests that embed and extract data in .wav and .mp3 files
and diffs the input message with the output message

Here is the usage page for the OpenStego command line: https://www.openstego.com/cmdline.html

Here are some example commands that you can run manually:
java -jar openstego.jar embed -a AudioLSB -mf msg1.txt -cf WAVCover1.wav -sf StegoResult.wav -e -p seeecretPassword
java -jar openstego.jar extract -a AudioLSB -sf StegoResult.wav -xf output.txt -e -p seeecretPassword

java -jar openstego.jar embed -a AudioLSB -mf MP3Cover1.mp3 -cf WAVCover1.wav -sf StegoResult.wav -e -p seeecretPassword
java -jar openstego.jar extract -a AudioLSB -sf StegoResult.wav -xf output.mp3 -e -p seeecretPassword


java -jar openstego.jar embed -a mp3Stego -mf msg1.txt -cf MP3Cover1.mp3 -sf StegoResult.mp3 -e -p seeecretPassword
java -jar openstego.jar extract -a mp3Stego -sf StegoResult.mp3 -xf output.txt -e -p seeecretPassword

java -jar openstego.jar embed -a mp3Stego -mf msg2.txt -cf MP3Cover2.mp3 -sf StegoResult.mp3 -e -p seeecretPassword
java -jar openstego.jar extract -a mp3Stego -sf StegoResult.mp3 -xf output.txt -e -p seeecretPassword