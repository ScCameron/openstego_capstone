#include <stdio.h>
#include <unistd.h>
#include <getopt.h>
#include "lexer.h"
#include "parser.h"
#include "tree.h"

void *ParseAlloc(void *(*)(size_t));
void Parse(void *, int, void *, treeNode *);
void ParseFree(void *, void (*)(void *));

void printHelp() {
printf("usage: mgo [options] file\n\n\
Compile programs written in the MacEwan Go programming language.\n\n\
Options :\n\
    -h      display this help and exit\n\
    -v      display extra ( verbose ) debugging information\n\
            ( multiple -v options increase verbosity )\n");

}


// Parse arguments and get all tokens from an input file
int main(int argc, char **argv ) {
	int verboseLevel = 0;
	int c;

	// getopt with options "h" for help message and "v" for verbose output
	while((c = getopt (argc, argv, "hv")) != -1){
		switch(c){
			case 'v':
				verboseLevel++;
				break;
			case 'h':
				printHelp();
				return 0;
		}
	}

	// if the user didn't input a filename, print usage and exit
	if(optind == argc){
        printHelp();
		return 0;
    }

	// set yyin to open the user input filename, exit on error 
    if((yyin = fopen(argv[optind], "r")) == NULL){
		printf("Please enter a valid filename\n");
		return 0;
	}



	void *parser = ParseAlloc(malloc);
	


	// get tokens from input file until end of file is reached
    int tok;
	char *yylval;
	treeNode *root = newNode("Root", 1);
	do {
		tok = getToken();
		if(verboseLevel > 1)
			printf("%d, %s, %s\n", yylineno, TokenNames[tok], yytext);
		char *tokText = calloc(strlen(yytext+1), sizeof(char));
		strcpy(tokText, yytext);
		Parse(parser, tok, tokText, root);
		//free(tokText);
	} while (tok != T_ENDFILE);

	if(verboseLevel > 0)
		traverseTree(0, root, printTreeNode, NULL);
	ParseFree(parser, free);
	yylex_destroy();
	traverseTree(0, root, printTreeNode, destroyTreeNode);

	return 0;
}
