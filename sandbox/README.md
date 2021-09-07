### Startup

Run the `watcher-sandbox-1.0.0.jar` with the following command:

```
java -jar -javaagent:watcher-1.0.1.jar -Dwatcher.logger.enabled=true -Dwatcher.asm.print=true -Dwatcher.main.package=sandbox watcher-sandbox-1.0.0.jar 
```

Properties:

- `watcher.logger.enabled` - enables logging
- `watcher.asm.print` - prints JVM bytecode
- `watcher.main.package` - root package of classes to transform by Watcher Agent
