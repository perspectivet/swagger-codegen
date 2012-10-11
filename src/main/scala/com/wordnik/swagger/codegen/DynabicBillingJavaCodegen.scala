/**
 *  Copyright 2012 Wordnik, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
