package org.fog.pMedianOfAllConsumersShortestPaths;

import java.util.ArrayList;
import java.util.List;

import org.fog.application.AppEdge;
import org.fog.application.Application;
import org.fog.examples.DataPlacement;
import org.fog.lpFileConstuction.BasisDelayMatrix;
import org.fog.placement.ModuleMapping;

public class AllShortestPathsNodes {
	
	
	public AllShortestPathsNodes(){
	}
	
	public List<Integer> getPotentialNodes(AppEdge edge, Application application){
		List<Integer> shortestPathNodes = new ArrayList<Integer>();
							
		int prodId = application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(edge.getSource())).getId(); 
				
		System.out.println("ProdId:"+prodId);
				
		for(String consumer: application.getDataConsIndexOfDataProd(edge.getTupleType())){
			System.out.println("Consumer:"+consumer);
			int devId = application.getFogDeviceByName(ModuleMapping.getDeviceHostModule(consumer)).getId();
			
			if(!shortestPathNodes.contains(devId))
				shortestPathNodes.add(devId);
			System.out.print(devId+"-->");
					
			if(devId!=prodId){
				int nodeId = BasisDelayMatrix.mFlowMatrix[prodId-3][devId-3]+3;
				if(!shortestPathNodes.contains(nodeId))
					shortestPathNodes.add(nodeId);
				System.out.print(nodeId+"-->");
						
				while (nodeId!=prodId){
					nodeId=BasisDelayMatrix.mFlowMatrix[prodId-3][nodeId-3]+3;
					if(!shortestPathNodes.contains(nodeId))
						shortestPathNodes.add(nodeId);
					System.out.print(nodeId+"-->");
				}
			}
			System.out.println();
		}
		
		while(shortestPathNodes.size()<DataPlacement.nb_shortest_Paths_Nodes){
			shortestPathNodes.add(getClosestNode(shortestPathNodes));
			System.out.println("Add a closed node to the shortest path nodes!");
		}

		return shortestPathNodes;
	}

	private Integer getClosestNode(List<Integer> shortestPathNodes) {
		// TODO Auto-generated method stub
		
		int closestNode = -1;
		float min = Float.MAX_VALUE;
		
		for(int node : shortestPathNodes){
			int closestNodeOfNode = - 1;
			float min_latency = Float.MAX_VALUE;
			
			for(int destNode = 3; destNode <BasisDelayMatrix.mFlowMatrix.length+3; destNode++){
				if(!shortestPathNodes.contains(destNode)){
					if(min_latency > BasisDelayMatrix.mFlowMatrix[node - 3][destNode -3]){
						closestNodeOfNode = destNode;
						min_latency = BasisDelayMatrix.mFlowMatrix[node - 3][destNode -3];
					}
				}
						
			}
			
			if(closestNodeOfNode == -1){
				System.out.println("Error, Closest node of a node is not found! for node"+node);
				System.exit(0);
			}
			
			if(min > min_latency){
				min = min_latency;
				closestNode = closestNodeOfNode;
			}
	
		}
		
		if(closestNode == -1){
			System.out.println("Error, Closest node is not found for shortest path nodes!");
			System.exit(0);
		}

		return closestNode;
	}

}
