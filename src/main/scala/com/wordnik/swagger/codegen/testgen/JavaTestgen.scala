package com.wordnik.swagger.codegen.testgen

trait JavaTestgen extends TestgenConfig {
  
  override def destinationDir = "src/test/java"
    
  override def testGenSpecsPath = destinationDir.replace("src/test/java", "src/test/resources") + "/testspecs"
  
  
  
}