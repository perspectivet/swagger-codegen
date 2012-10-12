package com.wordnik.swagger.codegen

import java.io.File

object DynabicBillingJavaCodegen extends BasicJavaGenerator {
  def main(args: Array[String]) = generateClient(args)

  override def packageName = "com.dynabic.sdk.billing"
    
  // where to write generated code
  override def destinationDir = "../generated-files/DynabicBillingJava/src/main/java"

  // supporting classes
  override def supportingFiles = {
    val destPath = destinationDir + File.separator + invokerPackage.get.replaceAll("\\.", File.separator)
    List(
      ("apiInvoker.mustache", destPath, "ApiInvoker.java"),
      ("apiException.mustache", destPath, "ApiException.java"),
      ("RequestSigner.mustache", destPath, "RequestSigner.java"),
      ("DynabicRequestSigner.mustache", destPath, "DynabicRequestSigner.java"),
      ("pom-dynabic-billing.mustache", "../generated-files/DynabicBillingJava", "pom.xml"))
  }
}
