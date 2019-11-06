package org.fog.examples;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


public class Log{
	public static void writeSolvingTime(int nb_HGW, String log) throws IOException{
		FileWriter lpFile = new FileWriter("Stats/solvingtime"+nb_HGW+"_"+DataPlacement.nb_DataCons_By_DataProd+DataPlacement.dcp, true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write(log+"\n");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void writeProblemFormulationTime(int nb_HGW, String log) throws IOException{
		FileWriter lpFile = new FileWriter("Stats/problemFormulationTime"+nb_HGW+"_"+DataPlacement.nb_DataCons_By_DataProd+DataPlacement.dcp, true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write(log+"\n");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void writeDataAllocationStats(int nb_HGW, String log) throws IOException{
		FileWriter lpFile = new FileWriter("Stats/iFogStor/dataAllocationStats"+nb_HGW+"_"+DataPlacement.nb_DataCons_By_DataProd+DataPlacement.dcp, true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write(log+"\n");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void writeDataAllocationStatsExternZoneCons(int nb_HGW, String log) throws IOException{
		FileWriter lpFile = new FileWriter("Stats/dataAllocationStatsExternZoneCons"+nb_HGW+"_"+DataPlacement.nb_DataCons_By_DataProd+DataPlacement.dcp, true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write(log+"\n");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void writeTotalNbCons(int nb_HGW, String log) throws IOException{
		FileWriter lpFile = new FileWriter("Stats/TotalNbCons"+nb_HGW+"_"+DataPlacement.nb_DataCons_By_DataProd+DataPlacement.dcp, true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write(log+"\n");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void writeSimulationTime(int nb_HGW, String log) throws IOException{
		FileWriter lpFile = new FileWriter("Stats/SimulationTime"+nb_HGW+"_"+DataPlacement.nb_DataCons_By_DataProd+DataPlacement.dcp, true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write(log+"\n");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void writeShortestPathTime(int nb_HGW, String log) throws IOException{
		FileWriter lpFile = new FileWriter("Stats/ShortestPathTime"+nb_HGW+"_"+DataPlacement.nb_DataCons_By_DataProd+DataPlacement.dcp, true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write(log+"\n");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void writePmedianTime(int nb_HGW, String log) throws IOException{
		FileWriter lpFile = new FileWriter("Stats/PmedianTime"+nb_HGW+"_"+DataPlacement.nb_DataCons_By_DataProd+DataPlacement.dcp, true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write(log+"\n");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void writeNbCombinaison(int nb_HGW, String log) throws IOException{
		FileWriter lpFile = new FileWriter("Stats/NbCombinaison"+nb_HGW+"_"+DataPlacement.nb_DataCons_By_DataProd+DataPlacement.dcp, true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write(log+"\n");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void writeInfraCreationTime(int nb_HGW, String log) throws IOException{
		FileWriter lpFile = new FileWriter("Stats/InfraCreationTime"+nb_HGW+"_"+DataPlacement.nb_DataCons_By_DataProd+DataPlacement.dcp, true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write(log+"\n");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void writeGraphPonderationTime(int nb_HGW, String log) throws IOException{
		FileWriter lpFile = new FileWriter("Stats/graphPonderationTime"+nb_HGW+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write(log+"\n");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void writePartitionDevicesInformation(int nb_HGW, String log) throws IOException{
		FileWriter lpFile = new FileWriter("Stats/graphPartitionInformation"+nb_HGW+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write(log+"\n");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void writePartitionTime(int nb_HGW, String log) throws IOException{
		FileWriter lpFile = new FileWriter("Stats/partitionTime"+nb_HGW+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write(log+"\n");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void writeShortPathTime(int nb_HGW, String log) throws IOException{
		FileWriter lpFile = new FileWriter("Stats/shortPathTime"+nb_HGW+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write(log+"\n");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
