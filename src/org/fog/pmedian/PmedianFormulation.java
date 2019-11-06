package org.fog.pmedian;

import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloNumVar;
import ilog.cplex.*;
import ilog.concert.*;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.fog.entities.FogBroker;
import org.fog.examples.DataPlacement;
import org.fog.lpFileConstuction.BasisDelayMatrix;
import org.fog.stats.Stats;

public class PmedianFormulation {
	
	int nb_median;

	public PmedianFormulation(int nb_median) {
		// TODO Auto-generated constructor stub
		this.nb_median=nb_median;
	}
	
	/*
	 * The p median linear formulation
	 * 
	 * min (sum{j in m} cj * y_j + sum{i in n} sum{j in m} c_i,j * x_i,j)
	 * 
	 * s.t.
	 * sum_{j in m} x_i,j = 1, forall i in n
	 * y_j - x_i,j >= 0, forall i in n, j in m
	 * sum_{j in m} y_j = p
	 * x_i,j, y_j in {0,1}
	 * 
	 */
	
	
	public boolean contructionLpFile(int sourceNode, List<Integer> nodes, List<Integer> potentialNodes, int nb_lpFile) throws IOException, InterruptedException{
		equation(sourceNode, nodes, potentialNodes, nb_lpFile);
		constraints(nodes, potentialNodes,nb_lpFile);
		bounds(nodes, potentialNodes,nb_lpFile);
		general(nodes, potentialNodes,nb_lpFile);
		end(nb_lpFile);
		return true;
	}
	
	private void equation(int sourceNode, List<Integer> nodes, List<Integer> potentialNodes, int nb_lpFile) throws IOException, InterruptedException{
		
		FileWriter lpFile = new FileWriter("linearFiles/cplex"+nb_lpFile+".lp");
		FileWriter nodesFile = new FileWriter("linearFiles/nodes"+nb_lpFile);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			BufferedWriter fw2 = new BufferedWriter(nodesFile);
			fw.write("Minimize\n");
			
			fw2.write("tuple type:"+nb_lpFile+"\n");
			fw2.write("source:"+sourceNode+"\n");
			fw2.write("nodes:"+nodes.toString()+"\n");
			fw2.write("potential nodes:"+potentialNodes.toString()+"\n");
			fw2.close();			
			
			for (int j = 0; j < potentialNodes.size(); j++){
				//float cost = BasisDelayMatrix.mDelayMatrix[sourceNode-3][potentialNodes.get(j)-3] * Stats.getProductionTimes(sourceNode);
				float cost = BasisDelayMatrix.mDelayMatrix[sourceNode-3][potentialNodes.get(j)-3];
				fw.write(cost+" y"+j+" + ");

			}
			
			for (int i = 0; i < nodes.size(); i++){
				for (int j = 0; j < potentialNodes.size(); j++){
					//float cost = BasisDelayMatrix.mDelayMatrix[nodes.get(i)-3][potentialNodes.get(j)-3] * Stats.getConsumptionTimes();
					float cost = BasisDelayMatrix.mDelayMatrix[nodes.get(i)-3][potentialNodes.get(j)-3];
					fw.write(cost+" x"+i+"_"+j);
					if (i< (nodes.size()-1) || j<(potentialNodes.size()-1)){
		 				fw.write(" + ");
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

	private void constraints(List<Integer> nodes, List<Integer> potentialNodes,int nb_lpFile) throws IOException{
		FileWriter lpFile = new FileWriter("linearFiles/cplex"+nb_lpFile+".lp",true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write("Subject To\n");
			
			/* constaints1 */
			for (int i = 0; i < nodes.size();i++){
				fw.write("  ");
		 		for (int j = 0; j< potentialNodes.size(); j++){
		 			fw.write(" x"+i+"_"+j);		 
		 			if (j<(potentialNodes.size()-1)){
		 				fw.write(" + ");
		 			}

		 		}
		 		fw.write(" = 1\n");
		 	}
			
			/* constraints 2 */
			for (int i = 0; i < potentialNodes.size();i++){
				for (int j = 0; j < potentialNodes.size(); j++){
			 		fw.write(" - x"+i+"_"+j+" + y"+j+" >= 0\n");		 
			 			
				}
			}
			
			/* limiter le nombre de replicas par min */
//			for (int i = 0; i < nodes.size(); i++){	
				for(int j=0; j < potentialNodes.size(); j++){
					fw.write(" y"+j);	
			 		if (!(j>=(potentialNodes.size()-1))){
		 				fw.write(" + ");
		 			}
//				}
		 	}
			fw.write(" = "+nb_median+"\n");		
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void bounds(List<Integer> nodes, List<Integer> potentialNodes,int nb_lpFile) throws IOException{
		FileWriter lpFile = new FileWriter("linearFiles/cplex"+nb_lpFile+".lp",true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			fw.write("Bounds\n");
				
			for(int i=0;i<nodes.size();i++)
				for(int j=0;j<potentialNodes.size();j++)
					fw.write("0 <= x"+i+"_"+j+" <=1\n");
			
			for(int j=0;j<potentialNodes.size();j++)
				fw.write("0 <= y"+j+" <=1\n");

			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void general(List<Integer> nodes, List<Integer> potentialNodes,int nb_lpFile) throws IOException{
		FileWriter lpFile = new FileWriter("linearFiles/cplex"+nb_lpFile+".lp",true);
		try {
			BufferedWriter fw = new BufferedWriter(lpFile);
			
			fw.write("General\n");
			
			for(int i=0;i<nodes.size();i++)
				for(int j=0;j<potentialNodes.size();j++)
					fw.write("x"+i+"_"+j+"\n");
			
			for(int j=0;j<potentialNodes.size();j++)
				fw.write("y"+j+"\n");
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void end(int nb_lpFile) throws IOException{
		FileWriter lpFile = new FileWriter("linearFiles/cplex"+nb_lpFile+".lp",true);
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

}
