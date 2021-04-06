package org.fog2.entities;
//package org.fog.entities;
//
//import java.util.List;
//
//
//public class Data implements Cloneable, Comparable<Data> {
//	
//	private String tupleType;
//	private List<String> destModuleName;
//	private String srcModuleName;
//	private int sourceDeviceId;
//	private int version;
//	private long size;
//
//	public Data(String tupleTupe, List<String> destModuleName, String srcModuleName, int sourceDeviceId, int version, long size) {
//		// TODO Auto-generated constructor stub
//		this.tupleType = tupleTupe;
//		this.destModuleName = destModuleName;
//		this.srcModuleName = srcModuleName;
//		this.sourceDeviceId= sourceDeviceId;
//		this.version=version;
//		this.size=size;
//	}
//	
//	@Override
//	public String toString() {
//		return  "Data tupe:"+this.tupleType+"\t version:"+this.version+"\t destModules:"+this.destModuleName+"\t srcModule:"+this.srcModuleName;
//	}
//
//	@Override
//	public int compareTo(Data data) {
//		// TODO Auto-generated method stub
//		if (data == null) {
//			return 1;
//			
//		} else if (this.version < data.version) {
//			return -1;
//			
//		} else if (this.version > data.version) {
//			return 1;
//			
//		}else {
//			return 0;
//			
//		}
//	}
//	
//
//}
