%include {

#include <assert.h>
#include "tree.c"
#include "lexer.h"
}

%syntax_error			{ fprintf(stderr, "\n	***syntax error***\n\n"); }
%parse_accept			{ printf("parsing complete!\n");}
%type constSpec	{ treeNode * }
%type varDecl {treeNode * }
%type varSpec {treeNode * }
%type statement { treeNode * }



%type varDeclRepeat {treeNode * }
/*
%left ID.
%left NUM.
%left AND.
%left OR.
%left ASSIGNOP.
%left RELOP.
%left BINARYOP.
%left UNARYOP.
%left ADDOP.
%left MULOP.
%left LPAREN.
%left RPAREN.
*/


/*
 BINARYOP.
binaryOp ::= RELOP.
binaryOp ::= ADDOP.
binaryOp ::= MULOP.
*/



/*TODO: TREAT ID AS TRENODE * OR STR*/
program ::= packageClause(B) SEMICOLON topLevelDecl SEMICOLON.	{
	printf("	accept program\n");
	treeNode *rootNode = newNode("root", 2);
	rootNode->child[0] = B;
	rootNode->child[1] = NULL;
	traverseTree(0, rootNode, print, NULL);
}

packageClause(A) ::= PACKAGE(B) identifier(C).	{
	treeNode *packageNode = newNode(B, 1);
	packageNode->child[0] = newNode(C, 0);
	A = packageNode;
}

identifier(A) ::= ID(B).	{
	A = B; //pass the ID string up
}

topLevelDecl(A) ::= topLevelDecl2(B) topLevelDecl3(C) functionDecl(D).	{ 
	treeNode *topLevelNode = newNode("Top Level Decl", 3);
	topLevelNode->child[0] = B;
	topLevelNode->child[1] = C;

}
topLevelDecl2(A) ::= constDecl(B) SEMICOLON.	{
	A = B;
}
topLevelDecl2(A) ::= .	{
	A = NULL;
}
topLevelDecl3(A) ::= varDecl(B) SEMICOLON.	{
	A = B;
}
topLevelDecl3(A) ::= .	{
	A = NULL;
}


constDecl(A) ::= CONST constSpec(B). {
	A = B;
}
constDecl(A) ::= CONST LPAREN constDeclRepeat(B) RPAREN. {
	A = B;
}

constDeclRepeat(A) ::= constSpec(B) SEMICOLON constDeclRepeat(C). {
	B->sibling = C;
	A = B;
}
constDeclRepeat(A) ::= . {
	A = NULL;
}
constSpec(A) ::= identifier(B) EQUALS NUM(C). {
	treeNode *constNode = newNode("=", 2);
	constNode->child[0] = newNode(B, 0);
	constNode->child[1] = newNode(C, 0);
	constNode->sibling = NULL;
	A = constNode;
}
constSpec(A) ::= identifier(B) EQUALS STR(C). {
    treeNode *constNode = newNode("=", 2);
    constNode->child[0] = newNode(B, 0);
    constNode->child[1] = newNode(C, 0);
	constNode->sibling = NULL;
	A = constNode;
}


varDecl(A) ::= VAR varSpec(B). {
	A = B;
	A->sibling = NULL;
}
varDecl(A) ::= VAR LPAREN varDeclRepeat(B) RPAREN. {
	A = B;
}
varDeclRepeat(A) ::= varSpec(B) SEMICOLON varDeclRepeat(C). {
	B->sibling = C;
	A = B;	
}
varDeclRepeat(A) ::= . {
	A = NULL;
}

varSpec(A) ::= identifier(B) type(C). {
	treeNode *varSpecNode = newNode("Var Spec", 2);
	varSpecNode->child[0] = newNode(B, 0);
	varSpecNode->child[1] = C;
	A = varSpecNode;
}
type(A) ::= typeName(B). {
	A = B;
}
type ::= arrayType.
arrayType(A) ::= LSQUARE constant(B) RSQUARE typeName(C). {
	
	treeNode *typeNode = newNode("Type", 2);
	treeNode *constNode = newNode(B, 0);
	typeNode->child[0] = constNode;
	typeNode->child[1] = C;
	A = typeNode;
}
typeName(A) ::= INT(B). {
	treeNode *typeNode = newNode(B, 0);
	A = typeNode;
}


functionDecl(A) ::= FUNC identifier(B) LPAREN RPAREN block(C). {
	treeNode *funcNode = newNode("Func", 2);
	funcNode->child[0] = newNode(B, 0);
	funcNode->child[1] = C;
}


block(A) ::= LBRACE block2(B) block3(C) statementList(D) RBRACE. {
	treeNode *blockNode	= newNode("Block", 3);
	blockNode->child[0] = B;
	blockNode->child[1] = C;
	blockNode->child[2] = D;
}
block2(A) ::= constDecl(B) SEMICOLON. {
	A = B;
}
block2(A) ::= . {
	A = NULL;
}
block3(A) ::= varDecl(B) SEMICOLON. {
	A = B;
}
block3(A) ::= . {
	A = NULL;
}

statementList(A) ::= statementListRepeat(B). {
	A = B;
}
statementListRepeat(A) ::= statement(B) SEMICOLON statementListRepeat(C). {
	B->sibling = C;
	A = B;
}
statementListRepeat(A) ::= . {
	A = NULL;
}

statement(A) ::= simpleStmt(B). { A = B; }
statement(A) ::= breakStmt(B). { A = B; }												
statement(A) ::= continueStmt(B). { A = B; }
statement(A) ::= block(B). { A = B; }
statement(A) ::= ifStmt(B). { A = B; }
statement(A) ::= switchStmt(B). { A = B; }
statement(A) ::= forStmt(B). { A = B; }
statement(A) ::= printlnStmt(B). { A = B; }
statement(A) ::= readlnStmt(B). { A = B; }

simpleStmt(A) ::= emptyStmt(B). { A = B; }
simpleStmt(A) ::= assignmentStmt(B). { A = B; }
simpleStmt(A) ::= expression(B). { A = B; }

emptyStmt(A) ::= . { A = NULL; }
assignmentStmt(A) ::= variable(B) assignOp(C) expression(D). {
	treeNode *assignNode = newNode(C, 2);
	assignNode->child[0] = B;
	assignNode->child[1] = D;
	A = assignNode;
}

variable(A) ::= identifier(B). {
	treeNode *varNode = newNode("Var", 2);
	varNode->child[0] = newNode(B, 0);
	varNode->child[1] = NULL;
}
variable(A) ::= identifier(B) LSQUARE expression(C) RSQUARE. {
    treeNode *varNode = newNode("Var", 2);
    varNode->child[0] = newNode(B, 0);
    varNode->child[1] = C;
}
assignOp(A) ::= ASSIGNOP(B). {
	A = B;
}
expression ::= unaryExpr.
expression ::= expression binaryOp expression.
unaryExpr ::= unaryOp unaryExpr.
unaryExpr ::= primaryExpr.

primaryExpr(A) ::= NUM(B). {
	treeNode *primExpNode = newNode("Primary Expression", 2);
	primExpNode->child[0] = newNode(B, 0);
	primExpNode->child[1] = NULL;
}
primaryExpr(A) ::= LPAREN expression(B) RPAREN. {
    treeNode *primExpNode = newNode("Primary Expression", 2);
    primExpNode->child[0] = NULL;
    primExpNode->child[1] = B;

}
primaryExpr(A) ::= identifier(B) LSQUARE expression(C) RSQUARE. {
    treeNode *primExpNode = newNode("Primary Expression", 2);
    primExpNode->child[0] = newNode(B, 0);
    primExpNode->child[1] = C;

}
primaryExpr(A) ::= identifier(B). {
    treeNode *primExpNode = newNode("Primary Expression", 2);
    primExpNode->child[0] = newNode(B, 0);
    primExpNode->child[1] = NULL;
}

unaryOp(A) ::= UNARYOP(B). { A = B; }

binaryOp ::= BINARYOP.
binaryOp ::= binaryOp2.
binaryOp2 ::= ADDOP.
binaryOp2 ::= binaryOp3.
binaryOp3 ::= MULOP.
binaryOp3 ::= binaryOp4.
binaryOp4 ::= UNIARYOP.


/*
binaryOp(A) ::= BINARYOP(B). { A = B; }
binaryOp(A) ::= RELOP(B). { A = B; }
binaryOp(A) ::= ADDOP(B). { A = B; }
binaryOp(A) ::= MULOP(B). { A = B; }
*/
breakStmt(A) ::= BREAK(B). { A = B; }
continueStmt(A) ::= CONTINUE(B). { A = B; }


ifStmt(A) ::= IF condition(B) block(C). {
	treeNode *ifNode = newNode("IF", 3); //children are condition, block, else. else child is block or ifstmt
	ifNode->child[0] = B;
	ifNode->child[1] = C;
	ifNode->child[2] = NULL;
	A = ifNode;
}
ifStmt(A) ::= IF condition(B) block(C) ELSE ifStmt(D). {
	treeNode *ifNode = newNode("IF", 3); //children are condition, block, else. else child is block or ifstmt
    ifNode->child[0] = B;
    ifNode->child[1] = C;
    treeNode *elseNode = newNode("Else", 1);
	elseNode->child[0] = D;
	ifNode->child[2] = elseNode;
    A = ifNode;
}


ifStmt(A) ::= IF condition(B) block(C) ELSE block(D). {
    treeNode *ifNode = newNode("IF", 3); //children are condition, block, else. else child is block or ifstmt
    ifNode->child[0] = B;
    ifNode->child[1] = C;
    treeNode *elseNode = newNode("Else", 1);
    elseNode->child[0] = D;
    ifNode->child[2] = elseNode;
    A = ifNode;
}

condition(A) ::= expression(B). { A = B; }
switchStmt ::= SWITCH expression LBRACE switchStmtRepeat RBRACE.	{printf("	switch statement\n");}
switchStmtRepeat ::= caseClause switchStmtRepeat.
switchStmtRepeat ::= .
caseClause ::= switchCase COLON statementList.

switchCase ::= CASE constList.
switchCase ::= DEFAULT.
constList ::= constant constListRepeat.
constListRepeat ::= COMMA constant constListRepeat.
constListRepeat ::= .
constant(A) ::= ID(B). { A = B; }
constant(A) ::= NUM(B). { A = B; }

forStmt ::= FOR block.										{printf("	for statement\n");}
forStmt ::= FOR condition block.							{printf("   for statement\n");}


printlnStmt ::= PRINTLN LPAREN expression printlnStmtRepeat RPAREN.	{printf("	print statement\n");}
printlnStmtRepeat ::= COMMA expression printlnStmtRepeat.
printlnStmtRepeat ::= .

readlnStmt ::= READLN LPAREN AMPERSAND variable readlnStmtRepeat.	{printf("   readln statement\n");}
readlnStmtRepeat ::= COMMA AMPERSAND variable readlnStmtRepeat.
readlnStmtRepeat ::= RPAREN.





