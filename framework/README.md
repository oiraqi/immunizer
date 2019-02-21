- **FeatureExtractor**: the core class, builds and returns FeatureRecord instances

- **FeatureRecord**: a class representing extracted features from an invocation

- **Invocation**: a class modeling a method invocation

- **LazySerializationHelper**: a class to support lazy (deferred) serialization

- ***InterceptAgent**: the intercept agents, each one instrumenting a specific application

- **ResultSetAdapter**: deprecated in favor of LazySerializationHelper

- **BenchmarkFeatureRecord**: a decorator/wrapper class around FeatureRecord to better organize OWASP Benchmark models according to test cases
