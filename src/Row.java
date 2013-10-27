import java.util.ArrayList;

public class Row implements Comparable<Row> {

	private String rowId;
	private ArrayList<String> data;
	private ArrayList<Integer> increasing;
	
	public Row() {
		data = new ArrayList<String>();
		increasing = new ArrayList<Integer>();
	}
	
	public void addData(String id, String dataCell, int desc) {
		rowId = id;
		data.add(dataCell);
		increasing.add(desc);
	}
	
	@Override
	public int compareTo(Row other) {
		// TODO Auto-generated method stub
		int n = data.size();
		for (int i = 0; i < n; i++) {
			int d = this.data.get(i).compareToIgnoreCase(other.data.get(i));
			if (d != 0) {
				return d * increasing.get(i);
			}
		}
		return this.rowId.compareToIgnoreCase(other.rowId);
	}

	public String getData() {
		String r = convertData(data.get(0), 0);
		for (int i = 1; i < data.size(); i++)
			r += " " + convertData(data.get(i), i);
		return r;
	}
	
	public String getRowId() {
		return rowId;
	}
	
	public String convertData(String s, int indexColumn) {
		String r = "";
		if (increasing.get(indexColumn) == -1) { 
			//TO DO: invert String
			for (int i = 0; i < s.length(); i++) {
				char ch = s.charAt(i);
				char t;
				if ('0' <= ch && ch <= '9') { 
					t = (char) ('9' - ch + '0');
				} else if ('a' <= ch && ch <= 'z') {
					t = (char) ('z' - ch + 'a');
				} else if ('A' <= ch && ch <= 'Z') {
					t = (char) ('Z' - ch + 'A');
				} else {
					t = ch;
				}
				r += t;
			}
		} else {
			r += s;
		}
		r = r.replaceAll("" + (char) 255, " "); //Convert NULL value
		return r;
	}
}
