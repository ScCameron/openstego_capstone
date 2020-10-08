all: lex.yy.c mgo


mgo: mgo.c lex.yy.c
	gcc -o mgo mgo.c

lex.yy.c: lexer.l
	flex lexer.l


clean:
	rm mgo lex.yy.c

tests: mgo tests/*
	./mgo tests/simple.go
	./mgo tests/nums.txt
	./mgo tests/comments.txt

