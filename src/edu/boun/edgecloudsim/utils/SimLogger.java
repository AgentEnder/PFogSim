/*
 * Title:        EdgeCloudSim - Simulation Logger
 * 
 * Description: 
 * SimLogger is responsible for storing simulation events/results
 * in to the files in a specific format.
 * Format is decided in a way to use results in matlab efficiently.
 * If you need more results or another file format, you should modify
 * this class.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.cloudbus.cloudsim.core.CloudSim;

import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
//import edu.boun.edgecloudsim.edge_server.EdgeHost;
//import edu.boun.edgecloudsim.utils.*;
//import edu.auburn.pFogSim.Puddle.Puddle;
//import edu.auburn.pFogSim.netsim.*;



public class SimLogger {
	public static enum TASK_STATUS {
		CREATED, UPLOADING, PROCESSING, DOWNLOADING, COMPLETED, REJECTED_DUE_TO_VM_CAPACITY, REJECTED_DUE_TO_BANDWIDTH, UNFINISHED_DUE_TO_BANDWIDTH, UNFINISHED_DUE_TO_MOBILITY
	}

	private static boolean fileLogEnabled;
	private static boolean printLogEnabled;
	private String filePrefix;
	private String outputFolder;
	private Map<Integer, LogItem> taskMap;
	private LinkedList<VmLoadLogItem> vmLoadList;
	private LinkedList<FNMipsUtilLogItem> fnMipsUtilList; // shaik added
	private LinkedList<FNNwUtilLogItem> fnNwUtilList; // shaik added
	private File centerLogFile;
	PrintWriter centerFileW;
	private ArrayList<Integer> utlizationArray;
	


	private static SimLogger singleton = new SimLogger();
	
	//Qian Print to console

	/*
	 * A private Constructor prevents any other class from instantiating.
	 */
	private SimLogger() {
		fileLogEnabled = false;
		printLogEnabled = false;
	}

	/* Static 'instance' method */
	public static SimLogger getInstance() {
		return singleton;
	}

	public static void enableFileLog() {
		fileLogEnabled = true;
	}

	public static void enablePrintLog() {
		printLogEnabled = true;
	}

	public static boolean isFileLogEnabled() {
		return fileLogEnabled;
	}

	public static void disablePrintLog() {
		printLogEnabled = false;
	}

	private void appendToFile(BufferedWriter bw, String line) throws IOException {
		bw.write(line);
		bw.newLine();
	}

	public static void printLine(String msg) {
		if (printLogEnabled)
			System.out.println(msg);
	}

	public static void print(String msg) {
		if (printLogEnabled)
			System.out.print(msg);
	}

	/**
	 * @param outFolder
	 * @param fileName
	 */
	public void simStarted(String outFolder, String fileName) {
		filePrefix = fileName;
		outputFolder = outFolder;
		taskMap = new HashMap<Integer, LogItem>();
		vmLoadList = new LinkedList<VmLoadLogItem>();
		fnMipsUtilList = new LinkedList<FNMipsUtilLogItem>(); // shaik added
		fnNwUtilList = new LinkedList<FNNwUtilLogItem>(); // shaik added
		utlizationArray = new ArrayList<Integer>();
		try {
			centerLogFile = new File(outputFolder, filePrefix + "_Cost_Logger.txt");
			centerFileW = new PrintWriter(centerLogFile);
		} catch (Exception e) {
			System.out.println("Centralize Logger File Cannot Find");
		}
	}
	
	/**
	 * @author szs0117
	 * @param time
	 * @param hostId
	 * @param hostLevel
	 * @param fnMipsUtil
	 */
	public void addFNMipsUtilizationLog(double time, int hostId, int hostLevel, double fnMipsUtil) {
		fnMipsUtilList.add(new FNMipsUtilLogItem(time, hostId, hostLevel, fnMipsUtil));
	}		

		
	/**
	 * @author szs0117
	 * @param time
	 * @param hostId
	 * @param hostLevel
	 * @param fnBwUtil
	 */
	public void addFNNwUtilizationLog(double time, int hostId, int hostLevel, double fnBwUtil) {
		fnNwUtilList.add(new FNNwUtilLogItem(time, hostId, hostLevel, fnBwUtil));
	}		

	
	public PrintWriter getCentralizeLogPrinter() {
		return centerFileW;
	}

	public void addLog(double taskStartTime, int taskId, int taskType, int taskLenght, int taskInputType,
			int taskOutputSize) {
		// printLine(taskId+"->"+taskStartTime);
		taskMap.put(taskId, new LogItem(taskStartTime, taskType, taskLenght, taskInputType, taskOutputSize));
	}

	public void uploadStarted(int taskId, double taskUploadTime) {
		taskMap.get(taskId).taskUploadStarted(taskUploadTime);
	}

	public void uploaded(int taskId, int datacenterId, int hostId, int vmId, int vmType) {
		taskMap.get(taskId).taskUploaded(datacenterId, hostId, vmId, vmType);
	}

	public void downloadStarted(int taskId, double taskDownloadTime) {
		taskMap.get(taskId).taskDownloadStarted(taskDownloadTime);
	}

	public void downloaded(int taskId, double taskEndTime) {
		taskMap.get(taskId).taskDownloaded(taskEndTime);
	}
	
	public void downloaded(int taskId, double taskEndTime, double cost) {
		taskMap.get(taskId).taskDownloaded(taskEndTime, cost);
	}

	public void rejectedDueToVMCapacity(int taskId, double taskRejectTime) {
		taskMap.get(taskId).taskRejectedDueToVMCapacity(taskRejectTime);
	}

	public void rejectedDueToBandwidth(int taskId, double taskRejectTime, int vmType) {
		taskMap.get(taskId).taskRejectedDueToBandwidth(taskRejectTime, vmType);
	}

	public void failedDueToBandwidth(int taskId, double taskRejectTime) {
		taskMap.get(taskId).taskFailedDueToBandwidth(taskRejectTime);
	}

	public void failedDueToMobility(int taskId, double time) {
		taskMap.get(taskId).taskFailedDueToMobility(time);
	}

	public void addVmUtilizationLog(double time, double load) {
		vmLoadList.add(new VmLoadLogItem(time, load));
	}
	
	public void addHostDistanceLog(int taskId, double dist) {
		taskMap.get(taskId).setDistance(dist);
	}
	
	public void addHops(int taskId, int hops) {
		taskMap.get(taskId).setHops(hops);
	}
	
	
	
	int[] totalNodesNmuberInEachLevel = {0, 0, 0, 0, 0, 0, 0};
	private int[] levelFogNodeCount = {0, 0, 0, 0, 0, 0, 0};
	
	private void getTotalFogNodesCountInEachLevel() {
		HashSet<NodeSim> nodes = ((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology().getNodes();
		for (NodeSim node: nodes) {
			totalNodesNmuberInEachLevel[node.getLevel() - 1]++;
		}
	}
	
	//Qian add method for counting fog nodes utilization
	public void addNodeUtilization(int hostId, EdgeHost host) {
		if (!utlizationArray.contains(hostId)) {
			utlizationArray.add(hostId);
			this.levelFogNodeCount[host.getLevel() - 1]++;
		}
	}
	
	private int[] levelCloudletCount = {0, 0, 0, 0, 0, 0, 0, 0};
	public void addCloudletToLevel(int level) {this.levelCloudletCount[level]++;}
	
	
	public void simStopped() throws IOException {
		int numOfAppTypes = SimSettings.getInstance().getTaskLookUpTable().length;

		File successFile = null, failFile = null, vmLoadFile = null, fnMipsUtilFile = null, fnNwUtilFile = null, locationFile = null, distFile = null, hopFile = null;
		FileWriter successFW = null, failFW = null, vmLoadFW = null, fnMipsUtilFW = null, fnNwUtilFW = null, locationFW = null, distFW = null, hopFW = null;
		BufferedWriter successBW = null, failBW = null, vmLoadBW = null, fnMipsUtilBW = null, fnNwUtilBW = null, locationBW = null, distBW = null, hopBW = null;

		/*File[] vmLoadFileClay = new File[numOfAppTypes]; 
		FileWriter[] vmLoadFWClay = new FileWriter[numOfAppTypes];
		BufferedWriter[] vmLoadBWClay = new BufferedWriter[numOfAppTypes];*/
		
		// Save generic results to file for each app type. last index is average
		// of all app types
		File[] genericFiles = new File[numOfAppTypes + 1];
		FileWriter[] genericFWs = new FileWriter[numOfAppTypes + 1];
		BufferedWriter[] genericBWs = new BufferedWriter[numOfAppTypes + 1];

		// extract following values for each app type. last index is average of
		// all app types
		int[] uncompletedTask = new int[numOfAppTypes + 1];
		int[] uncompletedTaskOnCloud = new int[numOfAppTypes + 1];
		int[] uncompletedTaskOnCloudlet = new int[numOfAppTypes + 1];

		int[] completedTask = new int[numOfAppTypes + 1];
		int[] completedTaskOnCloud = new int[numOfAppTypes + 1];
		int[] completedTaskOnCloudlet = new int[numOfAppTypes + 1];

		int[] failedTask = new int[numOfAppTypes + 1];
		int[] failedTaskOnCloud = new int[numOfAppTypes + 1];
		int[] failedTaskOnCloudlet = new int[numOfAppTypes + 1];

		double[] networkDelay = new double[numOfAppTypes + 1];
		double[] wanDelay = new double[numOfAppTypes + 1];
		double[] lanDelay = new double[numOfAppTypes + 1];

		double[] serviceTime = new double[numOfAppTypes + 1];
		double[] serviceTimeOnCloud = new double[numOfAppTypes + 1];
		double[] serviceTimeOnCloudlet = new double[numOfAppTypes + 1];

		double[] processingTime = new double[numOfAppTypes + 1];
		double[] processingTimeOnCloud = new double[numOfAppTypes + 1];
		double[] processingTimeOnCloudlet = new double[numOfAppTypes + 1];

		double[] cost = new double[numOfAppTypes + 1];
		int[] failedTaskDuetoBw = new int[numOfAppTypes + 1];
		int[] failedTaskDuetoLanBw = new int[numOfAppTypes + 1];
		int[] failedTaskDuetoWanBw = new int[numOfAppTypes + 1];
		int[] failedTaskDuetoMobility = new int[numOfAppTypes + 1];
		int[] rejectedTaskDoToVmCapacity = new int[numOfAppTypes + 1];
		
		double[] totalDist = new double[numOfAppTypes + 1];
		int[] totalHops = new int[numOfAppTypes + 1];
		int[] numberOfAppTypes = new int[numOfAppTypes + 1];

		double[] fogLayerAvgMipsUtil = {0, 0, 0, 0, 0, 0, 0}; // Shaik added
		double[] fogLayerTotalMipsUtil = {0, 0, 0, 0, 0, 0, 0}; // Shaik added
		double[] fogLayerEntryMipsCount = {0, 0, 0, 0, 0, 0, 0}; // Shaik added

		double[] fogLayerAvgNwUtil = {0, 0, 0, 0, 0, 0, 0}; // Shaik added
		double[] fogLayerTotalNwUtil = {0, 0, 0, 0, 0, 0, 0}; // Shaik added
		double[] fogLayerEntryNwCount = {0, 0, 0, 0, 0, 0, 0}; // Shaik added

		// open all files and prepare them for write
		if (fileLogEnabled) {
			if (SimSettings.getInstance().getDeepFileLoggingEnabled()) {
				successFile = new File(outputFolder, filePrefix + "_SUCCESS.log");
				successFW = new FileWriter(successFile, true);
				successBW = new BufferedWriter(successFW);

				failFile = new File(outputFolder, filePrefix + "_FAIL.log");
				failFW = new FileWriter(failFile, true);
				failBW = new BufferedWriter(failFW);
			}

			vmLoadFile = new File(outputFolder, filePrefix + "_VM_LOAD.log");
			vmLoadFW = new FileWriter(vmLoadFile, true);
			vmLoadBW = new BufferedWriter(vmLoadFW);

			// shaik added
			fnMipsUtilFile = new File(outputFolder, filePrefix + "_HOST_MIPS_UTILIZATION.log");
			fnMipsUtilFW = new FileWriter(fnMipsUtilFile, true);
			fnMipsUtilBW = new BufferedWriter(fnMipsUtilFW);

			// shaik added
			fnNwUtilFile = new File(outputFolder, filePrefix + "_HOST_NETWORK_UTILIZATION.log");
			fnNwUtilFW = new FileWriter(fnNwUtilFile, true);
			fnNwUtilBW = new BufferedWriter(fnNwUtilFW);

			locationFile = new File(outputFolder, filePrefix + "_LOCATION.log");
			locationFW = new FileWriter(locationFile, true);
			locationBW = new BufferedWriter(locationFW);
			
			distFile = new File(outputFolder, filePrefix + "_DISTANCES.log");
			distFW = new FileWriter(distFile, true);
			distBW = new BufferedWriter(distFW);
			
			hopFile = new File(outputFolder, filePrefix + "_HOPS.log");
			hopFW = new FileWriter(hopFile, true);
			hopBW = new BufferedWriter(hopFW);
			
			/*for(int i = 0; i < numOfAppTypes; i++)
			{
				vmLoadFileClay[i] = new File(outputFolder, "CLAYSTESTFILE" + "_" + i + ".log");
				vmLoadFWClay[i] = new FileWriter(vmLoadFileClay[i], true);
				vmLoadBWClay[i] = new BufferedWriter(vmLoadFWClay[i]);
				appendToFile(vmLoadBWClay[i], "#network delay\tsimulation time");
			}*/
			for (int i = 0; i < numOfAppTypes + 1; i++) {
				String fileName = "ALL_APPS_GENERIC.log";

				if (i < numOfAppTypes) {
					// if related app is not used in this simulation, just
					// discard it
					if (SimSettings.getInstance().getTaskLookUpTable()[i][0] == 0)
						continue;

					fileName = SimSettings.APP_TYPES.values()[i] + "_GENERIC.log";
				}

				genericFiles[i] = new File(outputFolder, filePrefix + "_" + fileName);
				genericFWs[i] = new FileWriter(genericFiles[i], true);
				genericBWs[i] = new BufferedWriter(genericFWs[i]);
				appendToFile(genericBWs[i], "#auto generated file!");
			}

			if (SimSettings.getInstance().getDeepFileLoggingEnabled()) {
				appendToFile(successBW, "#auto generated file!");
				appendToFile(failBW, "#auto generated file!");
			}

			appendToFile(vmLoadBW, "#auto generated file!");
			appendToFile(locationBW, "#auto generated file!");
			appendToFile(distBW, "#auto generated file!");
			appendToFile(hopBW, "#auto generated file!");
		}
		// Qian Print warm up tasks.
		int warmUpTasks = 0;
		// extract the result of each task and write it to the file if required
		for (Map.Entry<Integer, LogItem> entry : taskMap.entrySet()) {
			Integer key = entry.getKey();
			LogItem value = entry.getValue();
			totalDist[value.getTaskType()] += value.getHostDist();
			distBW.write(value.getHostDist() + ",");
			totalHops[value.getTaskType()] += value.getHops();
			hopBW.write(value.getHops() + ",");
			numberOfAppTypes[value.getTaskType()]++;
			
			
			if (value.isInWarmUpPeriod()) {
				warmUpTasks++;
				continue;
			}
				

			if (value.getStatus() == SimLogger.TASK_STATUS.COMPLETED) {
				completedTask[value.getTaskType()]++;

				if (value.getVmType() == SimSettings.VM_TYPES.CLOUD_VM.ordinal())
					completedTaskOnCloud[value.getTaskType()]++;
				else
					completedTaskOnCloudlet[value.getTaskType()]++;
			} else {
				failedTask[value.getTaskType()]++;

				if (value.getVmType() == SimSettings.VM_TYPES.CLOUD_VM.ordinal())
					failedTaskOnCloud[value.getTaskType()]++;
				else
					failedTaskOnCloudlet[value.getTaskType()]++;
			}

			if (value.getStatus() == SimLogger.TASK_STATUS.COMPLETED) {
				//value.se
				cost[value.getTaskType()] += value.getCost();
				serviceTime[value.getTaskType()] += value.getServiceTime();
				networkDelay[value.getTaskType()] += value.getNetworkDelay();
				
				//Make a file that saves all of the network delays and current time info
				//appendToFile(vmLoadBWClay[value.getTaskType()], value.getNetworkDelay() + "\t" + CloudSim.clock());
				
				processingTime[value.getTaskType()] += (value.getServiceTime() - value.getNetworkDelay());

				if (value.getVmType() == SimSettings.VM_TYPES.CLOUD_VM.ordinal()) {
					wanDelay[value.getTaskType()] += value.getNetworkDelay();
					serviceTimeOnCloud[value.getTaskType()] += value.getServiceTime();
					processingTimeOnCloud[value.getTaskType()] += (value.getServiceTime() - value.getNetworkDelay());
				} 
				else {
					lanDelay[value.getTaskType()] += value.getNetworkDelay();
					serviceTimeOnCloudlet[value.getTaskType()] += value.getServiceTime();
					processingTimeOnCloudlet[value.getTaskType()] += (value.getServiceTime() - value.getNetworkDelay());
				}

				if (fileLogEnabled && SimSettings.getInstance().getDeepFileLoggingEnabled())
					appendToFile(successBW, value.toString(key));
			} else if (value.getStatus() == SimLogger.TASK_STATUS.REJECTED_DUE_TO_VM_CAPACITY) {
				rejectedTaskDoToVmCapacity[value.getTaskType()]++;
				if (value.getVmType() == SimSettings.VM_TYPES.CLOUD_VM.ordinal())
					if (fileLogEnabled && SimSettings.getInstance().getDeepFileLoggingEnabled())
						appendToFile(failBW, value.toString(key));
			} else if (value.getStatus() == SimLogger.TASK_STATUS.REJECTED_DUE_TO_BANDWIDTH
					|| value.getStatus() == SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_BANDWIDTH) {
				failedTaskDuetoBw[value.getTaskType()]++;
				if (value.getVmType() == SimSettings.VM_TYPES.CLOUD_VM.ordinal())
					failedTaskDuetoWanBw[value.getTaskType()]++;
				else
					failedTaskDuetoLanBw[value.getTaskType()]++;

				if (fileLogEnabled && SimSettings.getInstance().getDeepFileLoggingEnabled())
					appendToFile(failBW, value.toString(key));
			} else if (value.getStatus() == SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_MOBILITY) {
				failedTaskDuetoMobility[value.getTaskType()]++;
				if (fileLogEnabled && SimSettings.getInstance().getDeepFileLoggingEnabled())
					appendToFile(failBW, value.toString(key));
			} else {
				uncompletedTask[value.getTaskType()]++;
				if (value.getVmType() == SimSettings.VM_TYPES.CLOUD_VM.ordinal())
					uncompletedTaskOnCloud[value.getTaskType()]++;
				else
					uncompletedTaskOnCloudlet[value.getTaskType()]++;
			}
		}
		
		// calculate total values
		uncompletedTask[numOfAppTypes] = IntStream.of(uncompletedTask).sum();
		uncompletedTaskOnCloud[numOfAppTypes] = IntStream.of(uncompletedTaskOnCloud).sum();
		uncompletedTaskOnCloudlet[numOfAppTypes] = IntStream.of(uncompletedTaskOnCloudlet).sum();

		completedTask[numOfAppTypes] = IntStream.of(completedTask).sum();
		completedTaskOnCloud[numOfAppTypes] = IntStream.of(completedTaskOnCloud).sum();
		completedTaskOnCloudlet[numOfAppTypes] = IntStream.of(completedTaskOnCloudlet).sum();

		failedTask[numOfAppTypes] = IntStream.of(failedTask).sum();
		failedTaskOnCloud[numOfAppTypes] = IntStream.of(failedTaskOnCloud).sum();
		failedTaskOnCloudlet[numOfAppTypes] = IntStream.of(failedTaskOnCloudlet).sum();

		networkDelay[numOfAppTypes] = DoubleStream.of(networkDelay).sum();
		lanDelay[numOfAppTypes] = DoubleStream.of(lanDelay).sum();
		wanDelay[numOfAppTypes] = DoubleStream.of(wanDelay).sum();

		serviceTime[numOfAppTypes] = DoubleStream.of(serviceTime).sum();
		serviceTimeOnCloud[numOfAppTypes] = DoubleStream.of(serviceTimeOnCloud).sum();
		serviceTimeOnCloudlet[numOfAppTypes] = DoubleStream.of(serviceTimeOnCloudlet).sum();

		processingTime[numOfAppTypes] = DoubleStream.of(processingTime).sum();
		processingTimeOnCloud[numOfAppTypes] = DoubleStream.of(processingTimeOnCloud).sum();
		processingTimeOnCloudlet[numOfAppTypes] = DoubleStream.of(processingTimeOnCloudlet).sum();

		cost[numOfAppTypes] = DoubleStream.of(cost).sum();
		failedTaskDuetoBw[numOfAppTypes] = IntStream.of(failedTaskDuetoBw).sum();
		failedTaskDuetoWanBw[numOfAppTypes] = IntStream.of(failedTaskDuetoWanBw).sum();
		failedTaskDuetoLanBw[numOfAppTypes] = IntStream.of(failedTaskDuetoLanBw).sum();
		failedTaskDuetoMobility[numOfAppTypes] = IntStream.of(failedTaskDuetoMobility).sum();
		rejectedTaskDoToVmCapacity[numOfAppTypes] = IntStream.of(rejectedTaskDoToVmCapacity).sum();
		
		totalDist[numOfAppTypes] = DoubleStream.of(totalDist).sum();
		totalHops[numOfAppTypes] = IntStream.of(totalHops).sum();
		numberOfAppTypes[numOfAppTypes] = taskMap.size();

		// calculate server load
		double totalVmLoad = 0;
		for (VmLoadLogItem entry : vmLoadList) {
			totalVmLoad += entry.getLoad();
			if (fileLogEnabled)
				appendToFile(vmLoadBW, entry.toString());
		}
		
		// Shaik added
		// calculate Average Mips utilization of all fog nodes		
		double totalFnMipsUtil = 0;		
		for (FNMipsUtilLogItem entry : fnMipsUtilList) {		
			totalFnMipsUtil += entry.getFnMipsUtil();	
			if (fileLogEnabled)	
				appendToFile(fnMipsUtilBW, entry.toString());
			
			// Capture metrics per layer
			fogLayerTotalMipsUtil[entry.getHostLevel()-1] += entry.getFnMipsUtil();
			fogLayerEntryMipsCount[entry.getHostLevel()-1]++;	
		}
		
		// Shaik added
		// calculate Average Network utilization of all fog nodes		
		double totalFnNwUtil = 0;		
		for (FNNwUtilLogItem entry : fnNwUtilList) {		
			totalFnNwUtil += entry.getFnNwUtil();	
			if (fileLogEnabled)	
				appendToFile(fnNwUtilBW, entry.toString());

			// Capture metrics per layer
			fogLayerTotalNwUtil[entry.getHostLevel()-1] += entry.getFnNwUtil();
			fogLayerEntryNwCount[entry.getHostLevel()-1]++;	
		}		

		// Shaik added
		// calculate Average node utilization per fog layer		
		for (int i=0; i < fogLayerAvgMipsUtil.length; i++ ) {
			fogLayerAvgMipsUtil[i] = fogLayerTotalMipsUtil[i] / fogLayerEntryMipsCount[i];
		}
		
		// Shaik added
		// calculate Average network utilization per fog layer		
		for (int i=0; i < fogLayerAvgNwUtil.length; i++ ) {
			fogLayerAvgNwUtil[i] = fogLayerTotalNwUtil[i] / fogLayerEntryNwCount[i];
		}

		
		
		
//		//Qian Write require data into file
//		//**********************************
//		//**********************************
//		PrintWriter netWritor = new PrintWriter(filePrefix + "_NetWorkDelay.txt", "UTF-8");
//		PrintWriter serviceWritor = new PrintWriter(filePrefix + "_ServiceTime.txt", "UTF-8");
//		PrintWriter processingWritor = new PrintWriter(filePrefix + "_ProcessingTime.txt", "UTF-8");
//		PrintWriter costWritor = new PrintWriter(filePrefix + "_Cost.txt", "UTF-8");
//		for (int i = 0; i < numOfAppTypes + 1; i++) {
//			double _serviceTime = (completedTask[i] == 0) ? 0.0 : (serviceTime[i] / (double) completedTask[i]);
//			double _networkDelay = (completedTask[i] == 0) ? 0.0 : (networkDelay[i] / (double) completedTask[i]);
//			double _processingTime = (completedTask[i] == 0) ? 0.0 : (processingTime[i] / (double) completedTask[i]);
//			double _cost = (completedTask[i] == 0) ? 0.0 : (cost[i] / (double) completedTask[i]);
//			netWritor.println("NetworkDelay=" + networkDelay[i] + "\t" + "TaskNumber=" + completedTask[i] + "\t" + "Average=" + _networkDelay);
//			serviceWritor.println("ServiceTime=" + serviceTime[i] + "\t" + "TaskNumber=" + completedTask[i] + "\t" + "Average=" + _serviceTime);
//			processingWritor.println("ProcessingTime=" + processingTime[i] + "\t" + "TaskNumber=" + completedTask[i] + "\t" + "Average=" + _processingTime);
//			costWritor.println("Cost=" + cost[i] + "\t" + "TaskNumber=" + completedTask[i] + "\t" + "Average=" + _cost);
//		}
//		netWritor.close();
//		serviceWritor.close();
//		processingWritor.close();
//		costWritor.close();
		//**********************************
		//**********************************
		if (fileLogEnabled) {
			// write location info to file
			for (int t = 1; t < (SimSettings.getInstance().getSimulationTime()
					/ SimSettings.getInstance().getVmLocationLogInterval()); t++) {
				int[] locationInfo = new int[SimSettings.PLACE_TYPES.values().length];
				Double time = t * SimSettings.getInstance().getVmLocationLogInterval();

				if (time < SimSettings.getInstance().getWarmUpPeriod())
					continue;

				for (int i = 0; i < SimManager.getInstance().getNumOfMobileDevice(); i++) {

					Location loc = SimManager.getInstance().getMobilityModel().getLocation(i, time);
					SimSettings.PLACE_TYPES placeType = loc.getPlaceType();
					//locationInfo[placeType.ordinal()]++;
				}

				locationBW.write(time.toString());
				for (int i = 0; i < locationInfo.length; i++)
					locationBW.write(SimSettings.DELIMITER + locationInfo[i]);

				locationBW.newLine();
			}

			for (int i = 0; i < numOfAppTypes + 1; i++) {

				if (i < numOfAppTypes) {
					// if related app is not used in this simulation, just
					// discard it
					if (SimSettings.getInstance().getTaskLookUpTable()[i][0] == 0)
						continue;
				}

				// check if the divisor is zero in order to avoid division by
				// zero problem
				double _serviceTime = (completedTask[i] == 0) ? 0.0 : (serviceTime[i] / (double) completedTask[i]);
				double _networkDelay = (completedTask[i] == 0) ? 0.0 : (networkDelay[i] / (double) completedTask[i]);
				double _processingTime = (completedTask[i] == 0) ? 0.0 : (processingTime[i] / (double) completedTask[i]);
				double _vmLoad = (vmLoadList.size() == 0) ? 0.0 : (totalVmLoad / (double) vmLoadList.size());
				double _fnMipsUtil = (fnMipsUtilList.size() == 0) ? 0.0 : (totalFnMipsUtil / (double) fnMipsUtilList.size());				
				double _fnNwUtil = (fnNwUtilList.size() == 0) ? 0.0 : (totalFnNwUtil / (double) fnNwUtilList.size());				
				double _cost = (completedTask[i] == 0) ? 0.0 : (cost[i] / (double) completedTask[i]);
				double dist = (numberOfAppTypes[i] == 0) ? 0.0 : (totalDist[i] / (double) numberOfAppTypes[i]);
				double hops = (numberOfAppTypes[i] == 0) ? 0.0 : ((double) totalHops[i] / (double) numberOfAppTypes[i]);

				// write generic results
				String genericResult1 = Integer.toString(completedTask[i]) + SimSettings.DELIMITER
						+ Integer.toString(failedTask[i]) + SimSettings.DELIMITER 
						+ Integer.toString(uncompletedTask[i]) + SimSettings.DELIMITER 
						+ Integer.toString(failedTaskDuetoBw[i]) + SimSettings.DELIMITER
						+ Double.toString(_serviceTime) + SimSettings.DELIMITER 
						+ Double.toString(_processingTime) + SimSettings.DELIMITER 
						+ Double.toString(_networkDelay) + SimSettings.DELIMITER
						+ Double.toString(_vmLoad) + SimSettings.DELIMITER 
						+ Double.toString(_cost) + SimSettings.DELIMITER 
						+ Integer.toString(rejectedTaskDoToVmCapacity[i]) + SimSettings.DELIMITER 
						+ Integer.toString(failedTaskDuetoMobility[i]) + SimSettings.DELIMITER
						+ Double.toString(_fnMipsUtil) + SimSettings.DELIMITER 
						+ Double.toString(_fnNwUtil);

				// check if the divisor is zero in order to avoid division by
				// zero problem
				double _lanDelay = (completedTaskOnCloudlet[i] == 0) ? 0.0
						: (lanDelay[i] / (double) completedTaskOnCloudlet[i]);
				double _serviceTimeOnCloudlet = (completedTaskOnCloudlet[i] == 0) ? 0.0
						: (serviceTimeOnCloudlet[i] / (double) completedTaskOnCloudlet[i]);
				double _processingTimeOnCloudlet = (completedTaskOnCloudlet[i] == 0) ? 0.0
						: (processingTimeOnCloudlet[i] / (double) completedTaskOnCloudlet[i]);
				String genericResult2 = Integer.toString(completedTaskOnCloudlet[i]) + SimSettings.DELIMITER
						+ Integer.toString(failedTaskOnCloudlet[i]) + SimSettings.DELIMITER
						+ Integer.toString(uncompletedTaskOnCloudlet[i]) + SimSettings.DELIMITER
						+ Integer.toString(failedTaskDuetoLanBw[i]) + SimSettings.DELIMITER
						+ Double.toString(_serviceTimeOnCloudlet) + SimSettings.DELIMITER
						+ Double.toString(_processingTimeOnCloudlet) + SimSettings.DELIMITER
						+ Double.toString(_lanDelay);

				// check if the divisor is zero in order to avoid division by
				// zero problem
				double _wanDelay = (completedTaskOnCloud[i] == 0) ? 0.0
						: (wanDelay[i] / (double) completedTaskOnCloud[i]);
				double _serviceTimeOnCloud = (completedTaskOnCloud[i] == 0) ? 0.0
						: (serviceTimeOnCloud[i] / (double) completedTaskOnCloud[i]);
				double _processingTimeOnCloud = (completedTaskOnCloud[i] == 0) ? 0.0
						: (processingTimeOnCloud[i] / (double) completedTaskOnCloud[i]);
				String genericResult3 = Integer.toString(completedTaskOnCloud[i]) + SimSettings.DELIMITER
						+ Integer.toString(failedTaskOnCloud[i]) + SimSettings.DELIMITER
						+ Integer.toString(uncompletedTaskOnCloud[i]) + SimSettings.DELIMITER
						+ Integer.toString(failedTaskDuetoWanBw[i]) + SimSettings.DELIMITER
						+ Double.toString(_serviceTimeOnCloud) + SimSettings.DELIMITER
						+ Double.toString(_processingTimeOnCloud) + SimSettings.DELIMITER 
						+ Double.toString(_wanDelay);
				
				String genericResult4 = Double.toString(dist) + SimSettings.DELIMITER + Double.toString(hops);

				appendToFile(genericBWs[i], genericResult1);
				appendToFile(genericBWs[i], genericResult2);
				appendToFile(genericBWs[i], genericResult3);
				appendToFile(genericBWs[i], genericResult4);
			}

			// close open files
			if (SimSettings.getInstance().getDeepFileLoggingEnabled()) {
				successBW.close();
				failBW.close();
			}
			vmLoadBW.close();
			fnMipsUtilBW.close(); // Shaik added
			fnNwUtilBW.close(); // Shaik added
			locationBW.close();
			for (int i = 0; i < numOfAppTypes + 1; i++) {
				if (i < numOfAppTypes) {
					// if related app is not used in this simulation, just
					// discard it
					if (SimSettings.getInstance().getTaskLookUpTable()[i][0] == 0)
						continue;
				}
				genericBWs[i].close();
			}
		}
		printLine("Mobile Devices Moving? : " + SimSettings.getInstance().areMobileDevicesMoving());
		// printout important results
		printLine("# of tasks: " + (failedTask[numOfAppTypes] + completedTask[numOfAppTypes]));
		
		// Do not provide info regarding warmup tasks; Commenting line below. - Shaik modified
		// Qian print warm up task
		//printLine("# of warm up tasks: "+ warmUpTasks);
		
		printLine("# of failed tasks: " + failedTask[numOfAppTypes]);
		
		printLine("# of completed tasks: " + completedTask[numOfAppTypes]);
		
		printLine("# of uncompleted tasks: " + uncompletedTask[numOfAppTypes]);
		
		printLine("# of failed tasks due to vm capacity/LAN bw/WAN bw/mobility: "
				+ rejectedTaskDoToVmCapacity[numOfAppTypes]
				+ "/" + +failedTaskDuetoLanBw[numOfAppTypes] 
				+ "/" + +failedTaskDuetoWanBw[numOfAppTypes] 
				+ "/" + failedTaskDuetoMobility[numOfAppTypes]);
		
		printLine("percentage of failed tasks: "
				+ String.format("%.6f", ((double) failedTask[numOfAppTypes] * (double) 100)
						/ (double) (completedTask[numOfAppTypes] + failedTask[numOfAppTypes]))
				+ "%");

		// Shaik modified the three lines below.
		/*printLine("average service time: "
				+ String.format("%.6f", serviceTime[numOfAppTypes] / (double) completedTask[numOfAppTypes])
				+ " seconds. (" + "on Cloudlet: "
				+ String.format("%.6f", serviceTimeOnCloudlet[numOfAppTypes] / (double) completedTaskOnCloudlet[numOfAppTypes])
				+ ", " + "on Cloud: "
				+ String.format("%.6f", serviceTimeOnCloud[numOfAppTypes] / (double) completedTaskOnCloud[numOfAppTypes])
				+ ")");
		*/
		printLine("average service time: "
				+ String.format("%.6f", serviceTime[numOfAppTypes] / (double) completedTask[numOfAppTypes])
				+ " seconds.");

		/*printLine("average processing time: "
				+ String.format("%.6f", processingTime[numOfAppTypes] / (double) completedTask[numOfAppTypes])
				+ " seconds. (" + "on Cloudlet: "
				+ String.format("%.6f", processingTimeOnCloudlet[numOfAppTypes] / (double) completedTaskOnCloudlet[numOfAppTypes])
				+ ", " + "on Cloud: " 
				+ String.format("%.6f", processingTimeOnCloud[numOfAppTypes] / (double) completedTaskOnCloud[numOfAppTypes])
				+ ")");
		*/
	
		printLine("average processing time: "
				+ String.format("%.6f", processingTime[numOfAppTypes] / (double) completedTask[numOfAppTypes])
				+ " seconds.");

		/*printLine("average network delay: "
				+ String.format("%.6f", networkDelay[numOfAppTypes] / (double) completedTask[numOfAppTypes])
				+ " seconds. (" + "LAN delay: "
				+ String.format("%.6f", lanDelay[numOfAppTypes] / (double) completedTaskOnCloudlet[numOfAppTypes])
				+ ", " + "WAN delay: "
				+ String.format("%.6f", wanDelay[numOfAppTypes] / (double) completedTaskOnCloud[numOfAppTypes]) + ")");
		*/
		
		printLine("average network delay: "
				+ String.format("%.6f", networkDelay[numOfAppTypes] / (double) completedTask[numOfAppTypes])
				+ " seconds.");

		printLine("average server utilization: " 
				+ String.format("%.6f", totalVmLoad / (double) vmLoadList.size() * 100) + "%"); // Shaik modified '*100'
		printLine("average Fog server utilization: " 
				+ String.format("%.6f", totalFnMipsUtil / (double) fnMipsUtilList.size()) + "%"); // Shaik added
		printLine("average Fog network utilization: " 
				+ String.format("%.6f", totalFnNwUtil / (double) fnNwUtilList.size()) + "%"); // Shaik added

		printLine("Tasks executed per fog layer: ");
		for(int i = 1; i <= SimSettings.getInstance().getMaxLevels(); i++) //From 1 to MAX_LEVELS because there won't be network apps running on mobile devices at level 0
			printLine(String.format("\tLevel %d:\t%d", i, levelCloudletCount[i]));
		
		//Clayton changed this so it would output a value based on the average cost per time I was seeing in the XML files
		//	this value may mean absolutely nothing so we should look into it more if we want to use it
		printLine("average cost: $" + String.format("%.2f", processingTime[numOfAppTypes] / (double) completedTask[numOfAppTypes]));
		printLine("ProcessingTime: " + processingTime[numOfAppTypes]);
		printLine("CompletedTask: " + completedTask[numOfAppTypes]);
		printLine("Average Distance from task to host: " + String.format("%.2f", totalDist[numOfAppTypes]/((double) taskMap.size())));
		printLine("Average number of hops from task to host: " + String.format("%.2f",((double) totalHops[numOfAppTypes])/((double) taskMap.size())));
		
		//Qian print average fog nodes utilization in each level.
		getTotalFogNodesCountInEachLevel();
		//printLine("average fog node utilization:"); // Shaik commented
		printLine("Percentage of fog nodes executing atleast one task:"); // Shaik modified
		for (int i = 0; i < 7; i++) {
			printLine("\tLevel " + (i + 1) + ": " + String.format("%.6f", ((double)levelFogNodeCount[i] / (double)totalNodesNmuberInEachLevel[i])));
		}
		
		printLine("Average fog node utilization per layer:"); // Shaik added
		for (int i = 0; i < fogLayerAvgMipsUtil.length; i++) {
			printLine("\tLevel " + (i + 1) + ": " + String.format("%.6f", ((double)fogLayerAvgMipsUtil[i])));
		}

		printLine("Average fog network utilization per layer:"); // Shaik added
		for (int i = 0; i < fogLayerAvgNwUtil.length; i++) {
			printLine("\tLevel " + (i + 1) + ": " + String.format("%.6f", ((double)fogLayerAvgNwUtil[i])));
		}
		
		// clear related collections (map list etc.)
		taskMap.clear();
		vmLoadList.clear();
		fnMipsUtilList.clear(); // Shaik added
		fnNwUtilList.clear(); // Shaik added
		centerFileW.close();
	}

	/**
	 * @return the printLogEnabled
	 */
	public static boolean isPrintLogEnabled() {
		return printLogEnabled;
	}

	/**
	 * @param printLogEnabled the printLogEnabled to set
	 */
	public static void setPrintLogEnabled(boolean printLogEnabled) {
		SimLogger.printLogEnabled = printLogEnabled;
	}

	/**
	 * @return the filePrefix
	 */
	public String getFilePrefix() {
		return filePrefix;
	}

	/**
	 * @param filePrefix the filePrefix to set
	 */
	public void setFilePrefix(String filePrefix) {
		this.filePrefix = filePrefix;
	}

	/**
	 * @return the outputFolder
	 */
	public String getOutputFolder() {
		return outputFolder;
	}

	/**
	 * @param outputFolder the outputFolder to set
	 */
	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	/**
	 * @return the taskMap
	 */
	public Map<Integer, LogItem> getTaskMap() {
		return taskMap;
	}

	/**
	 * @param taskMap the taskMap to set
	 */
	public void setTaskMap(Map<Integer, LogItem> taskMap) {
		this.taskMap = taskMap;
	}

	/**
	 * @return the vmLoadList
	 */
	public LinkedList<VmLoadLogItem> getVmLoadList() {
		return vmLoadList;
	}

	/**
	 * @param vmLoadList the vmLoadList to set
	 */
	public void setVmLoadList(LinkedList<VmLoadLogItem> vmLoadList) {
		this.vmLoadList = vmLoadList;
	}

	/**
	 * @return the centerLogFile
	 */
	public File getCenterLogFile() {
		return centerLogFile;
	}

	/**
	 * @param centerLogFile the centerLogFile to set
	 */
	public void setCenterLogFile(File centerLogFile) {
		this.centerLogFile = centerLogFile;
	}

	/**
	 * @return the centerFileW
	 */
	public PrintWriter getCenterFileW() {
		return centerFileW;
	}

	/**
	 * @param centerFileW the centerFileW to set
	 */
	public void setCenterFileW(PrintWriter centerFileW) {
		this.centerFileW = centerFileW;
	}

	/**
	 * @return the utlizationArray
	 */
	public ArrayList<Integer> getUtlizationArray() {
		return utlizationArray;
	}

	/**
	 * @param utlizationArray the utlizationArray to set
	 */
	public void setUtlizationArray(ArrayList<Integer> utlizationArray) {
		this.utlizationArray = utlizationArray;
	}

	/**
	 * @return the singleton
	 */
	public static SimLogger getSingleton() {
		return singleton;
	}

	/**
	 * @param singleton the singleton to set
	 */
	public static void setSingleton(SimLogger singleton) {
		SimLogger.singleton = singleton;
	}

	/**
	 * @return the totalNodesNmuberInEachLevel
	 */
	public int[] getTotalNodesNmuberInEachLevel() {
		return totalNodesNmuberInEachLevel;
	}

	/**
	 * @param totalNodesNmuberInEachLevel the totalNodesNmuberInEachLevel to set
	 */
	public void setTotalNodesNmuberInEachLevel(int[] totalNodesNmuberInEachLevel) {
		this.totalNodesNmuberInEachLevel = totalNodesNmuberInEachLevel;
	}

	/**
	 * @return the levelFogNodeCount
	 */
	public int[] getLevelFogNodeCount() {
		return levelFogNodeCount;
	}

	/**
	 * @param levelFogNodeCount the levelFogNodeCount to set
	 */
	public void setLevelFogNodeCount(int[] levelFogNodeCount) {
		this.levelFogNodeCount = levelFogNodeCount;
	}

	/**
	 * @return the levelCloudletCount
	 */
	public int[] getLevelCloudletCount() {
		return levelCloudletCount;
	}

	/**
	 * @param levelCloudletCount the levelCloudletCount to set
	 */
	public void setLevelCloudletCount(int[] levelCloudletCount) {
		this.levelCloudletCount = levelCloudletCount;
	}

	/**
	 * @param fileLogEnabled the fileLogEnabled to set
	 */
	public static void setFileLogEnabled(boolean fileLogEnabled) {
		SimLogger.fileLogEnabled = fileLogEnabled;
	}
	
}

class VmLoadLogItem {
	private double time;
	private double vmLoad;

	VmLoadLogItem(double _time, double _vmLoad) {
		time = _time;
		vmLoad = _vmLoad;
	}

	public double getLoad() {
		return vmLoad;
	}

	public String toString() {
		return time + SimSettings.DELIMITER + vmLoad;
	}
}

/**
 * @author szs0117
 *
 */
class FNMipsUtilLogItem {								
	private double time;							
	private int hostId;							
	private int hostLevel;							
	private double fnMipsUtil;							
								
	FNMipsUtilLogItem(double _time, int _hostId, int _hostLevel, double _fnMipsUtil) {							
		time = _time;						
		hostId = _hostId;						
		hostLevel = _hostLevel;						
		fnMipsUtil = _fnMipsUtil;						
	}							

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FNMipsUtilLogItem [time=" + time + ", hostId=" + hostId + ", hostLevel=" + hostLevel + ", fnMipsUtil="
				+ fnMipsUtil + "]";
	}

	/**
	 * @return the time
	 */
	public double getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(double time) {
		this.time = time;
	}

	/**
	 * @return the hostId
	 */
	public int getHostId() {
		return hostId;
	}

	/**
	 * @param hostId the hostId to set
	 */
	public void setHostId(int hostId) {
		this.hostId = hostId;
	}

	/**
	 * @return the hostLevel
	 */
	public int getHostLevel() {
		return hostLevel;
	}

	/**
	 * @param hostLevel the hostLevel to set
	 */
	public void setHostLevel(int hostLevel) {
		this.hostLevel = hostLevel;
	}

	/**
	 * @return the fnMipsUtil
	 */
	public double getFnMipsUtil() {
		return fnMipsUtil;
	}

	/**
	 * @param fnMipsUtil the fnMipsUtil to set
	 */
	public void setFnMipsUtil(double fnMipsUtil) {
		this.fnMipsUtil = fnMipsUtil;
	}
	
}								


/**
 * @author szs0117
 *
 */
class FNNwUtilLogItem {								
	private double time;							
	private int hostId;							
	private int hostLevel;							
	private double fnNwUtil;
	
	/**
	 * @param time
	 * @param hostId
	 * @param hostLevel
	 * @param fnNwUtil
	 */
	public FNNwUtilLogItem(double time, int hostId, int hostLevel, double fnNwUtil) {
		super();
		this.time = time;
		this.hostId = hostId;
		this.hostLevel = hostLevel;
		this.fnNwUtil = fnNwUtil;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FNNwUtilLogItem [time=" + time + ", hostId=" + hostId + ", hostLevel=" + hostLevel + ", fnNwUtil="
				+ fnNwUtil + "]";
	}

	/**
	 * @return the time
	 */
	public double getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(double time) {
		this.time = time;
	}

	/**
	 * @return the hostId
	 */
	public int getHostId() {
		return hostId;
	}

	/**
	 * @param hostId the hostId to set
	 */
	public void setHostId(int hostId) {
		this.hostId = hostId;
	}

	/**
	 * @return the hostLevel
	 */
	public int getHostLevel() {
		return hostLevel;
	}

	/**
	 * @param hostLevel the hostLevel to set
	 */
	public void setHostLevel(int hostLevel) {
		this.hostLevel = hostLevel;
	}

	/**
	 * @return the fnNwUtil
	 */
	public double getFnNwUtil() {
		return fnNwUtil;
	}

	/**
	 * @param fnNwUtil the fnNwUtil to set
	 */
	public void setFnNwUtil(double fnNwUtil) {
		this.fnNwUtil = fnNwUtil;
	}			

}

class LogItem {
	/**
	 * @return the datacenterId
	 */
	public int getDatacenterId() {
		return datacenterId;
	}

	/**
	 * @param datacenterId the datacenterId to set
	 */
	public void setDatacenterId(int datacenterId) {
		this.datacenterId = datacenterId;
	}

	/**
	 * @return the hostId
	 */
	public int getHostId() {
		return hostId;
	}

	/**
	 * @param hostId the hostId to set
	 */
	public void setHostId(int hostId) {
		this.hostId = hostId;
	}

	/**
	 * @return the vmId
	 */
	public int getVmId() {
		return vmId;
	}

	/**
	 * @param vmId the vmId to set
	 */
	public void setVmId(int vmId) {
		this.vmId = vmId;
	}

	/**
	 * @return the taskLenght
	 */
	public int getTaskLenght() {
		return taskLenght;
	}

	/**
	 * @param taskLenght the taskLenght to set
	 */
	public void setTaskLenght(int taskLenght) {
		this.taskLenght = taskLenght;
	}

	/**
	 * @return the taskInputType
	 */
	public int getTaskInputType() {
		return taskInputType;
	}

	/**
	 * @param taskInputType the taskInputType to set
	 */
	public void setTaskInputType(int taskInputType) {
		this.taskInputType = taskInputType;
	}

	/**
	 * @return the taskOutputSize
	 */
	public int getTaskOutputSize() {
		return taskOutputSize;
	}

	/**
	 * @param taskOutputSize the taskOutputSize to set
	 */
	public void setTaskOutputSize(int taskOutputSize) {
		this.taskOutputSize = taskOutputSize;
	}

	/**
	 * @return the numberOfHops
	 */
	public int getNumberOfHops() {
		return numberOfHops;
	}

	/**
	 * @param numberOfHops the numberOfHops to set
	 */
	public void setNumberOfHops(int numberOfHops) {
		this.numberOfHops = numberOfHops;
	}

	/**
	 * @return the taskStartTime
	 */
	public double getTaskStartTime() {
		return taskStartTime;
	}

	/**
	 * @param taskStartTime the taskStartTime to set
	 */
	public void setTaskStartTime(double taskStartTime) {
		this.taskStartTime = taskStartTime;
	}

	/**
	 * @return the taskEndTime
	 */
	public double getTaskEndTime() {
		return taskEndTime;
	}

	/**
	 * @param taskEndTime the taskEndTime to set
	 */
	public void setTaskEndTime(double taskEndTime) {
		this.taskEndTime = taskEndTime;
	}

	/**
	 * @return the bwCost
	 */
	public double getBwCost() {
		return bwCost;
	}

	/**
	 * @param bwCost the bwCost to set
	 */
	public void setBwCost(double bwCost) {
		this.bwCost = bwCost;
	}

	/**
	 * @return the cpuCost
	 */
	public double getCpuCost() {
		return cpuCost;
	}

	/**
	 * @param cpuCost the cpuCost to set
	 */
	public void setCpuCost(double cpuCost) {
		this.cpuCost = cpuCost;
	}

	/**
	 * @return the taskCost
	 */
	public double getTaskCost() {
		return taskCost;
	}

	/**
	 * @param taskCost the taskCost to set
	 */
	public void setTaskCost(double taskCost) {
		this.taskCost = taskCost;
	}

	/**
	 * @return the distanceToHost
	 */
	public double getDistanceToHost() {
		return distanceToHost;
	}

	/**
	 * @param distanceToHost the distanceToHost to set
	 */
	public void setDistanceToHost(double distanceToHost) {
		this.distanceToHost = distanceToHost;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(SimLogger.TASK_STATUS status) {
		this.status = status;
	}

	/**
	 * @param vmType the vmType to set
	 */
	public void setVmType(int vmType) {
		this.vmType = vmType;
	}

	/**
	 * @param taskType the taskType to set
	 */
	public void setTaskType(int taskType) {
		this.taskType = taskType;
	}

	/**
	 * @param networkDelay the networkDelay to set
	 */
	public void setNetworkDelay(double networkDelay) {
		this.networkDelay = networkDelay;
	}

	/**
	 * @param isInWarmUpPeriod the isInWarmUpPeriod to set
	 */
	public void setInWarmUpPeriod(boolean isInWarmUpPeriod) {
		this.isInWarmUpPeriod = isInWarmUpPeriod;
	}

	private SimLogger.TASK_STATUS status;
	private int datacenterId;
	private int hostId;
	private int vmId;
	private int vmType;
	private int taskType;
	private int taskLenght;
	private int taskInputType;
	private int taskOutputSize;
	private int numberOfHops;
	private double taskStartTime;
	private double taskEndTime;
	private double networkDelay;
	private double bwCost;
	private double cpuCost;
	private boolean isInWarmUpPeriod;
	private double taskCost = 0;
	private double distanceToHost;
	
	LogItem(double _taskStartTime, int _taskType, int _taskLenght, int _taskInputType, int _taskOutputSize) {
		taskStartTime = _taskStartTime;
		taskType = _taskType;
		taskLenght = _taskLenght;
		taskInputType = _taskInputType;
		taskOutputSize = _taskOutputSize;
		status = SimLogger.TASK_STATUS.CREATED;
		taskEndTime = 0;

		if (_taskStartTime < SimSettings.getInstance().getWarmUpPeriod())
			isInWarmUpPeriod = true;
		else
			isInWarmUpPeriod = false;
	}
	
	public void taskUploadStarted(double taskUploadTime) {
		networkDelay += taskUploadTime;
		status = SimLogger.TASK_STATUS.UPLOADING;
	}

	public void taskUploaded(int _datacenterId, int _hostId, int _vmId, int _vmType) {
		status = SimLogger.TASK_STATUS.PROCESSING;
		datacenterId = _datacenterId;
		hostId = _hostId;
		vmId = _vmId;
		vmType = _vmType;
	}

	public void taskDownloadStarted(double taskDownloadTime) {
		networkDelay += taskDownloadTime;
		status = SimLogger.TASK_STATUS.DOWNLOADING;
	}

	public void taskDownloaded(double _taskEndTime) {
		taskEndTime = _taskEndTime;
		status = SimLogger.TASK_STATUS.COMPLETED;
	}
	
	public void taskDownloaded(double _taskEndTime, double cost) {
		taskEndTime = _taskEndTime;
		taskCost = cost;
		status = SimLogger.TASK_STATUS.COMPLETED;
	}

	public void taskRejectedDueToVMCapacity(double _taskRejectTime) {
		taskEndTime = _taskRejectTime;
		status = SimLogger.TASK_STATUS.REJECTED_DUE_TO_VM_CAPACITY;
	}

	public void taskRejectedDueToBandwidth(double _taskRejectTime, int _vmType) {
		vmType = _vmType;
		taskEndTime = _taskRejectTime;
		status = SimLogger.TASK_STATUS.REJECTED_DUE_TO_BANDWIDTH;
	}

	public void taskFailedDueToBandwidth(double _time) {
		taskEndTime = _time;
		status = SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_BANDWIDTH;
	}

	public void taskFailedDueToMobility(double _time) {
		taskEndTime = _time;
		status = SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_MOBILITY;
	}

	public void setCost(double _bwCost, double _cpuCos) {
		bwCost = _bwCost;
		cpuCost = _cpuCos;
	}

	public boolean isInWarmUpPeriod() {
		return isInWarmUpPeriod;
	}

	public double getCost() {
		//return bwCost + cpuCost;
		return taskCost;
	}

	public double getNetworkDelay() {
		return networkDelay;
	}

	public double getServiceTime() {
		return taskEndTime - taskStartTime;
	}

	public SimLogger.TASK_STATUS getStatus() {
		return status;
	}

	public int getVmType() {
		return vmType;
	}

	public int getTaskType() {
		return taskType;
	}

	public String toString(int taskId) {
		String result = taskId + SimSettings.DELIMITER + datacenterId + SimSettings.DELIMITER + hostId
				+ SimSettings.DELIMITER + vmId + SimSettings.DELIMITER + vmType + SimSettings.DELIMITER + taskType
				+ SimSettings.DELIMITER + taskLenght + SimSettings.DELIMITER + taskInputType + SimSettings.DELIMITER
				+ taskOutputSize + SimSettings.DELIMITER + taskStartTime + SimSettings.DELIMITER + taskEndTime
				+ SimSettings.DELIMITER;

		if (status == SimLogger.TASK_STATUS.COMPLETED)
			result += networkDelay;
		else if (status == SimLogger.TASK_STATUS.REJECTED_DUE_TO_VM_CAPACITY)
			result += "1"; // failure reason 1
		else if (status == SimLogger.TASK_STATUS.REJECTED_DUE_TO_BANDWIDTH)
			result += "2"; // failure reason 2
		else if (status == SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_BANDWIDTH)
			result += "3"; // failure reason 3
		else if (status == SimLogger.TASK_STATUS.UNFINISHED_DUE_TO_MOBILITY)
			result += "4"; // failure reason 4
		else
			result += "0"; // default failure reason
		return result;
	}
	
	public double getHostDist() {
		return distanceToHost;
	}
	
	public void setDistance(double in) {
		distanceToHost = in;
	}
	
	public int getHops() {
		return numberOfHops;
	}
	
	public void setHops(int in) {
		numberOfHops = in;
	}



}