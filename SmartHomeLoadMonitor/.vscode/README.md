# Smart Home Load Monitor

IoT-based Smart Home Energy Management System for Ghanaian Homes

## Quick Start

### Method 1: One-Click (Easiest!)
Double-click `start.bat`

### Method 2: Step by Step
1. Run `compile.bat`
2. Run `run.bat`

### Method 3: VSCode
1. Open folder in VSCode
2. Press `F5`

## Requirements

- Java JDK 17+
- JavaFX SDK 21 (at C:\javafx-sdk-21.0.10\javafx-sdk-21.0.10)
- Windows 11

## Features

✅ Real-time monitoring (2-second updates)
✅ Overload detection
✅ Surge detection
✅ Energy & cost tracking
✅ Alert system
✅ Two simulation modes

## Usage

1. Click "▶ Start" to begin monitoring
2. Watch appliances update in real-time
3. Check alerts at bottom
4. View energy/cost on right panel

## Settings

Click "⚙ Settings" to:
- Change voltage (default: 230V)
- Set main limit (default: 40A)
- Adjust tariff (default: GHS 0.50/kWh)
- Switch simulation mode (Random/Scripted)

## Simulation Modes

**Random:** Realistic variations, occasional surges
**Scripted:** Predefined scenarios showing all features

## Project Structure
```
src/main/java/com/smartload/
├── SmartLoadApp.java          (Main entry)
├── controllers/               (UI)
├── models/                    (Data)
├── services/                  (Logic)
└── simulation/                (Test data)
```

## Troubleshooting

**"java not recognized"**
- Install Java 17
- Add to PATH

**Compilation errors**
- Check JavaFX path in .bat files
- Verify folder structure

**Application won't start**
- Run from Command Prompt to see errors
- Check console output

## Author

[Your Name]
Final Year Project - 2025/2026

## License

Educational Use Only