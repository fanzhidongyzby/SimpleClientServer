/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

/**
 *
 * @author fanzhidong
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class Server implements Runnable
{
    Thread tcp =new Thread(this);
    Thread udp =new Thread(this);
    Thread web =new Thread(this);
    public Server()
    {
        tcp.start();
        udp.start();
        web.start();
    }

    public void run()
    {
        if(Thread.currentThread()==tcp)
        {
            tcpRun();
        }
        else if(Thread.currentThread()==udp)
        {
            udpRun();
        }
        else if(Thread.currentThread()==web)
        {
            webRun();
        }
    }

    void webRun()
    {
        ServerSocket ListenSocket;
        Socket connectionSocket;
        while(true)
		{
			try
			{
                ListenSocket = new ServerSocket(6789);
                connectionSocket = ListenSocket.accept();
                if(connectionSocket!=null)
                {
                    new WebClientThread(connectionSocket).start();
                }
            }
            catch(Exception e)
            {
               // e.printStackTrace();
            }
        }

    }

    void tcpRun()
    {
        ServerSocket server ;
		Socket client ;

		while(true)
		{
			try
			{
				server = new ServerSocket(1400);
				//System.out.println("Waiting clients...");
				client=server.accept();
				if(client!=null)
				{
					new TcpClientThread(client).start();
				}
			}
			catch(Exception e)
			{
                //ServerIf.instance.getTxtShow().append("Server:\n      Tcp running failed !\n");
                //return ;
                //e.printStackTrace();
			}
		}
    }


    void udpRun()
    {
        DatagramSocket send ,rec  = null;
        DatagramPacket packIn = null , packOut ;

                
		while(true)
		{
			try
			{
                rec=new DatagramSocket(1500);
                
                byte data[]=new byte [1024];
                packIn=new DatagramPacket(data,data.length);
                
                rec.receive(packIn);

                InetAddress addr =packIn.getAddress();
				String s=new String(packIn.getData(),0,packIn.getLength());
                int spos=s.indexOf(':');
                String num =s.substring(0, spos);
                String txt=s.substring(spos+1, s.length());
                s="UDP Server:\n      Reply Client "+num+",you said : "+txt.toUpperCase()+"\n";
                
                byte buffer[]=s.getBytes();
                packOut=new DatagramPacket(buffer,buffer.length,addr,1501);
               
                
                send=new DatagramSocket();
                
                send.send(packOut);
                
                ServerIf.instance.getTxtShow().append("Client"+num+":\n      "+txt+"\n");
                ServerIf.instance.getTxtShow().append("UDP Server:\n      Reply Client "
                        +num+",you said : "+txt.toUpperCase()+"\n");
			}
			catch(Exception e)
			{
                //ServerIf.instance.getTxtShow().append("Server:\n      Tcp running failed !\n");
                //return ;
                //e.printStackTrace();
			}
		}
    }

}
