package com.cdamayab.ipcamera;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class IPCameraServerSocket extends Thread{

    private Socket mSocket;
    private int mPort = 8888;
    private CameraSurfaceView camera;

    public IPCameraServerSocket(CameraSurfaceView camera){
        this.camera=camera;
    }

    @Override
    public void run() {
        super.run();

        try {
            mSocket = new Socket();
            mSocket.connect(new InetSocketAddress(getIpAddress(), mPort), 10000); // hard-code server address

            BufferedOutputStream outputStream = new BufferedOutputStream(mSocket.getOutputStream());
            BufferedInputStream inputStream = new BufferedInputStream(mSocket.getInputStream());

            byte[] buff = new byte[256];
            int len = 0;
            String msg = null;
            while ((len = inputStream.read(buff)) != -1) {
                msg = new String(buff, 0, len);

                // send data
                while (true) {
                    outputStream.write(camera.getImageBuffer());
                    outputStream.flush();

                    if (Thread.currentThread().isInterrupted())
                        break;
                }

            }
            outputStream.close();
            inputStream.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getIpAddress() {
        String ip = "Error!";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ip = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            //httpServerLog("Something Wrong! cant get ip:" + e.toString() + "\n", "error");
        }
        return ip;
    }


}
