package com.wordnik.swagger.codegen

import java.io.File

object GroupDocsJavaCodegen extends BasicJavaGenerator {
  def main(args: Array[String]) = generateClient(args)

  override def packageName = "com.groupdocs.sdk"
    
  // where to write generated code
  override def destinationDir = "../generated-files/GroupDocsJava/src/main/java"

  // supporting classes
  override def supportingFiles = {
    val destPath = destinationDir + File.separator + invokerPackage.get.replaceAll("\\.", File.separator)
    List(
      ("apiInvoker.mustache", destPath, "ApiInvoker.java"),
      ("apiException.mustache", destPath, "ApiException.java"),
      ("RequestSigner.mustache", destPath, "RequestSigner.java"),
      ("GroupDocsRequestSigner.mustache", destPath, "GroupDocsRequestSigner.java"),
      ("pom-groupdocs.mustache", "../generated-files/GroupDocsJava", "pom.xml"))
  }
}
