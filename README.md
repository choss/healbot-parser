# HealBot SavedVariables Parser

A tool for parsing World of Warcraft HealBot addon configuration files and generating comprehensive HTML reports of your click-casting setups.

*Note: This app and documentation were exclusively vibe coded, but tested and works for my setup.*

## What It Does

This parser analyzes your HealBot SavedVariables files to extract and organize your click-casting configurations. It automatically searches your World of Warcraft installation directory and generates a beautiful, interactive HTML report showing:

- **All your characters** across different accounts and servers
- **Multiple HealBot files** per character (HealBot.lua and HealBot_Data.lua)
- **Click-casting bindings** organized by mouse button and modifier keys
- **Spell/action details** with Wowhead links for easy reference

## Features

- 📊 **Comprehensive Reports**: View all your HealBot configurations in one place
- 🎯 **Multi-Character Support**: Handles multiple accounts, servers, and characters
- 📁 **Multiple Files**: Processes both HealBot.lua and HealBot_Data.lua files
- 🔍 **Interactive Tables**: Sortable tables with filtering capabilities
- 🌐 **Wowhead Integration**: Direct links to spell information
- 📱 **Responsive Design**: Works on desktop and mobile devices
- ⚡ **Fast Native Version**: Available as a native executable for Windows

## Installation

### Option 1: JAR File (Recommended for most users)

1. Download the latest `healbot-parser-0.1.0-SNAPSHOT.jar` from the [Releases](https://github.com/choss/healbot-parser/releases) page
2. Ensure you have Java 17 or later installed
3. Run the JAR file as described in the Usage section below

### Option 2: Native Executable (Windows only)

1. Download the latest `healbot-parser.exe` from the [Releases](https://github.com/choss/healbot-parser/releases) page
2. No additional dependencies required - it's a standalone executable
3. Run the EXE file as described in the Usage section below

### Option 3: Build from Source

If you want to build from source:

```bash
# Clone the repository
git clone https://github.com/choss/healbot-parser.git
cd healbot-parser

# Build with Maven (requires Java 17+ and Maven)
mvn clean package

# The JAR file will be in target/healbot-parser-0.1.0-SNAPSHOT.jar
```

## Usage

### Basic Usage

#### JAR Version
```bash
java -jar healbot-parser-0.1.0-SNAPSHOT.jar [wow-directory] [output-file]
```

#### Native Executable (Windows)
```bash
healbot-parser.exe [wow-directory] [output-file]
```

### Parameters

- `wow-directory`: Path to your World of Warcraft installation directory (optional)
  - If not provided:
    - **JAR version**: Shows a file chooser dialog
    - **Native version**: Automatically searches for your World of Warcraft installation, or uses the current directory if not found
- `output-file`: Name of the output HTML file (optional, defaults to `healbot-report.html`)

The HTML report is generated in the same directory as the executable or JAR file.

### Examples

#### Example 1: Using JAR with file chooser (recommended for beginners)
```bash
java -jar healbot-parser-0.1.0-SNAPSHOT.jar
```
This will open a file browser where you can select your World of Warcraft folder.

#### Example 2: Using JAR with specific path
```bash
java -jar healbot-parser-0.1.0-SNAPSHOT.jar "C:\Program Files (x86)\World of Warcraft"
```

#### Example 3: Using JAR with custom output file
```bash
java -jar healbot-parser-0.1.0-SNAPSHOT.jar "C:\Program Files (x86)\World of Warcraft" my-report.html
```

#### Example 4: Using native executable (automatic detection)
```bash
healbot-parser.exe
```
This will automatically find your World of Warcraft installation and generate `healbot-report.html` in the same directory as the executable.

#### Example 5: Using native executable with specific path
```bash
healbot-parser.exe "C:\Program Files (x86)\World of Warcraft"
```

### Finding Your World of Warcraft Directory

Your World of Warcraft directory typically contains folders like `_retail_`, `_classic_`, etc. The parser will automatically find HealBot files in:

```
World of Warcraft/
├── WTF/
│   └── Account/
│       └── [Account Name]/
│           └── [Server Name]/
│               └── [Character Name]/
│                   └── SavedVariables/
│                       ├── HealBot.lua
│                       └── HealBot_Data.lua
```

## Output

The parser generates an HTML report in the same directory as the executable or JAR file, containing:

### Table of Contents
- Collapsible navigation showing all accounts, servers, and characters
- Character counts for each server
- Quick navigation to any character's configuration

### Character Reports
For each character, you'll see separate tables for each HealBot file:

- **File Header**: Shows which configuration file the bindings come from
- **Sortable Table**: Click column headers to sort by Button, Modifier, or Spell
- **Global Sort**: Buttons to sort all tables on the page simultaneously
- **Spell Links**: Click spell names to view details on Wowhead

### Sample Output Structure
```
Account/Server/Character
├── HealBot.lua
│   ├── Left Click → Healing Wave
│   ├── Right Click → Lesser Healing Wave
│   └── Middle Click → Mana Tide Totem
└── HealBot_Data.lua
    ├── Shift+Left → Chain Heal
    └── Ctrl+Right → Water Shield
```

## Troubleshooting

### "No HealBot configuration data found"
- Ensure you're pointing to the correct World of Warcraft directory
- Check that you have HealBot addon installed and configured
- Verify that HealBot has saved your settings (try logging in and out of the game)

### "Could not find or load main class" (JAR issues)
- Ensure you have Java 17 or later installed
- Try running with the full path: `java -jar /full/path/to/healbot-parser.jar`

### Native executable won't start
- Ensure you're on Windows (native executables are Windows-only)
- Try running from Command Prompt or PowerShell as Administrator
- Check Windows Defender or antivirus isn't blocking the executable

### GUI dialog doesn't appear (JAR version)
- You're likely in a headless environment (Linux server, Docker container, etc.)
- Use the command-line parameter instead: `java -jar healbot-parser.jar /path/to/wow`

### Report shows no bindings
- Some characters might not have HealBot configured yet
- Check that the character has logged in recently with HealBot enabled
- Verify the SavedVariables files exist and contain data

### Performance Issues
- Large numbers of characters may take longer to process
- The HTML report includes all data for fast browsing
- Consider processing one account at a time if you have many characters

## Tips

- **Regular Backups**: Run this after major HealBot configuration changes
- **Compare Configurations**: Use the report to compare setups between characters
- **Share Reports**: The HTML files are self-contained and can be shared with others
- **Browser Compatibility**: Works best in modern browsers (Chrome, Firefox, Edge)

## Support

If you encounter issues:

1. Check the troubleshooting section above
2. Ensure you're using the latest version from Releases
3. Verify your World of Warcraft and HealBot versions are compatible
4. Check the GitHub Issues for similar problems

## License

This project is open source. See individual file headers for license information.
