package org.fog.lpFileConstuction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.fog.examples.DataPlacement;

public class MakeLPFile2{
	public  static int nb_DataHost;
	public  static int nb_DataProd;
	public  static int nb_DataCons;
	
	public  static int base;
	
	private static final int nb_thread = 32;
	
	public  static float [][] write_basis_laenty; 
	public  static float [][] read_basis_laenty;
	
	public  static int [][] consProd_matrix;
	
	public  static long [] freeCapacity;
	public  static float [] dataSize;
	
	private float [][][] xfactors;
	private float [][] yfactors;
	
	public MakeLPFile2(int nb_GW) throws IOException, InterruptedException{
		intializeDataActor(nb_GW);
		initializeWriteLatencyMatrix(nb_GW);
		initializeReadLatencyMatrix(nb_GW);
		initializeConsProdMatrix(nb_GW);
		initializeFreeCapacity(nb_GW);
		initializeDataSize(nb_GW);
		
		contructionLpFile(nb_GW);
		//printWriteLatency();
		//printReadLatency();
		//printConsProdMatrix();
		//printFreeCapacity();
		//printDataSize();
	}
	
	private boolean contructionLpFile(int nb_GW) throws IOException, InterruptedException{
		if(checkSolution()){
			//findFactors();
			equation(nb_GW);
			constraints(nb_GW);
			bounds(nb_GW);
			general(nb_GW);
			end(nb_GW);
		}else{
			System.out.println("There's no possible solution, see MakeLPFile.java!");
			System.exit(0);
		}	
		return true;
	}

	private boolean checkSolution(){
		long fc=0;
		long ds=0;
		
		for(int i=0;i<nb_DataHost; i++){
			fc=+freeCapacity[i];
		}
		
		for(int j=0;j<nb_DataProd; j++){
			ds=(long) +dataSize[j];
		}
		
		if(ds>fc){
			//System.out.println("There is no solution! Insufficient storage capacity");
			return false;
		}
		return true;
	}
	
	private void equation(int nb_GW) throws IOException, InterruptedException{
		int x;
		float [][][] xfac = new float [nb_DataProd][nb_DataHost][nb_DataCons];
		float [][] yfac = new float [nb_DataProd][nb_DataHost];
		
		for (int i = 0; i < nb_DataProd;i++)
			for (int j = 0; j < nb_DataHost;j++){
				x=(int) (((float) dataSize[i])/((float) base));
				if((dataSize[i])%( base)!=0){
					x++;
				}
				yfac[i][j] = (write_basis_laenty[j][i])*x;

				int u = 0;
				for (int c = 0; c < nb_DataCons; c++){
					if(consProd_matrix[i][c] == 1){
						xfac[i][j][u] = x * read_basis_laenty[j][c];
						u++;
					}
				}
				if(u > DataPlacement.nb_DataCons_By_DataProd){
					System.out.println("Erreur! u > "+DataPlacement.nb_DataCons_By_DataProd);
					System.exit(0);
				}
			}
		
		xfactors = xfac;
		yfactors = yfac;
		
		FileWriter lpFile = new FileWriter(nb_GW+"cplex_"+DataPlacement.nb_DataCons_By_DataProd+".lp");
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write("Minimize\n");
			for (int i = 0; i < nb_DataProd; i++){
				for (int j = 0; j < nb_DataHost; j++){
					fw.write(yfactors[i][j]+" y"+i+"_"+j+" + ");
				}
			 }
			
			
			for (int i = 0; i < nb_DataProd; i++){
				for (int j = 0; j < nb_DataHost; j++){
					for (int u = 0; u < DataPlacement.nb_DataCons_By_DataProd; u++){
						fw.write(xfactors[i][j][u]+" x"+i+"_"+j+"_"+u+" + ");
					}
				}
			 }

			fw.write("\n");
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void constraints(int nb_GW) throws IOException{
		FileWriter lpFile = new FileWriter(nb_GW+"cplex_"+DataPlacement.nb_DataCons_By_DataProd+".lp", true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write("Subject To\n");
			
			/* espace de stockage suffisant pour chaque affectation */
			for (int j = 0; j < nb_DataHost;j++){
				fw.write("  ");
		 		for (int i = 0; i < nb_DataProd; i++){
		 			fw.write(dataSize[i]+" y"+i+"_"+j);		 
		 			if (i<(nb_DataProd-1)){
		 				fw.write(" + ");
		 			}

		 		}
		 		fw.write(" <= "+freeCapacity[j]+"\n");
		 	}
			
			/* tout consommateur devrait etre servi par QR noeuds */
			for (int i = 0; i < nb_DataProd;i++){
				for (int u = 0; u < DataPlacement.nb_DataCons_By_DataProd; u++){
					fw.write("  ");
					for (int j = 0; j < nb_DataHost;j++){
			 			fw.write(" x"+i+"_"+j+"_"+u);		 
			 			if (j<(nb_DataHost-1)){
			 				fw.write(" + ");
			 			}
					}
					fw.write(" = "+DataPlacement.QR+"\n");
				}
			}
			
			/*limiter le nombre de replicas par min */
			for (int i = 0; i < nb_DataProd; i++){
				fw.write("  ");
				for (int j = 0; j < nb_DataHost;j++){
		 			fw.write(" y"+i+"_"+j);		 
		 			if (j<(nb_DataHost-1)){
		 				fw.write(" + ");
		 			}

		 		}
		 		fw.write(" >= "+DataPlacement.min_data_replica+"\n");
		 		//fw.write(" >= "+DataPlacement.QW+"\n");
		 	}
			
			/*limiter le nombre de replicas par max */
			for (int i = 0; i < nb_DataProd; i++){
				fw.write("  ");
				for (int j = 0; j < nb_DataHost;j++){
		 			fw.write(" y"+i+"_"+j);		 
		 			if (j<(nb_DataHost-1)){
		 				fw.write(" + ");
		 			}

		 		}
		 		fw.write(" <= "+DataPlacement.max_data_replica+"\n");
		 	}
			
			/* stabilisation du systeme, un noeud ne peut pas fournir un replica qu il ne contient pas*/
			for(int i=0;i<nb_DataProd;i++)
				for(int j=0;j<nb_DataHost;j++)
					for(int u=0;u<DataPlacement.nb_DataCons_By_DataProd;u++){
						fw.write("  ");
						fw.write("x"+i+"_"+j+"_"+u+" - "+"y"+i+"_"+j+" <= 0\n");
					}
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	private void bounds(int nb_GW) throws IOException{
		FileWriter lpFile = new FileWriter(nb_GW+"cplex_"+DataPlacement.nb_DataCons_By_DataProd+".lp", true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write("Bounds\n");

			for(int i=0;i<nb_DataProd;i++)
				for(int j=0;j<nb_DataHost;j++)
					for(int u=0;u<DataPlacement.nb_DataCons_By_DataProd;u++)
						fw.write("0 <= x"+i+"_"+j+"_"+u+" <=1\n");
				
			for(int i=0;i<nb_DataProd;i++)
				for(int j=0;j<nb_DataHost;j++)
					fw.write("0 <= y"+i+"_"+j+" <=1\n");

			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void general(int nb_GW) throws IOException{
		FileWriter lpFile = new FileWriter(nb_GW+"cplex_"+DataPlacement.nb_DataCons_By_DataProd+".lp", true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			
			fw.write("General\n");
			for(int i=0;i<nb_DataProd;i++)
				for(int j=0;j<nb_DataHost;j++)
					for(int u=0;u<DataPlacement.nb_DataCons_By_DataProd;u++)
						fw.write("x"+i+"_"+j+"_"+u+"\n");
			
			for(int i=0;i<nb_DataProd;i++)
				for(int j=0;j<nb_DataHost;j++)
					fw.write("y"+i+"_"+j+"\n");
				
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void end(int nb_GW) throws IOException{
		FileWriter lpFile = new FileWriter(nb_GW+"cplex_"+DataPlacement.nb_DataCons_By_DataProd+".lp", true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write("End\n");
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	private void intializeDataActor(int nb_GW) throws FileNotFoundException{
		FileReader fichier = new FileReader(nb_GW+"dataActors_"+DataPlacement.nb_DataCons_By_DataProd+".txt");
		BufferedReader in = null;
		try{
			in = new BufferedReader (fichier);
			String line = in.readLine();
			String[] splited = line.split("\t");
			
			nb_DataHost = Integer.valueOf(splited[0]);
			nb_DataProd = Integer.valueOf(splited[1]);
			nb_DataCons = Integer.valueOf(splited[2]);
			base = Integer.valueOf(splited[3]);
			
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//System.out.println("nb_DataHost:"+nb_DataHost);
		//System.out.println("nb_DataProd:"+nb_DataProd);
		//System.out.println("nb_DataCons:"+nb_DataCons);
		//System.out.println("Exchange unit:"+base);
		
	}
	
	private void initializeWriteLatencyMatrix(int nb_GW) throws FileNotFoundException{
		float [][] writeDelay = new float [nb_DataHost][nb_DataProd];
		
		FileReader fichier = new FileReader(nb_GW+"writeDelay_"+DataPlacement.nb_DataCons_By_DataProd+".txt");
		BufferedReader in = null;
		try{
			in = new BufferedReader (fichier);
			String line =null;
			int row = 0;
	
			while((line = in.readLine()) != null){
				String[] splited = line.split("\t");
				int col = 0;

				for(String val : splited){
					writeDelay[row][col] = Float.valueOf(val);
					col++;
				}
				row++;
			}
			in.close();
			
			write_basis_laenty = writeDelay;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initializeReadLatencyMatrix(int nb_GW) throws FileNotFoundException{
		FileReader fichier = new FileReader(nb_GW+"readDelay_"+DataPlacement.nb_DataCons_By_DataProd+".txt");
		float [][] readDelay = new float [nb_DataHost][nb_DataCons];
		BufferedReader in = null;
		try{
			in = new BufferedReader (fichier);
			String line =null;
			int row = 0;
	
			while((line = in.readLine()) != null){
				String[] splited = line.split("\t");
				int col = 0;
				for(String val : splited){
					readDelay[row][col] = Float.valueOf(val);
					col++;
				}
				row++;
			}
			in.close();
			read_basis_laenty = readDelay;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initializeConsProdMatrix(int nb_GW) throws FileNotFoundException{
		FileReader fichier = new FileReader(nb_GW+"consProd_"+DataPlacement.nb_DataCons_By_DataProd+".txt");
		int [][] consProd = new int [nb_DataProd][nb_DataCons];
		BufferedReader in = null;
		try{
			in = new BufferedReader (fichier);
			String line =null;
			int row = 0;
	
			while((line = in.readLine()) != null){
				String[] splited = line.split("\t");
				int col = 0;
				for(String val : splited){
					if(Integer.valueOf(val) == 1){
						consProd[row][col] = 1;
					}else{
						consProd[row][col] = 0;
					}	
					col++;
				}
				row++;
			}
			in.close();
			consProd_matrix = consProd;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initializeFreeCapacity(int nb_GW) throws FileNotFoundException{
		FileReader fichier = new FileReader(nb_GW+"freeCapacity_"+DataPlacement.nb_DataCons_By_DataProd+".txt");
		long [] free = new long [nb_DataHost];
		BufferedReader in = null;
		try{
			in = new BufferedReader (fichier);
			String line = in.readLine();
	
			String[] splited = line.split("\t");
			int i =0;
			for(String val : splited){
				free[i]= Long.valueOf(val);
				i++;
			}

			in.close();
			freeCapacity = free;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initializeDataSize(int nb_GW) throws FileNotFoundException{
		FileReader fichier = new FileReader(nb_GW+"dataSize_"+DataPlacement.nb_DataCons_By_DataProd+".txt");
		float [] size = new float [nb_DataProd];
		BufferedReader in = null;
		try{
			in = new BufferedReader (fichier);
			String line = in.readLine();
			String[] splited = line.split("\t");
			int i =0;
			for(String val : splited){
				if(val.equals("null")){
					size[i]= 0;
				}else{
					size[i]= Float.valueOf(val);
				}
				i++;
			}
			in.close();
			
			dataSize = size;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

