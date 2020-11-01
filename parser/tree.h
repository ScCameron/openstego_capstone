//tree header


#ifndef TREE_H
#define TREE_H




typedef struct _treeNode{
	char *value;
	struct _treeNode **child;
	struct _treeNode *sibling;
	//struct _treeNode **sibling;
} treeNode;


treeNode *newNode(char *, int);


void traverseTree(int, treeNode *, void (*)(int, treeNode *),  void (*)(int, treeNode *));

//void destroyTree(int, treeNode *);


#endif /* TREE_H */
