# IMPORTANT: you must download and put ffmpeg.exe in this folder to run the tests

.\ffmpeg.exe -i losslessVid.mp4 -c:v rawvideo -pixel_format yuv420p raw.yuv


java -jar openstego.jar embed -a VideoStego -mf mp3Cover2.mp3 -cf raw.yuv -sf stegoResult.yuv


.\ffmpeg -f rawvideo -pix_fmt yuv420p -s:v 640:360 -i stegoResult.yuv -c:v libx264 -crf 0 mp4Stego.mp4

.\ffmpeg.exe -i mp4Stego.mp4 -c:v rawvideo -pixel_format yuv420p stegoResultDecompressed.yuv
compare-object (get-content stegoResult.yuv) (get-content stegoResultDecompressed.yuv)


java -jar openstego.jar extract -a VideoStego -sf stegoResultDecompressed.yuv -xf output.mp3
compare-object (get-content mp3Cover2.mp3) (get-content output.mp3)

Write-Output "Cleaning generated files"
Remove-Item raw.*
Remove-Item output.*
Remove-Item stegoResult*
Remove-Item mp4Stego.*

Read-Host -Prompt "Press Enter to exit"