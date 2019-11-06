package org.fog.combinaison;


import java.util.ArrayList;
import java.util.List;
 
public class Combinaison {
	public Combinaison(){
		
	}
	
	public List<List<Integer>> getAllCombinaison(int count, List<Integer> listAllNodes){
		//System.out.println("Get All combinaison:\tcount="+count+"\tlistNodes:"+listAllNodes);
		List<List<Integer>> combinaisons = new ArrayList<List<Integer>>();
		int[] tokens = new int [listAllNodes.size()];
		for (int i =0; i < listAllNodes.size();i++) {
			tokens[i] = listAllNodes.get(i);
		}
		
		
		Combiner combiner = new Combiner( count, tokens );
		int [] result = new int [count];
		while ( combiner.searchNext( result ) ){
			List<Integer> list = new ArrayList<Integer>();
			for (int i : result) {
				list.add(i);
			}
			//System.out.println("list:"+list.toString());
			combinaisons.add(list);
		}
		return combinaisons;
	}
 
	public class Combiner {
		protected int count;
		protected int [] array;
		protected int[] indexes;
 
		public Combiner(int count, int [] array )
		{
			super();
			this.count = count;
			this.array = array;
			indexes = new int[count];
			for ( int i = 0; i < count; i++ )
				indexes[i] = i;
		}
 
		public boolean searchNext( int[] result )
		{
			if ( indexes == null )
				return false;
 
			int resultIndex = 0;
			for ( int index : indexes )
				result[resultIndex++] = array[index];
 
			int indexesRank = count-1;
			int arrayRank = array.length-1;
			while ( indexes[indexesRank] == arrayRank )
			{
				if ( indexesRank == 0 )
				{
					indexes = null;
					return true;
				}
				indexesRank--;
				arrayRank--;
			}
 
			int restartIndex = indexes[indexesRank] + 1;
			while ( indexesRank < count )
				indexes[indexesRank++] = restartIndex++;
 
			return true;
		}
	}
}

