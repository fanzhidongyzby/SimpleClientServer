/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 *
 * @author fanzhidong
 */
public class WebClientThread extends Thread {

    String requestMessageLine;
    String fileName = null;
    Socket connectionSocket;
    BufferedReader infromClient;
    DataOutputStream outToClient;

    public WebClientThread(Socket so) {
        try {
            connectionSocket = so;
            infromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            ServerIf.instance.getTxtShow().append("Web　Server:\n      Connected with browser  !\n");
        } catch (Exception e) {
            //e.printStackTrace();
            ServerIf.instance.getTxtShow().append("Web　Server:\n      Connecting failed !\n");
        }

    }

    @Override
    public void run() {
        //while (true)
        {
            try {
                requestMessageLine = infromClient.readLine();
                StringTokenizer tokenizedLine = new StringTokenizer(requestMessageLine);
                if (tokenizedLine.nextToken().equals("GET")) {
                    ServerIf.instance.getTxtShow().append("Web　Server:\n      Got  a  http request from "+connectionSocket.getInetAddress().getHostAddress()+"/"+connectionSocket.getInetAddress().getHostName()+"  !\n");
                    fileName = tokenizedLine.nextToken();
                    if (fileName.startsWith("/")) {
                        fileName = fileName.substring(1);
                    }
                    File file = new File(fileName);
                    int numOfBytes = (int) file.length();
                    FileInputStream inFile = new FileInputStream(file);
                    byte[] fileInBytes = new byte[numOfBytes];
                    inFile.read(fileInBytes);
                    outToClient.writeBytes("HTTP/1.0 200  Document Follows \r\n");
                    if (fileName.endsWith(".jpg")) {
                        ServerIf.instance.getTxtShow().append("Web　Server:\n      Sent a picture(*.jpg) !\n");
                        outToClient.writeBytes("Content-Type:image/jpeg\r\n");
                    }
                    if (fileName.endsWith(".gif")) {
                        ServerIf.instance.getTxtShow().append("Web　Server:\n      Sent a picture(*.gif) !\n");
                        outToClient.writeBytes("Content-Type:image/gif\r\n");
                    }
                    outToClient.writeBytes("Content-Length" + numOfBytes + "\r\n");
                    outToClient.writeBytes("\r\n");
                    outToClient.write(fileInBytes, 0, numOfBytes);
                    connectionSocket.close();
                    ServerIf.instance.getTxtShow().append("Web Server:\n      Disconnected with Browser client" + "!\n");
                }
            } catch (Exception e) {
                ServerIf.instance.getTxtShow().append("Web Server:\n      Disconnected with Browser client" + "!\n");
                return;
            }
        }

    }
}
