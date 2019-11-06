package org.fog.dataConsistency;

import java.util.ArrayList;
import java.util.List;

import org.fog.lpFileConstuction.BasisDelayMatrix;

public class Median {
	
	protected List<Integer> replicasList;
	protected float [][] latencyMatrix;
	
	public Median(List<Integer> replicasList) {
		// TODO Auto-generated constructor stub
		this.setReplicasList(replicasList);
		this.setLatencyMatrix();
	}

	private void setLatencyMatrix() {
		// TODO Auto-generated method stub
		latencyMatrix = new float [replicasList.size()][replicasList.size()];

		for(int i=0;i<replicasList.size()-1;i++){
			for(int j=i+1;j<replicasList.size();j++){
				latencyMatrix[i][j]= BasisDelayMatrix.getFatestLink(replicasList.get(i), replicasList.get(j));
				latencyMatrix[j][i]= latencyMatrix[i][j];
			}
		}
	}

	private void setReplicasList(List<Integer> replicasList) {
		// TODO Auto-generated method stub
		this.replicasList = new ArrayList<>(replicasList);
	}
	
	protected int getMedianReplica(){
		/*
		 * value of sum of line i in matrix latencyMatrix equals the value of latency from i to all others nodes
		 * So if i is a median, the value of the line i in the matrix is the min
		 * So the line whose the min value, is the median
		 * 
		 */
		
		float [] tabOfSumInLines = new float [replicasList.size()];
		
		for(int i=0; i<replicasList.size();i++){
			int sum =0;
			for (int j = 0; j < replicasList.size(); j++) {
				sum += latencyMatrix[i][j];
			}
			tabOfSumInLines[i] = sum;
		}
		
		/*
		 * Search the min value in the table
		 */
		
		int min = 0 ;
		float minLatency = tabOfSumInLines[min];
		
		for (int i = 1; i < tabOfSumInLines.length; i++) {
			if (minLatency > tabOfSumInLines[i]){
				min = i;
				minLatency = tabOfSumInLines[min];
			}
		}
		
		/*
		 * return the replicas Node Id	
		 */
		return replicasList.get(min);
	}

}
