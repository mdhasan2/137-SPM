package onethreeseven.spm.model;

import java.util.HashMap;

/**
 * A linked sequence structure that facilities fast sub-sequence checking.
 * @author Luke Bermingham
 */
public class LookupSequence {


    private final HashMap<Integer, Node> headerTable;
    private final int[] sequence;
    private int nullId = -1;
    private int size;

    public LookupSequence(int[] sequence) {
        this.sequence = sequence;
        this.size = sequence.length;
        this.headerTable = new HashMap<>();
        //build the header table, node links and jump links
        processNodes(sequence);
    }

    public Integer[] getSymbolUniverse(){
        return headerTable.keySet().toArray(new Integer[headerTable.size()]);
    }

    private void processNodes(int... sequence){
        for (int i = 0; i < sequence.length; i++) {
            int symbol = sequence[i];
            Node node = new Node(i, symbol);
            Node headerNode = headerTable.get(node.symbol);
            if (headerNode == null) {
                this.headerTable.put(node.symbol, node);
            }
            //header node already exists, link this node as a jump node
            else {
                while (headerNode.jump != null) {
                    headerNode = headerNode.jump;
                }
                headerNode.jump = node;
            }
        }

        //check if our null index is okay still
        while(headerTable.containsKey(nullId)){
            nullId--;
        }

    }

    /**
     * Much like a {@link java.util.BitSet} nodes aren't removed from a {@link LookupSequence};
     * instead the index of the node is set to a special "null" index to indicate it has been cleared.
     * @param symbol The symbol to clear (the lookup will be used to find all occurrences of this symbol).
     */
    public void clear(int symbol){
        Node curNode = headerTable.get(symbol);
        while(curNode != null){
            size--;
            //clear the symbol by using the special "null" id
            this.sequence[curNode.index] = nullId;
            curNode = curNode.jump;
        }
        headerTable.remove(symbol);
    }

    /**
     * @return An int sequence of all symbols not set to the special "null" id.
     */
    public int[] getActiveSequence(){
        int[] seq = new int[size];
        int i = 0;
        for (int symbol : this.sequence) {
            if(symbol != nullId){
                seq[i] = symbol;
                i++;
            }
        }
        return seq;
    }

    /**
     * @param queryWithGaps A query nodeSequence (with gaps allowed)
     * @return Whether or not this lookup nodeSequence contains the query nodeSequence in some form.
     *         Note: The query nodeSequence does not have to be contiguous.
     */
    public boolean contains(final int[] queryWithGaps){
        int curIndex = -1;
        for (int symbol : queryWithGaps) {
            Node cur = headerTable.get(symbol);
            if(cur == null){return false;}
            Integer lowestSymbolIndex = null;
            while(cur != null){
                if(cur.index > curIndex){
                    lowestSymbolIndex = cur.index;
                    break;
                }
                cur = cur.jump;
            }
            if(lowestSymbolIndex == null){return false;}
            curIndex = lowestSymbolIndex;
        }
        return curIndex > -1;
    }

    public int size(){
        return size;
    }


    private class Node {
        private final int symbol;
        private final int index;
        private Node jump = null;

        Node(int index, int symbol) {
            this.index = index;
            this.symbol = symbol;
        }
    }

}
