/*
	CMPT 399 tree.h
	used for Assignment 2
	by Scott Cameron
*/

#ifndef TREE_H
#define TREE_H

/* A node in the tree */
typedef struct _treeNode{
	char *value; // the string value of the node
	struct _treeNode **child; // the array of children this node has
	struct _treeNode *sibling; // the next sibling node
	int childCount; // the number of children this node has
} treeNode;



/* create a new node for the tree. This function does not connect any nodes
   together on its own.
   param1: the string value of the node
   param2: the number of children this node will have
   return: a pointer to ths newly created node
*/
treeNode *newNode(char *, int);



/* fully traverse the tree including each node's children and siblings
   param1: the pointer to the root node (when called outside of treverseTree())
   param2: the current depth of the tree (always is 0 when called from outside traverseTree())
   param3: the preOrder function that you want to execute before traversing deeper into the tree
   param4: the postOrder function that you want to execute after traversing deeper into the tree
		pre and post Order functions must have:
		param1: treenode *  for the currently traversed node
		param2: int  for the current depth of the traversal
   credit: the idea to use a single traverseTree function with passed in pre and post Order
   function came from lab 5 by Nicholas M. Boers
*/
void traverseTree(treeNode *, int, void (*)(treeNode *, int),  void (*)(treeNode *, int));



/* print a treeNode value that's indented by its current depth
   to be used with the traverseTree() function as a pre or post Order parameter (usually preOrder)
   param1: a pointer to a treeNode you want to print
   param2: the current depth of the traveral
*/
void printTreeNode(treeNode *, int);



/* free the data associated with a given node
   to be used with the traverseTree() function as a post Order parameter
   param1: a pointer to a treeNode you want to print
   param2: the current depth of the traveral
   NOTE: currently does not free node->value as it can lead to strange segfaults
*/
void destroyTreeNode(treeNode *, int);


#endif /* TREE_H */
