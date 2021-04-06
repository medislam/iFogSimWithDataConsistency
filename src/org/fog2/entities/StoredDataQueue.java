package org.fog2.entities;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;


public class StoredDataQueue {

	/** The sorted set. */
	private final SortedSet<Tuple> sortedSet = new TreeSet<Tuple>();

	public void addData(Tuple newData){
		sortedSet.add(newData);
	}
	
	public boolean contains(Tuple tuple){
		if(sortedSet.contains(tuple))
			return true;
		else
			return false;
	}


	/**
	 * Returns an iterator to the queue.
	 * 
	 * @return the iterator
	 */
	public Iterator<Tuple> iterator() {
		return sortedSet.iterator();
	}

	/**
	 * Returns the size of this data queue.
	 * 
	 * @return the size
	 */
	public int size() {
		return sortedSet.size();
	}

	/**
	 * Removes the data from the queue.
	 * 
	 * @param data the Data
	 * @return true, if successful
	 */
	public boolean remove(Tuple data) {
		return sortedSet.remove(data);
	}

	/**
	 * Removes all the stored data from the queue.
	 * 
	 * @param listData the stored data
	 * @return true, if successful
	 */
	public boolean removeAll(Collection<Tuple> listData) {
		return sortedSet.removeAll(listData);
	}

	/**
	 * Clears the queue.
	 */
	public void clear() {
		sortedSet.clear();
	}

	
	

}
