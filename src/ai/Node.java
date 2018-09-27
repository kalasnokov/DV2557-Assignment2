
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
        boolean found = false;
        for(int i = 0; i < 6; i ++){
            if(children[i] == null){
                found = true;
            }
        }
        System.out.println("IsEmpty: " + found);
        return found;
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
        Node found = null;//goal node
        boolean remaining = false;//is ther any more nodes in the tree?
        Node leafList[] = new Node[6];//list of nodes that are usable istead of goal
        int LLlen = 0;
    }
    
    public IDDFRETURN DLS(int depth, int player, GameState state){
        IDDFRETURN ret = new IDDFRETURN();
        if(depth == 0){//we are at the bottom of the search
            
            System.out.println("Max depth reached.");
            ret.leafList[ret.LLlen] = this;//add node to leaflist
            ret.LLlen++;
            System.out.println("LL set.");
            
            if(victoryNode){//check if it is a goal
                ret.found = this;
                return ret;
            }else{
                ret.remaining = true;
                return ret;
            }
        }else if(depth > 0){//bottom not reached, expand search
            boolean rem = false;
            for(int i = 0; i < 6; i++){//check all nodes
                System.out.println("DLS loop i = " + i);
                GameState predict = state.clone();
                if(predict.moveIsPossible(i + 1)){//make sure move is legal
                    predict.makeMove(i + 1);
                    System.out.println("1");
                    if(children[i] == null){//Check to see if node has been expanded
                        addChild(new Node(this, 0, (predict.getNextPlayer() == player), depth, true), i);//expand node
                        children[i].calcScore(predict, player);
                    }
                    System.out.println("2");
                    IDDFRETURN NRET = children[i].DLS(depth-1, player, predict);//go deeper
                    System.out.println("3");

                    if(NRET.found != null){
                        System.out.println("Found goal.");
                        ret.found = NRET.found;//goal found
                        return ret;
                    }
                    if(NRET.remaining){
                        rem = true;
                    }
                    ret.leafList = NRET.leafList;//set the leaflist
                    ret.LLlen = NRET.LLlen;
                }else{
                    addChild(null, i);//node is invalid
                }
                
            }
            ret.remaining = rem;
            return ret;//return leaflist or goal
        }
        return null;//should never be reached...
    }
    
    public Node IDDF(int player, GameState state){
        IDDFRETURN ret = new IDDFRETURN();
        for(int i = 0; i < 6; i++){//loop through different depths, the 6 is temporary and will be replaces with the time limited version
            System.out.println("D = " + i);
            ret = DLS(i, player, state);//move to next phase and do depth search
            System.out.println("DLS returned.");
            if(ret.found != null){
                return ret.found;//goal found
            }else if (!ret.remaining){
                return null;//search has fully completed, but no optimal node found (this should be what is actually fucking shit up when only one move can be made, needs a do-over)
            }
        }
        Node chosenNode = ret.leafList[0];//leaflist contains all the deepest nodes in the search
        System.out.println("leafList length = " + ret.LLlen);
        for(int n = 1; n < ret.LLlen; n++){//loop through them...
            if(chosenNode.dScore < ret.leafList[n].dScore){//select the one with the highest score delta
                chosenNode = ret.leafList[n];
            }
        }
        return chosenNode;//return goal node
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