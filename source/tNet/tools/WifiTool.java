//package com.bc_aplicativos.wifichatapp;

package tNet.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.ArrayList;


public class WifiTool {
    public static ArrayList<String> deviceList;

    public static ArrayList<String> getDeviceList() {
        if (deviceList == null) {
            deviceList = new ArrayList();
        }
        BufferedReader br;
        boolean isFirstLine = true;

        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
				System.out.println(line);
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] splitted = line.split(" +");
                if (splitted.length >= 4) {
                    String ipAddress = splitted[0];
                    boolean isReachable = InetAddress.getByName(
                            splitted[0]).isReachable(500);
                    if (isReachable) {
                        deviceList.add(ipAddress);
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceList;
    }
}
