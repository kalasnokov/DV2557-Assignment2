
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
    int otherScore;
    int dScore;
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
        return (children[5] == null);
    }
    
    public void calcScore(GameState state, int myPlayer){//only calculates for own player, needs min version for calculating other player
        int otherPlayer = 3 - myPlayer;
        victoryNode = (state.getWinner() == myPlayer);
        score = state.getScore(myPlayer) * 25;
        otherScore = state.getScore(otherPlayer) * 25;
        for(int i = 0; i < 6; i++){
            score += state.getSeeds(i, myPlayer);
            otherScore += state.getSeeds(i, otherPlayer);
        }
        dScore = score - otherScore;
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
        Node leafList[] = new Node[6];
        int LLlen = 0;
    }
    
    public IDDFRETURN DLS(int depth, int player, GameState state){
        IDDFRETURN ret = new IDDFRETURN();
        if(depth == 0){
            
            System.out.println("Max depth reached.");
            ret.leafList[ret.LLlen] = this;
            ret.LLlen++;
            System.out.println("LL set.");
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
                System.out.println("DLS loop i = " + i);
                GameState predict = state.clone();
                predict.makeMove(i + 1);
                System.out.println("1");
                if(isEmpty()){//leaf node
                    addChild(new Node(this, 0, (predict.getNextPlayer() == player), depth, true), i);
                    children[i].calcScore(predict, player);
                }
                System.out.println("2");
                IDDFRETURN NRET = children[i].DLS(depth-1, player, predict);
                System.out.println("3");
                if(NRET.found != null){
                    System.out.println("Found goal.");
                    ret.found = NRET.found;
                    return ret;
                }
                if(NRET.remaining){
                    rem = true;
                }
                ret.leafList = NRET.leafList;
                ret.LLlen = NRET.LLlen;
            }
            ret.remaining = rem;
            return ret;
        }
        return null;//should never be reached...
    }
    
    public Node IDDF(int player, GameState state){
        IDDFRETURN ret = new IDDFRETURN();
        for(int i = 0; i < 4; i++){
            System.out.println("D = " + i);
            ret = DLS(i, player, state);
            System.out.println("DLS returned.");
            if(ret.found != null){
                return ret.found;
            }else if (!ret.remaining){
                return null;
            }
        }
        Node chosenNode = ret.leafList[0];
        System.out.println("leafList length = " + ret.LLlen);
        for(int n = 1; n < ret.LLlen; n++){
            if(chosenNode.dScore < ret.leafList[n].dScore){
                chosenNode = ret.leafList[n];
            }
        }
        return chosenNode;
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