cd ../framework
javac *.java
jar cfm ecommerce-intercept-agent.jar ../apps/ecommerce-manifest.mf ECommerceInterceptAgent*.class
mv ecommerce-intercept-agent.jar ../apps/ecommerce/
cd ../apps/ecommerce/
javac *.java
java -javaagent:ecommerce-intercept-agent.jar -Dmodels=../../models/ecommerce -Dbuffer=1 -Dskip=0 Test
