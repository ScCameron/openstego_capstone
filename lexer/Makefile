all: mgo

mgo: mgo.c lex.yy.c
	gcc -o mgo mgo.c

lex.yy.c: lexer.l
	flex lexer.l

clean:
	rm mgo lex.yy.c

tests: mgo
	./mgo -v tests/simple.go
	./mgo -v tests/nums.go
	./mgo -v tests/comments.go
	./mgo -v tests/operators.go
	./mgo -v tests/keywords.go
	./mgo -v tests/strings.go
	./mgo -v tests/example.go
