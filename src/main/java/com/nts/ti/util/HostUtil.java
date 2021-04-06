package com.nts.ti.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class HostUtil {
	private static final Map<String, String> t_hosts = new TreeMap<String, String>();
	protected static String HOSTFILE;

	
	static{
		HOSTFILE=System.getenv("windir")+"\\system32\\drivers\\etc\\hosts";
		try {
			BufferedReader br = new BufferedReader(new FileReader(HOSTFILE));
			String read = br.readLine();
			while(read!=null){
				if(!read.startsWith("#") && read.length()>0){
					read = new String(read.getBytes());
					StringTokenizer st = new StringTokenizer(read);
					if(st.countTokens()==2){
						String ip = st.nextToken();
						String dns = st.nextToken();
						t_hosts.put(dns, ip);
					}else if(st.countTokens()>2){
						String ip = st.nextToken();
						String dns = "";
						while(st.hasMoreTokens()){
							dns +=st.nextToken();
						}
						t_hosts.put(dns, ip);
					}
				}
				read = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 현재 시스템의 hosts 정보를 출력
	 * @return 호스트 정보
	 */
	public static String getHostList(){
		String rtn = "";
		for(String host:t_hosts.keySet()){
			rtn += host+"\t"+t_hosts.get(host)+"</HR>";
		}
		return rtn;
	}
	
	/**
	 * 입력된 dns가 hosts에 존재 하는지 확인
	 * @param dns
	 * @return 호스트 존재 시 true(default: false)
	 */
	public static boolean hasHost(String dns){
		for(String t:t_hosts.keySet()){
			if(t.equals(dns)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 입력된 ip, dns 정보를 hosts 파일에 추가함.
	 * @param ip
	 * @param dns
	 */
	public static void addHost(String ip, String dns){
		if(!hasHost(dns)){
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(HOSTFILE), true));
				bw.newLine();
				bw.append(ip+"\t"+dns);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			t_hosts.put(dns, ip);
		}
	}
	
	
	/**
	 * 입력된 dns를 hosts 파일에서 제거함.
	 * @param dns
	 */
	public static void deleteHost(String dns){
		if(hasHost(dns)){
			try{
				BufferedReader br = new BufferedReader(new FileReader(HOSTFILE));
				StringBuffer filebuf=new StringBuffer();
				filebuf.setLength(0);
				String read = br.readLine();
				while(read!=null){
					if (!read.startsWith("#") && read.length() > 0) {
						StringTokenizer st = new StringTokenizer(read);
						if (st.countTokens() >= 2) {
							st.nextToken();
							if(!dns.equals(st.nextToken())){
								filebuf.append(read+"\r\n");
							}
						}else{
							filebuf.append(read+"\r\n");
						}
						
					}else{
						filebuf.append(read+"\r\n");
					}
					read = br.readLine();
				}
				br.close();
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(HOSTFILE)));
				bw.write(filebuf.toString());
				bw.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for(int i=0;i<t_hosts.size();i++){
			if(t_hosts.containsKey(dns)){
				t_hosts.remove(dns);
			}
		}
	}
}
