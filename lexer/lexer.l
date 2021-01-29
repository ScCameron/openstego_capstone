
letter_				[A-Za-z_]
decimal_lit			0|[1-9]("_"?[0-9])*
hex_lit				0[xX]("_"?[0-9A-Fa-f])+
string_lit			'[\x20-\x26\x28-\x7F\x0A]*'
whitespace			[\n\t ]
add_operator		("+"|"-")
binary_operator		("||"|"&&")
relational_operator	("=="|"!="|"<"|"<="|">"|">=")
multiply_operator	("*"|"/"|"%"|"<<"|">>")
assign_operator		("="|"+="|"-=")

%option noyywrap nounput noinput yylineno

%top{
/*
	CMPT 399 assignment 1
	Author: Scott Cameron
	
	Description: Parse tokens from a text file to the MacEwan Go Spec
*/
#include <stdio.h>

// all available tokens, can be expanded easily.
// NOTE: some tokens such as LINE_COMMENT_START and BLOCK_COMMENT_END are 
// only used in the intermediary getToken() function and should never be returned by getToken()
typedef enum {
	T_ENDFILE, T_ERROR, PACKAGE, ID, FUNC, LPAREN, RPAREN, LBRACE, RBRACE, 
	PRINTLN, COMMA, NUM, BREAK, CASE, CONST, CONTINUE, DEFAULT, ELSE, FOR,
	IF, INT, READLN, SWITCH, VAR, STR, ADDOP, BINARYOP, RELOP, MULOP, ASSIGNOP,
	BLOCK_COMMENT_START, LINE_COMMENT_START, BLOCK_COMMENT_END, NEWLINE, 
	SEMICOLON, DOT, LSQUARE, RSQUARE, AMPERSAND, COLON,
	DUMMY_TOK
} Token;

static char *TokenNames[] = {
	"T_ENDFILE", "ERROR", "PACKAGE", "ID", "FUNC", "LPAREN", "RPAREN", "LBRACE", "RBRACE",
    "PRINTLN", "COMMA", "NUM", "BREAK", "CASE", "CONST", "CONTINUE", "DEFAULT", "ELSE", "FOR",
    "IF", "INT", "READLN", "SWITCH", "VAR", "STR", "ADDOP", "BINARYOP", "RELOP", "MULOP", "ASSIGNOP",
    "BLOCK_COMMENT_START", "LINE_COMMENT_START", "BLOCK_COMMENT_END", "NEWLINE", 
	"SEMICOLON", "DOT", "LSQUARE", "RSQUARE", "AMPERSAND", "COLON",
	"DUMMY"
};

}


%%
package						{return PACKAGE;} 					
func						{return FUNC;}
"("							{return LPAREN;}
")"							{return RPAREN;}
"{"							{return LBRACE;}
"}"							{return RBRACE;}
","							{return COMMA;}
";"							{return SEMICOLON;}
"."							{return DOT;}
"["							{return LSQUARE;}
"]"							{return RSQUARE;}
"&"							{return AMPERSAND;}
":"							{return COLON;}
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

{add_operator}				{return ADDOP;}
{binary_operator}			{return BINARYOP;}
{relational_operator}		{return RELOP;}
{multiply_operator}			{return MULOP;}
{assign_operator}			{return ASSIGNOP;}

"/*"                        {return BLOCK_COMMENT_START;}
"*/"                        {return BLOCK_COMMENT_END;}
"//"                        {return LINE_COMMENT_START;}
<<EOF>>						{return T_ENDFILE;}
"\n"						{return NEWLINE;}
[ \t]
.							{return T_ERROR;}


%%

// Get and return the next valid token
int getToken() {
	int tok;

	int blockCommentFlag = 0; // 1 if you are in a block comment
	int lineCommentFlag = 0; // 1 if you are in a line comment
	int restartFlag = 0; 	// used to run through the loop 1 extra time to catch back-to-back
							// comments and newlines

	// check for multi-line and single-line comments and keep calling
    // yyex() until you are no longer reading a comment
	do{
		restartFlag = 0;
		tok = yylex();

		// keep track of current line number
		if(tok == NEWLINE)
			restartFlag = 1;

		// check if you are exiting a comment
		if(!lineCommentFlag && blockCommentFlag && tok == BLOCK_COMMENT_END){
			blockCommentFlag = 0;
			restartFlag = 1;
		}
		if(!blockCommentFlag && lineCommentFlag && tok == NEWLINE){
            lineCommentFlag = 0;
			restartFlag = 1;
		}
	
		// check if you are initiating a comment
		if(!lineCommentFlag && tok == BLOCK_COMMENT_START)
            blockCommentFlag = 1;
		if(!blockCommentFlag && tok == LINE_COMMENT_START)
			lineCommentFlag = 1;

	} while(blockCommentFlag || lineCommentFlag || restartFlag);

	// edge case for when you get a */ when you arent inside a block comment
	if(tok == BLOCK_COMMENT_END)
		return T_ERROR;

	return tok;
}


