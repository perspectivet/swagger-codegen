package com.wordnik.swagger.codegen

import com.wordnik.swagger.codegen.language.CodegenConfig
import com.wordnik.swagger.codegen._
import com.wordnik.swagger.codegen.util._
import java.io.{ File, FileWriter }
import scala.io._
import scala.collection.JavaConversions._
import scala.collection.mutable.{ ListBuffer, HashMap, HashSet }
import scala.io.Source
import spec.SwaggerSpecValidator
import com.wordnik.swagger.codegen.testgen.TestgenConfig
import com.wordnik.swagger.model.Model

object TestSpecgen extends TestSpecgen {
  
}

trait TestSpecgen extends BasicJavaGenerator with TestgenConfig {
  
  override def generateClient(args: Array[String]) = {
    if (args.length == 0) {
      throw new RuntimeException("Need url to resources.json as argument. You can also specify VM Argument -DfileMap=/path/to/folder/containing.resources.json/")
    }
    val host = args(0)
    val apiKey = {
      if (args.length > 1) Some("?api_key=" + args(1))
      else None
    }
    val doc = {
      try {
        ResourceExtractor.fetchListing(getResourcePath(host), apiKey)
      } catch {
        case e: Exception => throw new Exception("unable to read from " + host, e)
      }
    }

    implicit val basePath = getBasePath(doc.basePath)

    val apiReferences = doc.apis
    if (apiReferences == null)
      throw new Exception("No APIs specified by resource")
    val apis = ApiExtractor.fetchApiListings(basePath, apiReferences, apiKey)

    new SwaggerSpecValidator(doc, apis).validate()

    val allModels = new HashMap[String, Model]
    val operations = extractApiOperations(apis, allModels)
    val operationMap = groupOperationsToFiles(operations)

    val apiBundle = prepareApiBundle(operationMap.toMap)
    val apiFiles = bundleToSource(apiBundle, specTemplateFiles.toMap)

    apiFiles.map(m => {
      val outputDir = new File(testGenSpecsPath)
      outputDir.mkdirs
      
      val filename = new File(outputDir, new File(m._1).getName())

      val fw = new FileWriter(filename, false)
      fw.write(m._2 + "\n")
      fw.close()
      println("wrote api " + filename)
    })
  }

}