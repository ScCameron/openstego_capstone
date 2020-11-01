
#include "tree.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>



treeNode *newNode(char *value, int childCount) {
	treeNode *node = (treeNode *) malloc(sizeof(treeNode));

	char* nodeValue = (char *) calloc(strlen(value)+1, sizeof(char));
	strcpy(nodeValue, value);
	node -> value = value;
	
	treeNode **children = calloc(childCount+1, sizeof(treeNode *));
	node -> child = children;
	node -> childCount = childCount;
	return node;

}


void traverseTree(int depth, treeNode *rootNode, void (*preOrder)(int, treeNode *), void (*postOrder)(int, treeNode *)) {

	if(preOrder != NULL) {
		preOrder(depth, rootNode);
	}
	

	int i;
	for(i = 0; i < rootNode -> childCount; i++) {
		if(rootNode -> child[i] == NULL) { continue; }
		traverseTree(depth+1, rootNode -> child[i], preOrder, postOrder);
	}

	if(rootNode -> sibling != NULL)
		traverseTree(depth, rootNode -> sibling, preOrder, postOrder);

	if(postOrder != NULL) {
        postOrder(depth, rootNode);
    }



}

void destroyTreeNode(int depth, treeNode *node) {
	free(node->value);
	free(node);
}


void printTreeNode(int level, treeNode *t) {
	printf("%*s%s\n", level*4, "", t->value);
}
