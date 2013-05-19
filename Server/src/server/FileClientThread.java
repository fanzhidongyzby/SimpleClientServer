/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author fanzhidong
 */
public class FileClientThread extends Thread
{
    String s;
    ServerSocket fileServer ;
	DataOutputStream fileOut ;//ZipOutputStream fileOut ;
	Socket fileSocket ;
    String fileName ;

    public FileClientThread(Socket so ,String dir)
    {
        fileSocket=so;
        fileName=dir ;
		try
		{
			fileOut = new DataOutputStream(fileSocket.getOutputStream());
		}
		catch(Exception e)
		{
            //e.printStackTrace();
            ServerIf.instance.getTxtShow().append("FTP　Server:\n      Connecting failed !\n");
        }
    }

    @Override
	public void run()
	{
		// TODO Auto-generated method stub
        try
        {
            //fileOut.putNextEntry(new ZipEntry(fileName.substring(fileName.indexOf('/')+1)));
            System.out.println(fileOut.toString());
            FileInputStream fi = new FileInputStream(fileName);
            byte b[]= new byte[1024];
            int n=-1;
            ServerIf.instance.getTxtShow().append("FTP　Server:\n      File sending... !\n");
            while ((n=fi.read(b, 0, 1024))!=-1)
            {
                fileOut.write(b, 0, n);
            }
            fi.close();
            fileOut.close();
            ServerIf.instance.getTxtShow().append("FTP　Server:\n      File send ok  !\n");
            return ;
        }
        catch(Exception e)
        {
            //e.printStackTrace();
            ServerIf.instance.getTxtShow().append("FTP　Server:\n      File transporting failed !\n");
        }

    }





}
