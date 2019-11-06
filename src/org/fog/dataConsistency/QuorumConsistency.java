package org.fog.dataConsistency;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.fog.lpFileConstuction.BasisDelayMatrix;

public class QuorumConsistency extends DataConsistency {
	
	private static int QW;
	private static int QR;
	
	public QuorumConsistency(int QW, int QR){
		this.QW = QW;
		this.QR = QR;
	}

	@Override
	public List<Integer> getReplicasListRequestForWrite(int requestNodeId, List<Integer> unLockedReplicas) {
		// TODO Auto-generated method stub
		List<Integer> list = new ArrayList<Integer>();
		
		if(unLockedReplicas.isEmpty())
			return list;
		
		int [] tabReplicas = new int [this.QW];
		float [] tabLatency = new float [this.QW];
		int nb_elem = 0;
		
		for(int rep : unLockedReplicas){
			if(nb_elem<this.QW){
				tabReplicas[nb_elem] = rep;
				tabLatency[nb_elem] = BasisDelayMatrix.getFatestLink(requestNodeId, rep);
			} else{
				if (BasisDelayMatrix.getFatestLink(requestNodeId, rep) < getMaxLatencyAndPosition(tabLatency).getFirst() ) {
					tabReplicas[getMaxLatencyAndPosition(tabLatency).getSecond()] = rep;
					tabLatency[getMaxLatencyAndPosition(tabLatency).getSecond()] = BasisDelayMatrix.getFatestLink(requestNodeId, rep);
				}
			}
			nb_elem++;
		}
		 
		for(int elem:tabReplicas)
			if(elem>0)
				list.add(elem);
		return list;
	}
	
//	@Override
//	public List<Integer> getReplicasListRequestForRead(int requestNodeId, List<Integer> unLockedReplicas) {
//		// TODO Auto-generated method stub
//		List<Integer> list = new ArrayList<Integer>();
//		
//		if(unLockedReplicas.isEmpty())
//			return list;
//		
//		int [] tabReplicas = new int [unLockedReplicas.size()];
//		float [] tabLatency = new float [unLockedReplicas.size()];
//		
//		
//		for (int i = 0; i < unLockedReplicas.size(); i++) {
//			tabReplicas[i] = unLockedReplicas.get(i);
//			tabLatency[i] = BasisDelayMatrix.getFatestLink(requestNodeId, unLockedReplicas.get(i));
//			
//		}
//		
//		for (int i = 0; i < this.QR; i++) {
//			float min = Float.MAX_VALUE;
//			int position = -1;
//			for (int j = 0; j < tabLatency.length; j++) {
//				if(tabLatency[j] < min){
//					position = j;
//					min = tabLatency[j];
//				}
//			}
//			if (position == -1) {
//				System.out.println("Error, in getReplicasListRequestForRead position = -1");
//				System.exit(0);
//			}
//			list.add(tabReplicas[position]);
//			tabLatency[position] = Float.MAX_VALUE;
//			
//		}
//		
//		
//		/*
//		 * test
//		 */
//		for(int rep : unLockedReplicas){
//			if(BasisDelayMatrix.getFatestLink(requestNodeId, rep) < BasisDelayMatrix.getFatestLink(requestNodeId, list.get(0))){
//				System.out.println("Error, in getReplicasListRequestForRead Neatest is not te nearest");
//				System.exit(0);
//			}
//		}
//		
//		return list;
//	}
	
	
	@Override
	public List<Integer> getReplicasListRequestForRead(int requestNodeId, List<Integer> unLockedReplicas) {
		// TODO Auto-generated method stub
		List<Integer> list = new ArrayList<Integer>();
		
		if(unLockedReplicas.isEmpty())
			return list;
		
		int [] tabReplicas = new int [this.QR];
		float [] tabLatency = new float [this.QR];
		int nb_elem = 0;
		
		for(int rep : unLockedReplicas){
			if(nb_elem<this.QR){
				tabReplicas[nb_elem] = rep;
				tabLatency[nb_elem] = BasisDelayMatrix.getFatestLink(requestNodeId, rep);
				
			} else{
				if (BasisDelayMatrix.getFatestLink(requestNodeId, rep) < getMaxLatencyAndPosition(tabLatency).getFirst() ) {
					tabReplicas[getMaxLatencyAndPosition(tabLatency).getSecond()] = rep;
					tabLatency[getMaxLatencyAndPosition(tabLatency).getSecond()] = BasisDelayMatrix.getFatestLink(requestNodeId, rep);
				}
			}
			nb_elem++;
		}
		
		for(int elem:tabReplicas)
			if(elem>0)
				list.add(elem);
		
			
//		for(int rep : unLockedReplicas){
//			if(BasisDelayMatrix.getFatestLink(requestNodeId, rep) < BasisDelayMatrix.getFatestLink(requestNodeId, list.get(0))){
//				System.out.println("\trequestNodeId"+requestNodeId +"\trep"+ rep +"\trequestNodeId:"+ requestNodeId +"\tlist.get(0)"+ list.get(0));
//				System.out.println(BasisDelayMatrix.getFatestLink(requestNodeId, rep)+ "<"+ BasisDelayMatrix.getFatestLink(requestNodeId, list.get(0)));
//				System.out.println("Error, in getReplicasListRequestForRead Nearest is not the nearest");
//				System.exit(0);
//			}
//		}
		return list;
	}

	@Override
	public int getNumberOfResponseWriteReplica(int nb_total_replica) {
		// TODO Auto-generated method stub
		return QW;
	}

	@Override
	public int getNumberOfResponseReadReplica(int nb_total_replica) {
		// TODO Auto-generated method stub
		return QR;
	}



}
