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
package vnreal.evaluations.metrics;

import java.io.IOException;
import java.io.InputStream;

import vnreal.core.Consts;
import vnreal.network.NetworkStack;


public class OSMemoryUsageMB implements EvaluationMetric<NetworkStack>, Runnable {

	private final static String cleartool = Consts.ALEVIN_DIR + "/tools/dropcaches";
	private final static String memtool = Consts.ALEVIN_DIR + "/tools/memKB.sh";
	
	boolean stop = false;
	long sum = 0;
	long i = 0;
	
	public OSMemoryUsageMB() {
	}
	
	public void run() {
		sum = 0;
		i = 0;
		final java.lang.Runtime r = java.lang.Runtime.getRuntime();
		
		try {
			r.exec(cleartool).waitFor();
		} catch (IOException e1) {
			throw new AssertionError();
		} catch (InterruptedException e) {
			throw new AssertionError();
		}
		long firstusage = getUsageMB(r);

		try {
			Thread.sleep(1000);
			while (!stop) {
				Thread.sleep(1000);

				long usage = getUsageMB(r) - firstusage;
				sum += usage;
				i++;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	long getUsageMB(java.lang.Runtime r) {
		Process p;
		try {
			p = r.exec(memtool);
		} catch (IOException e) {
			throw new AssertionError();
		}
		InputStream i = p.getInputStream();
		java.util.Scanner s = null;
		try {
			s = new java.util.Scanner(i).useDelimiter("\\A");
	        if (s.hasNext())
	        	return (Long.parseLong(s.next().trim()) / 1024);
		} finally {
			s.close();
		}

		throw new AssertionError();
	}
	
	public void stop() {
		stop = true;
	}

	@Override
	public double calculate(NetworkStack stack) {
		return (sum / (double) i);
	}

	@Override
	public String toString() {
		return "OSMemoryUsageMB";
	}

}
