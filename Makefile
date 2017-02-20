SALT=12345
OUT=out/production
DIR=src/ru/ifmo/ctddev/zhuchkova
LIB=java-advanced-2017/lib/*:java-advanced-2017/artifacts/*:./$(OUT)
KG=info.kgeorgiy.java.advanced
NZH=ru.ifmo.ctddev.zhuchkova

WALK=$(DIR)/walk/Walk.java
ARRAYSET=$(DIR)/arrayset/ArraySet.java


all: -walk -arrayset
-arrayset: 
	javac -d $(OUT) $(ARRAYSET)
-walk: 
	javac -d $(OUT) $(WALK)
walk:
	java -cp $(LIB) $(KG).walk.Tester Walk $(NZH).walk.Walk $(SALT) 
arrayset:
	java -cp $(LIB) $(KG).arrayset.Tester SortedSet $(NZH).arrayset.ArraySet $(SALT) 