package com.wordnik.swagger.codegen

import java.io.File
import java.io.FileWriter
import java.net.URL
import scala.Option.option2Iterable
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import scala.io.Source
import org.apache.commons.io.FileUtils
import org.fusesource.scalate.Template
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.TemplateSource
import org.fusesource.scalate.support.ScalaCompiler
import com.wordnik.swagger.codegen.testgen.AssertionExpression
import com.wordnik.swagger.codegen.testgen.AssignmentExpression
import com.wordnik.swagger.codegen.testgen.MethodCallExpression
import com.wordnik.swagger.codegen.testgen.Statement
import com.wordnik.swagger.codegen.testgen.TestSpecParser
import com.wordnik.swagger.codegen.testgen.converters.Converter
import com.wordnik.swagger.codegen.testgen.converters.JavaConverter
import com.wordnik.swagger.codegen.testgen.UtilityMethod

class Testgen extends TestSpecgen {

  val compiledTemplates = new HashMap[String, (TemplateEngine, Template)]
  val supportedLangs = List("java", "php", "python")
  val extraFiles = ListBuffer[Tuple2[String, String]]()
  
  def generateTests(args: Array[String]): Unit = {
    def showError(): Unit = throw new RuntimeException("""Incorrect arguments supplied. Please provide a correct combination of arguments depending on what you want.
          1. To generate test specs out of swagger specs use: "path_to_resources.json" (can be either URL or absolute file path) 
          2. To generate tests in target language use: java|php|python (i.e. to generate JUnit tests use: "java") """)
    
    if(args.length != 1) showError()
    
    args(0) match {
      case language if supportedLangs contains language => {
        val converter = optConverter(language).get

        new File(testGenSpecsPath).listFiles.foreach(file => {
          val parseResults = TestSpecParser.parseFile(file)
          val processedData = processStatements(parseResults, converter)
          val data = processedData ++ converter.mixinData
          val output = getRenderedTemplate(data, templateDir + "/" + converter.templateFile)
          writeToFile(output, converter.testFilePath(processedData("service").asInstanceOf[String]))
        })
        
        extraFiles ++= converter.extraFiles
        writeSupportingClasses(converter.mixinData.toMap, converter)
      }
      case uri if uri startsWith "http" => new URL(uri); generateClient(args)
      case file if new File(file).exists => System.setProperty("fileMap", args(0)); generateClient(args)
      case _ => showError()
    }
    
  }
  
  def optConverter(language: String): Option[Converter] = language match {
    case "java" => Some(new JavaConverter(this))
    case "php" => Some(new JavaConverter(this))
    case "python" => Some(new JavaConverter(this))
    case _ => None
  }
  
  def processStatements(parsed: Tuple2[String, List[Tuple2[String, List[Statement]]]], converter: Converter) = {
    val operations = new ListBuffer[Map[String, AnyRef]]
    val data = Map[String, AnyRef]("service" -> parsed._1, "operations" -> operations)
    
    parsed._2.foreach(operation => {
      val statements = new ListBuffer[Map[String,String]]
      operation._2.foreach(statement => {
        val stmt = statement match {
          case s: AssignmentExpression => Map("assignment"-> converter.statement2string(statement.asInstanceOf[AssignmentExpression]))
          case s: MethodCallExpression => Map("methodCall"-> converter.statement2string(statement.asInstanceOf[MethodCallExpression]))
          case s: AssertionExpression => Map("assertion"-> converter.statement2string(statement.asInstanceOf[AssertionExpression]))
          case s: UtilityMethod => Map("utilityMethod"-> converter.statement2string(statement.asInstanceOf[UtilityMethod]))
        }
        statements += stmt
      })
      
      val op = Map[String, AnyRef]("operation" -> operation._1, "statements" -> statements)
      operations += op
    })
    println(data)
    data
  }

  def writeSupportingClasses(data: Map[String, AnyRef], converter: Converter) = {
    extraFiles.foreach(file => {
      val srcFilepath = file._1
      val destFilepath = converter.testFileDir + "/" + file._2

      if (srcFilepath.endsWith(".mustache")) {
        val output = getRenderedTemplate(data, srcFilepath)
        writeToFile(output, destFilepath)
      } else {
        copyFile(srcFilepath, destFilepath)
      }
    })
  }
  
  private def getRenderedTemplate(data: Map[String, AnyRef], templateFilePath: String): String = {
    val rootDir = "."
    
    val engineData = compiledTemplates.getOrElse(templateFilePath, {
      val engine = new TemplateEngine(Some(new java.io.File(rootDir)))
      println("pre-compile " + templateFilePath)
      val template = engine.compile(TemplateSource.fromText(templateFilePath, 
          Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(templateFilePath)).mkString))
      val t = Tuple2(engine, template)
      compiledTemplates += templateFilePath -> t
      t
    })

    val engine = engineData._1
    val template = engineData._2

    var output = engine.layout(templateFilePath, template, data)
    engine.compiler.asInstanceOf[ScalaCompiler].compiler.askShutdown //TODO optimize to shutdown after process all files
    output
  }
  
  private def writeToFile(generated: String, filePath: String) = {
    val fw = new FileWriter(filePath, false)
    fw.write(generated + "\n")
    fw.close()
    println("wrote " + filePath)
  }
  
  private def copyFile(srcFilePath: String, destFilePath: String) = {
	val is = getClass.getClassLoader.getResourceAsStream(srcFilePath)
    FileUtils.copyInputStreamToFile(is, new File(destFilePath))
    println("copied " + destFilePath)
    is.close
  }

}