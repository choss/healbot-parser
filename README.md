# healbot-parser

A tool to extract click-casting configurations from HealBot SavedVariables and generate HTML reports.

## Features

- Parses HealBot.lua SavedVariables files using LuaJ
- Extracts HealBot_Config_Spells.EnabledKeyCombo mappings
- Generates formatted HTML reports showing all key bindings per character and profile
- Supports multiple characters and profiles

## Requirements

- Java 11 or higher
- Maven 3.6+ (for building from source)

## Building

```bash
mvn clean package
```

This will create an executable JAR file in the `target/` directory.

## Usage

```bash
java -jar target/healbot-parser-1.0-SNAPSHOT.jar <path-to-HealBot.lua> [output.html]
```

### Arguments

- `<path-to-HealBot.lua>` - Path to your HealBot SavedVariables file (required)
- `[output.html]` - Optional output path for the HTML report (default: `healbot-report.html`)

### Example

```bash
java -jar target/healbot-parser-1.0-SNAPSHOT.jar /path/to/WTF/Account/ACCOUNT/SavedVariables/HealBot.lua my-healbot-config.html
```

## Output

The tool generates a styled HTML report showing:

- All characters found in the configuration
- All profiles for each character
- All key bindings with:
  - Key combination (e.g., Ctrl+Left, Shift+Right)
  - Spell name
  - Spell ID
  - Button number
  - Target type

## Development

### Running Tests

```bash
mvn test
```

### Project Structure

- `src/main/java/com/example/healbotparser/`
  - `HealBotParser.java` - Core parsing logic using LuaJ
  - `HtmlReportGenerator.java` - HTML report generation using Thymeleaf
  - `Main.java` - Command-line interface
- `src/main/resources/templates/`
  - `report.html` - Thymeleaf template for the HTML report
- `src/test/` - Unit tests and sample data

## License

This project is open source.

