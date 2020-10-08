all: lex.yy.c mgo


mgo: mgo.c lex.yy.c
	gcc -o mgo mgo.c

lex.yy.c: lexer.l
	flex lexer.l


clean:
	rm mgo lex.yy.c

tests: mgo tests/*
	./mgo -v tests/simple.go
	./mgo -v tests/nums.txt
	./mgo -v tests/comments.txt
	./mgo -v tests/operators.txt
	./mgo -v tests/keywords.txt
	./mgo -v tests/strings.txt
