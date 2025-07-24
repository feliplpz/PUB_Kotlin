# Pocket Experimental Physics - Aplicativo Android

## Visão Geral

O Pocket Experimental Physics é um aplicativo Android desenvolvido para coleta e transmissão de dados de sensores inerciais via Bluetooth. O aplicativo permite a captura de dados do acelerômetro, giroscópio e magnetômetro do dispositivo móvel, transmitindo essas informações para um servidor Bluetooth para análise posterior.

## Requisitos do Sistema

### Requisitos de Software
- Android Studio Narwhal (2025.1.1) ou versão mais recente
- JDK 11 ou superior
- Gradle 8.11.1
- Kotlin 1.9.0
- Git 2.30 ou superior
- Sistema operacional: Windows 10+, macOS 10.14+, ou Linux Ubuntu 18.04+

### Requisitos do Dispositivo Android
- Android 12 (API Level 31) ou superior
- Bluetooth 4.0 ou superior
- Sensores inerciais (acelerômetro, giroscópio, magnetômetro)

## Instalação do Android Studio

### Download e Instalação

Acesse o site oficial do Android Studio em https://developer.android.com/studio para fazer o download da versão mais recente. Execute o instalador e siga as instruções padrão de instalação, certificando-se de que os seguintes componentes estejam selecionados durante o processo:

- Android SDK
- Android SDK Platform
- Android Virtual Device (AVD)
- Performance (Intel HAXM) - apenas para Windows/Linux com processador Intel

### Configuração Inicial

Após a primeira inicialização do Android Studio, complete o assistente de configuração inicial e permita que o programa baixe e configure o SDK do Android automaticamente. Verifique se o SDK do Android API Level 31 ou superior está instalado através do menu Tools → SDK Manager → Android SDK → SDK Platforms.

## Clonagem do Projeto via Android Studio

### Método Recomendado: Clonagem Direta no Android Studio

Na tela inicial do Android Studio, selecione a opção "Get from VCS" (Version Control System). Na janela que se abrir, insira a URL do repositório Git do projeto. Escolha o diretório de destino onde deseja salvar o projeto localmente e clique em "Clone". O Android Studio automaticamente detectará que se trata de um projeto Android e iniciará a configuração necessária.

### Configuração do Git (Primeira Vez)

Caso seja a primeira vez utilizando o Git no sistema, configure suas credenciais através do menu File → Settings → Version Control → Git. Insira seu nome de usuário e email institucional da USP para identificação nos commits.

## Sincronização do Gradle

### Processo de Sincronização

Após a clonagem do projeto, o Android Studio automaticamente detectará os arquivos de configuração do Gradle e exibirá uma notificação solicitando a sincronização. Clique em "Sync Now" na barra de notificação que aparece no topo da tela. Este processo pode levar alguns minutos na primeira execução, pois o sistema precisará baixar todas as dependências do projeto.

### Sincronização Manual

Caso a sincronização automática não ocorra, acesse o menu File → Sync Project with Gradle Files ou clique no ícone de sincronização do Gradle na barra de ferramentas. Aguarde a conclusão do processo e verifique se não há erros na aba "Build" na parte inferior da tela.

### Resolução de Problemas de Sincronização

Se a sincronização falhar, verifique sua conexão com a internet e execute uma limpeza do projeto através do menu Build → Clean Project, seguido de Build → Rebuild Project. Em casos persistentes, acesse File → Invalidate Caches and Restart para limpar os caches do Android Studio.

## Configuração do Dispositivo Android para Debug

### Habilitação do Modo Desenvolvedor

Acesse as Configurações do dispositivo Android e navegue até "Sobre o telefone" ou "Sobre o dispositivo". Localize a opção "Número da versão" ou "Build number" e toque sete vezes consecutivas neste campo. Uma mensagem confirmará que o modo desenvolvedor foi ativado.

### Configuração da Depuração USB

Retorne ao menu principal de Configurações e procure por "Opções do desenvolvedor" ou "Developer options". Ative a opção "Depuração USB" ou "USB Debugging" e confirme a ativação quando solicitado pelo sistema.

### Configuração de Permissões Adicionais

Nas Opções do desenvolvedor, ative também as seguintes configurações para garantir o funcionamento adequado durante o desenvolvimento:

- "Permanecer ativo" ou "Stay awake" para evitar que a tela desligue durante o debug
- "Permitir instalação via USB" para permitir a instalação de aplicativos via Android Studio

## Conexão do Dispositivo para Debug

### Conexão via USB

Conecte o dispositivo Android ao computador utilizando um cabo USB compatível. No dispositivo, uma notificação sobre "Depuração USB" deve aparecer automaticamente. Toque em "Permitir" ou "Allow" quando esta notificação for exibida. No Android Studio, o dispositivo deve aparecer automaticamente na lista de dispositivos disponíveis na barra de ferramentas.

### Conexão via Wi-Fi

Para utilizar debug wireless, certifique-se de que tanto o dispositivo quanto o computador estejam conectados à mesma rede Wi-Fi. No dispositivo, acesse Opções do desenvolvedor e ative "Depuração wireless" ou "Wireless debugging".

No Android Studio, acesse o menu Tools → Connection Assistant ou utilize o Device Manager. Selecione a opção "Pair using Wi-Fi" e siga as instruções apresentadas na tela. Será necessário inserir um código de pareamento que aparecerá no dispositivo Android.

### Verificação da Conexão

Após estabelecer a conexão (USB ou Wi-Fi), verifique se o dispositivo aparece corretamente na lista de dispositivos disponíveis na barra de ferramentas do Android Studio. O nome do dispositivo deve ser exibido junto com o nível da API do Android instalado.

## Compilação e Execução do Projeto

### Primeira Compilação

Selecione o dispositivo conectado na lista de dispositivos disponíveis na barra de ferramentas do Android Studio. Clique no botão "Run" (ícone de play verde) ou utilize o atalho Shift+F10. O Android Studio iniciará o processo de compilação, que pode levar alguns minutos na primeira execução.

### Instalação Automática

Após a compilação bem-sucedida, o aplicativo será automaticamente instalado no dispositivo conectado e executado. Na primeira execução, o aplicativo solicitará as permissões necessárias para acesso aos sensores e Bluetooth.

### Permissões Necessárias

O aplicativo requer as seguintes permissões para funcionamento adequado:

- Acesso ao Bluetooth para comunicação com o servidor
- Acesso aos sensores do dispositivo para coleta de dados
- Acesso à localização (requerido pelo Android 12+ para funcionalidades Bluetooth)

Conceda todas as permissões solicitadas para garantir o funcionamento completo da aplicação.

## Estrutura do Projeto

```
pocket-experimental-physics-android/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/br/usp/poli/pocketexperimentalphysics/
│       │   │   ├── MainActivity.kt
│       │   │   ├── connection/
│       │   │   │   └── BluetoothConnectionManager.kt
│       │   │   ├── sensors/
│       │   │   │   ├── SensorHandler.kt
│       │   │   │   ├── SensorData.kt
│       │   │   │   ├── AccelerometerData.kt
│       │   │   │   ├── GyroscopeData.kt
│       │   │   │   ├── MagnetometerData.kt
│       │   │   │   └── interfaces/
│       │   │   │       └── SensorDataListener.kt
│       │   │   └── ui/
│       │   │       └── theme/
│       │   │           ├── Color.kt
│       │   │           ├── Theme.kt
│       │   │           └── Type.kt
│       │   └── res/
│       │       ├── drawable/
│       │       ├── layout/
│       │       │   ├── activity_main.xml
│       │       │   └── dialog_info.xml
│       │       ├── mipmap-*/
│       │       ├── values/
│       │       │   ├── colors.xml
│       │       │   ├── strings.xml
│       │       │   └── themes.xml
│       │       └── xml/
│       ├── test/
│       └── androidTest/
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── README.md
```