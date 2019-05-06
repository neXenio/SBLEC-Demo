SBLEC Demo
============================

This repo contains a demo app integrating the SBLEC library. SBLEC enables reliable and secure data transfer using Bluetooth Low Energy without requiring a connection.

![Header Image](https://raw.githubusercontent.com/neXenio/SBLEC-Demo/master/media/demo_short.gif)

The app synchronizes a state (icon and background color) on nearby devices. It supports Android phones, tablets, watches and IOT devices.

## Resolving Dependencies

The library will be served through neXenio's public artifacory, reachable at `https://artifactory.nexenio.com`. It has dependencies on other libraries, some of which are served through JitPack.

```gradle
repositories {
    maven {
        url "https://artifactory.nexenio.com/artifactory/${nexenio_artifactory_repository}/"
        credentials { 
            username "${nexenio_artifactory_user}" 
            password "${nexenio_artifactory_password}"
        }
    }
    maven {
        url 'https://jitpack.io'
    }
}

dependencies {
    implementation 'com.nexenio.sblec:core:0.5.2'
}
```

You should extend your gobal `build.gradle` file with the following properties:

```gradle
# neXenio Artifactory
nexenio_artifactory_repository=PLACEHOLDER
nexenio_artifactory_user=PLACEHOLDER
nexenio_artifactory_password=PLACEHOLDER
```

## Getting Started

Work in progress.