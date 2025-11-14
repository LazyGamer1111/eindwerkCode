# Eindwerk

*Last updated: November 6, 2025*

## Overview

A Java application designed to run on a Raspberry Pi that provides real-time control of servos and Electronic Speed Controllers (ESCs) using controller data received over a serial connection. The application is built with a multi-threaded architecture for efficient handling of I/O operations and serial communication.

### Key Features

- **Multi-threaded Architecture**: Separates I/O operations from serial communication for better performance
  - **IOThread**: Initializes Pi4J providers and controls servos via PWM channels
  - **SerialThread**: Reads incoming bytes from a serial device (defaults to `ttyAMA0`) and updates shared controller data
  - **PIOThread**: Controls Electronic Speed Controllers (ESCs) based on controller input
- **Hardware Control**: Precise control of servos and ESCs with configurable parameters
- **Debug Mode**: Optional debug server for remote monitoring and diagnostics
- **Native Integration**: Uses JNI for direct hardware access where needed

The project uses Gradle (Kotlin DSL) with the Shadow plugin to produce a standalone runnable jar that includes all dependencies.

## Stack
- Language: Java (version not pinned in the build; see Requirements)
- Build tool: Gradle with Kotlin DSL (Gradle Wrapper included)
  - Plugin: com.gradleup.shadow (for fat jar)
- Key libraries:
  - Pi4J 2.x (core, raspberrypi, linuxfs, gpiod) for GPIO/PWM on Raspberry Pi
  - jSerialComm for serial communication
  - SLF4J API with reload4j binding (Log4j-compatible configuration via `log4j.properties`)
  - Avaje Config (present but not used in code yet)
  - JCTools (SPSC queue used by AsyncData)
  - Javalin (present as a dependency but not used yet; TODO)

## Entry point
- Main class: `com.github.lazygamer1111.Main`
  - Starts IO, Serial, and PIO threads.

## Requirements

### System Requirements
- **Operating System**: Linux (specifically designed for Raspberry Pi)
- **Hardware**: 
  - Raspberry Pi with GPIO PWM exposed via LinuxFS (e.g., `/sys/class/pwm`)
  - gpiod library installed
  - Serial device connected and accessible as `ttyAMA0` (default in code)
  
### Software Requirements
- **Java Development Kit**: Java 17 or newer (JDK 17 LTS or JDK 21 LTS recommended)
- **Gradle**: Gradle Wrapper is included; no separate installation required
- **Native Libraries**: The application requires a native library that must be compiled for the target platform

### Configuration Requirements
- **UART Configuration**: You may need to enable UART in Raspberry Pi configuration and ensure the serial console is disabled if using the same port
- **Permissions**:
  - Access to GPIO/pwmchip via LinuxFS and gpiod often requires root or proper group membership. The provided `start.sh` uses `sudo`
  - Log directory: by default `src/main/resources/log4j.properties` writes to `/home/pi/logs/awesome.log`. Ensure that directory exists and is writable, or change the configuration

## Setup
1. Clone this repository.
2. Ensure JDK is installed and on PATH (`java -version`).
3. On Raspberry Pi, enable UART and PWM as needed, and install gpiod userspace tools if not present.
4. Create the log directory if you keep the default path:
   - `mkdir -p /home/pi/logs` (or update `src/main/resources/log4j.properties`).

## Build
- Using the Gradle Wrapper:
  - Linux/macOS: `./gradlew build`
  - Windows: `gradlew.bat build`

This will also produce a fat jar via the Shadow plugin:
- Output: `build/libs/eindwerk-1.0-SNAPSHOT.jar`

## Run
- On Raspberry Pi (as root due to GPIO/serial access in many setups):
  - `sudo java -jar build/libs/eindwerk-1.0-SNAPSHOT.jar`
- Alternatively, use the provided script:
  - `./start.sh` (ensure it is executable: `chmod +x start.sh`)

Notes:
- SerialThread currently selects port name `ttyAMA0` directly. If your device is different (e.g., `ttyS0`, `ttyUSB0`), you will need to update `SerialThread.java` or make this configurable. TODO: externalize serial port via env var or config file.
- IOThread sets up a PWM on address 2 (`LinuxFsPwmProvider.newInstance(2)`), and a digital output on GPIO 17. Ensure these match your wiring. TODO: document wiring and pinout.

## Scripts and Tasks
- `start.sh`: runs the shaded jar with `sudo`.
- Gradle tasks of interest:
  - `build`: builds and also depends on `shadowJar` (configured in `build.gradle.kts`).
  - `shadowJar`: creates a runnable fat jar without the `-all` classifier.
  - `test`: runs tests (JUnit 5 platform). Currently there are no test sources in the repo.

## Configuration and Environment Variables
- Avaje Config is included but not used in the current codebase.
- SLF4J/reload4j reads Log4j configuration from `src/main/resources/log4j.properties`.
- Known configurable items (hardcoded today):
  - Serial port device: `ttyAMA0`. TODO: allow override via environment variable (e.g., `SERIAL_PORT`) or configuration.
  - Log file path: `/home/pi/logs/awesome.log`. You can change this in `log4j.properties`. TODO: make configurable per environment.
- No other environment variables are required by the current code.

## Tests
- Framework: JUnit 5 (Jupiter) is configured.
- Current status: No test classes are present. Running `./gradlew test` will execute zero tests.
- TODO: Add unit tests for:
  - Servo duty cycle calculations (`Servo.calcDutyCycle`)
  - Serial parsing logic in `SerialThread`
  - IO timing behavior as feasible (could be integration-level on hardware)

## Project Structure
- `src/main/java/com/github/lazygamer1111/Main.java` — application entrypoint; starts threads.
- `src/main/java/com/github/lazygamer1111/threads/IOThread.java` — Pi4J setup and servo control loop.
- `src/main/java/com/github/lazygamer1111/threads/SerialThread.java` — serial reader updating `controllerData`.
- `src/main/java/com/github/lazygamer1111/threads/PIOThread.java` — controls ESC hardware based on controller data.
- `src/main/java/com/github/lazygamer1111/components/output/Servo.java` — servo abstraction and duty cycle calculation.
- `src/main/java/com/github/lazygamer1111/dataTypes/*` — simple data holder/util classes (AsyncData, ControllerData, etc.).
- `src/main/resources/log4j.properties` — logging configuration (console + rolling file to `/home/pi/logs/awesome.log`).
- `build.gradle.kts` — Gradle build, dependencies, Shadow jar, and Main-Class manifest.
- `settings.gradle.kts` — project name and Pi4J version property (`pi4j-ver = 2.8.0`).
- `start.sh` — helper script to run the shaded jar with sudo.

## How it works (high level)
- `SerialThread` continually reads from the configured serial port, parsing a framing pattern, then fills a 14-element integer array with channel values.
- `IOThread` converts one of the channel values into a servo angle and writes PWM updates via Pi4J, with a sleep interval (~21ms) sized to servo update rates.
- `PIOThread` monitors another channel value and controls an ESC by setting its state to 0 or 1 based on whether the value exceeds a threshold (1500).

## Troubleshooting
- Permission errors accessing GPIO or PWM: run as root (`sudo`) or configure udev/group permissions for your user.
- Serial port not found: verify `ls /dev/tty*` and update the port name in `SerialThread.java` or via future config.
- No logs written to file: ensure `/home/pi/logs` exists and is writable, or change `log4j.properties` path.

## License
MIT License. See the LICENSE file for details.

## Roadmap / Future Enhancements
- **Configuration Improvements**:
  - Externalize serial port and PWM/GPIO configuration via environment variables or configuration file (using Avaje Config)
  - Make debug mode configurable through environment variables
- **Documentation**:
  - Add detailed hardware wiring diagrams (pins, power, level shifting if any)
  - Create setup guides for different Raspberry Pi models
- **Testing**:
  - Add unit tests for core components (Servo, ESC, data handling)
  - Implement integration tests for hardware interaction
- **Features**:
  - Implement web-based monitoring interface using Javalin
  - Add support for more controller types and protocols
  - Improve error handling and recovery mechanisms
