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
			int d = this.data.get(i).compareTo(other.data.get(i));
			if (d != 0)
				return d * increasing.get(i);
		}
		return 0;
	}

	public String getData() {
		String r = data.get(0);
		for (int i = 1; i < data.size(); i++)
			r += " " + data.get(i);
		return r;
	}
	
	public String getRowId() {
		return rowId;
	}
}
