package edu.upenn.cis.cis455.storage;

public interface StorageInterface {
    
    /**
     * How many documents so far?
     */

	/**
	 * Add a new document, getting its ID
	 */
	/**
	 * How many keywords so far?
	 */
	public int getLexiconSize();
	
	/**
	 * Gets the ID of a word (adding a new ID if this is a new word)
	 */
	public int addOrGetKeywordId(String keyword);
	
	/**
	 * Adds a user and returns an ID
	 */
	public int addUser(String username, String password);
	
	/**
	 * Tries to log in the user, or else throws a HaltException
	 */
	public boolean getSessionForUser(String username, String password);
	
	/**
	 * Retrieves a document's contents by URL
	 */
	public String getDocument(String url);
	
	/**
	 * Shuts down / flushes / closes the storage system
	 */
	public void close();
}
