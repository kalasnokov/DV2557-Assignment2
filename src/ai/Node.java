
package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.time.*;
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

    public class IDDFRETURN{
        Node found = null;//goal node
    }
    
    public Node DLS(int depth, int player, GameState state){
        Node leaflist[] = new Node[6];
        if(depth == 0){//we are at the bottom of the search
            
            System.out.println("Max depth reached.");
            
            return this;
            
        }else if(depth > 0){//bottom not reached, expand search
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
                    leaflist[i] = children[i].DLS(depth-1, player, predict);//go deeper
                    System.out.println("3");

                }else{
                    addChild(null, i);//node is invalid
                }
                
            }
        }
        System.out.println("4");//perform some checks...
        Node ret = leaflist[0];
        boolean noNodes = true;
        for(int n = 0; n < 6; n++){
            if(leaflist[n] != null){
                noNodes = false;
            }
        }
        if(!noNodes){
            System.out.println("5");
            for(int n = 0; ret == null; n++){
                System.out.println(n);
                ret = leaflist[n];
            }
            System.out.println("6");
            for(int i = 1; i < 6; i++){
                if(leaflist[i] != null){
                    if(ret.dScore < leaflist[i].dScore){//find best node
                        ret = leaflist[i];
                    }
                }
            }
            System.out.println("7");
        }else{
            ret = this;//node only contains null nodes, return itself
        }
        return ret;//return optimal node
    }
    
    public Node IDDF(int player, GameState state){
        Node ret = null;
        int i = 0;
        long start = System.currentTimeMillis();
        long dt = 0;
        do {
            //System.out.println("D = " + i);
            //Måste fixa något sätt att skala med djupet
            ret = DLS(i, player, state);//move to next phase and do depth search
            i++;
            dt = (System.currentTimeMillis() - start);
        } while(dt <= (4500 / i));
       /*
        for(int i = 0; i < 6; i++){//loop through different depths, the 6 is temporary and will be replaces with the time limited version
            System.out.println("D = " + i);
            ret = DLS(i, player, state);//move to next phase and do depth search
            System.out.println("DLS returned.");
            
        }*/
        return ret;//goal found
    }
    /*public String genTreeFullDepth(Node root, int depth, int myPlayer, GameState state){
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
    }*/
    
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