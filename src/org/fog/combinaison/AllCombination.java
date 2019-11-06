package org.fog.combinaison;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.fog.pmedian.Pmedian;

public class AllCombination {
	
		
	/*
	 * Map<Pair<Tupletype, Pair<nbReplicas, combinNum>>, List<replicas>>
	 */
	private Map<Pair<String, Pair<Integer,Integer>>, List<Integer>> combinaisonMap;
	private int nbMaxReplicas;
	private int nbMinReplicas;
	private Map<String, List<Integer>> dataMap;
	public long nb_combinaison = 0;
	
	public AllCombination(int nbMinReplicas, int nbMaxReplicas, Map<String, List<Integer>> dataMap){
		this.nbMaxReplicas = nbMaxReplicas;
		this.nbMinReplicas = nbMinReplicas;
		this.dataMap = dataMap;
		this.combinaisonMap = new HashMap<Pair<String, Pair<Integer,Integer>>, List<Integer>>();
	}
		
	public Map<Pair<String, Pair<Integer,Integer>>,List<Integer>> getCombinaisonMap(){
		return this.combinaisonMap;
	}

	public void setCombinaisonMap(){
		
		
		//for(String tupleType : Pmedian.shortestPathNodesMap.keySet()){
		for(String tupleType : dataMap.keySet()){
			//System.out.println("Compute Combin for tuple:"+tupleType);
			List <Integer> listOfNodes = dataMap.get(tupleType);
			//System.out.println("listOfAllShortestPathNodes:"+listOfAllShortestPathNodes);
		
			int max = (nbMaxReplicas < listOfNodes.size())  ?  nbMaxReplicas : listOfNodes.size();
			int min = (nbMinReplicas < listOfNodes.size())  ?  nbMinReplicas : listOfNodes.size();
			
			//System.out.println("Max repl :"+max+"\tMin repl :"+min);
			// for all selection replicas number 1 to max
			for (int p = min; p < max+1; p++) {
				Combinaison comb = new Combinaison();
				int num_comb = 0;
				//System.out.println("nb_replicas = "+p);
				for(List<Integer> l : comb.getAllCombinaison(p, listOfNodes)){
					
					Pair<Integer, Integer> pair1 = new Pair<Integer, Integer>(p, num_comb);
					Pair<String, Pair<Integer,Integer>> pair2 = new Pair<String, Pair<Integer,Integer>>(tupleType,pair1);
					//System.out.print(l+",");
					combinaisonMap.put(pair2, l);
					num_comb++;
				}
				nb_combinaison += num_comb;
				//System.out.println();
				
			}
		}
		
		//System.out.println("End setCombinaisonMap");
	}
	
	public void printCombinaisonMap(){
		System.out.println("Print All Combinaison Map");
		
		for(Pair<String, Pair<Integer,Integer>> pair2 : combinaisonMap.keySet()){
			String tupleType = pair2.getFirst();
			Pair<Integer,Integer> pair1 = pair2.getSecond();
			List<Integer> list = combinaisonMap.get(pair2);
			
			System.out.println("Tuple : "+tupleType+"\t Nb Replicas : "+pair1.getFirst());
			System.out.println("Num combin :"+pair1.getSecond()+"\t Comb : "+list.toString());
		}
		
	}
	
	
}
