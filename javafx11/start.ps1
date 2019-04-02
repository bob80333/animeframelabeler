$java = cmd /c "java -version" '2>&1' | Out-String
$version = $java|%{$_.split('"')[1]}|%{$_.split('.')[0] + '.' + $_.split('.')[1]}
If ([System.Version]$version -lt [System.Version]"1.11") {
    java -jar animeframelabeler.jar
    } Else {
    java --module-path lib\javafx_java11\windows\lib\ --add-modules=javafx.controls,javafx.swing -jar animeframelabeler.jar
}