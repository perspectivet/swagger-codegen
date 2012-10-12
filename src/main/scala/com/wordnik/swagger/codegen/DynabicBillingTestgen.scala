package com.wordnik.swagger.codegen

object DynabicBillingTestgen extends Testgen {
  def main(args: Array[String]): Unit = generateTests(args)
  
  override def packageName = "com.dynabic.sdk.billing"

  // where to write generated code
  override def destinationDir = "../generated-files/DynabicBillingJava"

  // supporting classes
  extraFiles += (Tuple2(templateDir + "/abstractIntegrationTest-dynabic-billing.mustache", "AbstractIntegrationTest.java"), 
      Tuple2(templateDir + "/utils-dynabic-billing.mustache", "Utils.java"))
  
}