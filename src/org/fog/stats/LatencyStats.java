package org.fog.stats;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.StorageMode.FogStorage;
import org.apache.commons.math3.util.Pair;
import org.fog.application.AppEdge;

public class LatencyStats {
	public static double Overall_read_latency=0;
	public static double Overall_write_latency=0;
	public static double Overall_delayed_write_latency=0;
	public static double Overall_blocked_write_latency=0;
	
	public static Map<String, Float> writeLatencyMap = new HashMap<String, Float>();
	public static Map<String, Float> readLatencyMap = new HashMap<String, Float>();

	public static void add_Overall_read_Latency(double latency){
		//**System.out.println("add to the overall read latency : + "+latency);
		Overall_read_latency+= latency;
	}
	
	public static void add_Overall_write_Latency(double latency){
		//**System.out.println("add to the overall write latency : + "+latency);
		Overall_write_latency+= latency;
	}
	
	public static void add_Overall_blocked_write_Latency(double latency){
		//**System.out.println("add to the overall bloacked write latency : + "+latency);
		Overall_blocked_write_latency+= latency;
	}
	
	public static void add_Overall_delayed_write_Latency(double latency){
		//**System.out.println("add to the overall delayed write latency : + "+latency);
		Overall_delayed_write_latency+= latency;
	}
		
	public static double getOverall_read_Latency(){
		
		return Overall_read_latency;
	}
	
	public static double getOverall_write_Latency(){
		return Overall_write_latency;
	}
	
	public static double getOverall_delayed_write_Latency(){
		return Overall_delayed_write_latency;
	}
	
	public static double getOverall_blocked_write_Latency(){
		return Overall_blocked_write_latency;
	}
	
	public static void reset_Overall_read_Latency(){
		Overall_read_latency= 0;
	}
	public static void reset_Overall_write_Latency(){
		Overall_write_latency= 0;
	}

	public static double getOverall_Latency() {
		// TODO Auto-generated method stub
		return Overall_read_latency+Overall_write_latency+Stats.version_exchange_latency+Overall_blocked_write_latency;
	}
	
	public static void addWriteLatency(String tupleType, float latency){
		if(writeLatencyMap.containsKey(tupleType)){
			writeLatencyMap.put(tupleType, latency + writeLatencyMap.get(tupleType));
		}else{
			writeLatencyMap.put(tupleType, latency);
		}
			
	}
	
	public static void addReadLatency(String tupleType, float latency){
		if(readLatencyMap.containsKey(tupleType)){
			readLatencyMap.put(tupleType, latency + readLatencyMap.get(tupleType));
		}else{
			readLatencyMap.put(tupleType, latency);
		}
			
	}
	
	public static void saveLatencyMap(int cond){
		FileWriter readfile;
		FileWriter writefile;
		
		try {
			if(cond==0){
				writefile = new FileWriter("Stats/iFogStor/writeLatencyMapFileGlobal");
				readfile = new FileWriter("Stats/iFogStor/readlatencyMapFileGlobal");
				
			}else if(cond==1){
				writefile = new FileWriter("Stats/iFogStorP/writeLatencyMapFileTemp", true);
				readfile = new FileWriter("Stats/iFogStorP/readlatencyMapFileTemp", true);
				
			}else if(cond==2){
				writefile = new FileWriter("Stats/iFogStorS/writeLatencyMapFileTemp", true);
				readfile = new FileWriter("Stats/iFogStorS/readlatencyMapFileTemp", true);
				
			}else if(cond==3){
				writefile = new FileWriter("Stats/Exact/writeLatencyMapFileTemp", true);
				readfile = new FileWriter("Stats/Exact/readlatencyMapFileTemp", true);
				
			}else {
				writefile = null;
				readfile = null;
				System.out.println("Erreur, saveLatencyMap cond = "+cond);
				System.exit(0);
			}
			
			BufferedWriter fw = new BufferedWriter(writefile);
			BufferedWriter fr = new BufferedWriter(readfile);
			
			for(AppEdge edge : FogStorage.application.getEdges()){
				if(edge.getTupleType().contains("SNR") || edge.getTupleType().contains("Act") ){
					continue;
				}
				fw.write(edge.getTupleType()+"\t"+writeLatencyMap.get(edge.getTupleType())+"\n");
				fr.write(edge.getTupleType()+"\t"+readLatencyMap.get(edge.getTupleType())+"\n");
			}
	
			fw.close();
			fr.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void reset_LatencyMapStats() {
		// TODO Auto-generated method stub
		writeLatencyMap.clear();
		readLatencyMap.clear();
	}
	
	public static void reset_ALLStats() {
		// TODO Auto-generated method stub
		Overall_read_latency=0;
		Overall_write_latency=0;
		Overall_delayed_write_latency=0;
		Overall_blocked_write_latency=0;
		
	}
	
}
