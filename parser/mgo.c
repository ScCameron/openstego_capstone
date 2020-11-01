#include <stdio.h>
#include <unistd.h>
#include <getopt.h>
#include "lexer.h"
#include "parser.h"
#include "tree.h"

void *ParseAlloc(void *(*)(size_t));
void Parse(void *, int, void *);
void ParseFree(void *, void (*)(void *));


// Parse arguments and get all tokens from an input file
int main(int argc, char **argv ) {
	int verboseFlag =0;
	int c;

	// getopt with options "h" for help message and "v" for verbose output
	while((c = getopt (argc, argv, "hv")) != -1){
		switch(c){
			case 'v':
				verboseFlag = 1;
				break;
			case 'h':
				printf("usage: mgo [options] file\n");
				return 0;
		}
	}

	// if the user didn't input a filename, print usage and exit
	if(optind == argc){
        printf("usage: mgo [options] file\n");
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
	do {
		tok = getToken();
		//yylval = getyylval();
		if(verboseFlag)
			printf("%d, %s, %s\n", yylineno, TokenNames[tok], yytext);
		    
		char *tokText = calloc(strlen(yytext+1), sizeof(char));
		strcpy(tokText, yytext);
		Parse(parser, tok, tokText);
		//free(tokText);
	} while (tok != T_ENDFILE);

	ParseFree(parser, free);
	
	yylex_destroy();

	return 0;
}
