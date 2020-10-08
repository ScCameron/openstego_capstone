#include <stdio.h>
#include "lex.yy.c"


//TODO add getopt stuff
int main(int argc, char **argv ) {
	lineNumber = 1;
	int verboseFlag = 1;

	if ( argc > 1 )
            yyin = fopen( argv[1], "r" );
    else
            yyin = stdin;

    int tok;
    while((tok = getToken()) != T_ENDFILE){
		if(verboseFlag)
			printf("%d, %s, %s\n", lineNumber, TokenNames[tok], yytext);
    }

}
