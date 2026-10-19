# Astrosoft 2.0 - Download & Installation Guide

This guide walks you through installing the required Java 21 runtime, downloading Astrosoft, and launching the software on **Windows** and **macOS**.

---

## 1. System Requirements

* **Operating System**: 
  * **Windows**: Windows 10 or 11 (64-bit)
  * **macOS**: macOS 11 (Big Sur) or later (Apple Silicon M1/M2/M3/M4 or Intel)
* **Java Runtime**: **Java 21 LTS** (Eclipse Adoptium Temurin 21 recommended).
* **Memory & Disk**: Minimum 512 MB RAM, ~150 MB free disk space.

---

## 2. Step 1: Install Java 21 (Prerequisite)

Astrosoft requires **Java 21 LTS**. Installing Java 21 will not affect or break your existing Astrosoft 1.0 or other applications.

1. Go to the official **Eclipse Adoptium Temurin 21** download page:  
   👉 **[Download Java 21 LTS (Adoptium Temurin)](https://adoptium.net/temurin/releases/?version=21)**
2. Download the installer for your operating system:
   * **Windows**: Download `.msi` (x64).
   * **macOS**: Download `.pkg` (select `aarch64` for Apple Silicon M1/M2/M3/M4, or `x64` for Intel Mac).
3. Run the installer:
   * **Windows users**: During setup, make sure **"Add to PATH"** and **"Set JAVA_HOME variable"** are selected (enabled by default).
   * **macOS users**: Follow the standard wizard to complete the installation.
4. **Verify Java installation**:
   Open a new Command Prompt (Windows) or Terminal (macOS) and run:
   ```bash
   java -version
   ```
   You should see output confirming Java 21:
   ```text
   openjdk version "21.0.x" ...
   OpenJDK Runtime Environment Temurin ...
   ```

---

## 3. Step 2: Download Astrosoft

You can download Astrosoft either as a ZIP archive or via Git.

### Option A: Direct ZIP Download (Recommended)
1. Download the latest source package directly from GitHub:  
   👉 **[Download Astrosoft ZIP](https://github.com/dnukum/Astrosoft/archive/master.zip)**
2. Save the ZIP file to a convenient folder (e.g., `Desktop` or `Downloads`).
3. Extract (unzip) the file:
   * **Windows**: Right-click `Astrosoft-master.zip` &rarr; select **Extract All...** &rarr; click **Extract**.
   * **macOS**: Double-click `Astrosoft-master.zip` to expand it into an `Astrosoft-master` folder.

### Option B: Clone via Git
If you have Git installed, open Terminal / Command Prompt and run:
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
> If Windows SmartScreen displays a protection prompt (*"Windows protected your PC"*), click **More info** and then click **Run anyway**.

---

### On macOS
1. Open **Terminal** (press `Cmd + Space`, type `Terminal`, and hit Enter).
2. Navigate to the extracted folder:
   ```bash
   cd ~/Downloads/Astrosoft-master
   ```
   *(Replace with your actual folder path; you can type `cd ` and drag the folder from Finder into Terminal).*
3. Grant execution permissions to the launch scripts:
   ```bash
   chmod +x RunAstroSoft.sh bin/RunAstroSoft.sh
   ```
4. Launch Astrosoft:
   ```bash
   ./RunAstroSoft.sh
   ```

> [!TIP]
> If macOS displays a gatekeeper security prompt regarding unsigned files, open **System Settings &rarr; Privacy & Security** and click **Open Anyway** next to the prompt.

---

## 5. First-Time Configuration & Quick Check

When Astrosoft launches:

1. **Main Screen**: You will see the Astrosoft desktop interface with top menus: *File, Edit, View, Window, Options, Help*.
2. **Setting Your Default Location**:
   * Go to **Options &rarr; Preferences** (or press `Ctrl+P` / `Cmd+,`).
   * Select the **Place** tab.
   * Select your City, State, and Country (built-in coordinates exist for major worldwide and Indian cities, or enter custom Latitude/Longitude and TimeZone).
   * Click **Save** / **OK**.
3. **Setting Ayanamsa**:
   * Astrosoft defaults to **Lahiri** ayanamsa (Chitrapaksha), with Raman and KP ayanamsas available under Preferences.
4. **Opening Muhurta**:
   * Click **View &rarr; My Muhurta** to open the Muhurta calculation screen.

---

## 6. Troubleshooting

| Issue | Cause | Solution |
| :--- | :--- | :--- |
| `'java' is not recognized as an internal or external command` | Java is not in your system `PATH`. | Re-run the Java installer and ensure the box **"Add to PATH"** is selected, then restart your Command Prompt or Terminal. |
| `Permission denied` when running `./RunAstroSoft.sh` on Mac | Shell script lacks execution permission. | Run `chmod +x RunAstroSoft.sh bin/RunAstroSoft.sh` in Terminal before executing. |
| `IllegalAccessError` or Look & Feel crash | Running on modern Java without required export flags. | Use the provided `RunAstroSoft.bat` (Windows) or `RunAstroSoft.sh` (macOS), which include the necessary `--add-exports` flags. |
| Timezone mismatch or invalid timing windows | Selected place timezone does not match system timezone. | Go to **Options &rarr; Preferences &rarr; Place** and select a place with the correct local timezone. |

---

## 7. Reporting Feedback & Issues

As a tester, your feedback is invaluable! Please report:
* The steps to reproduce any errors or unexpected behavior.
* The date, time, and location tested.
* Any console output printed in the Command Prompt or Terminal window.
* Screenshots of any visual or layout defects.
