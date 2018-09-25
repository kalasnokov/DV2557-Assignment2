
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
    boolean victoryNode = false;
    
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
    static int inf = 10000;
    int alphabeta(Node node, int depth, int alpha, int beta, boolean maximizingPlayer)
    {
        if(depth == 0 && valid && parent.valid)
        {
            return node.score;
        }
        if(maximizingPlayer)
        {
            int value = -inf;
            for(Node c : children)
            {
                value = Math.max(value, alphabeta(c, depth - 1, alpha, beta, false));
                alpha = Math.max(alpha, value);
                if (alpha >= beta)
                {
                    break;
                }
            }
            return value;
        }
        else
        {
            int value = inf;
            for(Node c : children)
            {
                value = Math.min(value, alphabeta(c, depth - 1, alpha, beta, true));
                beta = Math.min(beta, value);
                if(alpha >= beta)
                    break;      
            }
            return value;
        }
    }
    public class IDDFRETURN{
        Node found = null;
        boolean remaining = false;
    }
    
    public IDDFRETURN DLS(int depth){
        IDDFRETURN ret = new IDDFRETURN();
        if(depth == 0){
            if(victoryNode){
                ret.found = this;
                return ret;
            }else{
                ret.remaining = true;
                return ret;
            }
        }else if(depth > 0){
            boolean rem = false;
            for(int i = 0; i < 6; i++){
                IDDFRETURN NRET = children[i].DLS(depth-1);
                if(NRET.found != null){
                    ret.found = NRET.found;
                    return ret;
                }
                if(NRET.remaining){
                    rem = true;
                }
            }
            ret.remaining = rem;
            return ret;
        }
        return null;//should never be reached...
    }
    
    public Node IDDF(){
        for(int i = 0; i < 1000000; i++){
            IDDFRETURN ret = DLS(i);
            if(ret.found != null){
                return ret.found;
            }else if (!ret.remaining){
                return null;
            }
        }
        return null;
    }
    public String genTreeFullDepth(Node root, int depth, int myPlayer, GameState state){
        String ret = "";
        GameState predict;
        for(int i = 0; i < 6; i ++){
            predict = state.clone();
            int pScore = 0;
            boolean pValid = predict.moveIsPossible(i + 1);
            if(!pValid){
                pScore = -100000000;
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