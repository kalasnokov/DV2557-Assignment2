
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
    static int inf = 10000000;
    public long start = System.currentTimeMillis();
    public Node DLS(int depth, int player, GameState state, int A, int B){
        Node leaflist[] = new Node[6];
        if(depth == 0){//we are at the bottom of the search
            
            System.out.println("Max depth reached.");
            
            return this;
            
        }else if(depth > 0){//bottom not reached, expand search
            int value = 0;
            boolean max = (state.getNextPlayer() == player);
            if(!max){
                //maximize my player
                value = -inf;
            }else{
                //minimize other player
                value = inf;
            }
            for(int i = 0; i < 6; i++){//check all nodes
                System.out.println("DLS loop i = " + i);
                GameState predict = state.clone();
                if(predict.moveIsPossible(i + 1)){//make sure move is legal
                    predict.makeMove(i + 1);
                    System.out.println("1");
                    if(children[i] == null){//Check to see if node has been expanded
                        addChild(new Node(this, 0, (predict.getNextPlayer() == player), depth, true), i);//expand node
                        children[i].calcScore(predict, player);
                        if(max){
                            value = Math.max(value, children[i].score);
                            A = Math.max(A, value);
                            System.out.println("A = " + A);
                        }else{
                            value = Math.min(value, children[i].score);
                            B = Math.min(B, value);
                            System.out.println("B = " + B);
                        }
                    }
                    if(A >= B){
                        System.out.println("BREAKING");
                        break;
                    }
                    System.out.println("2");
                    leaflist[i] = children[i].DLS(depth-1, player, predict, A, B);//go deeper
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
        start = System.currentTimeMillis();
        long dt = 0;
        do {
            //System.out.println("D = " + i);
            //Måste fixa något sätt att skala med djupet
            ret = DLS(i, player, state, -inf, inf);//move to next phase and do depth search
            i++;
            dt = (System.currentTimeMillis() - start);
        } while(dt <= (4500 / i^2));
       /*
        for(int i = 0; i < 6; i++){//loop through different depths, the 6 is temporary and will be replaces with the time limited version
            System.out.println("D = " + i);
            ret = DLS(i, player, state);//move to next phase and do depth search
            System.out.println("DLS returned.");
            
        }*/
        return ret;//goal found
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