/*
	CMPT 399 tree.c
	Assignment 2
	by Scott Cameron
*/
#include "tree.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>


void mallocError() {
	fprintf(stderr, "error, malloc failed\n");
	exit(1);
}


treeNode *newNode(char *value, int childCount) {
	treeNode *node = (treeNode *) malloc(sizeof(treeNode));
	if(node == NULL) mallocError();

	
	// allocate the value string so it can be used outside of the function
	char* nodeValue = (char *) calloc(strlen(value)+1, sizeof(char));
	if(nodeValue == NULL) mallocError();
	strcpy(nodeValue, value);
	node -> value = value;

	// allocate aa array of treeNode * children	
	treeNode **children = calloc(childCount, sizeof(treeNode *));
	if(children == NULL) mallocError();
	node -> child = children;
	node -> childCount = childCount;
	return node;
}


void traverseTree(treeNode *rootNode, int depth, void (*preOrder)(treeNode *, int), void (*postOrder)(treeNode *, int)) {


	if(preOrder != NULL) {
		preOrder(rootNode, depth);
	}
	
	// recursively traverse every treeNode child
	int i;
	for(i = 0; i < rootNode -> childCount; i++) {
		if(rootNode -> child[i] == NULL) { continue; }
		traverseTree(rootNode -> child[i], depth + 1, preOrder, postOrder);
	}

	// traverse the node's sibling after recursively traversing the children
	if(rootNode -> sibling != NULL)
		traverseTree(rootNode -> sibling, depth, preOrder, postOrder);

	if(postOrder != NULL) {
        postOrder(rootNode, depth);
    }
}

void destroyTreeNode(treeNode *node, int depth) {
	//free(node->value); //NOTE: currently can cause a strange segfault if uncommented
	free(node->child);
	free(node);
}


void printTreeNode(treeNode *node, int depth) {
	int depthMultiplier = 4;
	printf("%*s%s\n", depth*depthMultiplier, "", node->value);
}
