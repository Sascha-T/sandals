# SANDALS

### A SOCKS5 Server Library for Java

## Installation
You can either use the GitHub Gradle Registry [here](https://github.com/Sascha-T/sandals/packages), or use **jitpack.io**:
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.Sascha-T:sandals:v1.0.0-rc.1'
}
```


## Architecture
This SOCKS5 server has extensive infrastructure for custom domain resolving, just like in Tor Daemon's SOCKS5 server which handles any connections to a *.onion domain over the TOR network. \
If you wish to do something similar using SANDALS, you will have to `.setResolver` on the SOCKS5ServerBuilder with your custom resolver class which implements `AddressResolver`. \
These features are mainly intended for SOCKS5 connections where address resolving is left to the proxy server, such as when connecting using curl with `-x socks5h://`, where the `h` makes curl send domain names as is.


You can also create custom authentication methods by extending `AuthHandler` and `AuthHandlerFactory`. \
By default, we ship you handlers for either allowing everyone or password authentication as per [RFC 2743](https://tools.ietf.org/html/rfc2743).

## Examples

Run a simple SOCKS5 server.
```java
SOCKS5Server server = new SOCKS5ServerBuilder(true);
// `true` makes it load default settings, which are at the time of writing:
// port = 1080, timeout = 5000ms, authentication = none, resolver = default
server.start();
```

Run a SOCKS5 server with password authentication, and one set of credentials (username: `test`, password: `123`).
```java
SOCKS5Server server = new SOCKS5ServerBuilder(true)
    .clearAuthHandlerFactories() // Remove default NO-AUTH factory
    .addAuthHandlerFactory(new PasswordAuthenticationHandlerFactory(
        new PasswordAuthenticationHandlerFactory.DefaultPasswordChecker( // Create password checker (Can be customized too, for example, to retrieve credentials from a database, or to compare passwords using hashes)
            List.of( // Create list of credentials
                new PasswordAuthenticationHandlerFactory.DefaultPasswordChecker.PasswordEntry(
                    "test", "123" // Credentials
                )
            )
        )
    ));
server.start();
```

### For personalized help or any questions, please message me on Discord: Sascha_T#3993
