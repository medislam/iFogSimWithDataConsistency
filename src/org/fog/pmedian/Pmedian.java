package org.fog.pmedian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Log;
import org.fog.application.AppEdge;
import org.fog.application.Application;
import org.fog.examples.DataPlacement;
import org.fog.pMedianOfAllConsumersShortestPaths.AllShortestPathsNodes;
import org.fog.placement.ModuleMapping;

public class Pmedian {
	
	
	/*
	 * this map contains all shortest path nodes existed between dp and dc
	 * TupleType , List of All shortest Path nodes
	 */
	public static Map<String, List<Integer>> shortestPathNodesMap;
	
	/*
	 * this map contains the p-median of all shortest path nodes 
	 * TupleType , nb-median, List of Pmedian
	 */
	public static Map<Pair<String,Integer>, List<Integer>> pmedianMap;
	
	public Pmedian() {
		// TODO Auto-generated constructor stub
		System.out.println("Create shortest path nodes Map");
		shortestPathNodesMap = new HashMap<String, List<Integer>>();
		
		System.out.println("Create P-median Map");
		pmedianMap = new HashMap<Pair<String,Integer>, List<Integer>>();
	}
	
	public void setPMedianList(String tupleType, int nb_median, List<Integer> pmedian){
		Pair<String,Integer> pair = new Pair<String,Integer>(tupleType, nb_median);
		pmedianMap.put(pair, pmedian);
	}
	
	public List<Integer> getPMedianList(String tupleType){
		if (pmedianMap.containsKey(tupleType)) {
			return pmedianMap.get(tupleType);
					
		} else {
			System.out.println("Error! there is no pmedian list for:"+tupleType);
		}
		
		return null;
	}
	

	public void setShortestPathNodesList(String tupleType, List<Integer> shortestPathNodes){
		Log.writeInLogFile("P median:", "\ttuple Type:"+tupleType+"\tshortest paths nodes:"+shortestPathNodes.toString());
		System.out.println("P median:"+ "\ttuple Type:"+tupleType+"\tshortest paths nodes:"+shortestPathNodes.toString());
		shortestPathNodesMap.put(tupleType, shortestPathNodes);
	}
	
	public List<Integer> getShortestPathNodes(String tupleType){
		if (shortestPathNodesMap.containsKey(tupleType)) {
			return shortestPathNodesMap.get(tupleType);
					
		} else {
			System.out.println("Error! there is no shortest path nodes list for:"+tupleType);
		}
		
		return null;
	}
	
	public void printAllPMedian(){
		System.out.println("All P-medians");
		
		for(Pair<String, Integer> pair : pmedianMap.keySet()){
			System.out.println("tupleType:"+pair.getFirst()+" nb_median:"+pair.getSecond());
			for (int median: pmedianMap.get(pair)) {
				System.out.print(median+"\t");
			}
			System.out.println();
		}
	}
	
	public void printAllShortestPathNodes(){
		System.out.println("All shortest Paths Nodes");
		for(String tupleType: shortestPathNodesMap.keySet()){
			System.out.println("tupleType:"+tupleType);
			for (int median: shortestPathNodesMap.get(tupleType)) {
				System.out.print(median+"\t");
			}
			System.out.println();
		}
	}
	
	public void computeAllShortestPathsNodes(Application application) throws IOException, InterruptedException{
		System.out.println("compute All Shortest Paths nodes");
		Log.writeInLogFile("P median:", "\tcompute All Shortest Paths nodes");
		//int i=0;
		for(AppEdge edge: application.getEdges()){
			if(!(edge.getTupleType().startsWith("TempSNR") || edge.getTupleType().startsWith("TempAct"))){
				//System.out.println("\n-----------------------------------");
				//System.out.println("Edge:"+edge.toString());
				
				AllShortestPathsNodes shortestPaths = new AllShortestPathsNodes();
				List<Integer> potentialNodes = shortestPaths.getPotentialNodes(edge, application);
				
				this.setShortestPathNodesList(edge.getTupleType(), potentialNodes);			
			}
			//i++;
		}
	}
	
	public void computeAllPMedian(Application application) throws IOException, InterruptedException{
		System.out.println("Compute All P-Median");
		Log.writeInLogFile("P median:", "\tcompute All P median");
		int i=0;
		
		for(AppEdge edge: application.getEdges()){
			
			if((edge.getTupleType().startsWith("TempSNR") || edge.getTupleType().startsWith("TempAct"))){
				continue;
			}
			
			System.out.println("\n-----------------------------------");
			System.out.println("Edge:"+edge.getTupleType());
			Log.writeInLogFile("P median:", "Tuple tupe:"+edge.getTupleType());
			
			int producerNode = 	application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(application.getEdgeMap().get(edge.getTupleType()).getSource()) ).getId();

			List<Integer> nodes = getNodes(edge.getTupleType(), application);
			List<Integer> potentialNodes = shortestPathNodesMap.get(edge.getTupleType());
			
			
			Log.writeInLogFile("P median:", "Tuple tupe:"+edge.getTupleType()+"\nodes:"+nodes);
			Log.writeInLogFile("P median:", "Tuple tupe:"+edge.getTupleType()+"\tshorest nodes:"+potentialNodes);
			
			if(potentialNodes.size() > DataPlacement.min_data_replica){
				
				for(int nb_median= DataPlacement.min_data_replica; nb_median<DataPlacement.max_data_replica+1;nb_median++){
					System.out.println("P-median formualtion");
					PmedianFormulation pMedianFormulation = new PmedianFormulation(nb_median);
					pMedianFormulation.contructionLpFile(producerNode, nodes, potentialNodes, i);
							
					System.out.println("P-median solving");
					PmedianSolving pMedianSolving = new PmedianSolving();
					pMedianSolving.problemSolving(nodes, potentialNodes, i);
					this.setPMedianList(edge.getTupleType(),nb_median ,pMedianSolving.getSolution(nodes, potentialNodes,i));
				}
				
			}else if(potentialNodes.size() == DataPlacement.min_data_replica){
				this.setPMedianList(edge.getTupleType(), potentialNodes.size(), potentialNodes);
			
			}else{
				System.out.println("shortest path nodes must be > to the min rep!");
				System.exit(0);
			}
			
			i++;
			
		}	
	}

	private List<Integer> getNodes(String tupleType, Application application) {
		// TODO Auto-generated method stub
		List<Integer> nodes = new ArrayList<Integer>();
//		int sourceNode = application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(application.getEdgeMap().get(tupleType).getSource()) ).getId();
		
		for(String consModule: application.getEdgeMap().get(tupleType).getDestination()){
			nodes.add(application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(consModule)).getId());
		}
		
//		if(!nodes.contains(sourceNode))
//			nodes.add(sourceNode);
		
		return nodes;
	}
}
