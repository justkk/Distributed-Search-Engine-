package edu.upenn.cis.cis455.util;


import java.util.LinkedList;
import java.util.List;

/**
 * Fixed Array to store the logs of server. Especially handling error callbacks if there is a failure.
 * @param <T>
 */
public class FixedSizeArray<T>  {

    private int size = 10000;
    private List<T> fixedSizeList;


    public FixedSizeArray(int size) {
        this.size = size;
        fixedSizeList = new LinkedList<>();
    }


    public synchronized void addElement(T element) {
        if(fixedSizeList.size() < size) {
            fixedSizeList.add(element);
        } else {
            fixedSizeList.remove(0);
            fixedSizeList.add(element);
        }
    }


    public List<T> getFixedSizeList() {
        return fixedSizeList;
    }
}
