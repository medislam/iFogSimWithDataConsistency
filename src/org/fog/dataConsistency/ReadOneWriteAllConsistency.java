package org.fog.dataConsistency;

import java.util.ArrayList;
import java.util.List;

import org.fog.lpFileConstuction.BasisDelayMatrix;

public class ReadOneWriteAllConsistency extends DataConsistency {

	@Override
	public List<Integer> getReplicasListRequestForWrite(int requestNodeId,List<Integer> unLockedReplicas) {
		// TODO Auto-generated method stub
		List<Integer> list = new ArrayList<Integer>(unLockedReplicas);
		return list;
				
	}

	@Override
	public List<Integer> getReplicasListRequestForRead(int requestNodeId,List<Integer> unLockedReplicas) {
		// TODO Auto-generated method stub*
		List<Integer> list = new ArrayList<Integer>();
		
		int nearestReplica = -1;
		float nearestReplicaLatency = Float.MAX_VALUE;
		
		for(int rep: unLockedReplicas){
			if(nearestReplicaLatency > BasisDelayMatrix.getFatestLink(requestNodeId, rep) ){
				nearestReplica = rep;
				nearestReplicaLatency = BasisDelayMatrix.getFatestLink(requestNodeId, rep);
			}			
		}
		
		if(nearestReplica!=-1){
			list.add(nearestReplica);
		}
		return list;
	}

	@Override
	public int getNumberOfResponseWriteReplica(int nb_total_replica) {
		// TODO Auto-generated method stub
		return nb_total_replica;
	}

	@Override
	public int getNumberOfResponseReadReplica(int nb_total_replica) {
		// TODO Auto-generated method stub
		return 1;
	}



}
