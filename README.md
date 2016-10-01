# wrapper-generator
A utility for generating wrapper class for any interface or class

## Usage
```
java -cp CLASSPATH com.github.zerkseez.codegen.wrappergenerator.Main
  --classMappings WRAPPEE1:WRAPPER1 WRAPPEE2:WRAPPER2 ...
  --outputDirectory OUTPUT_DIRECTORY
```

## Example
```
java -cp CLASSPATH com.github.zerkseez.codegen.wrappergenerator.Main
  --classMappings java.sql.Connection:com.mycompany.jdbc.wrapper.WrappedConnection
                  java.sql.ResultSet:com.mycompany.jdbc.wrapper.WrappedResultSet
  --outputDirectory $PROJECT_HOME/src/main/java
```

## License
Apache License, Version 2.0
