/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.StorageMode.FogStorage;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.predicates.Predicate;
import org.cloudbus.cloudsim.core.predicates.PredicateAny;
import org.cloudbus.cloudsim.core.predicates.PredicateNone;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.Tuple;
import org.fog.examples.DataPlacement;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacement;
import org.fog.stats.LatencyStats;
import org.fog.utils.FogEvents;



/**
 * This class extends the CloudSimCore to enable network simulation in CloudSim. Also, it disables
 * all the network models from CloudSim, to provide a simpler simulation of networking. In the
 * network model used by CloudSim, a topology file written in BRITE format is used to describe the
 * network. Later, nodes in such file are mapped to CloudSim entities. Delay calculated from the
 * BRITE model are added to the messages send through CloudSim. Messages using the old model are
 * converted to the apropriate methods with the correct parameters.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class CloudSim {

	/** The Constant CLOUDSIM_VERSION_STRING. */
	private static final String CLOUDSIM_VERSION_STRING = "3.0";

	/** The id of CIS entity. */
	private static int cisId = -1;

	/** The id of CloudSimShutdown entity. */
	@SuppressWarnings("unused")
	private static int shutdownId = -1;

	/** The CIS object. */
	private static CloudInformationService cis = null;

	/** The Constant NOT_FOUND. */
	private static final int NOT_FOUND = -1;

	/** The trace flag. */
	@SuppressWarnings("unused")
	private static boolean traceFlag = false;

	/** The calendar. */
	private static Calendar calendar = null;

	/** The termination time. */
	private static double terminateAt = -1;

	/** The minimal time between events. Events within shorter periods after the last event are discarded. */
	private static double minTimeBetweenEvents = 0.1;
	
	/**
	 * Initialises all the common attributes.
	 * 
	 * @param _calendar the _calendar
	 * @param _traceFlag the _trace flag
	 * @param numUser number of users
	 * @throws Exception This happens when creating this entity before initialising CloudSim package
	 *             or this entity name is <tt>null</tt> or empty
	 * @pre $none
	 * @post $none
	 */
	private static void initCommonVariable(Calendar _calendar, boolean _traceFlag, int numUser)
			throws Exception {
		initialize();
		// NOTE: the order for the below 3 lines are important
		traceFlag = _traceFlag;

		// Set the current Wall clock time as the starting time of
		// simulation
		if (_calendar == null) {
			calendar = Calendar.getInstance();
		} else {
			calendar = _calendar;
		}

		// creates a CloudSimShutdown object
		CloudSimShutdown shutdown = new CloudSimShutdown("CloudSimShutdown", numUser);
		shutdownId = shutdown.getId();
	}

	/**
	 * Initialises CloudSim parameters. This method should be called before creating any entities.
	 * <p>
	 * Inside this method, it will create the following CloudSim entities:
	 * <ul>
	 * <li>CloudInformationService.
	 * <li>CloudSimShutdown
	 * </ul>
	 * <p>
	 * 
	 * @param numUser the number of User Entities created. This parameters indicates that
	 *            {@link gridsim.CloudSimShutdown} first waits for all user entities's
	 *            END_OF_SIMULATION signal before issuing terminate signal to other entities
	 * @param cal starting time for this simulation. If it is <tt>null</tt>, then the time will be
	 *            taken from <tt>Calendar.getInstance()</tt>
	 * @param traceFlag <tt>true</tt> if CloudSim trace need to be written
	 * @see gridsim.CloudSimShutdown
	 * @see CloudInformationService.CloudInformationService
	 * @pre numUser >= 0
	 * @post $none
	 */
	public static void init(int numUser, Calendar cal, boolean traceFlag) {
		try {
			initCommonVariable(cal, traceFlag, numUser);

			// create a GIS object
			cis = new CloudInformationService("CloudInformationService");

			// set all the above entity IDs
			cisId = cis.getId();
		} catch (IllegalArgumentException s) {
			///*Log.printLine("CloudSim.init(): The simulation has been terminated due to an unexpected error");
			///*Log.printLine(s.getMessage());
		} catch (Exception e) {
			///*Log.printLine("CloudSim.init(): The simulation has been terminated due to an unexpected error");
			///*Log.printLine(e.getMessage());
		}
	}

	/**
	 * Initialises CloudSim parameters. This method should be called before creating any entities.
	 * <p>
	 * Inside this method, it will create the following CloudSim entities:
	 * <ul>
	 * <li>CloudInformationService.
	 * <li>CloudSimShutdown
	 * </ul>
	 * <p>
	 * 
	 * @param numUser the number of User Entities created. This parameters indicates that
	 *            {@link gridsim.CloudSimShutdown} first waits for all user entities's
	 *            END_OF_SIMULATION signal before issuing terminate signal to other entities
	 * @param cal starting time for this simulation. If it is <tt>null</tt>, then the time will be
	 *            taken from <tt>Calendar.getInstance()</tt>
	 * @param traceFlag <tt>true</tt> if CloudSim trace need to be written
	 * @param periodBetweenEvents - the minimal period between events. Events within shorter periods
	 * after the last event are discarded.
	 * @see gridsim.CloudSimShutdown
	 * @see CloudInformationService.CloudInformationService
	 * @pre numUser >= 0
	 * @post $none
	 */
	public static void init(int numUser, Calendar cal, boolean traceFlag, double periodBetweenEvents) {
	    if (periodBetweenEvents <= 0) {
		throw new IllegalArgumentException("The minimal time between events should be positive, but is:" + periodBetweenEvents);
	    }
	    
	    init(numUser, cal, traceFlag);
	    minTimeBetweenEvents = periodBetweenEvents;
	}
	
	
	
	/**
	 * Starts the execution of CloudSim simulation. It waits for complete execution of all entities,
	 * i.e. until all entities threads reach non-RUNNABLE state or there are no more events in the
	 * future event queue.
	 * <p>
	 * <b>Note</b>: This method should be called after all the entities have been setup and added.
	 * 
	 * @return the double
	 * @throws NullPointerException This happens when creating this entity before initialising
	 *             CloudSim package or this entity name is <tt>null</tt> or empty.
	 * @see gridsim.CloudSim#init(int, Calendar, boolean)
	 * @pre $none
	 * @post $none
	 */
	public static double startSimulation() throws NullPointerException {
		///*Log.printLine("Starting CloudSim version " + CLOUDSIM_VERSION_STRING);
		try {
			double clock = run();

			// reset all static variables
			cisId = -1;
			shutdownId = -1;
			cis = null;
			calendar = null;
			traceFlag = false;
			
			
			return clock;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new NullPointerException("CloudSim.startCloudSimulation() :"
					+ " Error - you haven't initialized CloudSim.");
		}
	}

	/**
	 * Stops Cloud Simulation (based on {@link Simulation#runStop()}). This should be only called if
	 * any of the user defined entities <b>explicitly</b> want to terminate simulation during
	 * execution.
	 * 
	 * @throws NullPointerException This happens when creating this entity before initialising
	 *             CloudSim package or this entity name is <tt>null</tt> or empty
	 * @see gridsim.CloudSim#init(int, Calendar, boolean)
	 * @see Simulation#runStop()
	 * @pre $none
	 * @post $none
	 */
	public static void stopSimulation() throws NullPointerException {
		try {
			runStop();
		} catch (IllegalArgumentException e) {
			throw new NullPointerException("CloudSim.stopCloudSimulation() : "
					+ "Error - can't stop Cloud Simulation.");
		}
	}

	/**
	 * This method is called if one wants to terminate the simulation.
	 * 
	 * @return true, if successful; false otherwise.
	 */
	public static boolean terminateSimulation() {
		running = false;
		printMessage("Simulation: Reached termination time.");
		return true;
	}

	/**
	 * This method is called if one wants to terminate the simulation at a given time.
	 * 
	 * @param time the time at which the simulation has to be terminated
	 * @return true, if successful otherwise.
	 */
	public static boolean terminateSimulation(double time) {
		if (time <= clock) {
			return false;
		} else {
			terminateAt = time;
		}
		return true;
	}

	
	/**
	 * Returns the minimum time between events. Events within shorter periods after the last event are discarded. 
	 * @return the minimum time between events.
	 */
	public static double getMinTimeBetweenEvents() {
	    return minTimeBetweenEvents;
	}

	/**
	 * Gets a new copy of initial simulation Calendar.
	 * 
	 * @return a new copy of Calendar object or if CloudSim hasn't been initialized
	 * @see gridsim.CloudSim#init(int, Calendar, boolean, String[], String[], String)
	 * @see gridsim.CloudSim#init(int, Calendar, boolean)
	 * @pre $none
	 * @post $none
	 */
	public static Calendar getSimulationCalendar() {
		// make a new copy
		Calendar clone = calendar;
		if (calendar != null) {
			clone = (Calendar) calendar.clone();
		}

		return clone;
	}

	/**
	 * Gets the entity ID of <tt>CloudInformationService</tt>.
	 * 
	 * @return the Entity ID or if it is not found
	 * @pre $none
	 * @post $result >= -1
	 */
	public static int getCloudInfoServiceEntityId() {
		return cisId;
	}

	/**
	 * Sends a request to Cloud Information Service (GIS) entity to get the list of all Cloud
	 * hostList.
	 * 
	 * @return A List containing CloudResource ID (as an Integer object) or if a CIS entity hasn't
	 *         been created before
	 * @pre $none
	 * @post $none
	 */
	public static List<Integer> getCloudResourceList() {
		if (cis == null) {
			return null;
		}

		return cis.getList();
	}

	// ======== SIMULATION METHODS ===============//

	/** The entities. */
	public static List<SimEntity> entities;

	/** The future event queue. */
	protected static FutureQueue future;

	/** The deferred event queue. */
	protected static DeferredQueue deferred;

	/** The simulation clock. */
	private static double clock;

	/** Flag for checking if the simulation is running. */
	private static boolean running;

	/** The entities by name. */
	private static Map<String, SimEntity> entitiesByName;

	// The predicates used in entity wait methods
	/** The wait predicates. */
	private static Map<Integer, Predicate> waitPredicates;

	/** The paused. */
	private static boolean paused = false;

	/** The pause at. */
	private static long pauseAt = -1;

	/** The abrupt terminate. */
	private static boolean abruptTerminate = false;
	
	public static FogBroker broker;

	/**
	 * Initialise the simulation for stand alone simulations. This function should be called at the
	 * start of the simulation.
	 */
	protected static void initialize() {
		///*Log.printLine("Initialising...");
		entities = new ArrayList<SimEntity>();
		entitiesByName = new LinkedHashMap<String, SimEntity>();
		future = new FutureQueue();
		deferred = new DeferredQueue();
		waitPredicates = new HashMap<Integer, Predicate>();
		clock = 0;
		running = false;
	}

	// The two standard predicates

	/** A standard predicate that matches any event. */
	public final static PredicateAny SIM_ANY = new PredicateAny();

	/** A standard predicate that does not match any events. */
	public final static PredicateNone SIM_NONE = new PredicateNone();

	// Public access methods

	/**
	 * Get the current simulation time.
	 * 
	 * @return the simulation time
	 */
	public static double clock() {
		return clock;
	}
	
	public static void resetClock() {
		clock = 0.0;
	}

	/**
	 * Get the current number of entities in the simulation.
	 * 
	 * @return The number of entities
	 */
	public static int getNumEntities() {
		return entities.size();
	}

	/**
	 * Get the entity with a given id.
	 * 
	 * @param id the entity's unique id number
	 * @return The entity, or if it could not be found
	 */
	public static SimEntity getEntity(int id) {
		for(SimEntity ent: entities)
			if(ent.getId()==id)
				return ent;
		return null;
	}

	/**
	 * Get the entity with a given name.
	 * 
	 * @param name The entity's name
	 * @return The entity
	 */
	public static SimEntity getEntity(String name) {
		return entitiesByName.get(name);
	}

	/**
	 * Get the id of an entity with a given name.
	 * 
	 * @param name The entity's name
	 * @return The entity's unique id number
	 */
	public static int getEntityId(String name) {
		SimEntity obj = entitiesByName.get(name);
		if (obj == null) {
			return NOT_FOUND;
		} else {
			return obj.getId();
		}
	}

	/**
	 * Gets name of the entity given its entity ID.
	 * 
	 * @param entityID the entity ID
	 * @return the Entity name or if this object does not have one
	 * @pre entityID > 0
	 * @post $none
	 */
	public static String getEntityName(int entityID) {
		try {
			return getEntity(entityID).getName();
		} catch (IllegalArgumentException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Gets name of the entity given its entity ID.
	 * 
	 * @param entityID the entity ID
	 * @return the Entity name or if this object does not have one
	 * @pre entityID > 0
	 * @post $none
	 */
	public static String getEntityName(Integer entityID) {
		if (entityID != null) {
			return getEntityName(entityID.intValue());
		}
		return null;
	}

	/**
	 * Returns a list of entities created for the simulation.
	 * 
	 * @return the entity iterator
	 */
	public static List<SimEntity> getEntityList() {
		// create a new list to prevent the user from changing
		// the list of entities used by Simulation
		List<SimEntity> list = new LinkedList<SimEntity>();
		list.addAll(entities);
		return list;
	}
	
	public static void deleteActuatorsAndSensorsFromEntities(){
		System.out.println("Entities size = "+entities.size());
		
		for (SimEntity ent : entities) {
			System.out.println("ent.getName()"+ent.getName());
			if(ent.getName().startsWith("s-") || ent.getName().startsWith("Act")){
				System.out.println("remove ent:"+ent.getName());
				entities.remove(ent);
			}
		}
	}
	
	public static void resetEntities(){
		System.out.println("reset CloudSim entities");
		entities.clear();
	}
	
	public static void setEntityList(List<FogDevice> list) {
		// create a new list to prevent the user from changing
		// the list of entities used by Simulation
		entities = new ArrayList<SimEntity>(list);

	}
	

	// Public update methods

	/**
	 * Add a new entity to the simulation. This is present for compatibility with existing
	 * simulations since entities are automatically added to the simulation upon instantiation.
	 * 
	 * @param e The new entity
	 */
	public static void addEntity(SimEntity e) {
		SimEvent evt;
		if (running) {
			// Post an event to make this entity
			evt = new SimEvent(SimEvent.CREATE, clock, 1, 0, 0, e);
//			//*System.out.println("add to future evnt:"+evt.toString());
			
			future.addEvent(evt);
			////*System.out.println("evt:"+evt.toString());
		}
		if (e.getId() == -1) { // Only add once!
			int id = entities.size();
			e.setId(id);
			entities.add(e);
			entitiesByName.put(e.getName(), e);
		}
	}

	/**
	 * Internal method used to add a new entity to the simulation when the simulation is running. It
	 * should <b>not</b> be called from user simulations.
	 * 
	 * @param e The new entity
	 */
	protected static void addEntityDynamically(SimEntity e) {
		if (e == null) {
			throw new IllegalArgumentException("Adding null entity.");
		} else {
			printMessage("Adding: " + e.getName());
		}
		e.startEntity();
	}

	/**
	 * Internal method used to run one tick of the simulation. This method should <b>not</b> be
	 * called in simulations.
	 * 
	 * @return true, if successful otherwise
	 */
	
	public static boolean runClockTick() {
		SimEntity ent = null;
		boolean queue_empty;
		
		int entities_size = entities.size();
		
		//*System.out.println("Processing events on deferred queue entities evbuf...");
		//*System.out.println("\n deferred queue:");
		//*System.out.println("=======================");
		//*System.out.println("deffred Size="+deferred.size());
		
		///*Log.writeInLogFile("CloudSim", "Processing events on deferred queue entities evbuf...");
		///*Log.writeInLogFile("CloudSim", "\n deferred queue:");
		///*Log.writeInLogFile("CloudSim", "=======================");
		///*Log.writeInLogFile("CloudSim", "deffred Size="+deferred.size());
		
		Iterator<?> itr = deferred.iterator();
		
		while(itr.hasNext()){
			SimEvent e = (SimEvent) itr.next();
			//*System.out.println(e.toString());
			///*Log.writeInLogFile("CloudSim", e.toString());
		}
		///*Log.writeInLogFile("CloudSim","=======================");
		///*Log.writeInLogFile("CloudSim","\n");
		//*System.out.println("=======================");
		//*System.out.println("\n");
		
		if(DataPlacement.estimatedTuple.equals("")){
			for (int i = 0; i < entities_size; i++) {
				ent = entities.get(i);
				if (ent.getState() == SimEntity.RUNNABLE) {
					//System.out.println("run entity:"+ent.getName());
					ent.run();
				}
			}
			
		}else{
			
//			int productor = FogStorage.application.tupleToFogDeviceMap.get(DataPlacement.estimatedTuple);
//
//			for(SimEntity fogdev: entities){
//				if(fogdev.getId()==productor){
//					ent = fogdev;
//					break;
//				}	
//			}
//			
//			if(ent == null){
//				///*Log.writeInLogFile("CloudSim:", "Error, productor node is not found in entities for tuple:"+DataPlacement.estimatedTuple);
//				System.out.println("CloudSim:"+ "Error, productor node is not found in entities for tuple:"+DataPlacement.estimatedTuple);
//				System.exit(0);
//			}
//
//			if (ent.getState() == SimEntity.RUNNABLE) {
//				//System.out.println("run entity:"+ent.getName());
//				ent.run();
//			}
			
			for (int i = 0; i < entities_size; i++) {
//				if(i == productor){
//					continue;
//				}
				
				ent = entities.get(i);
				if (ent.getState() == SimEntity.RUNNABLE) {
					//System.out.println("run entity:"+ent.getName());
					ent.run();
				}
			}
			
		}
		
		
		
		//*System.out.println("\n\nProcessing events on future ...");
		//*System.out.println("Futures size="+future.size());
		//*System.out.println("\n\nInit Futures queue:");
		//*System.out.println("=======================");
		//*System.out.println("future Size="+future.size());
		
		///*Log.writeInLogFile("CloudSim", "\n\nProcessing events on future ...");
		
		///*Log.writeInLogFile("CloudSim", "\n\nInit Futures queue:");
		///*Log.writeInLogFile("CloudSim", "************************");
		///*Log.writeInLogFile("CloudSim", "Futures size="+future.size());
		
		itr = future.iterator();
		while(itr.hasNext()){
			SimEvent e = (SimEvent) itr.next();
			//*System.out.println(e.toString());
			///*Log.writeInLogFile("CloudSim", e.toString());
		}
		///*Log.writeInLogFile("CloudSim", "************************\n");
		//*System.out.println("=======================");
		//*System.out.println("\n");
		// If there are more future events then process them

		
		if (future.size() > 0) {
			List<SimEvent> toRemove = new ArrayList<SimEvent>();
			Iterator<SimEvent> fit = future.iterator();
			queue_empty = false;
			SimEvent first = fit.next();
			//System.out.println("Clock="+first.eventTime());
			//*System.out.println("\n\nProcessing of:"+first.toString());
			///*Log.writeInLogFile("CloudSim", "\n\nProcessing of:"+first.toString());
			processEvent(first);
			future.remove(first);
			//*System.out.println("Remove from future :"+first.toString());
			///*Log.writeInLogFile("CloudSim", "Remove from future :"+first.toString());
			
			fit = future.iterator();

			// Check if next events are at same time...
			boolean trymore = fit.hasNext();
			while (trymore) {
				SimEvent next = fit.next();
				if (next.eventTime() == first.eventTime()) {
					//*System.out.println("\n\nProcessing of:"+next.toString());
					///*Log.writeInLogFile("CloudSim", "\n\nProcessing of:"+next.toString());
					processEvent(next);
					toRemove.add(next);
					//*System.out.println("Will Removed from future :"+next.toString());
					///*Log.writeInLogFile("CloudSim", "Will Removed from future :"+next.toString());
					trymore = fit.hasNext();
				} else {
					trymore = false;
				}
			}
			future.removeAll(toRemove);
			///*Log.writeInLogFile("CloudSim", "Futures size="+future.size());
			//*System.out.println("Futures size="+future.size());

		} else {
			queue_empty = true;
			running = false;
			printMessage("Simulation: No more future events");
			///*Log.writeInLogFile("CloudSim", "Simulation: No more future events");
		}

		return queue_empty;
	}

	/**
	 * Internal method used to stop the simulation. This method should <b>not</b> be used directly.
	 */
	public static void runStop() {
		terminateSimulation();
		printMessage("Simulation completed.");
		///*Log.writeInLogFile("CloudSim", "Simulation completed.");
		
	}

	/**
	 * Used to hold an entity for some time.
	 * 
	 * @param src the src
	 * @param delay the delay
	 */
	public static void hold(int src, long delay) {
		SimEvent e = new SimEvent(SimEvent.HOLD_DONE, clock + delay, src);
//		//*System.out.println("add to future evnt:"+e.toString());
		future.addEvent(e);
		entities.get(src).setState(SimEntity.HOLDING);
	}

	/**
	 * Used to pause an entity for some time.
	 * 
	 * @param src the src
	 * @param delay the delay
	 */
	public static void pause(int src, double delay) {
		SimEvent e = new SimEvent(SimEvent.HOLD_DONE, clock + delay, src);
//		//*System.out.println("add to future evnt:"+e.toString());
		future.addEvent(e);
		entities.get(src).setState(SimEntity.HOLDING);
	}

	/**
	 * Used to send an event from one entity to another.
	 * 
	 * @param src the src
	 * @param dest the dest
	 * @param delay the delay
	 * @param tag the tag
	 * @param data the data
	 */
	public static void send(int src, int dest, double delay, int tag, final Object data) {
		if (delay < 0) {
			throw new IllegalArgumentException("Send delay can't be negative.");
		}

		SimEvent e = new SimEvent(SimEvent.SEND, delay+clock , src, dest, tag, data);	

		////*System.out.println("add to future evnt:"+e.toString());
		future.addEvent(e);
		///*Log.writeInLogFile("CloudSim", "add to future evnt:"+e.toString());
		
		
	}

	/**
	 * Used to send an event from one entity to another, with priority in the queue.
	 * 
	 * @param src the src
	 * @param dest the dest
	 * @param delay the delay
	 * @param tag the tag
	 * @param data the data
	 */
	public static void sendFirst(int src, int dest, double delay, int tag, Object data) {
		if (delay < 0) {
			throw new IllegalArgumentException("Send delay can't be negative.");
		}

		SimEvent e = new SimEvent(SimEvent.SEND, clock + delay, src, dest, tag, data);
		//*System.out.println("add to future evnt:"+e.toString());
		///*Log.writeInLogFile("CloudSim", "add to future evnt:"+e.toString());
		future.addEventFirst(e);
	}

	/**
	 * Sets an entity's state to be waiting. The predicate used to wait for an event is now passed
	 * to Sim_system. Only events that satisfy the predicate will be passed to the entity. This is
	 * done to avoid unnecessary context switches.
	 * 
	 * @param src the src
	 * @param p the p
	 */
	public static void wait(int src, Predicate p) {
		entities.get(src).setState(SimEntity.WAITING);
		if (p != SIM_ANY) {
			// If a predicate has been used store it in order to check it
			waitPredicates.put(src, p);
		}
	}

	/**
	 * Checks if events for a specific entity are present in the deferred event queue.
	 * 
	 * @param d the d
	 * @param p the p
	 * @return the int
	 */
	public static int waiting(int d, Predicate p) {
		int count = 0;
		SimEvent event;
		Iterator<SimEvent> iterator = deferred.iterator();
		while (iterator.hasNext()) {
			event = iterator.next();
			if ((event.getDestination() == d) && (p.match(event))) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Selects an event matching a predicate.
	 * 
	 * @param src the src
	 * @param p the p
	 * @return the sim event
	 */
	public static SimEvent select(int src, Predicate p) {
		////*System.out.println("------------------------------------------");
		SimEvent ev = null;
		Iterator<SimEvent> iterator = deferred.iterator();
		
		while (iterator.hasNext()) {
			ev = iterator.next();
			/////*Log.writeInLogFile("CloudSim:", "\titerator ev:"+ev.toString());
			if (ev.getDestination() == src && p.match(ev)) {
				iterator.remove();
				
				///*Log.writeInLogFile("\nCloudSim:","Removed from deferred event :"+ev.toString());
				///*Log.writeInLogFile("CloudSim:", "deferred.size()="+deferred.size());
//				//*System.out.println("Add to evbuf "+ev.toString());
//				if(ev.getTag()==FogEvents.TUPLE_Process){
//					Tuple tuple = (Tuple) ev.getData();
//					////*System.out.println(tuple.toString());
//				}
				break;
			}
		}
		return ev;
	}

	/**
	 * Find first deferred event matching a predicate.
	 * 
	 * @param src the src
	 * @param p the p
	 * @return the sim event
	 */
	public static SimEvent findFirstDeferred(int src, Predicate p) {
		SimEvent ev = null;
		Iterator<SimEvent> iterator = deferred.iterator();
		while (iterator.hasNext()) {
			ev = iterator.next();
			if (ev.getDestination() == src && p.match(ev)) {
				break;
			}
		}
		return ev;
	}

	/**
	 * Removes an event from the event queue.
	 * 
	 * @param src the src
	 * @param p the p
	 * @return the sim event
	 */
	public static SimEvent cancel(int src, Predicate p) {
		SimEvent ev = null;
		Iterator<SimEvent> iter = future.iterator();
		while (iter.hasNext()) {
			ev = iter.next();
			if (ev.getSource() == src && p.match(ev)) {
				iter.remove();
				break;
			}
		}

		return ev;
	}

	/**
	 * Removes all events that match a given predicate from the future event queue returns true if
	 * at least one event has been cancelled; false otherwise.
	 * 
	 * @param src the src
	 * @param p the p
	 * @return true, if successful
	 */
	public static boolean cancelAll(int src, Predicate p) {
		
		///*Log.writeInLogFile("CloudSim:", "cancelAll -> deviceId:"+src+"\tevt.tag:"+p.toString() +"\t-> From future Queue!");
		SimEvent ev = null;
		int previousSize = future.size();
		Iterator<SimEvent> iter = future.iterator();
		while (iter.hasNext()) {
			ev = iter.next();
			if (ev.getSource() == src && p.match(ev)) {
				iter.remove();
			}
		}
		return previousSize < future.size();
	}

	//
	// Private internal methods
	//

	/**
	 * Processes an event.
	 * 
	 * @param e the e
	 */
	private static void processEvent(SimEvent e) {
		int dest, src;
		SimEntity dest_ent;
		
		///*Log.writeInLogFile("CloudSim", "Clock:"+clock);
		// Update the system's clock
		if (e.eventTime() < clock) {
			throw new IllegalArgumentException("Past event detected.");
		}
		
		clock = e.eventTime();

		///*Log.writeInLogFile("CloudSim", "Updating the clock while processing event:"+e.toString());
		///*Log.writeInLogFile("CloudSim", "New clock is the event time = "+clock);
		// Ok now process it
		switch (e.getType()) {
			case SimEvent.ENULL:
				throw new IllegalArgumentException("Event has a null type.");

			case SimEvent.CREATE:
				SimEntity newe = (SimEntity) e.getData();
				addEntityDynamically(newe);
				break;

			case SimEvent.SEND:
				// Check for matching wait
				dest = e.getDestination();
				if (dest < 0) {
					throw new IllegalArgumentException("Attempt to send to a null entity detected.");
				} else {
					int tag = e.getTag();
					dest_ent = getEntity(dest);
					if (dest_ent.getState() == SimEntity.WAITING) {
						Integer destObj = Integer.valueOf(dest);
						Predicate p = waitPredicates.get(destObj);
//						//*System.out.println("Entity "+dest_ent.getName()+" is wating for the predicate:"+p.toString());
						if ((p == null) || (tag == 9999) || (p.match(e))) {
//							//*System.out.println("The wating predicate: (p == null) || (tag == 9999) || (p.match(event)");
//							//*System.out.println("Clonning of: "+e.toString()+" on evbuf  of: "+dest_ent.getName());
							dest_ent.setEventBuffer((SimEvent) e.clone());
							dest_ent.setState(SimEntity.RUNNABLE);
							waitPredicates.remove(destObj);
						} else {
//							//*System.out.println("In the Else of : The wating predicate: (p == null) || (tag == 9999) || (p.match(event)");
//							//*System.out.println("Adding the event:"+e.toString()+" to deferred queue");
							deferred.addEvent(e);
//							//*System.out.println("deferred.size()="+deferred.size());
						}
					} else {
//						//*System.out.println("Entity "+dest_ent.getName()+" isn't wating for any predicate");
//						//*System.out.println("Adding the event to deferred queue:"+e.toString());
						
						
						deferred.addEvent(e);
						
						///*Log.writeInLogFile("CloudSim", "deferred.size()="+deferred.size());
					}
				}
				break;

			case SimEvent.HOLD_DONE:
				src = e.getSource();
				if (src < 0) {
					throw new IllegalArgumentException("Null entity holding.");
				} else {
					entities.get(src).setState(SimEntity.RUNNABLE);
				}
				break;

			default:
				break;
		}
	}

	/**
	 * Internal method used to start the simulation. This method should <b>not</b> be used by user
	 * simulations.
	 */
	public static void runStart() {
		System.out.println("Press enter...");
//		Scanner sc = new Scanner(System.in);
//		String str = sc.nextLine();
		running = true;
		// Start all the entities
		for (SimEntity ent : entities) {
			//System.out.println("Start Entity "+ent.getName());
			ent.startEntity();
		}
		
		printMessage("Entities started.");
		System.out.println("Entities started.");
//		sc = new Scanner(System.in);
//		str = sc.nextLine();
	}

	/**
	 * Check if the simulation is still running. This method should be used by entities to check if
	 * they should continue executing.
	 * 
	 * @return if the simulation is still running, otherwise
	 */
	public static boolean running() {
		return running;
	}

	/**
	 * This method is called if one wants to pause the simulation.
	 * 
	 * @return true, if successful otherwise.
	 */
	public static boolean pauseSimulation() {
		paused = true;
		return paused;
	}

	/**
	 * This method is called if one wants to pause the simulation at a given time.
	 * 
	 * @param time the time at which the simulation has to be paused
	 * @return true, if successful otherwise.
	 */
	public static boolean pauseSimulation(long time) {
		if (time <= clock) {
			return false;
		} else {
			pauseAt = time;
		}
		return true;
	}

	/**
	 * This method is called if one wants to resume the simulation that has previously been paused.
	 * 
	 * @return if the simulation has been restarted or or otherwise.
	 */
	public static boolean resumeSimulation() {
		paused = false;

		if (pauseAt <= clock) {
			pauseAt = -1;
		}

		return !paused;
	}

	/**
	 * Start the simulation running. This should be called after all the entities have been setup
	 * and added, and their ports linked.
	 * 
	 * @return the double last clock value
	 */
	public static double run() {
		if (!running) {
			runStart();
		}
		
		//*System.out.println("Press enter...");
		//Scanner sc = new Scanner(System.in);
		//String str = sc.nextLine();
		
		//*System.out.println("\n\nInit Futures queue:");
		//*System.out.println("=======================");
		//*System.out.println("future Size="+future.size());
		
		///*Log.writeInLogFile("CloudSim", "Init Futures queue:");
		///*Log.writeInLogFile("CloudSim", "************************");
		///*Log.writeInLogFile("CloudSim", "future Size="+future.size());
		
		
		Iterator<?> itr = future.iterator();
		while(itr.hasNext()){
			SimEvent e = (SimEvent) itr.next();
			//*System.out.println(e.toString());
			///*Log.writeInLogFile("CloudSim", e.toString()); 
			
			if(e.getTag()==FogEvents.TUPLE_PROCESS){
				Tuple tuple = (Tuple) e.getData();
				////*System.out.println(tuple.toString());
			}
		}
		
		//*System.out.println("=======================");
		//*System.out.println("\n");
		
		///*Log.writeInLogFile("CloudSim", "************************\n");
		
		int iteration =0;
		
		while (true) {
			//*System.out.println("************************************************************************************************************************************");
			//System.out.println("\n\nIteration:"+iteration);		
			
			///*Log.writeInLogFile("CloudSim","************************************************************************************************************************************");
			///*Log.writeInLogFile("CloudSim", "\n\nIteration:"+iteration);
			
			if (runClockTick() || abruptTerminate) {
				//*System.out.println("Terminate at 1");
				///*Log.writeInLogFile("CloudSim", "Terminate at 1");
				break;
			}

			// this block allows termination of simulation at a specific time
			if (terminateAt > 0.0 && clock >= terminateAt) {
				//*System.out.println("Terminate at 2");
				///*Log.writeInLogFile("CloudSim", "Terminate at 2");
				terminateSimulation();
				clock = terminateAt;
				break;
			}

			if (pauseAt != -1 && ((future.size() > 0 && clock <= pauseAt && pauseAt <= future.iterator().next().eventTime())
					|| future.size() == 0 && pauseAt <= clock)) {
				pauseSimulation();
				clock = pauseAt;
			}

			while (paused) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			iteration++;
			
			//*System.out.println("Overal read latency:"+LatencyStats.getOverall_read_Latency());
			//*System.out.println("Overal write latency:"+LatencyStats.getOverall_write_Latency());
			//*System.out.println("Overal latency:"+LatencyStats.getOverall_Latency());
			
			///*Log.writeInLogFile("CloudSim", "Overal read latency:"+LatencyStats.getOverall_read_Latency());
			///*Log.writeInLogFile("CloudSim", "Overal write latency:"+LatencyStats.getOverall_write_Latency());
			///*Log.writeInLogFile("CloudSim", "Overal latency:"+LatencyStats.getOverall_Latency());
			
			//*System.out.println("Press enter...");
			//sc = new Scanner(System.in);
			//str = sc.nextLine();
			
		}

		double clock = clock();

		finishSimulation();
		runStop();

		return clock;
	}

	/**
	 * Internal method that allows the entities to terminate. This method should <b>not</b> be used
	 * in user simulations.
	 */
	public static void finishSimulation() {
		// Allow all entities to exit their body method
		System.out.println("End of simulation");
		if (!abruptTerminate) {
			for (SimEntity ent : entities) {
				if (ent.getState() != SimEntity.FINISHED) {
					ent.run();
				}
			}
		}

		for (SimEntity ent : entities) {
			ent.shutdownEntity();
		}

		// reset all static variables
		// Private data members
		entities = null;
		entitiesByName = null;
		future = null;
		deferred = null;
		clock = 0L;
		running = false;

		waitPredicates = null;
		paused = false;
		pauseAt = -1;
		abruptTerminate = false;

	}

	/**
	 * Abruptally terminate.
	 */
	public static void abruptallyTerminate() {
		abruptTerminate = true;
	}

	/**
	 * Prints a message about the progress of the simulation.
	 * 
	 * @param message the message
	 */
	private static void printMessage(String message) {
		///*Log.printLine(message);
	}

	/**
	 * Checks if is paused.
	 * 
	 * @return true, if is paused
	 */
	public static boolean isPaused() {
		return paused;
	}
	
	public static void printFuturesEvents(){
		//*System.out.println("=======================");
		//*System.out.println("future Size="+future.size());
		Iterator<SimEvent> itr = future.iterator();
		while(itr.hasNext()){
			SimEvent e = (SimEvent) itr.next();
			//*System.out.println(e.toString());
			
			if(e.getTag()==FogEvents.TUPLE_PROCESS){
				Tuple tuple = (Tuple) e.getData();
				////*System.out.println(tuple.toString());
			}
			
		}
		//*System.out.println("=======================");
	}

	public static int getFutureSize(){
		return future.size();
	}

	
}
