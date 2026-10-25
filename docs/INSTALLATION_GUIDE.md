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
2. Locate the file **`RunAstroSoft20.bat`**.
3. **Double-click `RunAstroSoft20.bat`** to start Astrosoft.  
   *(Alternatively, open Command Prompt in that folder and type `RunAstroSoft20.bat`)*

> [!NOTE]
> If Windows SmartScreen displays a protection prompt (*"Windows protected your PC"*), click **More info** and then click **Run anyway**.

---

### On macOS

#### Method A: Install to Applications & Dock / Launch Bar (Recommended)
1. Open the extracted **`Astrosoft-master`** folder in **Finder**.
2. Locate **`InstallMacApp.command`** and **double-click** it (or right-click &rarr; **Open**).
   * *Note:* If macOS shows an *"unidentified developer"* security prompt on first run, open **System Settings &rarr; Privacy & Security**, scroll down to **Security**, and click **Open Anyway** next to `InstallMacApp.command`.
3. The installer will automatically build a self-contained **`Astrosoft20`** application inside your macOS **Applications** folder (`/Applications/Astrosoft20.app`) and reveal it in Finder.
4. Double-click **`Astrosoft20`** in **Applications** to launch it.
5. **Add to Launch Bar (Dock):** Drag **`Astrosoft20`** from **Applications** onto your **Dock** (or right-click the **Astrosoft20** icon in the Dock while it is running and select **Options &rarr; Keep in Dock**). You can now start Astrosoft20 anytime with a single click!

> [!TIP]
> **Subsequent Updates:** When you download a newer version of Astrosoft in the future, simply run `InstallMacApp.command` again. It cleanly removes the previous build and updates `/Applications/Astrosoft20.app` with the latest binaries. Your user settings (`~/.astrosoft_prefs/`) and charts are kept safe.

> [!NOTE]
> Application logs on macOS are standardly saved to `~/Library/Logs/Astrosoft20/astrosoft.log`.

#### Method B: Launch from Terminal (Advanced Users)
1. Open **Terminal** (press `Cmd + Space`, type `Terminal`, and hit Enter).
2. Navigate to the extracted folder:
   ```bash
   cd ~/Downloads/Astrosoft-master
   ```
   *(Replace with your actual folder path; you can type `cd ` and drag the folder from Finder into Terminal).*
3. Grant execution permissions to the launch scripts:
   ```bash
   chmod +x RunAstroSoft20.sh bin/RunAstroSoft20.sh InstallMacApp.command
   ```
4. Launch Astrosoft directly:
   ```bash
   ./RunAstroSoft20.sh
   ```
   *(Or run `./InstallMacApp.command` once to install `Astrosoft20.app` into `/Applications`).*

> [!TIP]
> If macOS displays a Gatekeeper security prompt regarding unsigned files, open **System Settings &rarr; Privacy & Security** and click **Open Anyway** next to the prompt.

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
| `Permission denied` when running `./RunAstroSoft20.sh` on Mac | Shell script lacks execution permission. | Run `chmod +x RunAstroSoft20.sh bin/RunAstroSoft20.sh` in Terminal before executing. |
| `IllegalAccessError` or Look & Feel crash | Running on modern Java without required export flags. | Use the provided `RunAstroSoft20.bat` (Windows) or `RunAstroSoft20.sh` (macOS), which include the necessary `--add-exports` flags. |
| Timezone mismatch or invalid timing windows | Selected place timezone does not match system timezone. | Go to **Options &rarr; Preferences &rarr; Place** and select a place with the correct local timezone. |

---

## 7. Reporting Feedback & Issues

As a tester, your feedback is invaluable! Please report:
* The steps to reproduce any errors or unexpected behavior.
* The date, time, and location tested.
* Any console output printed in the Command Prompt or Terminal window.
* Screenshots of any visual or layout defects.
