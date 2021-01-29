
letter_				[A-Za-z_]
decimal_lit			0|[1-9]("_"?[0-9])*
hex_lit				0[xX]("_"?[0-9A-Fa-f])+
string_lit			`[\x20-\x5F\x61-\x7F\x0A]*`
whitespace			[\n\t ]
add_operator		("+"|"-")
binary_operator		("||"|"&&")
relational_operator	("=="|"!="|"<"|"<="|">"|">=")
multiply_operator	("*"|"/"|"%"|"<<"|">>")
assign_operator		("="|"+="|"-=")

%option noyywrap nounput noinput yylineno

%top{
/*
	CMPT 399 assignment 2
	Author: Scott Cameron
	
	Description: Parse tokens from a text file to the MacEwan Go Spec
*/
#include <stdlib.h>

#define YYSTYPE char *
extern int yylval;


// all available tokens, can be expanded easily.
// NOTE: some tokens such as LINE_COMMENT_START and BLOCK_COMMENT_END are 
// only used in the intermediary getToken() function and should never be returned by getToken()
typedef enum {
	// this list of tokens matches the generated tokens in parser.h
	// if the token enum is changed by modifying parser.y then this must be changed
	T_ENDFILE,
	SEMICOLON ,
	PACKAGE   ,
	ID        ,
	CONST     ,
	LPAREN    ,
	RPAREN    ,
	ASSIGNOP  ,
	NUM       ,
	STR       ,
	VAR       ,
	LSQUARE   ,
	RSQUARE   ,
	INT       ,
	FUNC      ,
	LBRACE    ,
	RBRACE    ,
	BINARYOP  ,
	RELOP     ,
	ADDOP     ,
	MULOP     ,
	UNARYOP   ,
	BREAK     ,
	CONTINUE  ,
	IF        ,
	ELSE      ,
	SWITCH    ,
	COLON     ,
	CASE      ,
	DEFAULT   ,
	COMMA     ,
	FOR       ,
	PRINTLN   ,
	READLN    ,
	AMPERSAND ,
	T_ERROR   ,
	BLOCK_COMMENT_START,
	BLOCK_COMMENT_END,
	NEWLINE,
	DOT,
	


} Token;

// the string value of each token
static char *TokenNames[] = {
    "T_ENDFILE" ,
    "SEMICOLON" ,
    "PACKAGE"   ,
    "ID"        ,
    "CONST"     ,
    "RPAREN"    ,
    "LPAREN"    ,
    "ASSIGNOP"  ,
    "NUM"       ,
    "STR"       ,
    "VAR"       ,
    "LSQUARE"   ,
    "RSQUARE"   ,
    "INT"       ,
    "FUNC"      ,
    "LBRACE"    ,
    "RBRACE"    ,
    "BINARYOP"  ,
    "RELOP"     ,
    "ADDOP"     ,
    "MULOP"     ,
    "UNARYOP"	,
	"BREAK"     ,
    "CONTINUE"  ,
    "IF"        ,
    "ELSE"      ,
    "SWITCH"    ,
    "COLON"     ,
    "CASE"      ,
    "DEFAULT"   ,
    "COMMA"     ,
    "FOR"       ,
    "PRINTLN"   ,
    "READLN"    ,
    "AMPERSAND" ,
    "T_ERROR"   ,
    "BLOCK_COMMENT_START",
    "BLOCK_COMMENT_END",
    "NEWLINE",
    "DOT",


};

int getToken(void);

}
%%

package						{ return PACKAGE;}
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

{letter_}({letter_}|[0-9])*   {return ID;}
{decimal_lit}|{hex_lit}     {return NUM;}
{string_lit}                {return STR;}

{add_operator}				{return ADDOP;}
{binary_operator}			{return BINARYOP;}
{relational_operator}		{return RELOP;}
{multiply_operator}			{return MULOP;}
{assign_operator}			{return ASSIGNOP;}

"/*"                        {return BLOCK_COMMENT_START;}
"*/"                        {return BLOCK_COMMENT_END;}
<<EOF>>						{return T_ENDFILE;}
"\n"						{return NEWLINE;}
[ \t]
.							{return T_ERROR;}


%%


// Get and return the next valid token
int getToken() {
	int tok;
	int blockCommentFlag = 0; // 1 if you are in a block comment
	int restartFlag = 0; 	// used to run through the loop 1 extra time to catch back-to-back
							// comments and newlines
	
	// check for multi-line comments and keep calling
    // yyex() until you are no longer reading a comment
	do {
		restartFlag = 0;
		tok = yylex();

		// keep track of current line number
		if(tok == NEWLINE)
			restartFlag = 1;

		// check if you are exiting a comment
		if(blockCommentFlag && tok == BLOCK_COMMENT_END){
			blockCommentFlag = 0;
			restartFlag = 1;
		}
	
		// check if you are initiating a comment
		if(tok == BLOCK_COMMENT_START)
            blockCommentFlag = 1;

	} while(blockCommentFlag || restartFlag);

	// edge case for when you get a */ when you arent inside a block comment
	if(tok == BLOCK_COMMENT_END)
		return T_ERROR;
	return tok;
}
