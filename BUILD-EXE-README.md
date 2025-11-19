# Build Windows EXE Script

This script (`build-exe.bat`) builds a Windows executable for the HealBot Parser using GraalVM native-image.

## Prerequisites

1. **Java 17**: Ensure JDK 17 is installed.
2. **Maven**: Install Maven (e.g., via Chocolatey: `choco install maven`).
3. **GraalVM**: Download and install GraalVM Community Edition JDK 17 from [GraalVM Downloads](https://www.graalvm.org/downloads/).
   - Set `JAVA_HOME` to the GraalVM installation directory.
   - Add `%JAVA_HOME%\bin` to your PATH.
   - Install native-image: Run `gu install native-image` in a command prompt.

## Usage

1. Open Command Prompt as Administrator.
2. Navigate to the project directory: `cd path\to\healbot-parser`
3. Run the script: `build-exe.bat`

The script will:
- Clean and build the project with Maven.
- Generate the native image as `healbot-parser.exe`.

## Notes

- The build may take several minutes.
- Ensure no antivirus software interferes with the build process.
- For cross-platform builds, use the GitHub Actions CI.