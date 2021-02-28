# gms2-ide-csv-filter
Filter for GameMaker Studio 2 Language CSV files 

## Install

Go to [release](https://github.com/GamemakerChina/gms2-ide-csv-filter/releases) to download plugin.

Extract the zip file and copy a plugin jar file, paste to `$HOME/.omegat/plugin` or `C:\Program Files\OmegaT\plugin` depending on your operating system.

Need to disable Magento CE CSV support, Go to `Options --> File Filters`, Find `Mangeto CE Locale CSV` and uncheck the checkbox on the right.

## Build
```bash
gradlew build # Linux and macOS

gradlew.bat build # Windows
```