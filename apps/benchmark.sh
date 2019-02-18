cd ../framework
javac *.java
jar cfm benchmark-intercept-agent.jar ../apps/benchmark-manifest.mf BenchmarkInterceptAgent*.class
mv benchmark-intercept-agent.jar ../apps/benchmark/
cd ../apps/benchmark/
./runBenchmark.sh
