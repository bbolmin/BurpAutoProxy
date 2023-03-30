# BurpAutoProxy
## Description
- Burp Suite Extension
- "BurpAutoProxy" configures proxy settings like fiddler.

## Features
- support : macOS, Windows
- hotkey : F12 (on/off toggle) 
- Automatically turn (on / off) proxy when (execute / terminate) Burp Suite
- title change : prefix `(on) / (off)`
<img src="https://user-images.githubusercontent.com/7751652/228427980-05b5fb5a-74fe-4133-9c03-279f79bc503c.png" width="40%" height="40%">


## Caution
- The proxy setting is fixed at `127.0.0.1:8080`


## How to set proxy
macOS

    # enable proxy
    Runtime.getRuntime().exec("networksetup -setwebproxy Wi-Fi " + host + " " + port);
    Runtime.getRuntime().exec("networksetup -setwebproxystate Wi-Fi on");
    Runtime.getRuntime().exec("networksetup -setsecurewebproxystate Wi-Fi on");
    
    # disable proxy
    Runtime.getRuntime().exec("networksetup -setwebproxystate Wi-Fi off");
    Runtime.getRuntime().exec("networksetup -setsecurewebproxystate Wi-Fi off");

Windows

    # enable proxy
    Runtime.getRuntime().exec("reg add \"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable /t REG_DWORD /d 1 /f");
    Runtime.getRuntime().exec("reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyServer /t REG_SZ /d " + proxyServerString + " /f");

    # disable proxy
    Runtime.getRuntime().exec("reg add \"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable /t REG_DWORD /d 0 /f");
    Runtime.getRuntime().exec("reg delete \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyServer /f");
