
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

		int id = values.trim().indexOf(" ");
		id = values.indexOf(values.trim().substring(id - 1, id + 1)) + 1;
		
		values = values.trim();
		while (values.contains("  "))
			values = values.replaceAll("  ", " ");
		
		String[] s = values.split(" ");
		
		ArrayList<String> c = new ArrayList<String>();
		while (s[0].length() < id)
			s[0] = " " + s[0];
		c.add(s[0]);
		for (int i = 1; i <= numColumns; i++) {
			int k = columns.get(i - 1).getColLength();
			
			if (columns.get(i - 1).getColType() == Column.ColType.INT) {
				while (s[i].length() < k)
					s[i] = "0" + s[i];
			} else {
				while (s[i].length() < k)
					s[i] = s[i] + " ";
			}
			c.add(s[i]);
		}
		
		cells.add(c);
	}
	
	public ArrayList<String> getCellRow(int index) {
		return cells.get(index);
	}
}
