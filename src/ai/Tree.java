/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;
import java.util.*;
import java.util.List; 
import java.util.Arrays;


public class Tree<T>{

    T data;
    boolean myTurn;
    Tree<T> parent;
    List<Tree<T>> children;

    public Tree(T data, boolean myTurn) {
        this.myTurn = myTurn;
        this.data = data;
        this.children = new LinkedList<Tree<T>>();
    }

    public Tree<T> addChild(T child, boolean myTurn) {
        Tree<T> childNode = new Tree<T>(child, myTurn);
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    }
}
