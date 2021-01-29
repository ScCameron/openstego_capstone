#include <stdio.h>
#include <unistd.h>
#include "lex.yy.c"


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

	// get tokens from input file until end of file is reached
    int tok;
    while((tok = getToken()) != T_ENDFILE){
		if(verboseFlag)
			printf("%d, %s, %s\n", yylineno, TokenNames[tok], yytext);
    }

	return 0;
}
