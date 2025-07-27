# Pocket Experimental Physics - Android Application

A mobile Android application for collecting and transmitting inertial sensor data via Bluetooth to a server for real-time visualization and analysis.

## Features

- **Real-time Sensor Data Collection**: Captures accelerometer, gyroscope, and magnetometer data
- **Bluetooth Transmission**: Sends sensor data to Bluetooth server in real-time
- **Multi-sensor Support**: Individual control for each sensor type
- **Intuitive Interface**: Simple controls for connection and data transmission
- **Safety Warnings**: Built-in warnings for experimental use scenarios
- **Bilingual Support**: English and Portuguese interface

## System Requirements

### Software Requirements
- Android Studio Narwhal (2025.1.1) or later
- JDK 11 or higher
- Gradle 8.11.1
- Kotlin 1.9.0
- Git 2.30 or higher
- OS: Windows 10+, macOS 10.14+, or Linux Ubuntu 18.04+

### Android Device Requirements
- Android 12 (API Level 31) or higher
- Bluetooth 4.0 or higher
- Inertial sensors (accelerometer, gyroscope, magnetometer)

## Installation

### Android Studio Setup

1. **Download Android Studio**
   - Visit https://developer.android.com/studio
   - Download the latest version
   - Install with default settings including:
     - Android SDK
     - Android SDK Platform
     - Android Virtual Device (AVD)
     - Performance (Intel HAXM) for Intel processors

2. **Initial Configuration**
   - Complete setup wizard on first launch
   - Allow automatic SDK download and configuration
   - Verify Android SDK API Level 31+ is installed via Tools → SDK Manager

### Project Setup

1. **Clone Repository**
   ```bash
   git clone <repository-url>
   cd pocket-experimental-physics-android
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to project folder and select

3. **Gradle Sync**
   - Click "Sync Now" when prompted
   - Wait for dependency download (may take several minutes)
   - If sync fails, try: File → Invalidate Caches and Restart

## Device Setup for Development

### Enable Developer Mode
1. Go to Settings → About Phone
2. Tap "Build number" 7 times
3. Developer options will be enabled

### Enable USB Debugging
1. Go to Settings → Developer Options
2. Enable "USB Debugging"
3. Enable "Stay awake" (recommended)
4. Enable "Install via USB"

### Connect Device
- **USB**: Connect via USB cable, allow debugging when prompted
- **Wireless**: Enable "Wireless debugging" in Developer Options, pair via Android Studio Device Manager

## Building and Running

1. **Select Device**
   - Choose connected device from toolbar dropdown
   - Verify device appears in available devices list

2. **Build and Install**
   - Click Run button (green play icon) or press Shift+F10
   - Grant permissions when prompted:
     - Bluetooth access
     - Location access (required for Bluetooth on Android 12+)
     - Sensor access

3. **First Run**
   - App will request necessary permissions
   - Grant all permissions for full functionality

## Usage

1. **Server Connection**
   - Ensure Bluetooth server is running on computer
   - Tap "Connect" to search for available devices
   - Select server from list when prompted

2. **Sensor Configuration**
   - Use switches to enable/disable sensors:
     - Accelerometer (enabled by default)
     - Gyroscope (disabled by default)
     - Magnetometer (disabled by default)

3. **Data Transmission**
   - After connection, tap "Start Transmission"
   - Sensor data will stream to server in real-time
   - Tap "Stop Transmission" to pause data flow
   - Access web interface to visualize data

## Project Structure

```
app/src/main/java/br/usp/poli/pocketexperimentalphysics/
├── MainActivity.kt                    # Main application activity
├── connection/
│   └── BluetoothConnectionManager.kt  # Bluetooth communication handler
├── sensors/
│   ├── SensorHandler.kt              # Sensor management and data collection
│   ├── SensorData.kt                 # Abstract sensor data class
│   ├── AccelerometerData.kt          # Accelerometer data model
│   ├── GyroscopeData.kt              # Gyroscope data model
│   ├── MagnetometerData.kt           # Magnetometer data model
│   └── interfaces/
│       └── SensorDataListener.kt     # Sensor data callback interface
└── ui/theme/                         # UI theme and styling
```

## Data Format

Sensor data is transmitted as JSON:
```json
{
  "type": "accelerometer",
  "x": 0.123,
  "y": -0.456,
  "z": 9.789
}
```

## Troubleshooting

**Connection Issues**
- Verify Bluetooth is enabled
- Check server is running and discoverable
- Ensure devices are in close proximity
- Try restarting Bluetooth on both devices

**Build Issues**
- Clean and rebuild project: Build → Clean → Rebuild
- Invalidate caches: File → Invalidate Caches and Restart
- Check internet connection for dependency downloads

**Permission Issues**
- Manually grant permissions in device Settings → Apps → Pocket Experimental Physics
- For Android 12+, location permission is required for Bluetooth functionality

## Safety Warning

⚠️ **IMPORTANT**: We do not take responsibility for any damage to your device caused by experimental use. Avoid exposure to strong magnetic fields, extreme temperatures, or physical impacts that may harm your phone's sensors or functionality. Use at your own risk.

---

# Pocket Experimental Physics - Aplicativo Android

Um aplicativo móvel Android para coleta e transmissão de dados de sensores inerciais via Bluetooth para um servidor para visualização e análise em tempo real.

## Funcionalidades

- **Coleta de Dados de Sensores em Tempo Real**: Captura dados do acelerômetro, giroscópio e magnetômetro
- **Transmissão Bluetooth**: Envia dados dos sensores para servidor Bluetooth em tempo real
- **Suporte Multi-sensores**: Controle individual para cada tipo de sensor
- **Interface Intuitiva**: Controles simples para conexão e transmissão de dados
- **Avisos de Segurança**: Avisos integrados para cenários de uso experimental
- **Suporte Bilíngue**: Interface em inglês e português

## Requisitos do Sistema

### Requisitos de Software
- Android Studio Narwhal (2025.1.1) ou superior
- JDK 11 ou superior
- Gradle 8.11.1
- Kotlin 1.9.0
- Git 2.30 ou superior
- SO: Windows 10+, macOS 10.14+, ou Linux Ubuntu 18.04+

### Requisitos do Dispositivo Android
- Android 12 (API Level 31) ou superior
- Bluetooth 4.0 ou superior
- Sensores inerciais (acelerômetro, giroscópio, magnetômetro)

## Instalação

### Configuração do Android Studio

1. **Download do Android Studio**
   - Acesse https://developer.android.com/studio
   - Baixe a versão mais recente
   - Instale com configurações padrão incluindo:
     - Android SDK
     - Android SDK Platform
     - Android Virtual Device (AVD)
     - Performance (Intel HAXM) para processadores Intel

2. **Configuração Inicial**
   - Complete o assistente de configuração na primeira inicialização
   - Permita download e configuração automática do SDK
   - Verifique se Android SDK API Level 31+ está instalado via Tools → SDK Manager

### Configuração do Projeto

1. **Clonar Repositório**
   ```bash
   git clone <repository-url>
   cd pocket-experimental-physics-android
   ```

2. **Abrir no Android Studio**
   - Inicie o Android Studio
   - Selecione "Open an Existing Project"
   - Navegue até a pasta do projeto e selecione

3. **Sincronização do Gradle**
   - Clique em "Sync Now" quando solicitado
   - Aguarde o download das dependências (pode levar vários minutos)
   - Se a sincronização falhar, tente: File → Invalidate Caches and Restart

## Configuração do Dispositivo para Desenvolvimento

### Habilitar Modo Desenvolvedor
1. Vá para Configurações → Sobre o Telefone
2. Toque em "Número da versão" 7 vezes
3. Opções do desenvolvedor serão habilitadas

### Habilitar Depuração USB
1. Vá para Configurações → Opções do Desenvolvedor
2. Habilite "Depuração USB"
3. Habilite "Permanecer ativo" (recomendado)
4. Habilite "Instalação via USB"

### Conectar Dispositivo
- **USB**: Conecte via cabo USB, permita depuração quando solicitado
- **Sem fio**: Habilite "Depuração wireless" em Opções do Desenvolvedor, pareie via Device Manager do Android Studio

## Compilação e Execução

1. **Selecionar Dispositivo**
   - Escolha dispositivo conectado no dropdown da barra de ferramentas
   - Verifique se o dispositivo aparece na lista de dispositivos disponíveis

2. **Compilar e Instalar**
   - Clique no botão Run (ícone play verde) ou pressione Shift+F10
   - Conceda permissões quando solicitado:
     - Acesso Bluetooth
     - Acesso à localização (necessário para Bluetooth no Android 12+)
     - Acesso aos sensores

3. **Primeira Execução**
   - O app solicitará as permissões necessárias
   - Conceda todas as permissões para funcionalidade completa

## Uso

1. **Conexão com Servidor**
   - Certifique-se de que o servidor Bluetooth está rodando no computador
   - Toque em "Connect" para buscar dispositivos disponíveis
   - Selecione o servidor da lista quando solicitado

2. **Configuração de Sensores**
   - Use os switches para habilitar/desabilitar sensores:
     - Acelerômetro (habilitado por padrão)
     - Giroscópio (desabilitado por padrão)
     - Magnetômetro (desabilitado por padrão)

3. **Transmissão de Dados**
   - Após conexão, toque em "Start Transmission"
   - Dados dos sensores serão transmitidos para o servidor em tempo real
   - Toque em "Stop Transmission" para pausar o fluxo de dados
   - Acesse a interface web para visualizar os dados

## Estrutura do Projeto

```
app/src/main/java/br/usp/poli/pocketexperimentalphysics/
├── MainActivity.kt                    # Atividade principal da aplicação
├── connection/
│   └── BluetoothConnectionManager.kt  # Gerenciador de comunicação Bluetooth
├── sensors/
│   ├── SensorHandler.kt              # Gerenciamento e coleta de dados dos sensores
│   ├── SensorData.kt                 # Classe abstrata de dados de sensor
│   ├── AccelerometerData.kt          # Modelo de dados do acelerômetro
│   ├── GyroscopeData.kt              # Modelo de dados do giroscópio
│   ├── MagnetometerData.kt           # Modelo de dados do magnetômetro
│   └── interfaces/
│       └── SensorDataListener.kt     # Interface de callback de dados de sensor
└── ui/theme/                         # Tema e estilização da UI
```

## Formato de Dados

Os dados dos sensores são transmitidos como JSON:
```json
{
  "type": "accelerometer",
  "x": 0.123,
  "y": -0.456,
  "z": 9.789
}
```

## Solução de Problemas

**Problemas de Conexão**
- Verifique se o Bluetooth está habilitado
- Confirme se o servidor está rodando e descobrível
- Certifique-se de que os dispositivos estão próximos
- Tente reiniciar o Bluetooth em ambos os dispositivos

**Problemas de Compilação**
- Limpe e recompile o projeto: Build → Clean → Rebuild
- Invalide caches: File → Invalidate Caches and Restart
- Verifique conexão com internet para download de dependências

**Problemas de Permissão**
- Conceda permissões manualmente em Configurações → Apps → Pocket Experimental Physics
- Para Android 12+, permissão de localização é necessária para funcionalidade Bluetooth

## Aviso de Segurança

⚠️ **IMPORTANTE**: Não nos responsabilizamos por danos ao seu dispositivo causados por uso experimental. Evite exposição a campos magnéticos intensos, temperaturas extremas ou impactos físicos que possam prejudicar os sensores ou funcionalidade do telefone. Use por sua conta e risco.
