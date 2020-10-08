
letter_			[A-Za-z_]
decimal_lit		0|[1-9]("_"?[0-9])*
hex_lit			0[xX]("_"?[0-9A-Fa-f])+
string_lit		"'"[\x20-\x7F\x0A]*"'"
whitespace		[\n\t ]

%option noyywrap nounput noinput

%top{
/*
	CMPT 399 assignment 1
	Author: Scott Cameron
	
	description

	references
*/
#include <stdio.h>

typedef enum {
	T_ENDFILE, T_ERROR, PACKAGE, ID, FUNC, LPAREN, RPAREN, LBRACE, RBRACE, 
	PRINTLN, COMMA, NUM, BREAK, CASE, CONST, CONTINUE, DEFAULT, ELSE, FOR,
	IF, INT, READLN, SWITCH, VAR, STR, 
	BLOCK_COMMENT_START, LINE_COMMENT_START, BLOCK_COMMENT_END, NEWLINE, DUMMY_TOK
} Token;

static char *TokenNames[] = {
	"T_ENDFILE", "ERROR", "PACKAGE", "ID", "FUNC", "LPAREN", "RPAREN", "LBRACE", "RBRACE",
    "PRINTLN", "COMMA", "NUM", "BREAK", "CASE", "CONST", "CONTINUE", "DEFAULT", "ELSE", "FOR",
    "IF", "INT", "READLN", "SWITCH", "VAR", "STR",
    "BLOCK_COMMENT_START", "LINE_COMMENT_START", "BLOCK_COMMENT_END", "NEWLINE", "DUUUUUUUUUUMY"
};

int lineNumber;

}


%%
package						{return PACKAGE;} 					
func						{return FUNC;}
"("							{return LPAREN;}
")"							{return RPAREN;}
"{"							{return LBRACE;}
"}"							{return RBRACE;}
","							{return COMMA;}
println						{return PRINTLN;}
break						{return BREAK;}
case						{return CASE;}
const						{return CONST;}
continue					{return CONTINUE;}
default						{return DEFAULT;}
else						{return ELSE;}
for							{return FOR;}
if							{return IF;}
int							{return INT;}
readln						{return READLN;}
switch						{return SWITCH;}
var							{return VAR;}

{letter_}({letter_}|0-9)*   {return ID;}
{decimal_lit}|{hex_lit}     {return NUM;}
{string_lit}                {return STR;}


"/*"                        {return BLOCK_COMMENT_START;}
"*/"                        {return BLOCK_COMMENT_END;}
"//"                        {return LINE_COMMENT_START;}
<<EOF>>						{return T_ENDFILE;}
"\n"						{return NEWLINE;}
[ \t]
.							{return T_ERROR;}


%%


int getToken() {
	int tok;
	

	// check for multi-line and single-line comments and keep calling
	// yyex() until you are no longer reading a comment
	int blockCommentFlag = 0; // 1 if you are in a block comment
	int lineCommentFlag = 0; // 1 if you are in a line comment
	int restartFlag = 0;
	
	do{
		restartFlag = 0;
		tok = yylex();
		if(tok == NEWLINE){
			lineNumber++;
			restartFlag = 1;;
		}

		// first check if you are exiting a comment
		if(!lineCommentFlag && blockCommentFlag && tok == BLOCK_COMMENT_END){
			blockCommentFlag = 0;
			restartFlag = 1;
		}
		if(!blockCommentFlag && lineCommentFlag && tok == NEWLINE){
            lineCommentFlag = 0;
			restartFlag = 1;
		}
	
		// next check if you are initiating a comment in case you have back-to-back comments
		if(!lineCommentFlag && tok == BLOCK_COMMENT_START)
            blockCommentFlag = 1;
		if(!blockCommentFlag && tok == LINE_COMMENT_START)
			lineCommentFlag = 1;

	}while(blockCommentFlag || lineCommentFlag || restartFlag);

	return tok;
	
}


