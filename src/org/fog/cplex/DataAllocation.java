package org.fog.cplex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fog.application.Application;
import org.fog.examples.DataPlacement;
import org.fog.jni.GraphPartitioning;
import org.fog2.entities.FogDevice;

public class DataAllocation {
	//public static Map<String, Integer> dataPlacementMap = new HashMap<String, Integer>();
	public Map<String, List<Integer>> dataPlacementMap = new HashMap<String, List<Integer>>();

	public DataAllocation() throws FileNotFoundException {
		// TODO Auto-generated constructor stub
		System.out.println("\nConstruct the Data Allocation Map");
	}
	
	public void setDataPlacementMap(int nb_HGW, Application application) throws FileNotFoundException{
		FileReader fichier = new FileReader(nb_HGW+"Solution_"+DataPlacement.nb_DataCons_By_DataProd+".txt");
		BufferedReader in = null;
		try{
			in = new BufferedReader(fichier);
			String line =null;
			int row = 0;
			List<Integer> positions = null;
			
			while((line = in.readLine()) != null){
				String[] splited = line.split("\t");
				int col = 0;
				for(String val : splited){
					if(val.equals("1.0")){
						if(row >= (DataPlacement.nb_Service_HGW + DataPlacement.nb_Service_LPOP)){
							//TempRPOP
							int index = row - DataPlacement.nb_Service_HGW -DataPlacement.nb_Service_LPOP;
							if(dataPlacementMap.containsKey("TempRPOP"+index)){
								positions = dataPlacementMap.get("TempRPOP"+index);
							}else{
								positions = new ArrayList<Integer>();
							}
							positions.add(getFogDeviceIndex(col));
							dataPlacementMap.put("TempRPOP"+index, positions);
							
						}else if(row >= DataPlacement.nb_Service_HGW){
							//TempLPOP
							int index = row - DataPlacement.nb_Service_HGW;
							if(dataPlacementMap.containsKey("TempLPOP"+index)){
								positions = dataPlacementMap.get("TempLPOP"+index);
							}else{
								positions = new ArrayList<Integer>();
							}
							positions.add(getFogDeviceIndex(col));
							dataPlacementMap.put("TempLPOP"+index, positions);

//							System.out.println("TempLPOP"+index+"\t--> col:"+col);
//							System.out.println("TempLPOP"+index+"\t--> FogDeviceIndex:"+getFogDeviceIndex(col));
						}else if(row < DataPlacement.nb_Service_HGW){
							//TempHGW
							if(dataPlacementMap.containsKey("TempHGW"+row)){
								positions = dataPlacementMap.get("TempHGW"+row);
							}else{
								positions = new ArrayList<Integer>();
							}
							positions.add(getFogDeviceIndex(col));
							dataPlacementMap.put("TempHGW"+row, positions);
							
						}else{
							System.out.println("error on DataAllocation!");
							System.exit(0);
						}
					}
					col++;
				}
				row++;
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setDataPlacementMap(String tupleType, List<Integer> listofreplica){
		
		dataPlacementMap.put(tupleType, listofreplica);
	}

	private boolean willBeGenerated(String tupleType, Application application) {
		// TODO Auto-generated method stub
		if(application.mapSelectivity.containsKey(tupleType)){
    		//System.out.println("contains:"+key);
    		
    		if(application.mapSelectivity.get(tupleType).get(0).contains("HGW")){
    			return true;
    		}else{
    			if(application.mapSelectivity.containsKey(application.mapSelectivity.get(tupleType).get(0))){
	    			return true;
	    		}else
	    			return false;
    		}
		}
		
		return false;
	}

	private Integer getFogDeviceIndex(int col) {	
		if(col >= (DataPlacement.nb_HGW+DataPlacement.nb_LPOP+DataPlacement.nb_RPOP)){
			//datacenter 
			return col - DataPlacement.nb_HGW - DataPlacement.nb_LPOP - DataPlacement.nb_RPOP +3;
			
		}else if(col >= (DataPlacement.nb_HGW + DataPlacement.nb_LPOP)){
			//RPOP
			return col - DataPlacement.nb_HGW - DataPlacement.nb_LPOP +3 + DataPlacement.nb_DC;
			
		}else if(col >= (DataPlacement.nb_HGW)){
			//LPOP
			return col - DataPlacement.nb_HGW + 3 + DataPlacement.nb_DC + DataPlacement.nb_RPOP;
			
		}else if(col < (DataPlacement.nb_HGW)){
			return col + 3 + DataPlacement.nb_DC + DataPlacement.nb_RPOP + DataPlacement.nb_LPOP;
		}
		System.out.println("Error on data Allocation");
		System.exit(0);
		return -1;
	}
	
	public void setDataAllocationMapInZone(int nb_HGW,int zone, Map<String, List<Integer>> zoneDevices, Application application) throws FileNotFoundException{
		FileReader fichier = new FileReader(nb_HGW+"SolutionZone"+zone+"_"+DataPlacement.nb_DataCons_By_DataProd+".txt");
		BufferedReader in = null;
		try{
			in = new BufferedReader(fichier);
			String line =null;
			int row = 0;
	
			int nb_Extern_tuple_cons = 0;
			List<Integer> positions=null;
			while((line = in.readLine()) != null){
				String[] splited = line.split("\t");
				int col = 0;
				boolean cond = false;
				for(String val : splited){
					if(val.equals("1.0")){
						cond = true;
						if(row >= (zoneDevices.get("hgws").size()+ zoneDevices.get("lpops").size())){
						//if(row >= (DataPlacement.nb_Service_HGW + DataPlacement.nb_Service_LPOP)){
							//TempRPOP
							int index = (row - zoneDevices.get("hgws").size() - zoneDevices.get("lpops").size()) + zoneDevices.get("rpops").size()*zone;
							
							if(dataPlacementMap.containsKey("TempRPOP"+index)){
								positions = dataPlacementMap.get("TempRPOP"+index);
							}else{
								positions = new ArrayList<Integer>();
							}
							positions.add(application.getFogDeviceByName("RPOP"+index).getId());
							dataPlacementMap.put("TempRPOP"+index, positions);
							
							
							//dataAllocationMap.put("TempRPOP"+index, getFogDeviceIndex(col,zone, zoneDevices));
							//dataAllocationMap.put("TempRPOP"+index, index+DataPlacement.nb_DC+3);	
						
							//System.out.println("Set TempRPOP"+index+"   "+getFogDeviceIndex(col,zone, zoneDevices));
							
						}else if(row >= zoneDevices.get("hgws").size()){
							//TempLPOP
							int index = (row - zoneDevices.get("hgws").size()) + zoneDevices.get("lpops").size()*zone ;
							
							if(checkConsInZone("TempLPOP"+index, zone, zoneDevices, application)){
								if(dataPlacementMap.containsKey("TempLPOP"+index)){
									positions = dataPlacementMap.get("TempLPOP"+index);
								}else{
									positions = new ArrayList<Integer>();
								}
								positions.add(getFogDeviceIndex(col,zone, zoneDevices));
								dataPlacementMap.put("TempLPOP"+index, positions);
									
							}else{
								
								if(dataPlacementMap.containsKey("TempLPOP"+index)){
									positions = dataPlacementMap.get("TempLPOP"+index);
								}else{
									positions = new ArrayList<Integer>();
								}
								positions.add(application.getFogDeviceByName("LPOP"+index).getParentId());
								dataPlacementMap.put("TempLPOP"+index, positions);
																
								nb_Extern_tuple_cons++;
							}
							//System.out.println("Set TempLPOP"+index+"   "+getFogDeviceIndex(col,zone, zoneDevices));
							
						}else if(row < zoneDevices.get("hgws").size()){
							//TempHGW
							int index = row + zoneDevices.get("hgws").size()*zone;
							
							if(checkConsInZone("TempHGW"+index, zone, zoneDevices, application)){
								if(dataPlacementMap.containsKey("TempHGW"+index)){
									positions = dataPlacementMap.get("TempHGW"+index);
								}else{
									positions = new ArrayList<Integer>();
								}
								positions.add(getFogDeviceIndex(col,zone, zoneDevices));
								dataPlacementMap.put("TempHGW"+index, positions);
								
								//System.out.println("Set TempHGW"+index+"   "+getFogDeviceIndex(col,zone, zoneDevices));
							}else{
								if(dataPlacementMap.containsKey("TempHGW"+index)){
									positions = dataPlacementMap.get("TempHGW"+index);
								}else{
									positions = new ArrayList<Integer>();
								}
								positions.add(application.getFogDeviceById(application.getFogDeviceByName("HGW"+index).getParentId()).getParentId());
								dataPlacementMap.put("TempHGW"+index, positions);

								nb_Extern_tuple_cons++;
							}
							
							
							
						}else{
							System.out.println("error on DataAllocation!");
							System.exit(0);
						}
					}
					col++;
				}
				
				row++;
			}
			
			System.out.println("Zone : "+zone+"\tnb_extern tupe cons : "+nb_Extern_tuple_cons);
			
			DataPlacement.nb_externCons =+ nb_Extern_tuple_cons;

			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean checkConsInZone(String tupleType, int zone, Map<String, List<Integer>> zoneDevices, Application application) {
		// TODO Auto-generated method stub
		List<String> destination = application.getDataConsIndexOfDataProd(tupleType);
		for(String dest : destination){
			if(dest.startsWith("serviceDC")){
				return false;
			
			}else if(dest.startsWith("serviceRPOP")){
				int devIndex = Integer.valueOf(dest.substring(11));
				int  devId = application.getFogDeviceByName("RPOP"+devIndex).getId();
				if(!zoneDevices.get("rpops").contains(devId))
					return false;
				
			}else if(dest.startsWith("serviceLPOP")){
				int devIndex = Integer.valueOf(dest.substring(11));
				int  devId = application.getFogDeviceByName("LPOP"+devIndex).getId();
				if(!zoneDevices.get("lpops").contains(devId))
					return false;
			}
			
		}

		return true;
	}

	private Integer getFogDeviceIndex(int col, int zone, Map<String, List<Integer>> zoneDevices) {	
		if(col >= (zoneDevices.get("hgws").size() + zoneDevices.get("lpops").size())){
			//RPOP
			return col + 3 + DataPlacement.nb_DC +zoneDevices.get("rpops").size()*zone - zoneDevices.get("hgws").size()- zoneDevices.get("lpops").size() ;
			
		}else if(col >= (zoneDevices.get("hgws").size())){
			//LPOP
			return col + 3 + DataPlacement.nb_DC + DataPlacement.nb_RPOP + zoneDevices.get("lpops").size() * zone - zoneDevices.get("hgws").size();
			
		}else if(col < (zoneDevices.get("hgws").size())){
			//HGW
			return col + 3 + DataPlacement.nb_DC + DataPlacement.nb_RPOP + DataPlacement.nb_LPOP + zoneDevices.get("hgws").size()*zone;
		}
		System.out.println("Error on data Allocation");
		System.exit(0);
		return -1;
	}
	
	public void printDataAllocationMap(Application application){
		System.out.println("Print allocation Map:");
		
		for(String key : dataPlacementMap.keySet()){
			System.out.print(key+"   ->   ");
			for(int device : dataPlacementMap.get(key)){
				//System.out.print(application.getFogDeviceById(device).getName()+"\t");
				System.out.print(device+"\t");
			}
			System.out.println();
		}		
	}
	
	public String dataAllocationStats(Application application){
		int cptHGW =0, cptLPOP=0, cptRPOP=0, cptDC=0;
		String stats = new String();
		
		for(String key : dataPlacementMap.keySet()){
			for(int device : dataPlacementMap.get(key)){
				if(application.getFogDeviceById(device).getName().contains("RPOP")){
					cptRPOP++;
				}else if(application.getFogDeviceById(device).getName().contains("LPOP")){
					cptLPOP++;
				}else if(application.getFogDeviceById(device).getName().contains("HGW")){
					cptHGW++;
				}else if(application.getFogDeviceById(device).getName().contains("DC")){
					cptDC++;
				}
			}
		}
		
		stats = "Total data map:"+dataPlacementMap.size();
		stats = stats + "\nNB_ DC:"+cptDC;
		stats = stats +"\nNB_ RPOP:"+cptRPOP;
		stats = stats +"\nNB_ LPOP:"+cptLPOP;
		stats = stats +"\nNB_ HGW:"+cptHGW;
		
		return stats;
	}

	public void setDataAllocationMapInPartition(int nb_HGW,int partition, GraphPartitioning gp, Application application) throws FileNotFoundException{
		System.out.println("Set allocation map of partition:"+partition);
		
		FileReader fichier = new FileReader(nb_HGW+"SolutionPartition"+partition+"_"+DataPlacement.nb_DataCons_By_DataProd+".txt");
		BufferedReader in = null;
		
		List<Integer> HGW_list = gp.getHGWInPartition(partition);
		List<Integer> LPOP_list = gp.getLPOPInPartition(partition);
		List<Integer> RPOP_list = gp.getRPOPInPartition(partition);
		List<Integer> DC_list = gp.getDCInPartition(partition);
		
		
		
		try{
			in = new BufferedReader(fichier);
			String line =null;
			int row = 0;
			int nb_extern_tuple_cons =0;
	
			while((line = in.readLine()) != null){
				String[] splited = line.split("\t");
				int col = 0;
				boolean cond = false;
				List<Integer> positions = null;				
				for(String val : splited){
					if(val.equals("1.0")){
						cond = true;
						if(row >= (HGW_list.size()+ LPOP_list.size())){
							//TempRPOP
							int index = RPOP_list.get(row - HGW_list.size() - LPOP_list.size()) - DataPlacement.nb_DC;
							//dataAllocationMap.put("TempRPOP"+index, getFogDeviceIndexInPartition(col,partition, gp));
							//System.out.println("Set TempRPOP"+index+"   "+getFogDeviceIndex(col,zone, zoneDevices));
							
							if(checkConsInPartition("TempRPOP"+index, application, partition, HGW_list, LPOP_list, RPOP_list, DC_list)){
								if(dataPlacementMap.containsKey("TempRPOP"+index)){
									positions = dataPlacementMap.get("TempRPOP"+index);
								}else{
									positions = new ArrayList<Integer>();
								}
								positions.add(getFogDeviceIndexInPartition(col, partition, gp));
								dataPlacementMap.put("TempRPOP"+index, positions);
							
							}else{
								if(dataPlacementMap.containsKey("TempRPOP"+index)){
									positions = dataPlacementMap.get("TempRPOP"+index);
								}else{
									positions = new ArrayList<Integer>();
								}
								positions.add(application.getFogDeviceByName("RPOP"+index).getId());
								dataPlacementMap.put("TempRPOP"+index, positions);

								nb_extern_tuple_cons++;
							}
							
						}else if(row >= HGW_list.size()){
							//TempLPOP
							int index = LPOP_list.get(row- HGW_list.size()) - DataPlacement.nb_DC - DataPlacement.nb_RPOP;
							//dataAllocationMap.put("TempLPOP"+index, getFogDeviceIndexInPartition(col,partition, gp));
							//System.out.println("Set TempLPOP"+index+"   "+getFogDeviceIndex(col,zone, zoneDevices));
							
							if(checkConsInPartition("TempLPOP"+index, application, partition, HGW_list, LPOP_list, RPOP_list, DC_list)){
								if(dataPlacementMap.containsKey("TempLPOP"+index)){
									positions = dataPlacementMap.get("TempLPOP"+index);
								}else{
									positions = new ArrayList<Integer>();
								}
								positions.add(getFogDeviceIndexInPartition(col, partition, gp));
								dataPlacementMap.put("TempLPOP"+index, positions);

							}else{
								if(dataPlacementMap.containsKey("TempLPOP"+index)){
									positions = dataPlacementMap.get("TempLPOP"+index);
								}else{
									positions = new ArrayList<Integer>();
								}
								positions.add(application.getFogDeviceByName("LPOP"+index).getParentId());
								dataPlacementMap.put("TempLPOP"+index, positions);

								nb_extern_tuple_cons++;
							}
							
							
						}else if(row < HGW_list.size()){
							//TempHGW
							int index = HGW_list.get(row) - DataPlacement.nb_DC - DataPlacement.nb_RPOP - DataPlacement.nb_LPOP;
							
							//dataAllocationMap.put("TempHGW"+index, getFogDeviceIndexInPartition(col, partition, gp));
							//System.out.println("Set TempHGW"+index+"   "+getFogDeviceIndex(col,zone, zoneDevices));
							
							if(checkConsInPartition("TempHGW"+index, application, partition, HGW_list, LPOP_list, RPOP_list, DC_list)){
								if(dataPlacementMap.containsKey("TempHGW"+index)){
									positions = dataPlacementMap.get("TempHGW"+index);
								}else{
									positions = new ArrayList<Integer>();
								}
								positions.add(getFogDeviceIndexInPartition(col, partition, gp));
								dataPlacementMap.put("TempHGW"+index, positions);
							}else{
								if(dataPlacementMap.containsKey("TempHGW"+index)){
									positions = dataPlacementMap.get("TempHGW"+index);
								}else{
									positions = new ArrayList<Integer>();
								}
								positions.add(application.getFogDeviceById(application.getFogDeviceByName("HGW"+index).getParentId()).getParentId());
								dataPlacementMap.put("TempHGW"+index, positions);
								//dataAllocationMap.put("TempHGW"+index, application.getFogDeviceByName("HGW"+index).getParentId());
								nb_extern_tuple_cons++;
							}
							
						}else{
							System.out.println("error on DataAllocation!");
							System.exit(0);
						}
					}
					col++;
				}
				
				row++;
			}
			System.out.println("Partition:"+partition+"\t nb_Extern tupe Cons :"+nb_extern_tuple_cons);
			DataPlacement.nb_externCons =+ nb_extern_tuple_cons;
			
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean checkConsInPartition(String tupleType, Application application,
			int partition, List<Integer> hGW_list, List<Integer> lPOP_list,
			List<Integer> rPOP_list, List<Integer> dC_list) {
		// TODO Auto-generated method stub
		
		List<String> destination = application.getDataConsIndexOfDataProd(tupleType);
		int devId, devIndex;
		int nb_Ext_cons = 0;
		if(tupleType.startsWith("TempHGW")){
			
			for(String dest : destination){
				if(dest.startsWith("serviceHGW")){
					devIndex = Integer.valueOf(dest.substring(10));
					devId = application.getFogDeviceByName("HGW"+devIndex).getId();
					if(!hGW_list.contains(devId-3))
						nb_Ext_cons++;
						//return false;
					
				}else if(dest.startsWith("serviceLPOP")){
					devIndex = Integer.valueOf(dest.substring(11));
					devId = application.getFogDeviceByName("LPOP"+devIndex).getId();
					if(!lPOP_list.contains(devId-3))
						nb_Ext_cons++;
						//return false;
					
				}else if(dest.startsWith("serviceRPOP")){
					devIndex = Integer.valueOf(dest.substring(11));
					devId = application.getFogDeviceByName("RPOP"+devIndex).getId();
					if(!rPOP_list.contains(devId-3))
						nb_Ext_cons++;
						//return false;
					
				}else if(dest.startsWith("serviceDC")){
					devIndex = Integer.valueOf(dest.substring(9));
					devId = application.getFogDeviceByName("DC"+devIndex).getId();
					if(!dC_list.contains(devId-3))
						nb_Ext_cons++;
						//return false;
				}
			}
			
		}else if(tupleType.startsWith("TempLPOP")){
			for(String dest : destination){
				if(dest.startsWith("serviceLPOP")){
					devIndex = Integer.valueOf(dest.substring(11));
					devId = application.getFogDeviceByName("LPOP"+devIndex).getId();
					if(!lPOP_list.contains(devId-3))
						nb_Ext_cons++;
						//return false;
					
				}else if(dest.startsWith("serviceRPOP")){
					devIndex = Integer.valueOf(dest.substring(11));
					devId = application.getFogDeviceByName("RPOP"+devIndex).getId();
					if(!rPOP_list.contains(devId-3))
						nb_Ext_cons++;
						//return false;
					
				}else if(dest.startsWith("serviceDC")){
					devIndex = Integer.valueOf(dest.substring(9));
					devId = application.getFogDeviceByName("DC"+devIndex).getId();
					if(!dC_list.contains(devId-3))
						nb_Ext_cons++;
						//return false;
				}
			}
			
		}else if(tupleType.startsWith("TempRPOP")){
			for(String dest : destination){
				devIndex = Integer.valueOf(dest.substring(9));
				devId = application.getFogDeviceByName("DC"+devIndex).getId();
				if(!dC_list.contains(devId-3))
					nb_Ext_cons++;
					//return false;
			}
		}
		
		if((nb_Ext_cons*2)>= DataPlacement.nb_DataCons_By_DataProd)
			return false;

		return true;

	}

	public void setDataAllocationMapInPartitionNullDataCons(int partition, GraphPartitioning gp){
		List<Integer> HGW_list = gp.getHGWInPartition(partition);
		List<Integer> positions = null;
		for(int hgwId : HGW_list){
			int index = hgwId - DataPlacement.nb_DC - DataPlacement.nb_RPOP - DataPlacement.nb_LPOP;
			if(dataPlacementMap.containsKey("TempHGW"+index)){
				positions = dataPlacementMap.get("TempHGW"+index);
			}else{
				positions = new ArrayList<Integer>();
			}
			positions.add(hgwId+3);
			dataPlacementMap.put("TempHGW"+index, positions);
		}		
	}

	private Integer getFogDeviceIndexInPartition(int col, int partition, GraphPartitioning gp) {
		// TODO Auto-generated method stub
		
		List<Integer> HGW_list = gp.getHGWInPartition(partition);
		List<Integer> LPOP_list = gp.getLPOPInPartition(partition);
		List<Integer> RPOP_list = gp.getRPOPInPartition(partition);
		List<Integer> DC_list = gp.getDCInPartition(partition);
		
		int indexCol = -1;
		if(col >= (HGW_list.size() + LPOP_list.size()+ RPOP_list.size())){
			//DC
			indexCol = col - HGW_list.size()- LPOP_list.size()- RPOP_list.size();
			return DC_list.get(indexCol)+3;
			
		}else if(col >= (HGW_list.size() + LPOP_list.size())){
			//RPOP
			indexCol = col - HGW_list.size()- LPOP_list.size();
			return RPOP_list.get(indexCol)+3;
			
		}else if(col >= (HGW_list.size())){
			//LPOP
			indexCol = col - HGW_list.size();
			return LPOP_list.get(indexCol)+3;
			
		}else if(col < (HGW_list.size())){
			indexCol = col;
			return HGW_list.get(indexCol)+3;
		}
		System.out.println("Error on data Allocation");
		System.exit(0);
		return -1;
	}
	
	public List<Integer> getEmplacementNodeId(String tupleType){
		if(dataPlacementMap.containsKey(tupleType))
			return dataPlacementMap.get(tupleType);
		else {
			System.out.println("Error ! "+tupleType+" doen'ts exist in data placement map!");
			System.exit(0);	
		}
		return null;
	}

	public void createStorageListIneachStroageNode(Application application){
		for(String tupleType : dataPlacementMap.keySet()){
			for(int hostId: dataPlacementMap.get(tupleType)){
				FogDevice fogdev = application.getFogDeviceById(hostId);
				fogdev.addStoredData(tupleType, null);
			}
		}
	}
	
	
	public void loadDataAllocationMap() throws IOException {
		// System.out.println("Loading application Edges");
		FileReader fichier = new FileReader(DataPlacement.nb_HGW+"dataAllocationMap"+DataPlacement.nb_DataCons_By_DataProd+ ".txt");
		BufferedReader in = null;
		try {

			in = new BufferedReader(fichier);
			String line = null;

			while ((line = in.readLine()) != null) {
				String tupleType;
				List<Integer> emplacement = new ArrayList<Integer>();
				

				String[] splited = line.split("\t");
				tupleType = splited[0];
				
				for(int i=1; i<splited.length;i++){
					emplacement.add(Integer.valueOf(splited[i]));
				}

				dataPlacementMap.put(tupleType, emplacement);

			}
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveDataAllocationMap()throws IOException {
		// System.out.println("Generating Basis Latency file ...");
		FileWriter fichier;
		
		fichier = new FileWriter(DataPlacement.nb_HGW+"dataAllocationMap" +DataPlacement.nb_DataCons_By_DataProd+ ".txt");
		

		try {
			BufferedWriter fw = new BufferedWriter(fichier);
			
			for (String key: dataPlacementMap.keySet()) {
				fw.write(key+"\t");
				for (int rep : dataPlacementMap.get(key)) {
					fw.write(String.valueOf(rep)+"\t");
				}
				fw.write("\n");
			}
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
