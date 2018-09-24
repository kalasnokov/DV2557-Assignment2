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
    int myMove;//move from parent node to get here
    boolean valid;
    boolean myTurn;
    
    Node[] children = new Node[6];//position represents move
    
    public Node(Node parent, int score, boolean myTurn, int depth, boolean valid){
        this.parent = parent;
        this.score = score;
        this.myTurn = myTurn;
        this.depth = depth;
        this.valid = valid;
    }
    
    public void addChild(Node child, int pos){
        children[pos] = child;
    }
    
    public boolean isEmpty(){
        return (children[0] == null);
    }
    
    public String genTreeFullDepth(Node root, int depth, int myPlayer, GameState state){
        String ret = "";
        GameState predict;
        for(int i = 0; i < 6; i ++){
            predict = state.clone();
            int pScore = 0;
            boolean pValid = predict.moveIsPossible(i + 1);
            if(!pValid){
                pScore = -100000;
            }else{
                predict.makeMove(i + 1);
                pScore = predict.getScore(myPlayer);
            }
            int nextPlayer = predict.getNextPlayer();
            //ret += nextPlayer + " != " + myPlayer + "\n";
            boolean pMyTurn = false;
            if(nextPlayer == myPlayer){
                pMyTurn = true;
            }
            ret += "Node " + score + " : " + depth + "\n";
            root.addChild(new Node(root, pScore, pMyTurn, depth, pValid), i);
        }
        depth = depth - 1;
        if(depth > 0){
            predict = state.clone();
            for(int i = 0; i < 6; i ++){
                ret += genTreeFullDepth(children[i], depth, myPlayer, predict);
            }
        }
        return ret;
    }
    static public int minimax()
    {
        return -1;
    }
    public int DS(){
        int ret = 0;
        
        int mult = 1;
        if(!myTurn){
            mult = -1;
        }
        
        if(!isEmpty()){//node has children
            int[] scores = new int[6];
            for(int i = 0; i < 6; i++){
                scores[i] = children[i].DS();
            }
            
            int highest = 0;
            for(int i = 0; i < 6; i++){
                if(scores[i] > scores[highest]){
                    highest = i;
                }
            }
            
            ret = (score * mult) + scores[highest];
        } else{//final node
            ret = score * mult;
        }
        return ret;
    }
    
    public String printNode(){
        return "Node with depth " + depth + ", score " + score + ", myTurn: " + myTurn + ", valid: " + valid + ".\n";
    }
    
    public String printTree(){
        String ret = "";
        ret += printNode();
        for(int i = 0; i < 6; i++){
            if(children[i] != null){
                ret += children[i].printTree();
            }
        }
        return ret;
    }
}