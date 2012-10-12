package com.wordnik.swagger.codegen.testgen.converters

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import com.wordnik.swagger.codegen.testgen.AssertionExpression
import com.wordnik.swagger.codegen.testgen.AssignmentExpression
import com.wordnik.swagger.codegen.testgen.MethodCallExpression
import com.wordnik.swagger.codegen.testgen.UtilityMethod

abstract class Converter {
	val mixinData = HashMap[String, AnyRef]()
	val extraFiles = ListBuffer[Tuple2[String, String]]()  // in the form of (srcFilePath, destFile name)

	def templateFile: String
	def testFileDir: String
	def testFilePath(classUnderTest: String): String
	
	def statement2string(s: AssignmentExpression): String
	def statement2string(s: MethodCallExpression): String
	def statement2string(s: AssertionExpression): String
	def statement2string(s: UtilityMethod): String
}