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
    Tree<T> parent;
    List<Tree<T>> children;

    public Tree(T data) {
        this.data = data;
        this.children = new LinkedList<Tree<T>>();
    }

    public Tree<T> addChild(T child) {
        Tree<T> childNode = new Tree<T>(child);
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    }
}
