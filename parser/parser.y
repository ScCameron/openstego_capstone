%include {

#include <assert.h>
#include "tree.c"
#include "lexer.h"
}

%extra_argument { treeNode *root }

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

program ::= packageClause(B) SEMICOLON topLevelDecl(C) SEMICOLON.	{
	treeNode *programNode = newNode("program", 2);
	programNode->child[0] = B;
	programNode->child[1] = C;
	root->child[0] = programNode;
}

packageClause(A) ::= PACKAGE(B) identifier(C).	{
	treeNode *packageNode = newNode(B, 1);
	packageNode->child[0] = newNode(C, 0);
	A = packageNode;
}

identifier(A) ::= ID(B).	{
	A = B; //pass the ID string up
}

topLevelDecl(A) ::= topConstDecl(B) topVarDecl(C) functionDecl(D).	{ 
	treeNode *topLevelNode = newNode("Top Level Decl", 3);
	topLevelNode->child[0] = B;
	topLevelNode->child[1] = C;
	topLevelNode->child[2] = D;
	A = topLevelNode;
}
topConstDecl(A) ::= constDecl(B) SEMICOLON.	{
	A = B;
}
topConstDecl(A) ::= . {
	A = NULL;
}
topVarDecl(A) ::= varDecl(B) SEMICOLON.	{
	A = B;
}
topVarDecl(A) ::= . {
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
constSpec(A) ::= identifier(B) ASSIGNOP NUM(C). { /*unsure how to separate "=" from assignop*/
	treeNode *constNode = newNode("=", 2);
	constNode->child[0] = newNode(B, 0);
	constNode->child[1] = newNode(C, 0);
	constNode->sibling = NULL;
	A = constNode;
}
constSpec(A) ::= identifier(B) ASSIGNOP STR(C). {
    treeNode *constNode = newNode("=", 2);
    constNode->child[0] = newNode(B, 0);
    constNode->child[1] = newNode(C, 0);
	A = constNode;
}


varDecl(A) ::= VAR varSpec(B). {
	A = B;
	/*A->sibling = NULL;*/
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
type(A) ::= arrayType(B). {
	A = B;
}

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
	A = funcNode;
}


block(A) ::= LBRACE block2(B) block3(C) statementList(D) RBRACE. {
	treeNode *blockNode	= newNode("Block", 3);
	blockNode->child[0] = B;
	blockNode->child[1] = C;
	blockNode->child[2] = D;
	A = blockNode;
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
statement(A) ::= breakStmt(B). { A = newNode(B, 0); }												
statement(A) ::= continueStmt(B). { A = newNode(B, 0); }
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
	A = varNode;
}
variable(A) ::= identifier(B) LSQUARE expression(C) RSQUARE. {
    treeNode *varNode = newNode("Var", 2);
    varNode->child[0] = newNode(B, 0);
    varNode->child[1] = C;
	A = varNode;
}


assignOp(A) ::= ASSIGNOP(B). { A = B; }
expression(A) ::= unaryExpr(B). { A = B; }
expression(A) ::= opExpression(B). { A = B; }

%type binopExpression { treeNode * }
opExpression(A) ::= expression(B) binopExpression(C). {
	C->child[0] = B;
	A = C;
}
binopExpression(A) ::= BINARYOP(B) relopExpression(C). {
	treeNode *binaryOpNode = newNode(B, 2);
	binaryOpNode->child[1] = C;
	A = binaryOpNode;
}/*
binopExpression(A) ::= BINARYOP(B) unaryExpr(C). {
	treeNode *binaryOpNode = newNode(B, 2);
	binaryOpNode->child[1] = C;
	A = binaryOpNode;
}*/
binopExpression(A) ::= relopExpression(B). { A = B; }


relopExpression(A) ::= RELOP(B) addExpression(C). {
	treeNode *relOpNode = newNode(B, 2);
	relOpNode->child[1] = C;
	A = relOpNode;
}/*
relopExpression(A) ::= RELOP(B) unaryExpr(C). {
	treeNode *relOpNode = newNode(B, 2);
	relOpNode->child[1] = C;
	A = relOpNode;
}*/
relopExpression(A) ::= addExpression(B). { A = B; }


addExpression(A) ::= ADDOP(B) mulExpression(C). {
	treeNode *addOpNode = newNode(B, 2);
	addOpNode->child[1] = C;
	A = addOpNode;
}/*
addExpression(A) ::= ADDOP(B) unaryExpr(C). {
	treeNode *addOpNode = newNode(B, 2);
	addOpNode->child[1] = C;
	A = addOpNode;
}*/
addExpression(A) ::= mulExpression(B). { A = B; }

mulExpression(A) ::= MULOP(B) unaryExpr(C). {
	treeNode *mulOpNode = newNode(B, 2);
	mulOpNode->child[1] = C;
	A = mulOpNode;
}

mulExpression(A) ::= unaryExpr(B). { A = B; }
/*
mulExpression(A) ::= identifier(B). {
	treeNode *idNode = newNode(B, 0);
	A = idNode;
}
mulExpression(A) ::= NUM(B). {
	treeNode *idNode = newNode(B, 0);
	A = idNode;
}
*/

unaryExpr(A) ::= unaryOp(B) unaryExpr(C). {
	treeNode *unaryExpNpde = newNode(B, 1);
	unaryExpNpde->child[0] = C;
	A = unaryExpNpde;
}
unaryExpr(A) ::= primaryExpr(B). {
	A = B;
}

primaryExpr(A) ::= NUM(B). {
	treeNode *primExpNode = newNode(B, 1);
	primExpNode->child[0] = NULL;
	A = primExpNode;
}

primaryExpr(A) ::= LPAREN expression(B) RPAREN. {
	A = B;
}
primaryExpr(A) ::= identifier(B) LSQUARE expression(C) RSQUARE. {
	treeNode *primExpNode = newNode(B, 1);
	primExpNode->child[0] = C;
	A = primExpNode;
}
primaryExpr(A) ::= identifier(B). {
	treeNode *primExpNode = newNode(B, 1);
	primExpNode->child[0] = NULL;
	A = primExpNode;
}

unaryOp(A) ::= UNARYOP(B). { A = B; }

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

switchStmt(A) ::= SWITCH expression(B) LBRACE switchStmtRepeat(C) RBRACE. {
	treeNode *switchNode = newNode("Switch", 2);
	switchNode->child[0] = B;
	switchNode->child[1] = C;
	A = switchNode;
}
%type caseClause { treeNode * }
switchStmtRepeat(A) ::= caseClause(B) switchStmtRepeat(C). {
	B->sibling = C;
	A = B;
}
switchStmtRepeat(A) ::= . { A = NULL; }

caseClause(A) ::= switchCase(B) COLON statementList(C). {
	treeNode *caseClauseNode = newNode("Case Clause", 2);
	caseClauseNode->child[0] = B;
	caseClauseNode->child[1] = C;
	A = caseClauseNode;
}

switchCase(A) ::= CASE constList(B). {
	A = B;
}
switchCase(A) ::= DEFAULT. {
	treeNode *switchCaseNode = newNode("Default", 0);
	A = switchCaseNode;
}

constList(A) ::= constant(B) constListRepeat(C). {
	treeNode *constNode = newNode(B, 0);
	constNode->sibling = C;
	A = constNode;
}

constListRepeat(A) ::= COMMA constant(B) constListRepeat(C). {
	treeNode *constNode = newNode(B, 0);
	constNode->sibling = C;
	A = constNode;
}
constListRepeat(A) ::= . { A = NULL; }

constant(A) ::= ID(B). { A = B; }
constant(A) ::= NUM(B). { A = B; }

forStmt(A) ::= FOR block(B). {
	treeNode *forNode = newNode("For", 2);
	forNode->child[0] = NULL;
	forNode->child[1] = B;
	A = forNode;
}
forStmt(A) ::= FOR condition(B) block(C). {
	treeNode *forNode = newNode("For", 2);
	forNode->child[0] = B;
	forNode->child[1] = C;
	A = forNode;
}

%type expression { treeNode * }
printlnStmt(A) ::= PRINTLN LPAREN expression(B) printlnStmtRepeat(C) RPAREN. {
	treeNode *printNode = newNode("Print", 1);
	printNode->child[0] = B;
	printNode->child[0]->sibling = C;
	A = printNode;
}
printlnStmtRepeat(A) ::= COMMA expression(B) printlnStmtRepeat(C). {
	B->sibling = C;
	A = B;
}
printlnStmtRepeat(A) ::= . { A = NULL; }

readlnStmt(A) ::= READLN LPAREN AMPERSAND variable(B) readlnStmtRepeat(C) RPAREN. {
	treeNode *readNode = newNode("Readln", 1);
	readNode->child[0] = B;
	readNode->child[0]->sibling = C;
	A = readNode;
}
%type variable { treeNode * }
readlnStmtRepeat(A) ::= COMMA AMPERSAND variable(B) readlnStmtRepeat(C). {
	B->sibling = C;
	A = B;
}

readlnStmtRepeat(A) ::= . { A = NULL; }


