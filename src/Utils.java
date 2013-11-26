public class Utils {

	public Table createTableT1() {
		Table t = new Table("T1");
		t.setNumColumns(6);
		t.setTableCard(20);
		
		Column c = new Column(1, "C1", Column.ColType.INT, 10, false);
		c.setColCard(20);
		t.addColumn(c);
		
		c = new Column(2, "C2", Column.ColType.CHAR, 1, false);
		c.setColCard(5);
		t.addColumn(c);
		
		c = new Column(3, "C3", Column.ColType.CHAR, 10, false);
		c.setColCard(8);
		c.setHiKey("GARY");
		c.setLoKey("DAVID");
		t.addColumn(c);
		
		c = new Column(4, "C4", Column.ColType.INT, 10, false);
		c.setColCard(10);
		t.addColumn(c);
		
		c = new Column(5, "C5", Column.ColType.INT, 10, false);
		c.setColCard(4);
		c.setHiKey("90");
		c.setLoKey("50");
		t.addColumn(c);
		
		c = new Column(6, "C6", Column.ColType.INT, 10, false);
		c.setColCard(6);
		t.addColumn(c);
		
		//-------//
		Index index = new Index("T1X1");
		Index.IndexKeyDef def = index.new IndexKeyDef();
		def.colId = 1;
		def.descOrder = false;
		def.idxColPos = 1;
		index.addIdxKey(def);
		
		def = index.new IndexKeyDef();
		def.colId = 3;
		def.descOrder = false;
		def.idxColPos = 2;
		index.addIdxKey(def);
		
		t.addIndex(index);
		
		//-------//
		index = new Index("T1X2");
		def = index.new IndexKeyDef();
		def.colId = 4;
		def.descOrder = false;
		def.idxColPos = 1;
		index.addIdxKey(def);
		
		def = index.new IndexKeyDef();
		def.colId = 1;
		def.descOrder = false;
		def.idxColPos = 2;
		index.addIdxKey(def);
		
		def = index.new IndexKeyDef();
		def.colId = 2;
		def.descOrder = false;
		def.idxColPos = 3;
		index.addIdxKey(def);
		
		t.addIndex(index);
		
		//-------//
		index = new Index("T1X3");
		def = index.new IndexKeyDef();
		def.colId = 3;
		def.descOrder = false;
		def.idxColPos = 1;
		index.addIdxKey(def);
		
		t.addIndex(index);
		
		return t;
	}
	
	public Table createTableT2() {
		Table t = new Table("T2");
		t.setNumColumns(5);
		t.setTableCard(50);
		
		Column c = new Column(1, "C1", Column.ColType.INT, 10, false);
		c.setColCard(50);
		t.addColumn(c);
		
		c = new Column(2, "C2", Column.ColType.INT, 10, false);
		c.setColCard(20);
		t.addColumn(c);
		
		c = new Column(3, "C3", Column.ColType.CHAR, 1, false);
		c.setColCard(5);
		t.addColumn(c);
		
		c = new Column(4, "C4", Column.ColType.INT, 10, false);
		c.setColCard(25);
		t.addColumn(c);
		
		c = new Column(5, "C5", Column.ColType.INT, 10, false);
		c.setColCard(20);
		t.addColumn(c);
		
		//-------//
		Index index = new Index("T2X1");
		Index.IndexKeyDef def = index.new IndexKeyDef();
		def.colId = 1;
		def.descOrder = false;
		def.idxColPos = 1;
		index.addIdxKey(def);
		
		def = index.new IndexKeyDef();
		def.colId = 2;
		def.descOrder = false;
		def.idxColPos = 2;
		index.addIdxKey(def);
		
		t.addIndex(index);
		
		//-------//
		index = new Index("T2X2");
		def = index.new IndexKeyDef();
		def.colId = 2;
		def.descOrder = false;
		def.idxColPos = 1;
		index.addIdxKey(def);
		
		def = index.new IndexKeyDef();
		def.colId = 3;
		def.descOrder = false;
		def.idxColPos = 2;
		index.addIdxKey(def);
		
		t.addIndex(index);
		
		return t;
	}
}
