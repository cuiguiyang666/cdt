/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.failedTests;

import java.io.StringWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.tests.DOMTests;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author jcamelon
 */
public class DOMFailedTest extends DOMTests {

	public DOMFailedTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new DOMFailedTest("testBug36691"));
		suite.addTest(new DOMFailedTest("testBug36699"));
		suite.addTest(new DOMFailedTest("testBug36704"));
		suite.addTest(new DOMFailedTest("testBug36707"));
		suite.addTest(new DOMFailedTest("testBug36708"));
		suite.addTest(new DOMFailedTest("testBug36713"));
		suite.addTest(new DOMFailedTest("testBug36714"));
		suite.addTest(new DOMFailedTest("testBug36730"));

		return suite;
	}

	public void testBug36691() {
		boolean testPassed = false;
		try {
			Writer code = new StringWriter();
			code.write("template <class T, class H>\n");
			code.write(
				"typename H::template Rebind<T>::Result& Field(H& obj)\n");
			code.write("{	return obj;	}\n");
			TranslationUnit tu = parse(code.toString());
			testPassed = true;
			fail( "We should not reach this point");
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");
		}
	}

	public void testBug36699() {
		boolean testPassed = false;
		try {
			Writer code = new StringWriter();
			code.write(
				"template <	template <class> class ThreadingModel = DEFAULT_THREADING,\n");
			code.write("std::size_t chunkSize = DEFAULT_CHUNK_SIZE,\n");
			code.write(
				"std::size_t maxSmallObjectSize = MAX_SMALL_OBJECT_SIZE	>\n");
			code.write("class SmallObject : public ThreadingModel<\n");
			code.write(
				"SmallObject<ThreadingModel, chunkSize, maxSmallObjectSize> >\n");
			code.write("{};\n");
			TranslationUnit tu = parse(code.toString());
			testPassed = true;
			fail( "We should not reach this point");
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");
		}
	}

	public void testBug36704() {
		boolean testPassed = false;
		try {
			TranslationUnit tu =
				parse("template <class T, class U> struct Length< Typelist<T, U> >	{ enum { value = 1 + Length<U>::value };};);");
			testPassed = true;
			fail( "We should not reach this point");
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");
		}
	}

	public void testBug36707() {
		boolean testPassed = false;
		try {
			TranslationUnit tu =
				parse("enum { exists = sizeof(typename H::Small) == sizeof((H::Test(H::MakeT()))) };");
			testPassed = true;
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");
		}
	}

	public void testBug36708() {
		boolean testPassed = false;
		try {
			TranslationUnit tu =
				parse("enum { isPointer = PointerTraits<T>::result };");
			testPassed = true;
		} catch (Throwable e) {
			if (!(e instanceof ParserException))
				fail("Unexpected Error: " + e.getMessage());

			if (testPassed)
				fail("The expected error did not occur.");
		}
	}
	
	public void testBug36714(){
		boolean testPassed = false;
		try{
			Writer code = new StringWriter();
			code.write("unsigned long a = 0UL;\n");
			code.write("unsigned long a2 = 0L; \n");

			TranslationUnit tu = parse(code.toString());
			testPassed = true;
		} catch (Throwable e ) {
			if( ! (e instanceof ParserException))
				fail( "Unexpected Error: " + e.getMessage() );
		}
		if( testPassed )
			fail( "The expected error did not occur.");
	}
	
	public void testBug36730(){
		boolean testPassed = false;
		try{
			TranslationUnit tu = parse("FUNCTION_MACRO( 1, a );\n	int i;");
		
			testPassed = true;
		} catch (Throwable e ) {
			if( ! (e instanceof ParserException))
				fail( "Unexpected Error: " + e.getMessage() );
		}
		if( testPassed )
			fail( "The expected error did not occur.");
	}

}
