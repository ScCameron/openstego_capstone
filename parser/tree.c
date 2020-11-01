
#include "tree.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>


//change more from nicks version

treeNode *newNode(char *value, int childCount) {
	treeNode *node = (treeNode *) malloc(sizeof(treeNode));

	char* nodeValue = (char *) calloc(strlen(value)+1, sizeof(char));
	strcpy(nodeValue, value);
	node -> value = value;

	treeNode **children = calloc(childCount+1, sizeof(treeNode *));
	node -> child = children;

	return node;

}


void traverseTree(int depth, treeNode *rootNode, void (*preOrder)(int, treeNode *), void (*postOrder)(int, treeNode *)) {

	if(preOrder != NULL) {
		preOrder(depth, rootNode);
	}
	

	int i;
	for(i = 0; rootNode -> child[i] != NULL; i++) {
		traverseTree(depth+1, rootNode -> child[i], preOrder, postOrder);
	}

	if(postOrder != NULL) {
        postOrder(depth, rootNode);
    }



}

void destroyTree(int depth, treeNode *node) {
	free(node->value);
	free(node->child[0]);
	free(node);
}


void print(int level, treeNode *t) {
	printf("%*s%s\n", level, "", t->value);
}


void printSymbol(char *startPtr, char *endPtr) {
	char *i;
	for(i=startPtr; i<(char *)endPtr; i++)
		printf("%c", *i);
	printf("\n");
}

//delete, for testing only
int test(void) {
	treeNode *root, *tmp;
	
	root = newNode("root", 3);
	
	root->sibling = newNode("sibling1", 0);
    
	root->child[0] = newNode("child 1", 1);
    root->child[0]->child[0] = newNode("grandchild A of 1", 0);
    root->child[1] = newNode("child 2", 2);
    root->child[1]->child[0] = newNode("grandchild A of 2", 0);
    root->child[1]->child[1] = newNode("grandchild B of 2", 0);
    root->child[2] = newNode("child 3", 0);

	traverseTree(0, root, print, NULL);
	traverseTree(0, root, destroyTree, NULL);



}

