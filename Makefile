OUT=out/production
DIR=src/ru/ifmo/ctddev/zhuchkova
WALK=$(DIR)/walk/Walk.java
SALT=zzzz

all: 
	javac -d $(OUT) $(WALK)
walk:
	java -cp java-advanced-2017/lib/*:lib/*:./$(OUT) info.kgeorgiy.java.advanced.walk.Tester Walk ru.ifmo.ctddev.zhuchkova.walk.Walk $(SALT)
