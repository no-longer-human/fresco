/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.math.integer.linalg;


import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import java.math.BigInteger;

public class InnerProductFactoryImpl implements InnerProductFactory{

	private final BasicNumericFactory bnf;
	private final EntrywiseProductFactory dotProductFactory;

	public InnerProductFactoryImpl(BasicNumericFactory bnf,
			EntrywiseProductFactoryImpl dotProductFactory) {
		this.bnf = bnf;
		this.dotProductFactory = dotProductFactory;
	}
	
	@Override
	public InnerProductProtocol getInnerProductProtocol(SInt[] aVector,
			SInt[] bVector, SInt result) {
		return new InnerProductProtocolImpl(aVector, bVector, result, bnf, dotProductFactory);
	}

	@Override
	public InnerProductProtocol getInnerProductProtocol(SInt[] aVector,
      BigInteger[] bVector, SInt result) {
    return new InnerProductProtocolImpl(aVector, bVector, result, bnf, dotProductFactory);
	}

}
