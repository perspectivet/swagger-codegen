package com.wordnik.swagger.codegen.testgen

import java.io.File
import java.io.FileReader

import scala.util.parsing.combinator.JavaTokenParsers

object TestSpecParser extends TestSpecParser {

  val outputDir = "/home/zhaidarbek/workspace/groupdocs/scala/generated-code/DynabicPlatformJava/"
  
  def main(args: Array[String]): Unit = {
    parseFile(new File(outputDir + "testspecs/AppsApi.spec"))
//    new File(outputDir + "src/test/resources/testspecs").listFiles.foreach(parseFile(_))
    
    println(parse(utilityMethod, "#MyClass.hackCopy(arg1, arg2)"))
  }

  def parseFile(file: File) = {
    println("Parsing file " + file)
    val result = parseAll(feature, new FileReader(file))
    println(result)
    result.get
  }
  
}

class TestSpecParser extends JavaTokenParsers {
  override val whiteSpace = """[ \t]+""".r
  val eol = """(\r?\n)+""".r
  
  def feature: Parser[Tuple2[String, List[Tuple2[String, List[Statement]]]]] = ("Service:"~>ident<~eol)~rep(scenario) ^^ { case feature~scenarios => (feature, scenarios)}
  def scenario: Parser[Tuple2[String, List[Statement]]] = ("Operation:"~>ident<~eol)~rep(statement) ^^ { case scenario~statements => (scenario, statements)}
  def statement: Parser[Statement] = (assignmentExpression | assertExpression | methodCallExpression | utilityMethod)<~rep(eol)  
  
  val patternLeft = """^\s*[a-zA-Z_]\w*( +|\.|<[a-zA-Z_]\w*> +)[a-zA-Z_]\w*""".r
  val patternRight = """ *[a-zA-Z_]\w*(.\d|\.[a-zA-Z_]\w*|\(\))?( *\+ *\".*\")?""".r // must be either FieldAccessor, StringConcatenation or ObjectInstantiation, value is already covered 
  val patternVariableDeclaration = """^\s*(\w+|\w+<\w+>) +(\w+)""".r
  val patternFieldAccessor = """ *([a-zA-Z_]\w*)\.(\d|[a-zA-Z_]\w*)""".r
  val patternObjectInstantiation = """([a-zA-Z_]\w*)\(\)""".r
  val patternStringConcatenation = """ *([a-zA-Z_]\w*(\.[a-zA-Z_]\w*)?) *\+ *(\".*\")""".r
  def assignmentExpression: Parser[Statement] = patternLeft~"="~(value | methodCallExpression | patternRight) ^^ {
    case left~"="~right => {
      val l = left match {
        case patternVariableDeclaration(dataType, name) => VariableDeclaration(dataType, name)
        case patternFieldAccessor(varName, fieldName) => FieldSetter(varName, fieldName)
      }
      val r = right match {
        case MethodCallExpression(a, b, c) => MethodCallExpression(a, b, c)
        case patternFieldAccessor(varName, fieldName) => FieldGetter(varName, fieldName)
        case patternObjectInstantiation(model) => ObjectInstantiation(model)
        case v:Any => {
          v match {
            case patternStringConcatenation(a,b,c) => {
	          if (b == null) StringConcatenation(Value(a), c) 
	          else StringConcatenation(a match { case patternFieldAccessor(x, y) => FieldGetter(x, y) }, c)
	        }
            case _ => Value(v)
          }
        }
      }
      AssignmentExpression(l, r)
    }
  }
  def methodCallExpression: Parser[Statement] = ident~"."~ident~"("~repsep("""\w+""".r, ",")~")"<~eol ^^ { case api~"."~method~"("~arguments~")" => MethodCallExpression(api, method, arguments)}
  def assertExpression: Parser[Statement] = """^\s*[a-zA-Z_]\w*(\.[a-zA-Z_]\w*)?""".r~assertion~(value | """^\s*[a-zA-Z_]\w*(\.[a-zA-Z_]\w*)?""".r) ^^ { 
    case actual~assertion~expected => {
      val a = actual match {
        case patternFieldAccessor(varName, fieldName) => FieldGetter(varName, fieldName)
        case v:Any => Value(v)
      }
      val b = expected match {
        case patternFieldAccessor(varName, fieldName) => FieldGetter(varName, fieldName)
        case v:Any => Value(v)
      }
      AssertionExpression(a, assertion, b) }
    }
  def utilityMethod: Parser[Statement] = "#" ~> (ident~("."~>ident)~("("~>repsep(ident, ",")<~")")) ^^ {
    case className~method~arguments => UtilityMethod(className, method, arguments)
  }
  def value: Parser[Any] = stringLiteral | "null" | "true" | "false" | decimalNumber | ("&"~>ident) //TODO elaborate on this
  def assertion: Parser[String] = "Equals" | "NotEquals" | "More" | "Less" | "MoreEqual" | "LessEqual" | "HasSize" | "HasNotSize"
}

sealed trait Statement

sealed trait LeftOperand

sealed trait RightOperand

sealed trait SimpleOperand // TODO elaborate

case class MethodCallExpression(api: String, method: String, arguments: List[String]) extends Statement with RightOperand

case class AssertionExpression(actual: SimpleOperand, assertion: String, expected: SimpleOperand) extends Statement

case class AssignmentExpression(leftOperand: LeftOperand, rightOperand: RightOperand) extends Statement

case class VariableDeclaration(dataType: String, varName: String) extends LeftOperand

case class FieldSetter(varName: String, fieldName: String) extends LeftOperand

case class FieldGetter(varName: String, fieldName: String) extends RightOperand with SimpleOperand

case class ObjectInstantiation(modelName: String) extends RightOperand

case class Value(value: Any) extends RightOperand with SimpleOperand

case class StringConcatenation(variable: SimpleOperand, str: String) extends Value // TODO elaborate

case class UtilityMethod(className: String, methodName: String, arguments: List[String]) extends Statement

