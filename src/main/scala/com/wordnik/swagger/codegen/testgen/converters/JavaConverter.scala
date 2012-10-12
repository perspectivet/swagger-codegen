package com.wordnik.swagger.codegen.testgen.converters

import com.wordnik.swagger.codegen.testgen.AssertionExpression
import com.wordnik.swagger.codegen.testgen.AssignmentExpression
import com.wordnik.swagger.codegen.testgen.FieldGetter
import com.wordnik.swagger.codegen.testgen.FieldSetter
import com.wordnik.swagger.codegen.testgen.MethodCallExpression
import com.wordnik.swagger.codegen.testgen.ObjectInstantiation
import com.wordnik.swagger.codegen.testgen.TestgenConfig
import com.wordnik.swagger.codegen.testgen.Value
import com.wordnik.swagger.codegen.testgen.VariableDeclaration
import com.wordnik.swagger.codegen.testgen.StringConcatenation
import com.wordnik.swagger.codegen.testgen.UtilityMethod

class JavaConverter(config: TestgenConfig) extends Converter {
  val templateFile = "javaTest.mustache"
  val apiPackage = config.apiPackage.getOrElse(config.packageName + ".api")
  val modelPackage = config.modelPackage.getOrElse(config.packageName + ".model")
  
  mixinData += ("package" -> apiPackage, "imports" -> List(
		  Map("import" -> (config.invokerPackage.get + ".*")),
		  Map("import" -> (modelPackage + ".*"))))

  extraFiles += Tuple2(config.templateDir + "/" + "integrationTest.mustache", "IntegrationTest.java")
          
  def testFileDir = config.destinationDir + "/src/test/java/" + apiPackage.replaceAll("\\.", "/")

  def testFilePath(classUnderTest: String) = testFileDir + "/Test" + classUnderTest + ".java"

  def statement2string(s: AssignmentExpression): String = {
    val r = s.rightOperand match {
      case ObjectInstantiation(model) => "new " + model + "()"
      case MethodCallExpression(api, method, arguments) => methodCallString(api, method, arguments)
      case FieldGetter(varName, fieldName) => {
        s.leftOperand match {
          case VariableDeclaration(dataType, name) => if(dataType == "Boolean") fieldGetterString(varName, fieldName) else fieldGetterString(varName, fieldName)
          case _ => fieldGetterString(varName, fieldName)
        }
      }
      case Value("null") => null 
      case StringConcatenation(Value(variable), str) => variable + " + " + str
      case StringConcatenation(FieldGetter(varName, fieldName), str) => fieldGetterString(varName, fieldName) + " + " + str
      case Value(v: Any) => v
    }
    
    s.leftOperand match {
      case VariableDeclaration(dataType, name) => dataType + " " + name + " = " + r
      case FieldSetter(varName, fieldName) => varName + ".set" + fieldName.capitalize + "(" + r + ")"
    }
  }
  
  def statement2string(s: MethodCallExpression): String = {
    methodCallString(s.api, s.method, s.arguments)
  }
  
  def statement2string(s: AssertionExpression): String = {
    val expected = s.expected match {
      case Value("null") => null
      case Value(v) => v
      case FieldGetter(varName, fieldName) => fieldGetterString(varName, fieldName)
    }
    val actual = s.actual match {
      case Value("null") => null
      case Value(v) => v
      case FieldGetter(varName, fieldName) => fieldGetterString(varName, fieldName)
    }
    val assertionStmt = s.assertion match {
      //TODO as Enums
      case "Equals" => if(expected == null) "nullValue()" else "equalTo(" + expected + ")"
      case "NotEquals" => if(expected == null) "not(nullValue())" else "not(equalTo(" + expected + "))"
      case "More" => "greaterThan(" + expected + ")"
      case "Less" => "lessThan(" + expected + ")"
      case "MoreEqual" => "greaterThanOrEqualTo(" + expected + ")"
      case "LessEqual" => "lessThanOrEqualTo(" + expected + ")"
      case "HasSize" => "hasSize(" + expected + ")"
      case "HasNotSize" => "not(hasSize(" + expected + "))"
    }
    "assertThat(" + actual + ", " + assertionStmt + ")"
  }
  
  def statement2string(u: UtilityMethod): String = {
    u.className + "." + u.methodName + "(" + u.arguments.mkString(", ") + ")"
  }
  
  def methodCallString(api: String, method: String, arguments: List[String]): String = "new " + api + "()." + method + "(" + arguments.mkString(", ") + ")"
  
  def fieldGetterString(varName: String, fieldName: String, getter: String = "get") = {
    def decorate(fieldName: String) = {
      if(fieldName endsWith "Id()") "String.valueOf(" + fieldName + ")" else fieldName
    }
  
    val accessor = fieldName match {
      case Int(i) => "(" + (i - 1) + ")"
      case _ => fieldName.capitalize + "()"
    }
    decorate(varName + "." + getter + accessor)
  }
  
  object Int {
    def unapply(s : String) : Option[Int] = try {
      Some(s.toInt)
    } catch {
      case _ : java.lang.NumberFormatException => None
    }
  }

}