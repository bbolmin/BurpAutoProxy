package extension;

import burp.IBurpExtender;
import burp.IBurpExtenderCallbacks;

import java.awt.*;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class BurpExtender implements IBurpExtender {
    final String EXTENSION_NAME = "AutoProxy";
    final int ProxyHotKey = KeyEvent.VK_F12;

    private static String OS = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);
    private IBurpExtenderCallbacks callbacks;
    HashMap<JFrame, String> burpFrameMap;
    private boolean isProxyEnabled = false;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {

        this.callbacks = callbacks;
        callbacks.setExtensionName(EXTENSION_NAME);

        //get burpFrame and change title to (off).
        burpFrameMap = getBurpFrame();

        //initialize proxy on
        changeProxySetting();

        //Bind hotkey (default : F12)
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (e.getKeyCode() == ProxyHotKey) {
                    changeProxySetting();
                    return true;
                }
            }
            return false;
        });

        //register a handler on terminate (Disable the proxy setting when unloading the extension)
        callbacks.registerExtensionStateListener(new ExtensionStateListener(callbacks));
    }


    private HashMap<JFrame, String> getBurpFrame() {
        HashMap<JFrame, String> burpFrameMap = new HashMap<>();

        for (java.awt.Frame frame : java.awt.Frame.getFrames()) {
            if (frame.isVisible() && frame.getTitle().startsWith("Burp Suite")) {
//                callbacks.printOutput("[getBurpFrame]" + frame.getTitle());
                burpFrameMap.put((JFrame) frame, frame.getTitle());
            }
        }
        return burpFrameMap;
    }

    private void appendBurpTitle(String str) {
        for (Map.Entry<JFrame, String> entry : burpFrameMap.entrySet()) {
            JFrame burpFrame = entry.getKey();
            String orgTitle = entry.getValue();
//            callbacks.printOutput(" [orgTitle]:" + orgTitle);

            burpFrame.setTitle((str + orgTitle));
        }
    }

    private void changeProxySetting() {
        PrintWriter stderr = new PrintWriter(callbacks.getStderr(), true);
        if (!isProxyEnabled) {
            if (isMac()) {
                callbacks.printOutput("Enable Mac Proxy");
                setMacProxy("127.0.0.1", "8080");
                isProxyEnabled = true;
                appendBurpTitle("(on) ");

            } else if (isWindows()) {
                callbacks.printOutput("Enable Windows Proxy");
                setWindowsProxy("127.0.0.1", "8080");
                isProxyEnabled = true;
                appendBurpTitle("(on) ");
            } else {
                stderr.println("Not supported");
            }
        } else {
            if (isMac()) {
                callbacks.printOutput("Disable Mac Proxy");
                unsetMacProxy();
                isProxyEnabled = false;
                appendBurpTitle("(off) ");
            } else if (isWindows()) {
                callbacks.printOutput("Disable Windows Proxy");
                unsetWindowsProxy();
                isProxyEnabled = false;
                appendBurpTitle("(off) ");
            }
        }
    }


    private class ExtensionStateListener implements burp.IExtensionStateListener {
        private final IBurpExtenderCallbacks callbacks;

        public ExtensionStateListener(IBurpExtenderCallbacks callbacks) {
            this.callbacks = callbacks;
        }

        @Override
        public void extensionUnloaded() {
            callbacks.printOutput("Extension unloaded");
            if (isMac()) {
                callbacks.printOutput("Disable Mac Proxy");
                unsetMacProxy();
            } else if (isWindows()) {
                callbacks.printOutput("Disable Windows Proxy");
                unsetWindowsProxy();
            }
        }
    }

    public static void setMacProxy(String host, String port) {
        try {
            Runtime.getRuntime().exec("networksetup -setwebproxy Wi-Fi " + host + " " + port);
            Runtime.getRuntime().exec("networksetup -setsecurewebproxy Wi-Fi " + host + " " + port);
            Runtime.getRuntime().exec("networksetup -setwebproxystate Wi-Fi on");
            Runtime.getRuntime().exec("networksetup -setsecurewebproxystate Wi-Fi on");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void unsetMacProxy() {
        try {
            Runtime.getRuntime().exec("networksetup -setwebproxystate Wi-Fi off");
            Runtime.getRuntime().exec("networksetup -setsecurewebproxystate Wi-Fi off");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setWindowsProxy(String proxyAddress, String proxyPort) {
        try {
            String proxyServer = proxyAddress + ":" + proxyPort;
            String proxyServerString = "http=" + proxyServer + ";https=" + proxyServer;

            Runtime.getRuntime().exec("reg add \"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable /t REG_DWORD /d 1 /f");
            Runtime.getRuntime().exec("reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyServer /t REG_SZ /d " + proxyServerString + " /f");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void unsetWindowsProxy() {
        try {
            Runtime.getRuntime().exec("reg add \"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable /t REG_DWORD /d 0 /f");
            Runtime.getRuntime().exec("reg delete \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyServer /f");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }
}
