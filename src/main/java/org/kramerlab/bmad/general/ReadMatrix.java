package org.kramerlab.bmad.general;

import org.kramerlab.bmad.matrix.BooleanMatrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVFormat;

import java.nio.charset.StandardCharsets;

public class ReadMatrix{


    /**
     * Reads matrix from sparse file. Each line in the file should be 
     * &lt;rowindex\gt; \lt;columnindex\gt;
     * @return Resulting Boolean matrix.
     */
    public static BooleanMatrix readSparse(String location) throws FileNotFoundException, IOException{
	BufferedReader bur = new BufferedReader(new FileReader(new File(location)));
	String line = bur.readLine();
	HashMap<Integer, Integer> mapping = new HashMap<Integer, Integer>();

	int sizeX = 0;
	int sizeY = 0;
	
	while (line != null) {
	    String[] linespl = line.split(" ");

	    int x = Integer.parseInt(linespl[0]);
	    int y = Integer.parseInt(linespl[1]);
	    
	    mapping.put(x,y);


	    if (x > sizeX) {
		sizeX = x;
	    }

	    if (y > sizeY) {
		sizeY = y;
	    }

	    
	    line = bur.readLine();
	}

	byte[][] mat = new byte[sizeX+1][sizeY+1];

	for (Integer key : mapping.keySet()) {
	    mat[key][mapping.get(key)] = BooleanMatrix.TRUE;
	}
	return new BooleanMatrix(mat);
	
    }

    /**
     * Reads matrix from sparse file. Each line in the file should be 
     * \lt;rowindex\gt; \lt;columnindex\gt; t|f
     * @return Resulting Boolean matrix.
     */
    public static BooleanMatrix readSparseWithMissing(String location) throws FileNotFoundException, IOException{
	BufferedReader bur = new BufferedReader(new FileReader(new File(location)));
	String line = bur.readLine();
	HashMap<Integer, Integer> mappingPos = new HashMap<Integer, Integer>();
	HashMap<Integer, Integer> mappingNeg = new HashMap<Integer, Integer>();


	HashMap<String, Integer> xMap = new HashMap<String, Integer>();
	HashMap<String, Integer> yMap = new HashMap<String, Integer>();

	
	int sizeX = 0;
	int sizeY = 0;
	
	while (line != null) {
	    String[] linespl = line.split(" ");
	    
	    String x = linespl[0];
	    String y = linespl[1];


	    
	    if(xMap.get(linespl[0]) == null){
		xMap.put(linespl[0], sizeX);
		sizeX++;
	    }

	    if(yMap.get(linespl[1]) == null){
		yMap.put(linespl[1], sizeY);
		sizeY++;
		
	    }
	    
	    if (linespl[2].equals("t")) {
		mappingPos.put(xMap.get(linespl[0]),
			       xMap.get(linespl[1]));

	    }
	    if (linespl[2].equals("n")) {
		mappingNeg.put(xMap.get(linespl[0]),
			       xMap.get(linespl[1]));

	    }

	    

	    line = bur.readLine();
	}

	byte[][] mat = new byte[sizeX+1][sizeY+1];
	for (int i = 0; i < mat.length; i ++) {
	    for (int j = 0; j < mat[i].length; j ++) {
		mat[i][j] = BooleanMatrix.UNKNOWN;
	    }
	}


	
	for (Integer key : mappingPos.keySet()) {
	    mat[key][mappingPos.get(key)] = BooleanMatrix.TRUE;
	}
	for (Integer key : mappingNeg.keySet()) {
	    mat[key][mappingNeg.get(key)] = BooleanMatrix.FALSE;
	}
	return new BooleanMatrix(mat);
	
    }



    /**
     * Reads matrix from csv file. 
     * 
     * @return Resulting Boolean matrix.
     */
    public static BooleanMatrix readDense(String location) throws FileNotFoundException, IOException{
	File csvData = new File(location);
	CSVParser parser = CSVParser.parse(csvData, StandardCharsets.UTF_8, CSVFormat.RFC4180);
	int width = 0;
	ArrayList<byte[]> dataList = new ArrayList<byte[]>();
	for (CSVRecord csvRecord : parser) {
	    byte[] row = new byte[csvRecord.size()];
	    int count = 0;
	    for (String entry : csvRecord) {
		if (entry.equals("?")) {
		    row[count] = BooleanMatrix.UNKNOWN;

		}

		if (Boolean.parseBoolean(entry) ||
		    entry.equals("1")||
		    entry.equals("t")) {
		    row[count] = BooleanMatrix.TRUE;

		}

		if (!Boolean.parseBoolean(entry) ||
		    entry.equals("0")||
		    entry.equals("f")) {
		    row[count] = BooleanMatrix.FALSE;
		    
		}

		
		count++;
	    }
	    dataList.add(row);
	}

	byte[][] data = new byte[dataList.size()][dataList.get(0).length];
	int count = 0;
	for (byte[] row : dataList) {
	    data[count] = row;
	    count++;
	}
	return new BooleanMatrix(data);

    }


    /**
     * Reads matrix from sparse file. Each line in the file should be 
     * &lt;rowid\gt; \lt;columnid\gt;
     * @return Resulting Boolean matrix.
     */
    public static BooleanMatrix readSparseWithIDs(String location) throws FileNotFoundException, IOException{
	BufferedReader bur = new BufferedReader(new FileReader(new File(location)));
	String line = bur.readLine();
	HashMap<Integer, Integer> mapping = new HashMap<Integer, Integer>();

	int sizeX = 0;
	int sizeY = 0;

	HashMap<String, Integer> xMap = new HashMap<String, Integer>();
	HashMap<String, Integer> yMap = new HashMap<String, Integer>();
	
	while (line != null) {
	    String[] linespl = line.split(" ");

	    int x = -1;
	    int y = -1;
	    
	    if(xMap.get(linespl[0]) == null){
		xMap.put(linespl[0], sizeX);
		sizeX++;
	    }

	    if(yMap.get(linespl[1]) == null){
		yMap.put(linespl[1], sizeY);
		sizeY++;
		
	    }
	    
	    
	    mapping.put(xMap.get(linespl[0]),yMap.get(linespl[1]));
	    
	    line = bur.readLine();
	}

	byte[][] mat = new byte[sizeX+1][sizeY+1];

	for (Integer key : mapping.keySet()) {
	    mat[key][mapping.get(key)] = BooleanMatrix.TRUE;
	}
	return new BooleanMatrix(mat);
	
    }

    
}
