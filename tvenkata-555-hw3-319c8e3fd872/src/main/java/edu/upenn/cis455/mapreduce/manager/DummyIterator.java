package edu.upenn.cis455.mapreduce.manager;

import java.util.Iterator;

public class DummyIterator implements Iterator<String> {

    private Iterator<TupleStore> iterator;

    public DummyIterator(Iterator<TupleStore> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public String next() {
        TupleStore tupleStore = iterator.next();
        return tupleStore.getValue();
    }
}
