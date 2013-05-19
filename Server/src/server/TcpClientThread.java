/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author fanzhidong
 */
public class TcpClientThread extends Thread
{

	String s;
	DataInputStream in ;
	DataOutputStream out ;
	Socket socket ;
    Thread fileThread=null;

    ServerSocket fileServer ;


	public TcpClientThread(Socket so)
	{
		socket=so;
		try
		{
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		}
		catch(Exception e)
		{
            ServerIf.instance.getTxtShow().append("TCP　Server:\n      Connecting failed !\n");
        }
	}


    @Override
	public void run()
	{
		// TODO Auto-generated method stub
        String num = null ;
		while(true)
		{
			try
			{
				s=in.readUTF();
                int spos=s.indexOf(':');
                num =s.substring(0, spos);
                String txt=s.substring(spos+1, s.length());
                if(txt.startsWith("#get file"))//文件传输
                {
                    if(txt.equals("#get file"))//文件传输请求，发送目录
                    {
                        //发送文件目录
                        File  f = new File("./file");
                        String [] fl = f.list();
                        out.writeUTF("FTP Server:\n      "+"Dir  ./ has files or directories as follows :" + "\n");
                        int i=0;
                        while(i<fl.length)
                        {
                            out.writeUTF("      "+fl[i]+"\n");
                            i++;
                        }
                    }
                    else//实际文件传输
                    {
                        String d=txt.substring(12);
                        String dir = "./file/"+d;//文件或者目录名
                        File  f = new File(dir);
                        if(f.isDirectory())
                        {
                            String[] fl = f.list();
                            out.writeUTF("FTP Server:\n      " + "Dir  ./"+d+" has files or directories as follows :" + "\n");
                            int i = 0;
                            while (i < fl.length)
                            {
                                out.writeUTF("      " + fl[i] + "\n");
                                i++;
                            }
                        }
                        else if(f.isFile())
                        {        
                            if(fileServer!=null)
                                if(fileServer.isBound())
                                    fileServer.close();
                            fileServer = new ServerSocket(1600);
                            Socket fileClient ;
                            ServerIf.instance.getTxtShow().append("FTP Server:\n      Waiting for ftp connect ...!\n");
                            out.writeUTF("file");//传输控制命令                            
                            while(true)
                            {                                
                                fileClient = fileServer.accept();
                                if (fileClient != null)
                                {
                                    ServerIf.instance.getTxtShow().append("FTP Server:\n      New ftp client comes !\n");
                                    fileThread = new FileClientThread(fileClient, dir);
                                    fileThread.start();
                                    //Thread.sleep(1000);
                                    break;
                                }
                            }
                        }
                        else
                        {
                            out.writeUTF("FTP Server:\n      " + "Bad file request !" + "\n");//传输控制命令
                        }

                    }
                }
                else//普通数据传输
                {
                    out.writeUTF("TCP Server:\n      Reply Client " + num + ",you said : " + txt.toUpperCase() + "\n");
                    ServerIf.instance.getTxtShow().append("Client" + num + ":\n      " + txt + "\n");
                    ServerIf.instance.getTxtShow().append("TCP Server:\n      Reply Client" + num
                            + "(" + socket.getInetAddress().getHostAddress() + "/"
                            + socket.getInetAddress().getHostName() + ")"
                            + "said : " + txt.toUpperCase() + "\n");
                }
			}
			catch(Exception e)
			{
                //e.printStackTrace();
                ServerIf.instance.getTxtShow().append("TCP Server:\n      Disconnected with client"+num+"!\n");
				return ;
			}
		}


	}

}
