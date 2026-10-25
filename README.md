Astrosoft
=========

Astrosoft is a free indian astrology software program for astrologers



## Documentation:

* 📥 **[Tester Download & Installation Guide](docs/INSTALLATION_GUIDE.md)**: Step-by-step installation instructions for Windows, macOS, and Linux.
* 🌟 **[Muhurta User Guide (9-Step Workflow)](docs/MUHURTA_USER_GUIDE.md)**: Complete guide to electing auspicious Muhurtas (Tara Balam, Chandra Balam, Nakshatra priorities, Panchanga, and chart evaluation).

## Installation:

* Install Java (JDK or JRE 21 LTS) from [Adoptium Temurin](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/) if you don't already have Java installed on your system.
* Run `java -version` to verify Java is installed. **Java 21 (LTS)** is required.
* Clone the repository or download the source code:
  * Using Git: `git clone https://github.com/dnukum/Astrosoft.git`
  * Or download the ZIP from [GitHub](https://github.com/dnukum/Astrosoft) by clicking this [link](https://github.com/dnukum/Astrosoft/archive/master.zip) and unzip `Astrosoft-master.zip`.
* Open a Command Prompt / PowerShell window on Windows, or a Terminal window on Mac / Linux.
* Navigate to the Astrosoft installation directory:
  ```bash
  cd Astrosoft
  ```

### Launching Astrosoft:

* **On Windows:**
  * Run `RunAstroSoft20.bat` (or navigate to `bin` and run `RunAstroSoft20.bat`):
    ```cmd
    RunAstroSoft20.bat
    ```
* **On macOS / Linux:**
  * **macOS Applications & Dock Setup (Recommended on Mac):** Double-click `InstallMacApp.command` in Finder (or run `./InstallMacApp.command`) to install `Astrosoft20.app` into `/Applications`, then drag it to your Dock. Log files are placed at `~/Library/Logs/Astrosoft20/astrosoft.log`.
  * **Launch via Terminal:**
    Ensure the launch script is executable (especially if downloaded via ZIP):
    ```bash
    chmod +x RunAstroSoft20.sh bin/RunAstroSoft20.sh InstallMacApp.command
    ```
    Launch Astrosoft:
    ```bash
    ./RunAstroSoft20.sh
    ```
    *(Or from `bin`: `cd bin && ./RunAstroSoft20.sh`)*

> **Note:** The launch scripts include `--add-exports` and `--add-opens` flags configured for Java 21 to access Swing/AWT desktop components cleanly.

Features:
---------

+ Birth Chart:   Provides Birth chart of individual with details of Lagna, Rasi, Nakshata, Thithi, Paksha, Siderial Time, Dasa Balance. 

+ Planetary Positions:   Provides Longitudes, Rasi, Nakshathra Pada postion of all planets and jaimini karakas. 

+ Bhava Positions:   Provides Start , Mid and End Positions for all bhavas. 

+ Divisional charts:  Computes Bhava, Navamsa and all 16 shodasha varga charts. 

+ Vimshottari dasa:  Computes Vimshottari dasa details up to anthra. 

+ Ashtavarga:  Computes Ashtavarga of all planets with trikona, ekathipathya reduction and gunaharas. 

+ Shadbala:  Computes all shadbala of planets with ishta bala, kashta bala, bhava bala and ranks them based on strength. 

+ Ephemeris:  Provides daily and monthy Ephemeris for all the planets. 

+ Panchang:  Provides daily panchang with details of Nakshathra, Thithi, Yoga, Sunrise/Sunset, Rahu kala, Yama kanda, Auspicious time. 

+ Muhurtha:  Provides auspicious muhurtha days with thara and chandara bala for given rasi/nakshathra. 

+ My Muhurta & Balam Calendar: Provides personal Muhurta evaluation with dual-profile Tara/Chandra Balam calendars, exact Thithi transition timestamps, and Subha Hora snapping.

+ Marriage Compactability:  Computes compactibility kutas and dosha for given pair of birth chart or nakshathra. 

+ Ayanamsa:  Supports Lahari, Raman and Krishnamoorthy ayanamsa. 

+ Location:  All calculations can be computed for any place in world by providing latitude / longitude. It also has built-in latitudes / longitudes for all cities in India. 

+ Language:  Available in English and Tamil. 

Development Setup:
-------------------

1. Install JDK (Java 21 LTS) and [Apache Ant](https://ant.apache.org/bindownload.cgi) and ensure both are added to your system `PATH`.
2. Navigate to the Astrosoft project directory:
   ```bash
   cd Astrosoft
   ```
3. Build and package the application:
   ```bash
   ant deploy
   ```
4. Build, deploy, and launch the application:
   ```bash
   ant deploy-run
   ```

License:
---------
Astrosoft source code is licensed under [GNU General Public License version 2](http://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
