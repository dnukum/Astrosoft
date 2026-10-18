# Astrosoft - Tester Download & Installation Guide

Welcome to the **Astrosoft** tester program! This guide walks you through downloading, installing Java prerequisites, and launching Astrosoft on **Windows**, **macOS**, and **Linux**.

---

## 1. System Requirements

* **Operating System**: 
  * Windows 10 or 11 (64-bit)
  * macOS 11 (Big Sur) or later (Apple Silicon M1/M2/M3/M4 or Intel)
  * Linux (Ubuntu 20.04+, Debian 11+, Fedora, etc.)
* **Java Runtime**: Java 8, 11, 17, or 21 LTS (recommended: **Java 17 or 21 LTS** from [Adoptium Temurin](https://adoptium.net/)).
* **Memory & Disk**: Minimum 512 MB RAM, ~150 MB free disk space.

---

## 2. Step 1: Install Java (Prerequisite)

Astrosoft is a Java application. If you do not have Java installed:

1. Visit **[Adoptium Temurin Downloads](https://adoptium.net/temurin/releases/)** (free, open-source OpenJDK).
2. Download the installer for your operating system:
   * **Windows**: Download `.msi` (x64) and run the installer. **Ensure "Set JAVA_HOME variable" and "Add to PATH" are selected during installation.**
   * **macOS**: Download `.pkg` (select `x64` for Intel Mac, or `aarch64` for Apple Silicon M1/M2/M3) and complete the installer.
   * **Linux**: Install via package manager, e.g.:
     ```bash
     # Ubuntu / Debian
     sudo apt update && sudo apt install -y openjdk-17-jre
     
     # Fedora / RHEL
     sudo dnf install -y java-17-openjdk
     ```
3. **Verify Java installation**:
   Open Command Prompt (Windows) or Terminal (Mac/Linux) and run:
   ```bash
   java -version
   ```
   You should see output similar to:
   ```text
   openjdk version "17.0.x" ...
   OpenJDK Runtime Environment Temurin ...
   ```

---

## 3. Step 2: Download Astrosoft

You can download Astrosoft either as a ZIP archive or via Git.

### Option A: Direct ZIP Download (Recommended for Testers)
1. Download the latest source package directly from GitHub:
   👉 **[Download Astrosoft ZIP](https://github.com/dnukum/Astrosoft/archive/master.zip)**
2. Save the ZIP file to a convenient folder (e.g., `Desktop` or `Documents`).
3. Extract (unzip) the file:
   * **Windows**: Right-click `Astrosoft-master.zip` &rarr; select **Extract All...** &rarr; click **Extract**.
   * **macOS**: Double-click `Astrosoft-master.zip` to expand it into an `Astrosoft-master` folder.
   * **Linux**:
     ```bash
     unzip Astrosoft-master.zip -d Astrosoft
     ```

### Option B: Clone via Git
If you have Git installed, open Terminal/Command Prompt and run:
```bash
git clone https://github.com/dnukum/Astrosoft.git
cd Astrosoft
```

---

## 4. Step 3: Launch Astrosoft

### On Windows
1. Open the extracted `Astrosoft-master` (or `Astrosoft`) folder.
2. Locate the file **`RunAstroSoft.bat`**.
3. **Double-click `RunAstroSoft.bat`** to start Astrosoft.
   *(Alternatively, open Command Prompt in that folder and type `RunAstroSoft.bat`)*

> [!NOTE]
> If Windows SmartScreen displays a blue protection prompt (*"Windows protected your PC"*), click **More info** and then click **Run anyway**.

---

### On macOS
1. Open **Terminal** (press `Cmd + Space`, type `Terminal`, and hit Enter).
2. Navigate to the extracted folder:
   ```bash
   cd ~/Downloads/Astrosoft-master
   ```
   *(Replace with your actual folder path; you can type `cd ` and drag the folder into Terminal).*
3. Grant execution permissions to the launch scripts:
   ```bash
   chmod +x RunAstroSoft.sh bin/RunAstroSoft.sh
   ```
4. Launch Astrosoft:
   ```bash
   ./RunAstroSoft.sh
   ```

> [!TIP]
> If macOS displays a security prompt regarding unsigned files, open **System Settings &rarr; Privacy & Security** and click **Open Anyway** next to the prompt.

---

### On Linux
1. Open Terminal and navigate to the extracted folder:
   ```bash
   cd Astrosoft
   ```
2. Make scripts executable:
   ```bash
   chmod +x RunAstroSoft.sh bin/RunAstroSoft.sh
   ```
3. Run the application:
   ```bash
   ./RunAstroSoft.sh
   ```

---

## 5. First-Time Configuration & Quick Check

When Astrosoft launches for the first time:

1. **Main Screen**: You will see the Astrosoft desktop interface with top menus: *File, Edit, View, Window, Options, Help*.
2. **Setting Your Default Location**:
   * Go to **Options &rarr; Preferences** (or press `Ctrl+P` / `Cmd+,`).
   * Select the **Place** tab.
   * Select your City, State, and Country (built-in coordinates exist for major worldwide and Indian cities, or enter custom Latitude/Longitude and TimeZone).
   * Click **Save** / **OK**.
3. **Setting Ayanamsa**:
   * Astrosoft defaults to **Lahiri** ayanamsa (Chitrapaksha), with Raman and KP ayanamsas available under Preferences.
4. **Opening Muhurta**:
   * Click **View &rarr; My Muhurta** (or the Muhurta icon on the toolbar) to open the Muhurta calculation screen.

---

## 6. Troubleshooting

| Issue | Cause | Solution |
| :--- | :--- | :--- |
| `'java' is not recognized as an internal or external command` | Java is not in your system `PATH`. | Re-run the Java installer and check the box **"Add to PATH"**, or restart your command prompt/terminal. |
| `Permission denied` when running `./RunAstroSoft.sh` | Shell script lacks execution permission. | Run `chmod +x RunAstroSoft.sh bin/RunAstroSoft.sh` in Terminal before executing. |
| `IllegalAccessError: class com.digitprop.tonic.ComboBoxUI cannot access class sun.awt.AppContext` | Modern Java (Java 16+) encapsulation check. | Use the provided `RunAstroSoft.bat` or `RunAstroSoft.sh` scripts, which include required `--add-exports` and `--add-opens` flags. |
| Timezone mismatch or invalid timing windows | Selected place timezone does not match system timezone. | Go to **Options &rarr; Preferences &rarr; Place** and select a place with the correct local timezone. |

---

## 7. Reporting Feedback & Issues

As a tester, your feedback is invaluable! Please report:
* The steps to reproduce any errors or unexpected behavior.
* The date, time, and location tested.
* Any console output printed in the Command Prompt or Terminal window.
* Screenshots of any visual or layout defects.
