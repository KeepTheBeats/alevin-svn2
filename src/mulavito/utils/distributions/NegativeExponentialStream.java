/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2008-2011, The 100GET-E3-R3G Project Team.
 * 
 * This work has been funded by the Federal Ministry of Education
 * and Research of the Federal Republic of Germany
 * (BMBF Förderkennzeichen 01BP0775). It is part of the EUREKA project
 * "100 Gbit/s Carrier-Grade Ethernet Transport Technologies
 * (CELTIC CP4-001)". The authors alone are responsible for this work.
 *
 * See the file AUTHORS for details and contact information.
 * 
 * This file is part of MuLaViTo (Multi-Layer Visualization Tool).
 *
 * MuLaViTo is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License Version 3 or later
 * (the "GPL"), or the GNU Lesser General Public License Version 3 or later
 * (the "LGPL") as published by the Free Software Foundation.
 *
 * MuLaViTo is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * or the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License and
 * GNU Lesser General Public License along with MuLaViTo; see the file
 * COPYING. If not, see <http://www.gnu.org/licenses/>.
 *
 * ***** END LICENSE BLOCK ***** */
package mulavito.utils.distributions;

/**
 * A stream for negative-exponentially distributed random numbers.
 * 
 * @author Michael Duelli
 * 
 * @see http://en.wikipedia.org/wiki/Negative_exponential_distribution
 */
public final class NegativeExponentialStream extends AbstractRandomStream {
	private final double mean;

	public NegativeExponentialStream(double mean) {
		if (mean <= 0)
			throw new IllegalArgumentException("mean must be positive!");

		this.mean = mean;
	}

	/**
	 * <pre>
	 * A(t) = 1 - exp(-t/E[A]) <=> t = -E[A] * ln(1-A(t))
	 * </pre>
	 */
	@Override
	protected double inversePDF(double A_t) {
		return (-mean) * Math.log(1 - A_t);
	}
}
