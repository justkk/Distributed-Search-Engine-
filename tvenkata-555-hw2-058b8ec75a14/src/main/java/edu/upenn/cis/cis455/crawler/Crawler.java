package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.queue.CrawlDataStructure;
import edu.upenn.cis.cis455.crawler.service.CrawlerService;
import edu.upenn.cis.cis455.ms2.CrawlerTopology;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;

import java.util.HashSet;
import java.util.Set;


public class Crawler implements CrawlMaster {

    static final int NUM_WORKERS = 10;
    private CrawlDataStructure crawlDataStructure;
    private CrawlerService crawlerService;
    private CrawlManagerStatus status;
    private Set<CrawlerThread> crawlerThreadSet = new HashSet<>();
    private CrawlerCleaner crawlerCleaner;
    private CrawlerTopology crawlerTopology;
    private StorageInterface db;


    private int threadCount;
    private int maxDocumentSize;

    public static Crawler crawler_for_this_run;

    public Crawler() {
    }


    public static Crawler getCrawler() {
        return crawler_for_this_run;
    }

    public Crawler(String startUrl, StorageInterface db, int size, int count) {
        // TODO: initialize
        this.crawlDataStructure = new CrawlDataStructure(startUrl, count, 0);
        this.status = CrawlManagerStatus.NOT_STARTED;
        this.threadCount = NUM_WORKERS;
        this.maxDocumentSize = size;
        this.crawlerService = new CrawlerService((StorageInterfaceImpl) db, this);
        this.db = db;
    }

    ///// TODO: you'll need to flesh all of this out.  You'll need to build a thread
    // pool of CrawlerWorkers etc. and to implement the functions below which are
    // stubs to compile

    /**
     * Main thread
     */
    public void start() {
        synchronized (this) {
            if (this.status != CrawlManagerStatus.NOT_STARTED) {
                return;
            }
            initializeThreads();
            initializeCleaner();
            this.status = CrawlManagerStatus.RUNNING;
        }
    }

    /**
     * Returns true if it's permissible to access the site right now
     * eg due to robots, etc.
     */
    public boolean isOKtoCrawl(String site, int port, boolean isSecure) {
        return crawlerService.isOKtoCrawl(site, port, isSecure);
    }

    /**
     * Returns true if the crawl delay says we should wait
     */
    public boolean deferCrawl(String site) {
        // tested
        return crawlerService.deferCrawl(site);
    }

    /**
     * Returns true if it's permissible to fetch the content,
     * eg that it satisfies the path restrictions from robots.txt
     */
    public boolean isOKtoParse(URLInfo url) {
        return crawlerService.isOKtoParse(url);
    }

    /**
     * Returns true if the document content looks worthy of indexing,
     * eg that it doesn't have a known signature
     */
    public boolean isIndexable(String content) {
        //tested
        return crawlerService.isIndexable(content);
    }

    /**
     * We've indexed another document
     */
    public void incCount() {
        // tested
        synchronized (this) {
            crawlDataStructure.increamentCounter(1);
        }
    }

    /**
     * Workers can poll this to see if they should exit, ie the
     * crawl is done
     */
    public boolean isDone() {
        //tested
        return this.status == CrawlManagerStatus.TERMINATE;
    }

    /**
     * Workers should notify when they are processing an URL
     */
    public void setWorking(boolean working) {
    }

    /**
     * Workers should call this when they exit, so the master
     * knows when it can shut down
     */
    public void notifyThreadExited() {
    }


    public void initializeThreads() {
        for (int i = 0; i < threadCount; i++) {
            CrawlerThread crawlerThread = new CrawlerThread(this);
            crawlerThreadSet.add(crawlerThread);
            crawlerThread.start();
        }
    }

    public void initializeCleaner() {
        crawlerCleaner = new CrawlerCleaner(this, 400);
        crawlerCleaner.start();
    }

    public boolean shouldThreadTerminate() {
        if (this.status == CrawlManagerStatus.WAITING_FOR_TERMINATE || this.status == CrawlManagerStatus.TERMINATE) {
            return true;
        }
        if (areAllThreadsNotRunning() && crawlDataStructure.getQueueSize() == 0) {
            return true;
        }
        return false;
    }

    public boolean shouldThreadTerminateV2() {
        if (this.status == CrawlManagerStatus.WAITING_FOR_TERMINATE || this.status == CrawlManagerStatus.TERMINATE) {
            return true;
        }
        if (crawlDataStructure.getTaskSize() == 0 && crawlDataStructure.getQueueSize() == 0) {
            return true;
        }
        return false;
    }

    public void closeTopology() {

        while (true) {
            synchronized (this) {
                if (crawlDataStructure.getTaskSize() == 0 && crawlDataStructure.getQueueSize() == 0) {
                    break;
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //
            }

        }
    }

    // wait for cleaner is for external call.
    public void shutDownV2(boolean force, boolean waitForCleaner) {

        synchronized (this) {
            crawlDataStructure.getReadyQueueInstances().clear();
            crawlDataStructure.getWaitingQueueInstances().clear();
        }
        if (!force) {
            closeTopology();
        }
        crawlerTopology.shutdown();
        this.status = CrawlManagerStatus.TERMINATE;
        if (waitForCleaner) {
            while (true) {
                try {
                    crawlerCleaner.join();
                    break;
                } catch (InterruptedException e) {
                    //
                }
            }
        }
    }

    public CrawlManagerStatus getStatus() {
        return status;
    }

    public CrawlDataStructure getCrawlDataStructure() {
        return crawlDataStructure;
    }


    public boolean areAllThreadsNotRunning() {

        for (CrawlerThread crawlerThread : crawlerThreadSet) {
            if (crawlerThread.getCrawlerThreadStatus() == CrawlerThread.CrawlerThreadStatus.RUNNING) {
                return false;
            }
        }

//        if (crawlerCleaner != null && crawlerCleaner.getCrawlerThreadStatus() == CrawlerThread.CrawlerThreadStatus.RUNNING) {
//            return false;
//        }

        return true;
    }

    public void processWorkerThreadExit(CrawlerThread crawlerThread) {
        synchronized (this) {
            crawlerThreadSet.remove(crawlerThread);
        }
        processThreadExit();

    }

    public void processThreadExit() {
        synchronized (this) {
            if (areAllThreadsNotRunning() && crawlDataStructure.getQueueSize() == 0 &&
                    this.status != CrawlManagerStatus.WAITING_FOR_TERMINATE && this.status != CrawlManagerStatus.TERMINATE) {
                this.status = CrawlManagerStatus.WAITING_FOR_TERMINATE;
            }
            if (crawlerThreadSet.size() == 0 && this.status == CrawlManagerStatus.WAITING_FOR_TERMINATE) {
                while (true) {
                    try {
                        this.crawlerCleaner.interrupt();
                        this.crawlerCleaner.join();
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                this.status = CrawlManagerStatus.TERMINATE;
            }
        }
    }

    /**
     * Call will crawler lock
     */
    public void balanceThreads() {

        if (status != CrawlManagerStatus.RUNNING) {
            return;
        }

        if (crawlerThreadSet.size() < NUM_WORKERS) {
            int delta = NUM_WORKERS - crawlerThreadSet.size();
            for (int i = 0; i < delta; i++) {
                CrawlerThread crawlerThread = new CrawlerThread(this);
                crawlerThreadSet.add(crawlerThread);
                crawlerThread.start();
            }
        }
    }

    public void startV2() {
        initializeCleaner();
        crawlerTopology = new CrawlerTopology(this);
        crawlerTopology.buildTopologyAndStart();
    }

    /**
     * Main program:  init database, start crawler, wait
     * for it to notify that it is done, then close.
     */
    public static void main(String args[]) {
        if (args.length < 3 || args.length > 5) {
            System.out.println("Usage: Crawler {start URL} {database environment path} {max doc size in MB} {number of files to index}");
            System.exit(1);
        }

        System.out.println("Crawler starting");
        String startUrl = args[0];
        String envPath = args[1];
        Integer size = Integer.valueOf(args[2]);
        Integer count = args.length == 4 ? Integer.valueOf(args[3]) : 100;
        StorageInterface db = StorageFactory.getDatabaseInstance(envPath);
        Crawler crawler = new Crawler(startUrl, db, size * 1048576, count);
        crawler.setDb(db);
        Crawler.crawler_for_this_run = crawler;
        System.out.println("Starting crawl of " + count + " documents, starting at " + startUrl);
        crawler.startV2();

        while (!crawler.isDone())
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        // TODO: final shutdown
        db.close();
        System.out.println("Done crawling!");
    }
//    public static void main(String[] args) {
//        StorageInterface storageInterface = new StorageInterfaceImpl("/Users/nikhilt/Desktop/playground");
//        Crawler crawler = new Crawler("https://dbappserv.cis.upenn.edu/crawltest.html",
//                storageInterface, 12000, 10);
////        System.out.println(crawler.isOKtoCrawl("https://dbappserv.cis.upenn.edu/crawltest.html", 443, true));
////        System.out.println(crawler.isOKtoCrawl("https://dbappserv.cis.upenn.edu/crawltest.html", 443, true));
//
////        System.out.println(crawler.deferCrawl("https://dbappserv.cis.upenn.edu/crawltest.html"));
////        System.out.println(crawler.deferCrawl("https://dbappserv.cis.upenn.edu/crawltest.html"));
////        System.out.println(crawler.deferCrawl("https://dbappserv.cis.upenn.edu/crawltest.html"));
//
////        System.out.println(crawler.getCrawlDataStructure().getDocumentIndexed());
////        crawler.getCrawlDataStructure().increamentCounter(10);
////        System.out.println(crawler.getCrawlDataStructure().getDocumentIndexed());
//
//
//
////        System.out.println(crawler.isOKtoParse(new URLInfo("https://dbappserv.cis.upenn.edu/crawltest.html")));
////        crawler.getCrawlerService().addContent("hello world");
////        System.out.println(crawler.getCrawlerService().isIndexable("hello world"));
//
//        crawler.start();
//
//        while (!crawler.isDone()) {
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//        //System.out.println(crawler.getCrawlDataStructure().getQueueSize());
//        //System.out.println("Done crawling!");
//
//    }

    public enum CrawlManagerStatus {
        NOT_STARTED,
        RUNNING,
        WAITING_FOR_TERMINATE,
        TERMINATE;
    }

    public CrawlerService getCrawlerService() {
        return crawlerService;
    }

    public int getMaxDocumentSize() {
        return maxDocumentSize;
    }

    public StorageInterface getDb() {
        return db;
    }

    public void setDb(StorageInterface db) {
        this.db = db;
    }

    public void setCrawlDataStructure(CrawlDataStructure crawlDataStructure) {
        this.crawlDataStructure = crawlDataStructure;
    }

    public void setCrawlerService(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    public void setStatus(CrawlManagerStatus status) {
        this.status = status;
    }

    public void setCrawlerThreadSet(Set<CrawlerThread> crawlerThreadSet) {
        this.crawlerThreadSet = crawlerThreadSet;
    }

    public void setCrawlerCleaner(CrawlerCleaner crawlerCleaner) {
        this.crawlerCleaner = crawlerCleaner;
    }

    public void setCrawlerTopology(CrawlerTopology crawlerTopology) {
        this.crawlerTopology = crawlerTopology;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public void setMaxDocumentSize(int maxDocumentSize) {
        this.maxDocumentSize = maxDocumentSize;
    }

    public static void setCrawler_for_this_run(Crawler crawler_for_this_run) {
        Crawler.crawler_for_this_run = crawler_for_this_run;
    }
}
