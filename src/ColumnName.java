
public class ColumnName {
	String tableName;
	String colName;
	
	public ColumnName(String tableName, String colName) {
		this.tableName = tableName.toUpperCase();
		this.colName = colName.toUpperCase();
	}
	
	public String getTabelName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public String getColName() {
		return colName;
	}
	
	public void setColName(String colName) {
		this.colName = colName;
	}
}
