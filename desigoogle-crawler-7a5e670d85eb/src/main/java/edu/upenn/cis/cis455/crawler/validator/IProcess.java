package edu.upenn.cis.cis455.crawler.validator;

public interface IProcess {

    boolean validateRequest(ProcessContext processContext);
    void processRequest(ProcessContext processContext);
}
