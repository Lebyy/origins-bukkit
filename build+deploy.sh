#!/bin/bash
java -Dmaven.multiModuleProjectDirectory=/home/carbolemons/Desktop/jeniserver/BlueYoshiDevelopment/origins-bukkit -Dmaven.home=/home/carbolemons/IntelliJStudio/plugins/maven/lib/maven3 -Dclassworlds.conf=/home/carbolemons/IntelliJStudio/plugins/maven/lib/maven3/bin/m2.conf -Dmaven.ext.class.path=/home/carbolemons/IntelliJStudio/plugins/maven/lib/maven-event-listener.jar -javaagent:/home/carbolemons/IntelliJStudio/lib/idea_rt.jar=39887:/home/carbolemons/IntelliJStudio/bin -Dfile.encoding=UTF-8 -classpath /home/carbolemons/IntelliJStudio/plugins/maven/lib/maven3/boot/plexus-classworlds-2.6.0.jar:/home/carbolemons/IntelliJStudio/plugins/maven/lib/maven3/boot/plexus-classworlds.license org.codehaus.classworlds.Launcher -Didea.version=2021.3.1 clean install
read -p "Press enter to continue"
rm -rf ../devserver/plugins/Origins-Bukkit/
rm ../devserver/plugins/Origins-Bukkit*.jar
cp compile/target/Origins-Bukkit\ Fates.jar ../devserver/plugins/
cd ../devserver
./start.sh
