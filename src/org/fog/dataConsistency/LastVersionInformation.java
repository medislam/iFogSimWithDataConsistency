package org.fog.dataConsistency;

public class LastVersionInformation {
	
	private String tupleType;
	private int replicaNodeId;
	private int version = -1;
	

	public LastVersionInformation(String tupleType, int replicaNodeId) {
		// TODO Auto-generated constructor stub
		setTupleType(tupleType);
		setReplicaNodeId(replicaNodeId);
	}


	public String getTupleType() {
		return tupleType;
	}


	public void setTupleType(String tupleType) {
		this.tupleType = tupleType;
	}


	public int getReplicaNodeId() {
		return replicaNodeId;
	}


	public void setReplicaNodeId(int replicaNodeId) {
		this.replicaNodeId = replicaNodeId;
	}


	public int getVersion() {
		return version;
	}


	public void setVersion(int version) {
		this.version = version;
	}

}
