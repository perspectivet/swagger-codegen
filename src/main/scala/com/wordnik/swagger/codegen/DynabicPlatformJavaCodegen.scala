package com.wordnik.swagger.codegen

import java.io.File

object DynabicPlatformJavaCodegen extends BasicJavaGenerator {
  def main(args: Array[String]) = generateClient(args)

  override def packageName = "com.dynabic.sdk.platform"
    
  // where to write generated code
  override def destinationDir = "../generated-files/DynabicPlatformJava/src/main/java"

  // supporting classes
  override def supportingFiles = {
    val destPath = destinationDir + File.separator + invokerPackage.get.replaceAll("\\.", File.separator)
    List(
      ("apiInvoker.mustache", destPath, "ApiInvoker.java"),
      ("apiException.mustache", destPath, "ApiException.java"),
      ("RequestSigner.mustache", destPath, "RequestSigner.java"),
      ("DynabicRequestSigner.mustache", destPath, "DynabicRequestSigner.java"),
      ("pom-dynabic-platform.mustache", "../generated-files/DynabicPlatformJava", "pom.xml"))
  }
}
