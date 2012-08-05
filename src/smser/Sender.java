package smser;

import java.net.Socket;
import java.net.InetAddress;
import java.net.URL;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import java.util.ArrayList;

public class Sender {
    
    private File[] requests;
    private String host;
    private String params[][];
    private int length;
    
    public Sender(String host,String params[][]) {
        this.params=params;
        this.host=host;
        requests=new File[5];
        try {
            for(int i=0;i<5;i++) requests[i]=new File("requests/req-"+i);
        } catch(Exception e) {e.printStackTrace();}
        this.length=96+params[3][1].length()+params[2][1].length();
    }
    
    public boolean sendSMS() {
        this.getSession();
        this.login();
        this.redirect();
        this.sendMessage();
        this.logout();
        return true;
    }
    
    private String getSession() {
        String resp[]=this.sendRequest(0);
        this.dumpArray(resp);
        String sid=null;
        for(int i=0;i<resp.length;i++) {
            if(resp[i].startsWith("Set-Cookie:")) sid=resp[i].substring(11, resp[i].indexOf(';'));
        }
        if(sid!=null) sid=sid.substring(sid.indexOf('=')+1).trim();
        params[0][1]=sid;
        return sid;
    }
    
    private void login() {
        String resp[]=this.sendRequest(1);
        this.dumpArray(resp);
        for(int i=0;i<resp.length;i++) {
            if(resp[i].startsWith("Location:")) params[1][1]=resp[i].substring(resp[i].indexOf('=')+1).trim();
        }
    }
    
    private void redirect() {
        String resp[]=this.sendRequest(2);
        this.dumpArray(resp);
    }
    
    private void sendMessage() {
        String resp[]=this.sendRequest(3);
        this.dumpArray(resp);
    }
    
    private void logout() {
        String resp[]=this.sendRequest(4);
        this.dumpArray(resp);
    }
    
    private String[] sendRequest(int id) {
        ArrayList<String> resp=new ArrayList<String>();
        try {
            InetAddress addr=InetAddress.getByName(host);
            Socket sock=new Socket(addr,80);
            PrintWriter out=new PrintWriter(sock.getOutputStream());
            Scanner fin=new Scanner(this.getClass().getResourceAsStream("/requests/req-"+id));
            while(fin.hasNextLine()) {
                String reqline=this.injectParameters(fin.nextLine(),id);
                out.println(reqline);
                System.out.println(reqline);
            }
            System.out.println();
            out.println();
            out.flush();
            Scanner in=new Scanner(sock.getInputStream());
            while(true) {
                String line=in.nextLine();
                if(line.isEmpty()) break;
                resp.add(line);
            }
            sock.close();
        } catch(IOException e) {e.printStackTrace();}
        String response[]=new String[resp.size()];
        for(int i=0;i<response.length;i++) response[i]=resp.get(i);
        return response;
    }
    
    private String injectParameters(String line,int id) {
        for(int i=0;i<params.length;i++) line=line.replaceAll(params[i][0]+"=", params[i][0]+"="+params[i][1]);
        if(id==3) {
            if(line.startsWith("Content-Length:")) line="Content-Length: "+this.length;
        }
        return line;
    }
    
    private void dumpArray(String ar[]) {
        for(int i=0;i<ar.length;i++) System.out.println(ar[i]);
        System.out.println();
    }
}
