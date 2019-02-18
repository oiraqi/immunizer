pom.xml contains this configuration to hook our instrumentation agent and set some command line arguments:

<cargo.jvmargs>
	-Xmx4G
	-javaagent:${basedir}/benchmark-intercept-agent.jar
	-Dmodels=${basedir}/../../models/benchmark -Dbuffer=1000 -Dskip=10000
</cargo.jvmargs>
