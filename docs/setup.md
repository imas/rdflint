# Setup

[Home](index.md) |
[Setup](setup.md) |
[Usage](usage.md) |
[Configuration](config.md) |
[Rules](rules.md) |
[Development](developer.md)

How to setup rdflint.

## Manual Setup

1. rdflint needs java runtime environment. If you do not have runtime, download and install JRE or JDK from follwing website.

   Java SE - Downloads | Oracle Technology Network | Oracle  
   [https://www.oracle.com/technetwork/java/javase/downloads/index.html](https://www.oracle.com/technetwork/java/javase/downloads/index.html)

2. Download rdflint from JitPack.  
   Put your browser ``https://jitpack.io/com/github/imas/rdflint/{{site.RDFLINT_VERSION}}/rdflint-{{site.RDFLINT_VERSION}}.jar`` , and download jar file.  
   If you like wget, you can use following command.

   ```
   $ wget https://jitpack.io/com/github/imas/rdflint/{{site.RDFLINT_VERSION}}/rdflint-{{site.RDFLINT_VERSION}}.jar
   ```

3. Move to rdflint downloaded directory, and run rdflint with following command.  
   No error messages is OK.

   ```
   $ java -jar rdflint-{{site.RDFLINT_VERSION}}.jar
   ```

## Setup to Visual Studio Code

1. rdflint needs java runtime environment. If you do not have runtime, download and install JRE or JDK from follwing website.

   Java SE - Downloads | Oracle Technology Network | Oracle  
   [https://www.oracle.com/technetwork/java/javase/downloads/index.html](https://www.oracle.com/technetwork/java/javase/downloads/index.html)

2. Set Java installed directory to enviroment variable ``JAVA_HOME`` .  
   Note. Java installed directory is usually ``C:\Program Files\Java\jdk-(version number)``.

3. Start Visual Studio Code.  
   If you already started VSS, restart for activate environment variable.

4. From Extensions menu(File->Preferences->Extensions),  
   search with keyword ``rdflint`` etc, and select ``RDF lanauage support via rdflint``  
   Select ``Install``, and install extention.

5. From Ctrl+Shift+P menu, select ``rdflint interactive mode: SPARQL playground``  
   and start rdflint with interactive mode.

## Setup with homebrew - for macOS

1. Install homebrew from following website interaction, if you do not installed yet.

   Homebrew  
   [https://brew.sh/index_ja](https://brew.sh/index_ja)

2. Install rdflint with following command.

   ```
   $ brew tap imas/rdflint
   $ brew install rdflint
   ```

   Note. If you use ``takemikami/takemikami`` (Before version 0.1.1), change formulaue repository with following step。

   ```
   $ brew untap takemikami/takemikami
   $ brew tap imas/rdflint
   ```

3. Run rdflint with following command.  
   No error messages is OK.

   ```
   $ rdflint
   ```

Note, In the case of install by Homebrew, replace ``java -jar rdflint-{{site.RDFLINT_VERSION}}.jar`` to ``rdflint`` in Usage.

{{site.cookie_consent}}
