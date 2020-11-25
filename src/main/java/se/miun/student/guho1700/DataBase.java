package se.miun.student.guho1700;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;

public class DataBase {

    private static final String baseDirectory = "." + File.separator + "database";
    private static final String fileSuffix = ".sensor";
    private final File folder;

    public DataBase() {
        folder = new File(baseDirectory);

        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    public boolean fileExists(File file) {
        return file.exists() && file.isFile();
    }


    // used for POST requests to create a record for a new sensor
    public void createNewSensor(String sensorName) throws IOException {
        File f = new File(folder.getPath(), sensorName.trim() + fileSuffix);

        if (!f.createNewFile()) {
            throw new FileAlreadyExistsException("File " + f.getPath() + " already exists");
        }
    }

    //used for PUT requests to save a sensor value
    public void storeValue(String sensorName, Double value) throws IOException {
        File f = new File(folder.getPath(), sensorName + fileSuffix);

        if (!fileExists(f)) {
            throw new FileNotFoundException("File " + f.getPath() + " does not exist");
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(f));
        writer.write(String.valueOf(value));
        writer.close();
    }

    //used for GET requests to retrieve a sensor value
    public Double fetchValue(String sensorName) throws IOException {
        File f = new File(folder.getPath(), sensorName + fileSuffix);

        if (!fileExists(f)) {
            throw new FileNotFoundException("File " + f.getPath() + " does not exist");
        }

        BufferedReader reader = new BufferedReader(new FileReader(f));
        String firstLine = reader.readLine();

        reader.close();

        return firstLine == null ? 0.0 : Double.parseDouble(firstLine);
    }

    public void deleteRecord(String sensorName) throws FileNotFoundException {
        File f = new File(folder.getPath(), sensorName + fileSuffix);

        if (!fileExists(f)) {
            throw new FileNotFoundException("File " + f.getPath() + " does not exist");
        }

        f.delete();
    }

}
