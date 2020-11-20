package edu.upenn.cis.cis455.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StorageFactory {


    public static StorageInterface getDatabaseInstance(String directory) {
	// TODO: factory object, instantiate your storage server

        Path path = Paths.get(directory).toAbsolutePath();
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.exit(1);
            }
        }

        StorageInterfaceImpl storageInterface = new StorageInterfaceImpl(path.toString());
        return storageInterface;
    }
}
