package com.wordnik.swagger.codegen

object DynabicPlatformTestgen extends Testgen {
  def main(args: Array[String]): Unit = generateTests(args)
  
  override def packageName = "com.dynabic.sdk.platform"

  // where to write generated code
  override def destinationDir = "../generated-files/DynabicPlatformJava"

  // supporting classes
  extraFiles += (Tuple2(templateDir + "/abstractIntegrationTest-dynabic-platform.mustache", "AbstractIntegrationTest.java"), 
      Tuple2(templateDir + "/utils-dynabic-platform.mustache", "Utils.java"))
  
}