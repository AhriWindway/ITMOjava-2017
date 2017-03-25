SALT=
OUT=out/production
DIR=src/ru/ifmo/ctddev/zhuchkova
LIB=java-advanced-2017/lib/*:java-advanced-2017/artifacts/JarImplementorTest.jar:./$(OUT)
KG=info.kgeorgiy.java.advanced
NZH=ru.ifmo.ctddev.zhuchkova

WALK=$(DIR)/walk/Walk.java
ARRAYSET=$(DIR)/arrayset/ArraySet.java
IMPL=$(DIR)/implementor/Implementor.java
IT=$(DIR)/concurrent/IterativeParallelism.java

all: @walk @arrayset @impl @it
@arrayset: 
	javac -d $(OUT) $(ARRAYSET)
@walk: 
	javac -d $(OUT) $(WALK)
@impl:
	javac -cp $(LIB) -d $(OUT) $(IMPL)
@impl_jar:
	javac -cp $(LIB) -d $(OUT) $(IMPL)
@it:
	javac -cp java-advanced-2017/lib/*:java-advanced-2017/artifacts/IterativeParallelismTest.jar:./$(OUT) -d $(OUT) $(IT)
walk:
	java -cp $(LIB) $(KG).walk.Tester Walk $(NZH).walk.Walk $(SALT) 
arrayset:
	java -cp $(LIB) $(KG).arrayset.Tester SortedSet $(NZH).arrayset.ArraySet $(SALT) 
impl:
	java -cp $(LIB) $(KG).implementor.Tester class $(NZH).implementor.Implementor $(SALT) 
impl_jar:
	java -Dfile.encoding=UTF-8 -cp java-advanced-2017/lib/*:java-advanced-2017/artifacts/JarImplementorTest.jar:./$(OUT) $(KG).implementor.Tester jar-class $(NZH).implementor.Implementor $(SALT) 
it:
	java -Xmx7168m -Xms4096m -cp java-advanced-2017/lib/*:java-advanced-2017/artifacts/IterativeParallelismTest.jar:./$(OUT) $(KG).concurrent.Tester list $(NZH).concurrent.IterativeParallelism $(SALT) 
gen_jar:
	jar cvfm implementor.jar src/META-INF/MANIFEST.MF -C out/production ru/ifmo/ctddev/zhuchkova/implementor/Implementor.class
gen_javadoc:
	javadoc -author -private -link http://docs.oracle.com/javase/8/docs/api/ -d out/production/javadoc src/ru/ifmo/ctddev/zhuchkova/implementor/Implementor.java java-advanced-2017/java/info/kgeorgiy/java/advanced/implementor/Impler.java java-advanced-2017/java/info/kgeorgiy/java/advanced/implementor/JarImpler.java java-advanced-2017/java/info/kgeorgiy/java/advanced/implementor/ImplerException.java