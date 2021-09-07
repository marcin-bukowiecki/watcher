### Watcher

Watcher is an JVM agent which instruments the bytecode so all instructions manipulating the function frame such as: `iload`, `dload`, `invokevirtual` etc. are being recorded.
You can set "virtual" breakpoints which won't stop the program execution but will dump the whole thread stack to an `json` file.
This file will contain events from the thread execution flow.

Be aware that this project is in its early stage of development.

### Requirements

- `Maven` (minimum `3.8.1`)
- `Java` (minimum version 11 e.g. `openjdk 11.0.11 2021-04-20 LTS`)

### Building

To build the project just execute following command:

```
mvn package
```

This will produce a jar in `target` directory (watcher-1.0.1.jar) which is just a typical JVM agent.

### Building

To test the agent you will need to build the `sandbox` project. The `sandbox` project is a simple Spring Boot application.

From the `sandbox` root directory execute following command: `mvn clean install`.

After that `cd` to the `target` directory, copy the `watcher-1.0.1.jar` from previous build and execute following command:

```
java -jar -javaagent:watcher-1.0.1.jar -Dwatcher.logger.enabled=true -Dwatcher.asm.print=true -Dwatcher.main.package=sandbox watcher-sandbox-1.0.0.jar 
```

This command will run the Spring Boot application with `Watcher Agent`.

Passed arguments are responsible for:

- `watcher.logger.enabled` - enables logging
- `watcher.asm.print` - prints JVM bytecode
- `watcher.main.package` - root package of classes to transform by Watcher Agent

After running the program you should see in console the following message: `Program running with Watcher Agent version 1.0.1`.

### Testing

Best way to test the agent is to use [Postman](https://www.postman.com/). In `watcher/postman` directory you will find
postman collection: `Watcher API.postman_collection.json`. You can import this collection to `Postman` and start testing it.

Sample request flow:

1. `GET Watcher check` - checks if `Watcher Agent` is running.

2. `POST Start debug session` - starts the debug session.

Request body (JSON):

```
{
    "debugSessionStatus": "ON",
    "basePackages": "sandbox"
}
```

`debugSessionStatus` and `basePackages` are required. Property `basePackages` indicates the root package of classes to be instrumented by agent (can't be empty).


3. `POST Add breakpoint` - adds a virtual breakpoint. 

Request body (JSON):

```
{
    "classCanonicalName": "sandbox.controller.HelloController",
    "lineNumber": 23
}
```

`classCanonicalName` - class where to set the breakpoint (with package name e.g. `foo.bar.Main`).
`lineNumber` - line number of class source file.


4. `GET Sandbox request` - sends a request to the sandbox Spring Boot application.

After executing this request you should see a generated `json` file in the same directory where you started the Spring Boot sandbox application.


5. `DELETE Remove breakpoint` - removes virtual breakpoint.

Request body (JSON):

```
{
    "classCanonicalName": "sandbox.controller.HelloController",
    "lineNumber": 23
}
```


7. `POST Stop debug session` - stops debug session (removes all breakpoints and bytecode instrumentation).

```
{
    "debugSessionStatus": "OFF",
    "basePackages": "sandbox"
}
```

