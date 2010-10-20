ROOT=$(shell pwd)
LIBS=colt.jar commons-collections-3.1.jar commons-collections-testframework-3.1.jar concurrent.jar \
derby.jar jgraph.jar jscience.jar jung-1.7.4.jar junit-4.0.jar mysql-connector-java-5.1.13-bin.jar \
vecmath.jar j3dcore.jar

comma:= :
empty:=
space:= $(empty) $(empty)

LIBS2=$(addprefix $(ROOT)/lib/, $(LIBS))
LIBS3=$(subst $(space),$(comma),$(LIBS2))
# LIBS2=$(patsubst %, $(ROOT)/lib/%, $(LIBS))

all:
	#echo $(LIBS3)
	# javac -classpath $(LIBS3):src/. src/blink/*.java 
	# find src/. | grep java$ | grep -v "#" | xargs -I {} -t javac -classpath $(LIBS3):$(ROOT)/src {}
	find . | grep [.]java$ > file.list
	javac -classpath $(LIBS3):src/. @file.list


run:
	java -classpath $(LIBS3):src/. blink/cli/CommandLineInterface