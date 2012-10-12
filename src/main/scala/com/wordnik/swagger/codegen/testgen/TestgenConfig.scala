package com.wordnik.swagger.codegen.testgen

import scala.collection.mutable.HashMap

import com.wordnik.swagger.codegen.language.CodegenConfig

trait TestgenConfig extends CodegenConfig {

  // where to write generated code
  override def destinationDir = "generated-tests/"
    
  // location of templates
  override def templateDir = "test"

  val specTemplateFiles = HashMap[String, String]("testSpec.mustache" -> ".spec")
  
  def testGenSpecsPath = destinationDir + "/testspecs"
  
  def testGenTestsPath = destinationDir + "/tests"
  
}