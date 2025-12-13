# BPager


BPager is an Android application that forwards notifications from selected apps to a connected Bluetooth device. Perfect for keeping track of important notifications on secondary devices like Bluetooth speakers, displays, or custom hardware projects.

## Features

###  Smart Notification Forwarding
- **Selective App Monitoring**: Choose which apps' notifications you want to forward
- **Real-time Notification Capture**: Intercepts and forwards notifications as they arrive
- **Rich Notification Content**: Forwards app name, notification title, and full text content
- **Multi-line Support**: Handles complex notifications with multiple text lines

###  Bluetooth Integration
- **Easy Device Selection**: Scan and connect to any paired Bluetooth device
- **Connection Status Monitoring**: Clear visual indicators for connection state
- **Auto-reconnect Support**: Maintains connection stability
- **Bluetooth Permissions**: Fully compliant with Android 12+ Bluetooth permission requirements

###  Modern Material Design
- **Material 3 Design**: Beautiful, modern UI following Google's latest design guidelines
- **Dynamic Colors**: Adapts to your device's color scheme
- **User-friendly Interface**: Intuitive navigation and clear status indicators

## Requirements

- **Android 8.0 (API 26) or higher**
- **Bluetooth-enabled Android device**
- **Target device**: Any Bluetooth device capable of receiving serial data

## Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/tikiya99/BPager.git
   ```

2. Open the project in Android Studio

3. Build and run the application on your Android device

## Permissions

BPager requires the following permissions:

- **Bluetooth Permissions** (`BLUETOOTH`, `BLUETOOTH_ADMIN`, `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`, `BLUETOOTH_ADVERTISE`): For managing Bluetooth connections
- **Location Permissions** (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`): Required for Bluetooth scanning on Android 10+
- **Notification Access** (`BIND_NOTIFICATION_LISTENER_SERVICE`): To intercept and read notifications
- **Post Notifications** (`POST_NOTIFICATIONS`): For foreground service notifications
- **Foreground Service** (`FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE`): For maintaining stable Bluetooth connection

## Setup & Usage

### First-time Setup

1. **Enable Notification Access**:
   - Open BPager
   - Tap "Enable Notification Access" when prompted
   - Toggle on BPager in the notification access settings
   - Return to the app

2. **Select Apps to Monitor**:
   - Tap "Select Apps" in the main screen
   - Choose which apps you want to forward notifications from
   - Common choices: WhatsApp, Messenger, Telegram, etc.

3. **Connect Bluetooth Device**:
   - Ensure your target Bluetooth device is paired with your phone
   - Tap "Select Device" in BPager
   - Choose your device from the list
   - Wait for connection confirmation

### Notification Format

Notifications are forwarded in the following format:
```
[package.name]
Notification Title
Notification content text
```

Example:
```
[com.whatsapp]
John Doe
Hey, are you available?
```

## Project Structure

```
app/src/main/java/com/example/lumanotifier/
├── MainActivity.kt                    # Main app interface & Bluetooth management
├── NotificationForwarderService.kt    # Notification listener service
├── AppSelectionActivity.kt            # App selection screen
├── AppSelectionAdapter.kt             # Adapter for app list
└── BluetoothDeviceAdapter.kt          # Adapter for Bluetooth device list
```

## Technical Details

### Architecture
- **Language**: Kotlin
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 36
- **UI**: View Binding with Material Components
- **Networking**: OkHttp 4.12.0 for potential future HTTP features

### Key Components

#### NotificationListenerService
Monitors notifications from selected apps and forwards them via Bluetooth using a singleton pattern for communication.

#### Bluetooth Manager
Handles device discovery, connection, and data transmission over Bluetooth serial communication.

#### App Selector
Allows users to choose which installed apps should have their notifications forwarded.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues, questions, or suggestions, please open an issue on GitHub.

## Changelog

### Version 1.0
- Initial release
- Notification forwarding functionality
- Bluetooth device management
- App selection interface
- Material 3 UI design
