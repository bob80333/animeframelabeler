version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
if [[ "$version" > "1.11" ]]; then
    java --module-path ./lib/javafx_java11/macos/lib/ --add-modules=javafx.controls,javafx.swing -jar animeframelabeler.jar
else
    java -jar animeframelabeler.jar
fi