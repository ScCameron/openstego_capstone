



#used makefile from lab 4 as a template
SHELL=/bin/bash
CC=gcc
CFLAGS=-std=c99 #-Wall
LEX=flex
LFLAGS=--header-file=$*.h

all: mgo

mgo: mgo.o parser.o lexer.o

mgo.o: mgo.c parser.h lexer.h

lemon: lemon.o
lemon.o: lemon.c

parser.c parser.h: parser.y lempar.c lemon
	./lemon $< || true

lexer.o: lexer.c parser.h
	$(CC) $(CFLAGS) -D_POSIX_C_SOURCE=1 -Wno-unused-function -c $<
%.c %.h: %.l
	$(LEX) $(LFLAGS) -o $*.c $<


clean:
	$(RM) *.o *.out parser.h parser.c lexer.h lexer.c lemon mgo

