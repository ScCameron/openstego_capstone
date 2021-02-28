# WAV Testing:
Write-Output "Testing AudioLSB Plugin"
java -jar openstego.jar embed -a AudioLSB -mf msg1.txt -cf WAVCover1.wav -sf StegoResult.wav -e -p seeecretPassword
java -jar openstego.jar extract -a AudioLSB -sf StegoResult.wav -xf output.txt -e -p seeecretPassword
compare-object (get-content msg1.txt) (get-content output.txt)
Write-Output ""

java -jar openstego.jar embed -a AudioLSB -mf msg2.txt -cf WAVCover2.wav -sf StegoResult.wav -e -p seeecretPassword
java -jar openstego.jar extract -a AudioLSB -sf StegoResult.wav -xf output.txt -e -p seeecretPassword
compare-object (get-content msg2.txt) (get-content output.txt)
Write-Output ""

java -jar openstego.jar embed -a AudioLSB -mf MP3Cover1.mp3 -cf WAVCover1.wav -sf StegoResult.wav -e -p seeecretPassword
java -jar openstego.jar extract -a AudioLSB -sf StegoResult.wav -xf output.mp3 -e -p seeecretPassword
compare-object (get-content MP3Cover1.mp3) (get-content output.mp3)
Write-Output ""


# MP3 Testing:
Write-Output "Testing mp3Stego Plugin"
java -jar openstego.jar embed -a mp3Stego -mf msg1.txt -cf MP3Cover1.mp3 -sf StegoResult.mp3 -e -p seeecretPassword
java -jar openstego.jar extract -a mp3Stego -sf StegoResult.mp3 -xf output.txt -e -p seeecretPassword
compare-object (get-content msg1.txt) (get-content output.txt)
Write-Output ""

java -jar openstego.jar embed -a mp3Stego -mf msg2.txt -cf MP3Cover2.mp3 -sf StegoResult.mp3 -e -p seeecretPassword
java -jar openstego.jar extract -a mp3Stego -sf StegoResult.mp3 -xf output.txt -e -p seeecretPassword
compare-object (get-content msg2.txt) (get-content output.txt)
Write-Output ""

java -jar openstego.jar embed -a mp3Stego -mf msg2.txt -cf MP3Cover3.mp3 -sf StegoResult.mp3 -e -p seeecretPassword
java -jar openstego.jar extract -a mp3Stego -sf StegoResult.mp3 -xf output.txt -e -p seeecretPassword
compare-object (get-content msg2.txt) (get-content output.txt)
Write-Output ""

java -jar openstego.jar embed -a mp3Stego -mf msg1.txt -cf MP3Cover4.mp3 -sf StegoResult.mp3 -e -p seeecretPassword
java -jar openstego.jar extract -a mp3Stego -sf StegoResult.mp3 -xf output.txt -e -p seeecretPassword
compare-object (get-content msg1.txt) (get-content output.txt)
Write-Output ""

java -jar openstego.jar embed -a mp3Stego -mf msg3.file -cf MP3Cover5.mp3 -sf StegoResult.mp3 -e -p seeecretPassword
java -jar openstego.jar extract -a mp3Stego -sf StegoResult.mp3 -xf output.file -e -p seeecretPassword
compare-object (get-content msg3.file) (get-content output.file)
Write-Output ""

Write-Output "Cleaning generated files"
Remove-Item output.*
Remove-Item StegoResult.*
Read-Host -Prompt "Press Enter to exit"