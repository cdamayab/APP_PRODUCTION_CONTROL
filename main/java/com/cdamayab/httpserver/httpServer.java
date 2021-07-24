//#define DESKTOP
//#define ANDROID FALSE

//#if ANDROID
package com.cdamayab.httpserver;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
//#endif

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;

public class httpServer {
    // ANDROID PERMISSIONS FLAGS
    private Boolean permission_INTERNET;

    // CLIENT CONNECTION VIA SOCKET CLASS
    public ServerSocket httpServerSocket;
    public HttpServerThread httpServerThread=null;
    public static final int HttpServerPORT = 8080;

    // VERBOSE MODE
    public static final boolean verbose = true;
    public static final boolean errors = false;
    public static final boolean toast = false;
    public static final boolean logcat = true;

    static final File WEB_ROOT = new File(".");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";

    public Socket socket = null;

    public void main(){//String[] args
        httpServerLog(("Stating Server..."));
        httpServerThread = new HttpServerThread();
        httpServerThread.start();
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
            httpServerLog("Something Wrong! cant get ip:" + e.toString() + "\n", "error");
        }
        return ip;
    }

     private class HttpServerThread extends Thread {

        public HttpResponseThread httpResponseThread = null;

        @Override
        public void run() {
            try {
                httpServerSocket = new ServerSocket(HttpServerPORT);
                httpServerLog( "Listening on " + getIpAddress()+":"+ HttpServerPORT + " ...\n\n");

                while(true){
                    socket = httpServerSocket.accept();
                    httpResponseThread = new HttpResponseThread(socket);
                    httpResponseThread.start();
                }
            }
            catch (IOException e) {
                httpServerLog("Server Connection error : " + e.getMessage(), "error");
            }
        }
    }

    private class HttpResponseThread extends Thread {

        Socket socket;

        HttpResponseThread(Socket socket){
            httpServerLog("New connection accepted (" + new Date() + ")" +
                    socket.getInetAddress().toString() + ":" + socket.getPort());
            this.socket = socket;
        }

        @Override
        public void run() {
            BufferedReader is = null;
            PrintWriter os = null;
            BufferedOutputStream dataOut = null;
            String request="",fileRequested="",resp="";

            try {
                // we read characters from the client via input stream on the socket
                is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // we get character output stream to client (for headers)
                os = new PrintWriter(socket.getOutputStream(), true);
                // get binary output stream to client (for requested data)
                dataOut = new BufferedOutputStream(socket.getOutputStream());

                // get first line of the request from the client ex: GET /index.php HTTP/1.1
                request = is.readLine();
                httpServerLog("\treceive "+request.toString().length()+" bytes");

                // we parse the request with a string tokenizer
                StringTokenizer parse = new StringTokenizer(request);
                String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
                // we get file requested
                fileRequested = parse.nextToken().toLowerCase();

                if (fileRequested.endsWith("/")) {fileRequested += DEFAULT_FILE;}

                switch (method) {
                    case "GET":
                        resp=response(200,"<h1>501: " + method + " Not Implemented method.</h1>");

                        break;
                    case "FILE":
                        File file = new File(WEB_ROOT, fileRequested);
                        int fileLength = (int) file.length();
                        String content = getContentType(fileRequested);
                        byte[] fileData = readFileData(file, fileLength);
                        dataOut.write(fileData, 0, fileLength);
                        dataOut.flush();
                        httpServerLog("\tsend file "+fileLength+" bytes");
                        break;
                    default:
                        httpServerLog("501 Not Implemented : " + method + " method.");
                        resp=response(200,"<h1>501: " + method + " Not Implemented method.</h1>");
                        break;
                }

                if(resp!=""){os.print(resp);}
                os.flush();
                httpServerLog("\tsend "+resp.toString().length()+" bytes");

            } catch (IOException e) {
                httpServerLog("Server error : " + e.getMessage(), "error");
            }
            finally {
                try {
                    is.close();
                    os.close();
                    dataOut.close();
                    socket.close(); // we close socket connection
                    httpServerLog("Connection closed.\n\n");
                } catch (Exception e) {
                    httpServerLog("Error closing stream : " + e.getMessage(), "error");
                }
            }
            return;
        }//END HttpResponseThread::run() METHOD
    }

    String getHeaders(int code, int length){
        String resp="";
        String contentMimeType = "text/html";

        // we send HTTP Headers with data to client
        switch(code){
            case 404: resp+="HTTP/1.1 404 File Not Found"; break;
            case 501: resp+="HTTP/1.1 501 Not Implemented"; break;
            case 200: resp+="HTTP/1.1 200 OK"+"\n"; break;
        }

        resp+="Server: Java HTTP Server : 1.0"+"\n";
        resp+="Date: " + new Date()+"\n";
        resp+="Content-type: " + contentMimeType+"\n";
        resp+="Content-length: " + length+"\n";
        resp+="\n"; // blank line between headers and content, very important !

        return resp;
    }

    private String response(int code, String str){
        String resp="";

        try { resp+=resp+=getHeaders(code, str.getBytes("UTF-8").length);
        } catch (UnsupportedEncodingException e) {e.printStackTrace();}
        resp+=str;

        return resp+ "\r\n";
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
    }

    // return supported MIME Types
    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
            return "text/html";
        else
            return "text/plain";
    }

    public void httpServerLog(String str){
        httpServerLog(str, "");
    }

    public void httpServerLog(String str, String type){
        if(type == "error"){System.err.println(str);}
        else if (verbose){System.out.println(str);}
    }

}