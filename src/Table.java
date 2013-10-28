
import java.util.ArrayList;

/**
 * CS 267 - Project - Implements a Table for the DBMS.
 */
public class Table {
	private String tableName;
	private int numColumns;
	private int numIndexes;
	private int tableCard;
	private ArrayList<Column> columns;
	private ArrayList<Index> indexes;
	private ArrayList<String> data;
	private ArrayList<ArrayList<String>> cells;
	private int dataLength;
	
	public boolean delete = false;

	public Table(String tableName) {
		this.tableName = tableName;
		numColumns = 0;
		numIndexes = 0;
		tableCard = 0;
		columns = new ArrayList<Column>();
		indexes = new ArrayList<Index>();
		data = new ArrayList<String>();
		cells = new ArrayList<ArrayList<String>>();
		dataLength = 0;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public int getNumColumns() {
		return numColumns;
	}

	public void setNumColumns(int numColumns) {
		this.numColumns = numColumns;
	}

	public int getNumIndexes() {
		return numIndexes;
	}

	public void setNumIndexes(int numIndexes) {
		this.numIndexes = numIndexes;
	}

	public int getTableCard() {
		return tableCard;
	}

	public void setTableCard(int tableCard) {
		this.tableCard = tableCard;
	}

	public ArrayList<Column> getColumns() {
		return columns;
	}

	public void addColumn(Column column) {
		columns.add(column);
		numColumns++;
		dataLength += column.getColLength();
	}

	public ArrayList<Index> getIndexes() {
		return indexes;
	}

	public void addIndex(Index index) {
		indexes.add(index);
		numIndexes++;
	}

	public ArrayList<String> getData() {
		return data;
	}

	public void addData(String values) {
		data.add(values);

		/*
		 *  Parse string data to ArrayList<String> 
		 */
		int id = values.trim().indexOf(" ");
		id = values.indexOf(values.trim().substring(id - 1, id + 1)) + 1;
		
		while (values.length() <= dataLength + id + numColumns)
			values += " ";
		
		ArrayList<String> c = new ArrayList<String>();
		c.add(values.substring(0, id));
		
		for (int i = 1; i <= numColumns; i++) {
			int k = columns.get(i - 1).getColLength();
			String s = values.substring(id + 1, id + 1 + k);
			id = id + k + 1;

			if (isNull(s)) {
				s = s.replaceAll(" ", "" + (char) 255);
			} else if (columns.get(i - 1).getColType() == Column.ColType.INT) {
				s  = s.replaceAll(" ", "0");
			} 
			
			c.add(s);
		}
		
		cells.add(c);
	}
	
	public ArrayList<String> getRow(int index) {
		return cells.get(index);
	}
	
	public String getCell(int indexRow, int indexCol) {
		return cells.get(indexRow).get(indexCol);
	}
	
	public boolean isNull(String s) {
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) != ' ')
				return false;
		return true;
	}
}
