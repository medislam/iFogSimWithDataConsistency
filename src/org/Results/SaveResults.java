package org.Results;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.fog.examples.DataPlacement;
import org.fog.stats.LatencyStats;
import org.fog.stats.Stats;


/**
 * 
 * @author islam
 * this class contains methods to save various simulations results as latency time, cost, energy and so on
 * 
 *
 */


public class SaveResults {
	
	public SaveResults() {
		
	}
	
	public static void saveLatencyTimes(int dataConsPerDataProd, String storageMode, String tupleType, int nb_median,double write, double read, double delayed, double version, double blocked,
			double overall) throws IOException {
		saveWriteLatency(dataConsPerDataProd, storageMode, write, tupleType, nb_median);
		saveReadLatency( dataConsPerDataProd,  storageMode,  read, tupleType, nb_median);
		saveVersionLatency( dataConsPerDataProd,  storageMode,  version, tupleType, nb_median);
		saveOverallLatency( dataConsPerDataProd,  storageMode,  overall, tupleType, nb_median);
		saveDelayedWriteLatency( dataConsPerDataProd,  storageMode,  delayed, tupleType, nb_median);
		saveBlockedWriteLatency( dataConsPerDataProd,  storageMode,  blocked, tupleType, nb_median);
		
	}
	
	public static void saveWriteLatency(int dataConsPerDataProd, String storageMode, double write, String tupleType, int nb_median) throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_write_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/latency/"+ DataPlacement.nb_HGW+"_write_latency"+DataPlacement.nb_DataCons_By_DataProd+"_"+nb_median, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			
			//fw.write(write + "\n");
			
			fw.write("tuple:"+tupleType+"\tnb_medina:"+nb_median+"\twrite:"+write + "\n");

			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveReadLatency(int dataConsPerDataProd, String storageMode, double read, String tupleType, int nb_median) throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_Read_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/latency/"+ DataPlacement.nb_HGW+"_Read_latency"+DataPlacement.nb_DataCons_By_DataProd+"_"+nb_median, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			
			//fw.write(read + "\n");
			
			fw.write("tuple:"+tupleType+"\tnb_medina:"+nb_median+"\twrite:"+read + "\n");

			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveVersionLatency(int dataConsPerDataProd, String storageMode, double version, String tupleType, int nb_median) throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_Version_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/latency/"+ DataPlacement.nb_HGW+"_Version_latency"+DataPlacement.nb_DataCons_By_DataProd+"_"+nb_median, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			
			//fw.write(version + "\n");
			
			fw.write("tuple:"+tupleType+"\tnb_medina:"+nb_median+"\twrite:"+version + "\n");

			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveOverallLatency(int dataConsPerDataProd, String storageMode, double overall, String tupleType, int nb_median) throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_Overall_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/latency/"+ DataPlacement.nb_HGW+"_Overall_latency"+DataPlacement.nb_DataCons_By_DataProd+"_"+nb_median, true);

		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			
			//fw.write(overall + "\n");
			
			fw.write("tuple:"+tupleType+"\tnb_medina:"+nb_median+"\twrite:"+overall + "\n");

			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveDelayedWriteLatency(int dataConsPerDataProd, String storageMode, double delayed, String tupleType, int nb_median) throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_delayed_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/latency/"+DataPlacement.nb_HGW+"_delayed_latency"+DataPlacement.nb_DataCons_By_DataProd+"_"+nb_median, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			
			//fw.write(delayed + "\n");
			
			fw.write("tuple:"+tupleType+"\tnb_medina:"+nb_median+"\twrite:"+delayed + "\n");

			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveBlockedWriteLatency(int dataConsPerDataProd, String storageMode, double blocked, String tupleType, int nb_median) throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_delayed_latency"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/latency/"+DataPlacement.nb_HGW+"_blocked_latency"+DataPlacement.nb_DataCons_By_DataProd+"_"+nb_median, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			
			//fw.write(blocked + "\n");
			
			fw.write("tuple:"+tupleType+"\tnb_medina:"+nb_median+"\twrite:"+blocked + "\n");

			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void saveAllStats() throws IOException {
		
		
		FileWriter fichier_allStats = new FileWriter("Stats/iFogStor/data/"+DataPlacement.nb_HGW+"fichier_allStats_Global"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		BufferedWriter fw_fichier_allStats = new BufferedWriter(fichier_allStats);
		
		try{

			fw_fichier_allStats.write("Consistency protocole = \t"+DataPlacement.Consistencyprotocol+"\n\n");
			
			fw_fichier_allStats.write("write_latency = \t"+LatencyStats.getOverall_write_Latency()+"\n");
			fw_fichier_allStats.write("read_latency = \t"+LatencyStats.getOverall_read_Latency()+"\n");
			fw_fichier_allStats.write("version_latency = \t"+Stats.version_exchange_latency+"\n");
			fw_fichier_allStats.write("overall_latency = \t"+LatencyStats.getOverall_Latency()+"\n");
			fw_fichier_allStats.write("delayed_latency = \t"+LatencyStats.getOverall_delayed_write_Latency()+"\n");
			fw_fichier_allStats.write("blocked_latency = \t"+LatencyStats.getOverall_blocked_write_Latency()+"\n");
			
			fw_fichier_allStats.write("\n");
			fw_fichier_allStats.write("\n");
			
			fw_fichier_allStats.write("total_write = \t"+Stats.nb_totol_write+"\n");
			fw_fichier_allStats.write("done_write = \t"+Stats.nb_done_write+"\n");
			fw_fichier_allStats.write("repone_write = \t"+Stats.nb_response_write+"\n");
			fw_fichier_allStats.write("blocked_write = \t"+Stats.nb_bloked_write+"\n");
			fw_fichier_allStats.write("blocked_for_blocked_write = \t"+Stats.nb_bloked_write_for_blocked_write+"\n");
			fw_fichier_allStats.write("delayed_write = \t"+Stats.nb_delayed_write+"\n");
			fw_fichier_allStats.write("locked_write = \t"+Stats.nb_locked_write+"\n");
			fw_fichier_allStats.write("replica_violation_write = \t"+Stats.nb_replica_violation_in_write+"\n");
			
			fw_fichier_allStats.write("\n");
			fw_fichier_allStats.write("\n");
			 
			fw_fichier_allStats.write("total_read = \t"+Stats.nb_totol_read+"\n");
			fw_fichier_allStats.write("served_read = \t"+Stats.nb_served_read+"\n");
			fw_fichier_allStats.write("non_served_read = \t"+Stats.nb_non_served_read+"\n");
			fw_fichier_allStats.write("replica_vilation_read = \t"+Stats.nb_replica_violation_in_read+"\n");
			
			fw_fichier_allStats.write("read_served_with_recent_version   = \t"+Stats.nb_read_served_with_recent_version+"\n");
			fw_fichier_allStats.write("nb_read_served_with_version_old_1 = \t"+Stats.nb_read_served_with_version_old_1+"\n");
			fw_fichier_allStats.write("nb_read_served_with_version_old_2 = \t"+Stats.nb_read_served_with_version_old_2+"\n");
			fw_fichier_allStats.write("nb_read_served_with_version_old_3 = \t"+Stats.nb_read_served_with_version_old_3+"\n");
			fw_fichier_allStats.write("nb_read_served_with_version_old_4 = \t"+Stats.nb_read_served_with_version_old_4+"\n");
			fw_fichier_allStats.write("nb_read_served_with_version_old_5 = \t"+Stats.nb_read_served_with_version_old_5+"\n");
			fw_fichier_allStats.write("nb_read_served_with_version_old_up5 = \t"+Stats.nb_read_served_with_version_old_up5+"\n");
			fw_fichier_allStats.write("======================================================================================\n");
			
			fw_fichier_allStats.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		
//		saveNbTotaleWrite();
//		saveNbDoneWrite();
//		saveNbResponseWrite();
//		saveNbBlockedWrite();
//		saveNbBlockedWriteForBlockedWrite();
//		saveNbDelayedWrite();
//		saveNbLockedWrite();
//		saveNbLockedWrite();
//		saveNbTotalRead();
//		saveNbServedRead();
//		saveNbNonServedRead();
//		saveNbReplicaViolationRead();
//		saveNbReadServedWithRecentVersion();
	}
	
	public static void saveNbTotaleWrite() throws IOException {
	
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_nb_totol_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/write/"+DataPlacement.nb_HGW+"_nb_totol_write"+"_"+DataPlacement.nb_DataCons_By_DataProd+"_"+1, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			fw.write(Stats.nb_totol_write +"\n");
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveNbDoneWrite() throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_nb_done_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/write/"+DataPlacement.nb_HGW+"_nb_done_write"+"_"+DataPlacement.nb_DataCons_By_DataProd+"_"+1, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			fw.write(Stats.nb_done_write +"\n");
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveNbResponseWrite() throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_nb_response_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/write/"+DataPlacement.nb_HGW+"_nb_response_write"+"_"+DataPlacement.nb_DataCons_By_DataProd+"_"+1, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			fw.write(Stats.nb_response_write +"\n");
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveNbBlockedWrite() throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_nb_bloked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/write/"+DataPlacement.nb_HGW+"_nb_bloked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd+"_"+1, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			fw.write(Stats.nb_bloked_write +"\n");
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveNbBlockedWriteForBlockedWrite() throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_nb_bloked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/write/"+DataPlacement.nb_HGW+"_nb_bloked_write_for_blocked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd+"_"+1, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			fw.write(Stats.nb_bloked_write_for_blocked_write +"\n");
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveNbDelayedWrite() throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_nb_delayed_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/write/"+DataPlacement.nb_HGW+"_nb_delayed_write"+"_"+DataPlacement.nb_DataCons_By_DataProd+"_"+1, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			fw.write(Stats.nb_delayed_write+"\n");
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveNbLockedWrite() throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_nb_locked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/write/"+DataPlacement.nb_HGW+"_nb_locked_write"+"_"+DataPlacement.nb_DataCons_By_DataProd+"_"+1, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			fw.write(Stats.nb_locked_write+"\n");
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveNbReplicaViolationInWrite() throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_nb_replica_violation_in_write"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/write/"+DataPlacement.nb_HGW+"_nb_replica_violation_in_write"+"_"+DataPlacement.nb_DataCons_By_DataProd+"_"+1, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			fw.write(Stats.nb_replica_violation_in_write+"\n");
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveNbTotalRead() throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_nb_totol_read"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/read/"+DataPlacement.nb_HGW+"_nb_totol_read"+"_"+DataPlacement.nb_DataCons_By_DataProd+"_"+1, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			fw.write(Stats.nb_totol_read+"\n");
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveNbServedRead() throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_nb_served_read"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/read/"+DataPlacement.nb_HGW+"_nb_served_read"+"_"+DataPlacement.nb_DataCons_By_DataProd+"_"+1, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			fw.write(Stats.nb_served_read+"\n");
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveNbNonServedRead() throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_nb_served_read"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/read/"+DataPlacement.nb_HGW+"_nb_non_served_read"+"_"+DataPlacement.nb_DataCons_By_DataProd+"_"+1, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			fw.write(Stats.nb_non_served_read+"\n");
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveNbReplicaViolationRead() throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_nb_replica_violation_in_read"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/read/"+DataPlacement.nb_HGW+"_nb_replica_violation_in_read"+"_"+DataPlacement.nb_DataCons_By_DataProd+"_"+1, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			fw.write(Stats.nb_replica_violation_in_read+"\n");
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveNbReadServedWithRecentVersion() throws IOException {
		
		//FileWriter fichier = new FileWriter("Stats/"+DataPlacement.dcp+"/"+ DataPlacement.nb_HGW+"_nb_read_served_with_recent_version"+"_"+DataPlacement.nb_DataCons_By_DataProd, true);
		FileWriter fichier = new FileWriter("Stats/read/"+DataPlacement.nb_HGW+"_nb_read_served_with_recent_version"+"_"+DataPlacement.nb_DataCons_By_DataProd+"_"+1, true);
		try {
			BufferedWriter fw = new BufferedWriter(fichier);

			fw.write("recnt:\t"+Stats.nb_read_served_with_recent_version+"\n");
			fw.write("old 1:\t"+Stats.nb_read_served_with_version_old_1+"\n");
			fw.write("old 2:\t"+Stats.nb_read_served_with_version_old_2+"\n");
			fw.write("old 3:\t"+Stats.nb_read_served_with_version_old_3+"\n");
			fw.write("old 4:\t"+Stats.nb_read_served_with_version_old_4+"\n");
			fw.write("old 5:\t"+Stats.nb_read_served_with_version_old_5+"\n");
			fw.write("up 5 :\t"+Stats.nb_read_served_with_version_old_up5+"\n");
			
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
