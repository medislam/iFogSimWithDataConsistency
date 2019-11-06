package org.fog.criticalDataPourcentage;

import java.util.ArrayList;
import java.util.List;


public class CriticalData {

	public CriticalData(){
		
	}
	
	public List<Integer> getCriticalData(List<String> list, int pourcentage){
		List<Integer> critic = new ArrayList<Integer>();
		System.out.println("All Data :"+list.size()+"\t%"+pourcentage);
		
		List<Integer> all = new ArrayList<Integer>();
		for (int j = 0; j < list.size(); j++) {
			all.add(j);
		}
		
		int nb_elem = (list.size() * pourcentage) / 100;
		
		while (nb_elem > 0){
			nb_elem--;
			int index = (int) (Math.random() * all.size());
			critic.add(all.get(index));
			all.remove(index);
		}
		System.out.println("Critical data:"+critic.size());
		System.out.println("Critical data:"+critic.toString());
		return critic;
	}

}
