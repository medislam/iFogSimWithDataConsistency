package org.fog.utils;

public class FogEvents {
	private static final int BASE = 50;
	public static final int TUPLE_ARRIVAL = BASE + 1;
	public static final int LAUNCH_MODULE = BASE + 2;
	public static final int RELEASE_OPERATOR = BASE + 3;
	public static final int SENSOR_JOINED = BASE + 4;
	public static final int TUPLE_ACK = BASE + 5;
	public static final int APP_SUBMIT = BASE + 6;
	public static final int CALCULATE_INPUT_RATE = BASE + 7;
	public static final int CALCULATE_UTIL = BASE + 8;
	public static final int UPDATE_RESOURCE_USAGE = BASE + 9;
	//public static final int UPDATE_TUPLE_QUEUE = BASE + 10;
	public static final int TUPLE_FINISHED = BASE + 11;
	public static final int ACTIVE_APP_UPDATE = BASE+12;
	public static final int CONTROLLER_RESOURCE_MANAGE = BASE+13;
	public static final int ADAPTIVE_OPERATOR_REPLACEMENT = BASE+14;
	public static final int GET_RESOURCE_USAGE = BASE+15;
	public static final int RESOURCE_USAGE = BASE+16;
	public static final int CONTROL_MSG_ARRIVAL = BASE + 17;
	public static final int UPDATE_NORTH_TUPLE_QUEUE = BASE+18;
	public static final int UPDATE_SOUTH_TUPLE_QUEUE = BASE+19;
	public static final int ACTUATOR_JOINED = BASE+20;
	public static final int STOP_SIMULATION = BASE + 21;
	public static final int SEND_PERIODIC_TUPLE = BASE+22;
	public static final int LAUNCH_MODULE_INSTANCE = BASE+23;
	public static final int RESOURCE_MGMT = BASE+24;
	public static final int INITIALIZE_SENSOR = BASE+24;
	public static final int EMIT_TUPLE = BASE+25;
	
	public static final int TUPLE_STORAGE = BASE + 26;
	public static final int TUPLE_PROCESS = BASE + 27;
	public static final int TUPLE_STORAGE_ACK = BASE + 28;
	public static final int TUPLE_PROCESS_ACK = BASE +29;
	
	public static final int TUPLE_RETRIEVE = BASE +30;
	public static final int TUPLE_RETRIEVE_ACK = BASE +31;
	
	public static final int INITIALIZE_CONSUMPTION = BASE + 32;
	public static final int SEND_DATA_TO_CONSUMER = BASE + 33;
	
	public static final int LOCK_REPLICA = BASE + 34;
	public static final int UNLOCK_REPLICA = BASE + 35;
	
	public static final int DELAY_TUPLE_STORAGE = BASE + 36;
	public static final int DELAY_TUPLE_STORAGE_ACK = BASE + 37;
	
	public static final int NON_TUPLE_RETRIEVE = BASE + 38;
	
	public static final int VERSION_EXCHANGE = BASE + 39;
	public static final int GET_LAST_VERSION = BASE + 40;
	public static final int RETURN_LAST_VERSION = BASE + 41;
	public static final int BLOCKED_WRITE_STORAGE = BASE + 42;
	public static final int INITIALIZE_PERIODIC_TUPLE_PRODUCTION = BASE + 43;
	
}
