/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client;

/**
 *
 * @author fanzhidong
 */
import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Client implements Runnable
{

	static int id=0;
    String protType ;
    
    String recMsg ;
	Socket socket;
	DataInputStream in ;
	DataOutputStream out ;
    Thread transportThread ;
    
    
    DatagramSocket send ,rec  = null;
    DatagramPacket packIn = null , packOut ;
    InetAddress addr ;

    Thread fileThread=null;
    boolean fileOver=true;//文件传输完毕
    Socket fileSocket;
    DataInputStream fileIn ;//ZipInputStream fileIn;
    String fileName;

	public Client()
	{
		id=(int)(Math.random()*1000)+1;
	}

    public boolean connect(String ip)
    {
        protType=ClientIf.instance.getSltProt().getSelectedItem().toString();
        if(protType=="TCP")
        {
            return connectTcp(ip);
        }
        else if(protType=="UDP")
        {
            return connectUdp(ip);
        }
        return false;
    }

    public boolean connectUdp(String ip)
    {
        try
        {
            addr =InetAddress.getByName(ip);
            rec = new DatagramSocket(1501);

            byte data[] = new byte[1024];
            packIn = new DatagramPacket(data, data.length);
        }
        catch(Exception e)
        {}
        try
        {
            transportThread=new Thread(this);
            transportThread.start();
            return true ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ClientIf.instance.getTxtChat().append("Client"+String.valueOf(id)+":\n      Connecting failed !\n");
            //e.printStackTrace();
            return true;
		}
    }

    public boolean connectTcp(String ip)
    {
        try
        {
			socket=new Socket(ip,1400);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
            transportThread=new Thread(this);
            transportThread.start();
            return true ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ClientIf.instance.getTxtChat().append("Client"+String.valueOf(id)+":\n      Connecting failed !\n");
            return false;
		}
    }

    public void send(String msg)
    {
        try
        {
            if(protType.equals("TCP"))
            {
                out.writeUTF(String.valueOf(id) + ":" + msg);
            }
            else if(protType.equals("UDP"))
            {
                String s=String.valueOf(id) + ":" + msg;

                byte buffer[]=s.getBytes();
                packOut=new DatagramPacket(buffer,buffer.length,addr,1500);

                send=new DatagramSocket();

                send.send(packOut);
            }
            if (fileOver&&msg.startsWith("#get file"))//没有文件传输
            {
                fileName=msg.substring(msg.lastIndexOf("/")+1);
            }
            ClientIf.instance.getTxtChat().append("Client" + String.valueOf(id) + ":\n      " + msg + "\n");
            ClientIf.instance.getTxtSend().setText("");
        }
        catch(Exception e)
        {
            ClientIf.instance.getTxtChat().append("Client"+String.valueOf(id)+":\n      Disconnected !\n");
        }
    }

    public void run()
    {
        if(Thread.currentThread()==transportThread)
        {
            if(protType.equals("TCP"))
            {
                while(true)
                    {
                        try
                        {
                            recMsg = in.readUTF();
                            if(recMsg!=null)
                            {
                                if(recMsg.equals("file"))//启动文件传输线程
                                {
                                    if(!fileOver)//文件正在传输
                                    {
                                        ClientIf.instance.getTxtChat().append("Client:"+String.valueOf(id)+"\n      File  transporting ... Please wait ... !\n");
                                    }
                                    else
                                    {
                                        try
                                        {
                                            ClientIf.instance.getTxtChat().append("Client"+String.valueOf(id)+":\n      Request for ftp server ... !\n");
                                            //创建文件传输连接
                                            fileSocket = new Socket(ClientIf.instance.getTxtIp().getText(), 1600);
                                            fileIn = new DataInputStream(fileSocket.getInputStream());
                                            fileThread = new Thread(this);
                                            fileThread.start();
                                        }
                                        catch(Exception e)
                                        {
                                            //e.printStackTrace();
                                            ClientIf.instance.getTxtChat().append("Client"+String.valueOf(id)+":\n      File connecting failed !\n");
                                        }
                                    }
                                }
                                else
                                    ClientIf.instance.getTxtChat().append(recMsg);
                            }
                        }
                        catch (IOException ex)
                        {
                            ex.printStackTrace();
                            ClientIf.instance.getTxtChat().append("Client"+String.valueOf(id)+":\n      Disconnected !\n");
                            ClientIf.instance.getBtnSend().setEnabled(false);
                            return ;
                        }
                    }
            }
            else if(protType.equals("UDP"))
            {
                while(true)
                    {
                        try
                        {
                            rec.receive(packIn);
                            recMsg = new String(packIn.getData(), 0, packIn.getLength());
                            if(recMsg!=null)
                                ClientIf.instance.getTxtChat().append(recMsg);
                        }
                        catch (Exception ex)
                        {
                            ClientIf.instance.getTxtChat().append("Client"+String.valueOf(id)+":\n      Disconnected !\n");
                            ClientIf.instance.getBtnSend().setEnabled(false);
                            return ;
                        }
                    }
            }
        }
        else if(Thread.currentThread()==fileThread)
        {            
            //传送文件
            ZipEntry ent=null;
            byte b[] =new byte[1024];
            try
            {
                File f=new File("./recieve");
                if(!f.exists())
                {
                    f.mkdir();
                }
                FileOutputStream fs =new FileOutputStream("./recieve/"+fileName);
                while(true)
                {
                    System.out.println(fileIn.toString());
                    ClientIf.instance.getTxtChat().append("Client"+String.valueOf(id)+":\n      File recieving ... !\n");
                    //ent = fileIn.getNextEntry();
                    //while (ent!= null)
                    {
                        int n = -1;
                        int i = 0;
                        while ((n = fileIn.read(b, 0, 1024)) != -1)
                        {
                            fs.write(b, 0, n);
                            i++;
                            fileOver = true;
                        }
                    }
                    fs.close();
                    fileIn.close();
                    fileOver=true;
                    ClientIf.instance.getTxtChat().append("Client"+String.valueOf(id)+":\n      File recieve ok !\n");
                    return ;
                }
            }
            catch(Exception e)
            {
                //e.printStackTrace();
                ClientIf.instance.getTxtChat().append("Client"+String.valueOf(id)+":\n      File recieving failed !\n");
            }            
        }
    }

}
