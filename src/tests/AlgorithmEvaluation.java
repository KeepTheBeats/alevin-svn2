/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2010-2011, The VNREAL Project Team.
 * 
 * This work has been funded by the European FP7
 * Network of Excellence "Euro-NF" (grant agreement no. 216366)
 * through the Specific Joint Developments and Experiments Project
 * "Virtual Network Resource Embedding Algorithms" (VNREAL). 
 *
 * The VNREAL Project Team consists of members from:
 * - University of Wuerzburg, Germany
 * - Universitat Politecnica de Catalunya, Spain
 * - University of Passau, Germany
 * See the file AUTHORS for details and contact information.
 * 
 * This file is part of ALEVIN (ALgorithms for Embedding VIrtual Networks).
 *
 * ALEVIN is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License Version 3 or later
 * (the "GPL"), or the GNU Lesser General Public License Version 3 or later
 * (the "LGPL") as published by the Free Software Foundation.
 *
 * ALEVIN is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * or the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License and
 * GNU Lesser General Public License along with ALEVIN; see the file
 * COPYING. If not, see <http://www.gnu.org/licenses/>.
 *
 * ***** END LICENSE BLOCK ***** */
package tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cern.colt.Arrays;
import tests.generators.constraints.ConstraintsGenerator;
import tests.generators.network.NetworkGenerator;
import tests.generators.network.NetworkGeneratorParameter;
import tests.generators.network.NetworkGeneratorParameters;
import tests.io.CSVPrintWriterDataReceiver;
import tests.scenarios.AbstractScenarioTest;
import vnreal.algorithms.AlgorithmParameter;
import vnreal.algorithms.utils.SubgraphBasicVN.Utils;
import vnreal.constraints.demands.AbstractDemand;
import vnreal.constraints.resources.AbstractResource;
import vnreal.core.Consts;
import vnreal.evaluations.metrics.EvaluationMetric;
import vnreal.io.GraphMLExporter;
import vnreal.io.XMLExporter;
import vnreal.io.XMLImporter;
import vnreal.network.IDSource;
import vnreal.network.Network;
import vnreal.network.NetworkStack;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;
import vnreal.network.virtual.VirtualLink;
import vnreal.network.virtual.VirtualNetwork;
import vnreal.network.virtual.VirtualNode;

public final class AlgorithmEvaluation {

	public static boolean DEBUG = false;
	public static final String MAIL_NOTIFICATION_TOOL = "/home/beck/workspace/Documents/Linux/sendmail.py";

	protected final NetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> sNetGenerator;
	protected final NetworkGenerator<AbstractDemand, VirtualNode, VirtualLink, VirtualNetwork> vNetGenerator;
	protected final boolean generateDuplicateEdges;

	public AlgorithmEvaluation(
			NetworkGenerator<AbstractResource, SubstrateNode, SubstrateLink, SubstrateNetwork> sNetGenerator,
			NetworkGenerator<AbstractDemand, VirtualNode, VirtualLink, VirtualNetwork> vNetGenerator,
			boolean generateDuplicateEdges) {

		this.sNetGenerator = sNetGenerator;
		this.vNetGenerator = vNetGenerator;
		this.generateDuplicateEdges = generateDuplicateEdges;
	}

	public static void execSysinfoTool(String name) {
		java.lang.Runtime r = java.lang.Runtime.getRuntime();
		try {
			r.exec(Consts.SYSINFOTOOL + " " + name + "_sysinfo.txt").waitFor();
		} catch (IOException e1) {
			throw new AssertionError();
		} catch (InterruptedException e) {
			throw new AssertionError();
		}
	}

	public static void execMailNotificationTool(String subject, String content) {
		java.lang.Runtime r = java.lang.Runtime.getRuntime();
		try {
			r.exec(new String[] {MAIL_NOTIFICATION_TOOL, "--subject", subject, "--content", content}).waitFor();
		} catch (IOException e1) {
			throw new AssertionError();
		} catch (InterruptedException e) {
			throw new AssertionError();
		}
	}

	public void executeTests(String name,
			boolean export, int numScenarios, long maxRuntimeInSeconds,
			DistributionParameter distributionParameter,
			NetworkGeneratorParameters sNetParams,
			ConstraintsGenerator<SubstrateNetwork> sNetConstraintsGenerator,
			NetworkGeneratorParameters vNetParams,
			Integer[] numVNetsArray,
			ConstraintsGenerator<VirtualNetwork> vNetConstraintsGenerator,
			LinkedList<AbstractScenarioTest> algorithms)
					throws IOException, InterruptedException, ExecutionException {

		int numAlgoParams = 0;
		int maxEvents = 1;
		if (distributionParameter != null)
			maxEvents = distributionParameter.numEvents;

		LinkedList<Request> activeRequests = new LinkedList<Request>();
		HashMap<AbstractScenarioTest, CSVPrintWriterDataReceiver> resultwriters = new HashMap<AbstractScenarioTest, CSVPrintWriterDataReceiver>();
		
//		String fullname = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + name;
		String fullname = name;
		File dir = new File(Consts.RESULTS_DIR, fullname);
		if (dir.exists())
			throw new AssertionError("directory exists: " + dir.getName());
		if (!dir.mkdir())
			throw new AssertionError("cannot create directory: " + dir.getName());
		File xmldir = null;
		if (export) {
			xmldir = new File(dir.getAbsoluteFile(), "xml");
			if (!xmldir.mkdir())
				throw new AssertionError("cannot create directory: " + xmldir.getName());
		}

		HashMap<AbstractScenarioTest, List<AlgorithmParameter>> algoParams = new HashMap<AbstractScenarioTest, List<AlgorithmParameter>>();
		for (AbstractScenarioTest algo : algorithms) {
			LinkedList<AlgorithmParameter> params = algo.getAlgorithmParams();
			numAlgoParams += params.size();
			algoParams.put(algo, params);

			PrintWriter paramswriter = new PrintWriter(new FileWriter(dir.getAbsolutePath() + File.separator + algo.getName().replace("_",  "-") + "_params.txt", false), true);
			String algoPars = "";
			for (AlgorithmParameter p : params)
				if (p == null)
					algoPars += "algoParam = null\n";
				else
					algoPars += p.toString("algoParam.") + "\n";
			paramswriter.println("numScenarios: " + numScenarios
					+ "\nmaxRuntimeInSeconds: " + maxRuntimeInSeconds + "\n\n\n"
					+ (distributionParameter == null ? "distributionParameter:null" : distributionParameter.toString("distributionParameter.")) + "\n\n"
					+ sNetParams.toString("sNetParams.") + "\n\n"
					+ vNetParams.toString("vNetParams.") + "\n\n"
					+ "numVNetsArray: " + Arrays.toString(numVNetsArray) + "\n\n"
					+ algoPars);
			paramswriter.close();
		}
		
		
		HashMap<String, String> scenarioNames = new HashMap<String, String>();
		for (NetworkGeneratorParameter sNetparameter : sNetParams.getParams()) {
			for (int numVNFChains : numVNetsArray) {
				for (NetworkGeneratorParameter vNetparam : vNetParams.getParams()) {

					for (final AbstractScenarioTest algorithm : algorithms) {
						for (AlgorithmParameter algoParam : algoParams.get(algorithm)) {
							String preSuffix = getSuffix(sNetparameter, sNetConstraintsGenerator, numVNFChains, vNetparam, algorithm, algoParam);
							scenarioNames.put(preSuffix, null);
						}
					}
				}
			}
		}
		
//		vnreal.algorithms.Utils.getScenarioNames(scenarioNames);
		
		DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		

		int total = numScenarios * sNetParams.getParams().size() * numVNetsArray.length * vNetParams.getParams().size() * numAlgoParams * maxEvents;
		int pos = 0;
		int sNetparameternum;
		int vNetparamnum;
		int AbstractScenarioTestnum;
		int AlgorithmParameternum;
		for (int numScenario = 0; numScenario < numScenarios; numScenario++) {
			long seed = System.currentTimeMillis();

			for (NetworkGeneratorParameter sNetparameter : sNetParams.getParams()) {
				sNetparameternum = 0;
				for (int numVNets : numVNetsArray) {
					for (NetworkGeneratorParameter vNetparam : vNetParams.getParams()) {
						vNetparamnum = 0;
						algoFor: for (final AbstractScenarioTest algorithm : algorithms) {
							AbstractScenarioTestnum = 0;
							CSVPrintWriterDataReceiver resultwriter = resultwriters.get(algorithm);
							if (resultwriter == null) {
								resultwriter = new CSVPrintWriterDataReceiver(dir, algorithm, false);
								resultwriters.put(algorithm, resultwriter);
							}

							for (AlgorithmParameter algoParam : algoParams.get(algorithm)) {
								AlgorithmParameternum = 0;
								int layerCounter = 1;
								int numEvents = 0;
								long timeSlot = 0;
								Random sNetTopologyRandom = new Random(seed);
//								Random sNetConstraintsRandom = new Random(seed);
								Random eventRandom = new Random(seed);
//								Random algorithmRandom = new Random(seed);

								IDSource.reset();
								SubstrateNetwork sNet = this.sNetGenerator.generate(sNetTopologyRandom, sNetConstraintsGenerator, sNetparameter);
								if (this.generateDuplicateEdges)
									sNet.generateDuplicateEdges();

								if (algorithm.timeoutSNSize != -1 && algorithm.timeoutSNSize >= sNet.getVertexCount()) {
									System.out.println("skip");
									continue algoFor;
								}

								while (numEvents < maxEvents) {
									pos++;
									long nextEvent = 0;
									if (distributionParameter != null)
										nextEvent = timeSlot + Math.round(Utils.exponentialDistribution(eventRandom, distributionParameter.lambda));

									if (distributionParameter != null) {
										for (Request reqs : new LinkedList<Request>(activeRequests)) {
											if (reqs.termTime <= nextEvent) {
												// System.out.println("DESTROY " + reqs.initTime);
												activeRequests.remove(reqs);

												if (reqs.mappingResult != null) {
													for (VirtualNetwork v : reqs.mappingResult.getVirtuals()) {
														algorithm.unmap(sNet, v);
													}
												}
											}
										}
									}

									timeSlot = nextEvent;
									numEvents++;

									long termTime = -1;
									if (distributionParameter != null) {
										long duration = Math.round(Utils.exponentialDistribution(eventRandom, distributionParameter.mu_L));
										termTime = timeSlot + duration;
									}
									// System.out.println("INIT " + timeSlot  + " " + termTime + " ");
									Request request = new Request(timeSlot, termTime);
									activeRequests.add(request);

									LinkedList<VirtualNetwork> vNets = new LinkedList<VirtualNetwork>();
									for (int i = 0; i < numVNets; ++i) {
										Random vNetTopologyRandom = new Random(seed+i);
//										Random vNetConstraintsRandom = new Random(seed+i);
										this.vNetGenerator.setLayer(layerCounter++);

										VirtualNetwork vNet = this.vNetGenerator.generate(vNetTopologyRandom, vNetConstraintsGenerator, vNetparam);
//										if (vNetConstraintsGenerator != null)
//											vNetConstraintsGenerator.addConstraints(vNet, vNetConstraintsRandom);
										if (this.generateDuplicateEdges)
											vNet.generateDuplicateEdges();

										vNets.add(vNet);
									}


									String preSuffix = getSuffix(sNetparameter, sNetConstraintsGenerator, numVNets, vNetparam, algorithm, algoParam);
									String scenarioName = scenarioNames.get(preSuffix);
									String exportfilename = algorithm.getName().replace("_",  "-") + "_scenario:" + numScenario + "_timeSlot:" + timeSlot + (scenarioName == null ? "" : ("_" + scenarioName));
									String scenarioSuffix = "scenario:" + numScenario + "_timeSlot:" + timeSlot + (scenarioName == null ? "" : ("_" + scenarioName));
									System.out.println("[" + name + " " + dateFormat.format(new Date()) + ", " + pos + "/" + total + "] " + exportfilename);
									
									//import a .xml topo
									NetworkStack stack1 = XMLImporter.importScenario("/home/ubuntu/mine/DPVNE_alevin_experi/data/nrxml/import"+name+".xml").getNetworkStack();
//									NetworkStack stack1 = XMLImporter.importScenario("/home/ubuntu/mine/topo/import50.xml").getNetworkStack();
									NetworkStack stack2 = XMLImporter.importScenario("/home/ubuntu/mine/topo/Sc_0_sp_0_VN_5_vp_0_AT_0_AP_0.xml").getNetworkStack();
									SubstrateNetwork sNet1 = stack1.getSubstrate();
									SubstrateNetwork sNet2 = stack2.getSubstrate();
									LinkedList<VirtualNetwork> vNets1 = (LinkedList<VirtualNetwork>) stack1.getVirtuals();
									LinkedList<VirtualNetwork> vNets2 = (LinkedList<VirtualNetwork>) stack2.getVirtuals();
									//import end
									
									long startTime = System.currentTimeMillis();
									ExecutorService service = Executors.newSingleThreadExecutor();
									Future<NetworkStack> future = service.submit(new Callable<NetworkStack>() {
										@Override
										public NetworkStack call() {
											return algorithm.map(algoParam, sNet1, vNets1);
										}
									});

									NetworkStack stack = null;
									//NetworkStack stack1 = XMLImporter.importScenario("/home/ubuntu/mine/topo/Sc_0_sp_0_VN_5_vp_0_AT_0_AP_0.xml").getNetworkStack();
									//stack = XMLImporter.importScenario("/home/ubuntu/mine/topo/Sc_0_sp_0_VN_5_vp_0_AT_0_AP_0.xml").getNetworkStack();
									boolean timeout = false;
									try {
										if (future != null) {
											if (maxRuntimeInSeconds > -1) {
												stack = future.get(maxRuntimeInSeconds, TimeUnit.SECONDS);
											} else {
												stack = future.get();
												
											}
										}
										
//										String fileName = "/home/ubuntu/mine/topo/Sc_"+numScenario+"_sp_"+sNetparameternum+"_VN_"+Integer.toString(numVNets)+"_vp_"+vNetparamnum+"_AT_"+AbstractScenarioTestnum+"_AP_"+AlgorithmParameternum+".xml";
//										//if (!fileName.endsWith(".xml")) {
//										//	fileName = fileName + ".xml";
//										//}
//
//										try {
//											XMLExporter.exportStack(fileName, stack);
//										} catch (Exception ex) {
//											// TODO why this catch-all for XML export?
//											ex.printStackTrace();
//										}
										
									} catch(TimeoutException e) {
										timeout = true;
										algorithm.timeoutSNSize = sNet.getVertexCount();
										
//										future.cancel(true);
//										while(!future.isDone()) {
//											Thread.sleep(1000);
//										}
										
										service.shutdown(); // Disable new tasks from being submitted
										while (!service.isTerminated() || !future.isDone()) {
											try {
												// Wait a while for existing tasks to terminate
												if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
													service.shutdownNow(); // Cancel currently executing tasks
													// Wait a while for tasks to respond to being cancelled
													if (!service.awaitTermination(60, TimeUnit.SECONDS))
														System.err.println("Pool did not terminate");
												}
											} catch (InterruptedException ie) {
												// (Re-)Cancel if current thread also interrupted
												service.shutdownNow();
												// Preserve interrupt status
												Thread.currentThread().interrupt();
											}
										}
										
										continue algoFor;
									} finally {
										service.shutdown();
									}
									long elapsedTimeMS = System.currentTimeMillis() - startTime;

									if (DEBUG) {
										System.out.println(sNet);
										System.out.println("--");
										System.out.println(vNets);
										System.out.println("##########");
									}

									if (timeout) {
										System.out.println("Timeout after " + (elapsedTimeMS  / 1000) + " seconds");
									} else {
										request.mappingResult = stack;

//										if (export) {
//											int VNetPos = 0;
//											for (Network<?,?,?> v : request.mappingResult.getVirtuals()) {
//												GraphMLExporter.export(xmldir.getAbsolutePath() + File.separator + exportfilename + "_vNet" + VNetPos + ".graphml", v);
//												VNetPos++;
//											}
//											GraphMLExporter.export(xmldir.getAbsolutePath() + File.separator + exportfilename + "_sNet.graphml", request.mappingResult.getSubstrate());
//										}

										for (EvaluationMetric<NetworkStack> m : algorithm.getMetrics(elapsedTimeMS)) {
											System.out.println("    " + m.getClass().getSimpleName() + ": " + m.calculate(stack));
										}

										resultwriter.receive(scenarioSuffix, stack, algorithm.getMetrics(elapsedTimeMS));
									}

								}
								AlgorithmParameternum++;
							}
							AbstractScenarioTestnum++;
						}
						vNetparamnum++;
					}
				}
				sNetparameternum++;
			}
		}

		for (CSVPrintWriterDataReceiver w : resultwriters.values())
			w.finish();
	}
	
	
	private String getSuffix(
			NetworkGeneratorParameter sNetparameter,
			ConstraintsGenerator<SubstrateNetwork> sNetConstraintsGenerator,
			int numVNFChains, NetworkGeneratorParameter vNetparameter,
			AbstractScenarioTest algorithm, AlgorithmParameter algoParam) {
		
		String scenarioSuffix =
				(sNetparameter == null ? "sNetparameter:null" : sNetparameter.getSuffix("sNetparameter.")) + "_" +
				(sNetConstraintsGenerator == null ? "sNetConstraintsGenerator:null" : sNetConstraintsGenerator.getSuffix("sNetConstraintsGenerator.")) + "_" +
				"numVNFFGs:" + numVNFChains + "_" +
				(vNetparameter == null ? "vNetparameter:null" : vNetparameter.getSuffix("VNFFGparameter.")) + "_" +
				(algoParam == null ? "algoParam:null" : algoParam.getSuffix("algoParam."));
		
		return scenarioSuffix;
	}


	public static class Request {
		public long initTime;
		public long termTime;
		public NetworkStack mappingResult;

		public Request(long initTime, long termTime) {
			this.initTime = initTime;
			this.termTime = termTime;
		}
	}

	//	protected void removeRandomNodes(NetworkStack stack, int n) {
	//		SubstrateNetwork snet = stack.getSubstrate();
	//		LinkedList<SubstrateNode> snodes = new LinkedList<SubstrateNode>(snet.getVertices());
	//
	//		Collections.shuffle(snodes);
	//
	//		for (int i = 0; i < n && !snodes.isEmpty(); ++i) {
	//			snet.removeVertex(snodes.pollFirst());
	//		}
	//	}
	
}
