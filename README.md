# Eindwerk

A small Java application intended to run on a Raspberry Pi to read controller data over a serial port and drive a servo via GPIO/PWM using Pi4J. The application spawns two threads:
- IOThread: initializes Pi4J providers and controls a servo on a PWM channel.
- SerialThread: reads incoming bytes from a serial device (defaults to `ttyAMA0`) and updates shared controller channel data.

The project uses Gradle (Kotlin DSL) with the Shadow plugin to produce a standalone runnable jar.

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
  - Starts IO and Serial threads.

## Requirements
- Operating system: Linux (intended for Raspberry Pi)
- Hardware: Raspberry Pi with GPIO PWM exposed via LinuxFS (e.g., `/sys/class/pwm`) and gpiod available
- Serial device connected and accessible as `ttyAMA0` (default in code). You may need to enable UART in Raspberry Pi configuration and ensure the serial console is disabled if using the same port.
- Permissions:
  - Access to GPIO/pwmchip via LinuxFS and gpiod often requires root or proper group membership. The provided `start.sh` uses `sudo`.
  - Log directory: by default `src/main/resources/log4j.properties` writes to `/home/pi/logs/awesome.log`. Ensure that directory exists and is writable, or change the configuration.
- Java Development Kit: TODO: Specify exact version. The project likely requires Java 17+ (Gradle plugins and modern dependencies) but this is not explicitly configured. Install a current LTS JDK (e.g., 17 or 21) on the target system.
- Gradle: Gradle Wrapper is provided; no separate installation required.

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
- `src/main/java/com/github/lazygamer1111/components/output/Servo.java` — servo abstraction and duty cycle calculation.
- `src/main/java/com/github/lazygamer1111/dataTypes/*` — simple data holder/util classes (AsyncData, ControllerData, etc.).
- `src/main/resources/log4j.properties` — logging configuration (console + rolling file to `/home/pi/logs/awesome.log`).
- `build.gradle.kts` — Gradle build, dependencies, Shadow jar, and Main-Class manifest.
- `settings.gradle.kts` — project name and Pi4J version property (`pi4j-ver = 2.8.0`).
- `start.sh` — helper script to run the shaded jar with sudo.

## How it works (high level)
- `SerialThread` continually reads from the configured serial port, parsing a framing pattern, then fills a 14-element integer array with channel values.
- `IOThread` converts one of the channel values into a servo angle and writes PWM updates via Pi4J, with a sleep interval (~21ms) sized to servo update rates.

## Troubleshooting
- Permission errors accessing GPIO or PWM: run as root (`sudo`) or configure udev/group permissions for your user.
- Serial port not found: verify `ls /dev/tty*` and update the port name in `SerialThread.java` or via future config.
- No logs written to file: ensure `/home/pi/logs` exists and is writable, or change `log4j.properties` path.

## License
- TODO: Add a LICENSE file and state the chosen license here (e.g., MIT, Apache-2.0).

## Roadmap / TODOs
- Externalize serial port and PWM/GPIO configuration via environment variables or configuration file (Avaje Config).
- Document hardware wiring (pins, power, level shifting if any).
- Add unit/integration tests.
- Consider using Javalin (already a dependency) for an optional web API/status page or remove it if not needed.
