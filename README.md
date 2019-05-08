SBLEC Demo
==========

This repo contains a dummy app integrating the SBLEC library.

![Header Image](https://raw.githubusercontent.com/neXenio/SBLEC-Demo/master/media/header.jpg)

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

You should start by checking out the Android library module called `sblecdemo`. It contains just 4 simple Model, View and Presenter classes:

- `DemoPayloadWrapper`
- `DemoView`, implemented in `DemoLayout`
- `DemoPresenter`

The `DemoPresenter` contains the business logic and actually uses the `SBLEC` library to send and receive payloads. Encoding and decoding of payload data is implemented in the `DemoPayloadWrapper`. Please refer to the JavaDoc in these classes to learn more.

`DemoLayout` is a custom view extending `RelativeLayout` and implementing the `DemoView` interface. It's in charge of visualizing the data from a `DemoPayloadWrapper` and showing error messages.

The `app` and `wear` module just contain one actvity each, inflating a layout that contains a `DemoLayout` view.
