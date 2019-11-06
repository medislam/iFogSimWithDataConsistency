package org.fog.pmedian;

import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.cplex.IloCplex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PmedianSolving {
	

	public PmedianSolving() {
		// TODO Auto-generated constructor stub
	}

	public boolean problemSolving(List<Integer> nodes, List<Integer> potentialNodes, int nb_lpFile) throws IOException {
		try {
			IloCplex cplex = new IloCplex();
						
			//cplex.setParam(IloCplex.IntParam.WorkMem, 1024*20);
			System.out.println("Work memory ="+cplex.getParam(IloCplex.IntParam.WorkMem));
			
			cplex.setParam(IloCplex.IntParam.VarSel, 4);
			cplex.setParam(IloCplex.BooleanParam.MemoryEmphasis, true);
			cplex.setParam(IloCplex.DoubleParam.TreLim, 2048*60);
			cplex.setParam(IloCplex.IntParam.NodeFileInd, 3);

				
			System.out.println("Importing the LP file...");

			cplex.importModel("linearFiles/cplex"+nb_lpFile+".lp");
			
			//cplex.importModel("linearFiles/islamPmedian.lp");
			
			IloLPMatrix lp = (IloLPMatrix)cplex.LPMatrixIterator().next();
			
									
			System.out.println("Solving the problem...");
			cplex.setOut(null);
						
			if(cplex.solve()){
				
				System.out.println("The problem "+nb_lpFile+" is well solving");
				
				double objval = cplex.getObjValue();
				System.out.println("Objective ="+objval);

				System.out.println("Status:"+cplex.getStatus());
				
				double [] x = cplex.getValues(lp);
				
				FileWriter fichier2 = new FileWriter("linearFiles/Solution"+nb_lpFile);
				FileWriter nodesFile = new FileWriter("linearFiles/nodes"+nb_lpFile, true);

				try{
					BufferedWriter fw2 = new BufferedWriter(fichier2);
					BufferedWriter fw = new BufferedWriter(nodesFile);

					fw.write("length:"+x.length+"\n");
					
					fw.close();

					for(int j=0;j<potentialNodes.size();j++){
							fw2.write(String.valueOf(x[(j)])+"\t");
						
					}
					fw2.write("\n");

	
					for(int i=0;i<nodes.size();i++){
						for(int j=0;j<potentialNodes.size();j++){
							fw2.write(String.valueOf(x[(i*potentialNodes.size()+j)+potentialNodes.size()])+"\t");
						
						}
						fw2.write("\n");
					}			
					fw2.close();
					
				}catch (FileNotFoundException e){
					e.printStackTrace();
				}catch (IOException e){
					e.printStackTrace();
				}
				
				
				System.out.println(cplex.getStatus());
				cplex.end();
				return true;
			}else{
				System.out.println("Problem doesn't solving!, may there is insuffusent work memory!");
			}
			cplex.end();
			} catch (IloException e) {
				System.err.println("Concert exception caught: " + e);
			}
		return false;
		
	}

	public List<Integer> getSolution(List<Integer> nodes, List<Integer> potentialNodes, int nb_lpFile) throws FileNotFoundException{
		List<Integer> pmedian = new ArrayList<Integer>();
		
		FileReader lpFile = new FileReader("linearFiles/Solution"+nb_lpFile);
		try {
			BufferedReader in = new BufferedReader(lpFile);
			
			String line =null;
			
			if((line = in.readLine()) != null){
				String[] splited = line.split("\t");
				for (int i = 0; i < splited.length; i++) {
					if(splited[i].equals("1.0")){
						pmedian.add(potentialNodes.get(i));
						
					}
				}
			}else{
				System.out.println("there is no found solution for:"+nb_lpFile);
				System.exit(0);
			}

		
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pmedian;
	}
}
