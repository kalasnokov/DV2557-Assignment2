package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import kalaha.*;

/**
 *
 * @author Kalasnokov
 */

public class Node {
    Node parent = null;
    int depth;
    int score;//self explanatory
    int myMove = 0;//move from parent node to get here
    boolean myTurn;
    
    Node[] children = new Node[6];//position represents move
    
    public Node(Node parent, int score, boolean myTurn, int depth){
        this.parent = parent;
    }
    
    public void addChild(Node child, int pos){
        children[pos] = child;
    }
    
    public void genTreeFullDepth(Node root, int depth, int myPlayer){
        for(int i = 0; i < 6; i ++){
            GameState predict = new GameState();
            predict.makeMove(i);
            int score = predict.getScore(myPlayer);
            int nextPlayer = predict.getNextPlayer();
            boolean myTurn = false;
            if(nextPlayer == myPlayer){
                myTurn = true;
            }
            root.addChild(new Node(root, score, myTurn, depth), i);
        }
        depth--;
        if(depth > 0){
            for(int i = 0; i < 6; i ++){
                genTreeFullDepth(children[i], depth, myPlayer);
            }
        }
        return;
    }
    
    public String printTree(Node root){
        String ret = "";
        ret += "Node with depth " + root.depth + " and score " + root.score + "\n";
        for(int i = 0; i < 6; i++){
            if(root.children[i] != null){
                ret += "Child found\n";
                ret += root.printTree(root.children[i]);
            }
        }
        return ret;
    }
}