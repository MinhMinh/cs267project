import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * CS 267 - Project - Implements create index, drop index, list table, and
 * exploit the index in select statements.
 * Author: Minh H Dang
 */
public class DBMS {
	private static final String COMMAND_FILE_LOC = "Commands.txt";
	private static final String OUTPUT_FILE_LOC = "Output.txt";

	private static final String TABLE_FOLDER_NAME = "tables";
	private static final String TABLE_FILE_EXT = ".tab";
	private static final String INDEX_FILE_EXT = ".idx";

	private DbmsPrinter out;
	private ArrayList<Table> tables;
	private ArrayList<ColumnName> selectColumns;
	private ArrayList<ColumnName> whereColumns;
	private ArrayList<ColumnName> sortColumns;
	private ArrayList<String> fromTables;
	private ArrayList<Predicate> predicates;

	public DBMS() {
		tables = new ArrayList<Table>();
	}

	/**
	 * Main method to run the DBMS engine.
	 * 
	 * @param args
	 *            arg[0] is input file, arg[1] is output file.
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		DBMS db = new DBMS();
		db.out = new DbmsPrinter();
		Scanner in = null;
		try {
			// set input file
			if (args.length > 0) {
				in = new Scanner(new File(args[0]));
			} else {
				in = new Scanner(new File(COMMAND_FILE_LOC));
			}

			// set output files
			if (args.length > 1) {
				db.out.addPrinter(args[1]);
			} else {
				db.out.addPrinter(OUTPUT_FILE_LOC);
			}

			// Load data to memory
			db.loadTables();
			
			// Go through each line in the Command.txt file
			while (in.hasNextLine()) {
				String sql = in.nextLine();
				StringTokenizer tokenizer = new StringTokenizer(sql);

				// Evaluate the SQL statement
				if (tokenizer.hasMoreTokens()) {
					String command = tokenizer.nextToken();
					if (command.equalsIgnoreCase("CREATE")) {
						if (tokenizer.hasMoreTokens()) {
							command = tokenizer.nextToken();
							if (command.equalsIgnoreCase("TABLE")) {
								db.createTable(sql, tokenizer);
							} else if (command.equalsIgnoreCase("UNIQUE") || command.equalsIgnoreCase("INDEX")) {
								// TODO your PART 1 code goes here
								boolean isUnique = false;
								if (command.equalsIgnoreCase("UNIQUE")) {
									isUnique = true;
									if (!tokenizer.hasMoreTokens()) {
										throw new DbmsError("Invalid CREATE " + command
												+ " statement. '" + sql + "'."); 
									}
									command = tokenizer.nextToken(); //Move to INDEX
								}
								db.createIndex(sql, tokenizer, isUnique);
							} else {
								throw new DbmsError("Invalid CREATE " + command
										+ " statement. '" + sql + "'.");
							}
						} else {
							throw new DbmsError("Invalid CREATE statement. '"
									+ sql + "'.");
						}
					} else if (command.equalsIgnoreCase("INSERT")) {
						db.insertInto(sql, tokenizer);
					} else if (command.equalsIgnoreCase("DROP")) {
						if (tokenizer.hasMoreTokens()) {
							command = tokenizer.nextToken();
							if (command.equalsIgnoreCase("TABLE")) {
								db.dropTable(sql, tokenizer);
							} else if (command.equalsIgnoreCase("INDEX")) {
								// TODO your PART 1 code goes here
								db.dropIndex(sql, tokenizer);
							} else {
								throw new DbmsError("Invalid DROP " + command
										+ " statement. '" + sql + "'.");
							}
						} else {
							throw new DbmsError("Invalid DROP statement. '"
									+ sql + "'.");
						}
					} else if (command.equalsIgnoreCase("RUNSTATS")) {
						// TODO your PART 1 code goes here
						if (!tokenizer.hasMoreTokens()) {
							throw new NoSuchElementException();
						}
						
						String tableName = tokenizer.nextToken();
						
						String tok = tokenizer.nextToken();
						if (!";".equalsIgnoreCase(tok)) {
							throw new NoSuchElementException();
						}
						
						db.doRunstats(tableName);
						// TODO replace the table name below with the table name
						// in the command to print the RUNSTATS output
						db.printRunstats(tableName);
					} else if (command.equalsIgnoreCase("SELECT")) {
						// TODO your PART 2 code goes here
						db.select(sql, tokenizer);
					} else if (command.equalsIgnoreCase("--")) {
						// Ignore this command as a comment
					} else if (command.equalsIgnoreCase("COMMIT")) {
						try {
							// Check for ";"
							if (!tokenizer.nextElement().equals(";")) {
								throw new NoSuchElementException();
							}

							// Check if there are more tokens
							if (tokenizer.hasMoreTokens()) {
								throw new NoSuchElementException();
							}

							// Save tables to files
							for (Table table : db.tables) {
								db.storeTableFile(table);
							}
						} catch (NoSuchElementException ex) {
							throw new DbmsError("Invalid COMMIT statement. '"
									+ sql + "'.");
						}
					} else {
						throw new DbmsError("Invalid statement. '" + sql + "'.");
					}
				}
			}

			// Save tables to files
			for (Table table : db.tables) {
				db.storeTableFile(table);
			}
		} catch (DbmsError ex) {
			db.out.println("DBMS ERROR:  " + ex.getMessage());
			ex.printStackTrace();
		} catch (Exception ex) {
			db.out.println("JAVA ERROR:  " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			// clean up
			try {
				in.close();
			} catch (Exception ex) {
			}

			try {
				db.out.cleanup();
			} catch (Exception ex) {
			}
		}
	}
	
	/**
	 * Loads tables to memory
	 * 
	 * @throws Exception
	 */
	private void loadTables() throws Exception {
		// Get all the available tables in the "tables" directory
		File tableDir = new File(TABLE_FOLDER_NAME);
		if (tableDir.exists() && tableDir.isDirectory()) {
			for (File tableFile : tableDir.listFiles()) {
				// For each file check if the file extension is ".tab"
				String tableName = tableFile.getName();
				int periodLoc = tableName.lastIndexOf(".");
				String tableFileExt = tableName.substring(tableName
						.lastIndexOf(".") + 1);
				if (tableFileExt.equalsIgnoreCase("tab")) {
					// If it is a ".tab" file, create a table structure
					Table table = new Table(tableName.substring(0, periodLoc));
					Scanner in = new Scanner(tableFile);

					try {
						// Read the file to get Column definitions
						int numCols = Integer.parseInt(in.nextLine());

						for (int i = 0; i < numCols; i++) {
							StringTokenizer tokenizer = new StringTokenizer(
									in.nextLine());
							String name = tokenizer.nextToken();
							String type = tokenizer.nextToken();
							boolean nullable = Boolean.parseBoolean(tokenizer
									.nextToken());
							switch (type.charAt(0)) {
							case 'C':
								table.addColumn(new Column(i + 1, name,
										Column.ColType.CHAR, Integer
												.parseInt(type.substring(1)),
										nullable));
								break;
							case 'I':
								table.addColumn(new Column(i + 1, name,
										Column.ColType.INT, 4, nullable));
								break;
							default:
								break;
							}
						}

						// Read the file for index definitions
						int numIdx = Integer.parseInt(in.nextLine());
						for (int i = 0; i < numIdx; i++) {
							StringTokenizer tokenizer = new StringTokenizer(
									in.nextLine());
							Index index = new Index(tokenizer.nextToken());
							index.setIsUnique(Boolean.parseBoolean(tokenizer
									.nextToken()));

							int idxColPos = 1;
							while (tokenizer.hasMoreTokens()) {
								String colDef = tokenizer.nextToken();
								Index.IndexKeyDef def = index.new IndexKeyDef();
								def.idxColPos = idxColPos;
								def.colId = Integer.parseInt(colDef.substring(
										0, colDef.length() - 1));
								switch (colDef.charAt(colDef.length() - 1)) {
								case 'A':
									def.descOrder = false;
									break;
								case 'D':
									def.descOrder = true;
									break;
								default:
									break;
								}

								index.addIdxKey(def);
							}

							table.addIndex(index);
							loadIndex(table, index);
						}

						// Read the data from the file
						int numRows = Integer.parseInt(in.nextLine());
						for (int i = 0; i < numRows; i++) {
							table.addData(in.nextLine());
						}
						
						// Read RUNSTATS from the file
						while(in.hasNextLine()) {
							String line = in.nextLine();
							StringTokenizer toks = new StringTokenizer(line);
							if(toks.nextToken().equals("STATS")) {
								String stats = toks.nextToken();
								if(stats.equals("TABCARD")) {
									table.setTableCard(Integer.parseInt(toks.nextToken()));
								} else if (stats.equals("COLCARD")) {
									Column col = table.getColumns().get(Integer.parseInt(toks.nextToken()));
									col.setColCard(Integer.parseInt(toks.nextToken()));
									col.setHiKey(toks.nextToken());
									col.setLoKey(toks.nextToken());
								} else {
									throw new DbmsError("Invalid STATS.");
								}
							} else {
								throw new DbmsError("Invalid STATS.");
							}
						}
					} catch (DbmsError ex) {
						throw ex;
					} catch (Exception ex) {
						throw new DbmsError("Invalid table file format.");
					} finally {
						in.close();
					}
					tables.add(table);
				}
			}
		} else {
			throw new FileNotFoundException(
					"The system cannot find the tables directory specified.");
		}
	}
	
	/**
	 * Loads specified table to memory
	 * 
	 * @throws DbmsError
	 */
	private void loadIndex(Table table, Index index) throws DbmsError {
		try {
			Scanner in = new Scanner(new File(TABLE_FOLDER_NAME,
					table.getTableName() + index.getIdxName() + INDEX_FILE_EXT));
			String def = in.nextLine();
			String rows = in.nextLine();

			while (in.hasNext()) {
				String line = in.nextLine();
				Index.IndexKeyVal val = index.new IndexKeyVal();
				val.rid = Integer.parseInt(new StringTokenizer(line)
						.nextToken());
				val.value = line.substring(line.indexOf("'") + 1,
						line.lastIndexOf("'"));
				index.addKey(val);
			}
			in.close();
		} catch (Exception ex) {
			throw new DbmsError("Invalid index file format.");
		}
	}

	/**
	 * CREATE TABLE
	 * <table name>
	 * ( <col name> < CHAR ( length ) | INT > <NOT NULL> ) ;
	 * 
	 * @param sql
	 * @param tokenizer
	 * @throws Exception
	 */
	private void createTable(String sql, StringTokenizer tokenizer)
			throws Exception {
		try {
			// Check the table name
			String tok = tokenizer.nextToken().toUpperCase();
			if (Character.isAlphabetic(tok.charAt(0))) {
				// Check if the table already exists
				for (Table tab : tables) {
					if (tab.getTableName().equals(tok)) {
						throw new DbmsError("Table " + tok
								+ " already exists. '" + sql + "'.");
					}
				}

				// Create a table instance to store data in memory
				Table table = new Table(tok.toUpperCase());

				// Check for '('
				tok = tokenizer.nextToken();
				if (tok.equals("(")) {
					// Look through the column definitions and add them to the
					// table in memory
					boolean done = false;
					int colId = 1;
					while (!done) {
						tok = tokenizer.nextToken();
						if (Character.isAlphabetic(tok.charAt(0))) {
							String colName = tok;
							Column.ColType colType = Column.ColType.INT;
							int colLength = 4;
							boolean nullable = true;

							tok = tokenizer.nextToken();
							if (tok.equalsIgnoreCase("INT")) {
								// use the default Column.ColType and colLength

								// Look for NOT NULL or ',' or ')'
								tok = tokenizer.nextToken();
								if (tok.equalsIgnoreCase("NOT")) {
									// look for NULL after NOT
									tok = tokenizer.nextToken();
									if (tok.equalsIgnoreCase("NULL")) {
										nullable = false;
									} else {
										throw new NoSuchElementException();
									}

									tok = tokenizer.nextToken();
									if (tok.equals(",")) {
										// Continue to the next column
									} else if (tok.equalsIgnoreCase(")")) {
										done = true;
									} else {
										throw new NoSuchElementException();
									}
								} else if (tok.equalsIgnoreCase(",")) {
									// Continue to the next column
								} else if (tok.equalsIgnoreCase(")")) {
									done = true;
								} else {
									throw new NoSuchElementException();
								}
							} else if (tok.equalsIgnoreCase("CHAR")) {
								colType = Column.ColType.CHAR;

								// Look for column length
								tok = tokenizer.nextToken();
								if (tok.equals("(")) {
									tok = tokenizer.nextToken();
									try {
										colLength = Integer.parseInt(tok);
									} catch (NumberFormatException ex) {
										throw new DbmsError(
												"Invalid table column length for "
														+ colName + ". '" + sql
														+ "'.");
									}

									// Check for the closing ')'
									tok = tokenizer.nextToken();
									if (!tok.equals(")")) {
										throw new DbmsError(
												"Invalid table column definition for "
														+ colName + ". '" + sql
														+ "'.");
									}

									// Look for NOT NULL or ',' or ')'
									tok = tokenizer.nextToken();
									if (tok.equalsIgnoreCase("NOT")) {
										// Look for NULL after NOT
										tok = tokenizer.nextToken();
										if (tok.equalsIgnoreCase("NULL")) {
											nullable = false;

											tok = tokenizer.nextToken();
											if (tok.equals(",")) {
												// Continue to the next column
											} else if (tok
													.equalsIgnoreCase(")")) {
												done = true;
											} else {
												throw new NoSuchElementException();
											}
										} else {
											throw new NoSuchElementException();
										}
									} else if (tok.equalsIgnoreCase(",")) {
										// Continue to the next column
									} else if (tok.equalsIgnoreCase(")")) {
										done = true;
									} else {
										throw new NoSuchElementException();
									}
								} else {
									throw new DbmsError(
											"Invalid table column definition for "
													+ colName + ". '" + sql
													+ "'.");
								}
							} else {
								throw new NoSuchElementException();
							}

							// Everything is ok. Add the column to the table
							table.addColumn(new Column(colId, colName, colType,
									colLength, nullable));
							colId++;
						} else {
							// if(colId == 1) {
							throw new DbmsError(
									"Invalid table column identifier " + tok
											+ ". '" + sql + "'.");
							// }
						}
					}

					// Check for the semicolon
					tok = tokenizer.nextToken();
					if (!tok.equals(";")) {
						throw new NoSuchElementException();
					}

					// Check if there are more tokens
					if (tokenizer.hasMoreTokens()) {
						throw new NoSuchElementException();
					}

					if (table.getNumColumns() == 0) {
						throw new DbmsError(
								"No column descriptions specified. '" + sql
										+ "'.");
					}

					// The table is stored into memory when this program exists.
					tables.add(table);

					out.println("Table " + table.getTableName()
							+ " was created.");
				} else {
					throw new NoSuchElementException();
				}
			} else {
				throw new DbmsError("Invalid table identifier " + tok + ". '"
						+ sql + "'.");
			}
		} catch (NoSuchElementException ex) {
			throw new DbmsError("Invalid CREATE TABLE statement. '" + sql
					+ "'.");
		}
	}

	/**
	 * INSERT INTO
	 * <table name>
	 * VALUES ( val1 , val2, .... ) ;
	 * 
	 * @param sql
	 * @param tokenizer
	 * @throws Exception
	 */
	private void insertInto(String sql, StringTokenizer tokenizer)
			throws Exception {
		try {
			String tok = tokenizer.nextToken();
			if (tok.equalsIgnoreCase("INTO")) {
				tok = tokenizer.nextToken().trim().toUpperCase();
				Table table = null;
				for (Table tab : tables) {
					if (tab.getTableName().equals(tok)) {
						table = tab;
						break;
					}
				}

				if (table == null) {
					throw new DbmsError("Table " + tok + " does not exist.");
				}

				tok = tokenizer.nextToken();
				if (tok.equalsIgnoreCase("VALUES")) {
					tok = tokenizer.nextToken();
					if (tok.equalsIgnoreCase("(")) {
						tok = tokenizer.nextToken();
						String values = String.format("%3s", table.getData()
								.size())
								+ " ";
						int colId = 0;
						boolean done = false;
						while (!done) {
							if (tok.equals(")")) {
								done = true;
								break;
							} else if (tok.equals(",")) {
								// Continue to the next value
							} else {
								if (colId == table.getNumColumns()) {
									throw new DbmsError(
											"Invalid number of values were given.");
								}

								Column col = table.getColumns().get(colId);

								if (tok.equals("-") && !col.isColNullable()) {
									throw new DbmsError(
											"A NOT NULL column cannot have null. '"
													+ sql + "'.");
								}

								if (col.getColType() == Column.ColType.INT) {
									try {
										int temp = Integer.parseInt(tok);
									} catch (Exception ex) {
										throw new DbmsError(
												"An INT column cannot hold a CHAR. '"
														+ sql + "'.");
									}

									tok = String.format("%10s", tok.trim());
								} else if (col.getColType() == Column.ColType.CHAR) {
									int length = tok.length();
									if (length > col.getColLength()) {
										throw new DbmsError(
												"A CHAR column cannot exceede its length. '"
														+ sql + "'.");
									}

									tok = String.format(
											"%-" + col.getColLength() + "s",
											tok.trim());
								}

								values += tok + " ";
								colId++;
							}
							tok = tokenizer.nextToken().trim();
						}

						if (colId != table.getNumColumns()) {
							throw new DbmsError(
									"Invalid number of values were given.");
						}

						// Check for the semicolon
						tok = tokenizer.nextToken();
						if (!tok.equals(";")) {
							throw new NoSuchElementException();
						}

						// Check if there are more tokens
						if (tokenizer.hasMoreTokens()) {
							throw new NoSuchElementException();
						}

						// insert the value to table
						table.addData(values);
						out.println("One line was saved to the table. "
								+ table.getTableName() + ": " + values);
					} else {
						throw new NoSuchElementException();
					}
				} else {
					throw new NoSuchElementException();
				}
			} else {
				throw new NoSuchElementException();
			}
		} catch (NoSuchElementException ex) {
			throw new DbmsError("Invalid INSERT INTO statement. '" + sql + "'.");
		}
	}

	/**
	 * DROP TABLE
	 * <table name>
	 * ;
	 * 
	 * @param sql
	 * @param tokenizer
	 * @throws Exception
	 */
	private void dropTable(String sql, StringTokenizer tokenizer)
			throws Exception {
		try {
			// Get table name
			String tableName = tokenizer.nextToken();

			// Check for the semicolon
			String tok = tokenizer.nextToken();
			if (!tok.equals(";")) {
				throw new NoSuchElementException();
			}

			// Check if there are more tokens
			if (tokenizer.hasMoreTokens()) {
				throw new NoSuchElementException();
			}

			// Delete the table if everything is ok
			boolean dropped = false;
			for (Table table : tables) {
				if (table.getTableName().equalsIgnoreCase(tableName)) {
					table.delete = true;
					dropped = true;
					break;
				}
			}

			if (dropped) {
				out.println("Table " + tableName + " does not exist.");
			} else {
				out.println("Table " + tableName + " was dropped.");
			}
		} catch (NoSuchElementException ex) {
			throw new DbmsError("Invalid DROP TABLE statement. '" + sql + "'.");
		}

	}

	private void printRunstats(String tableName) {
		for (Table table : tables) {
			if (table.getTableName().equals(tableName)) {
				out.println("TABLE CARDINALITY: " + table.getTableCard());
				for (Column column : table.getColumns()) {
					out.println(column.getColName());
					out.println("\tCOLUMN CARDINALITY: " + column.getColCard());
					out.println("\tCOLUMN HIGH KEY: " + column.getHiKey());
					out.println("\tCOLUMN LOW KEY: " + column.getLoKey());
				}
				break;
			}
		}
	}

	private void storeTableFile(Table table) throws FileNotFoundException {
		File tableFile = new File(TABLE_FOLDER_NAME, table.getTableName()
				+ TABLE_FILE_EXT);

		// Delete the file if it was marked for deletion
		if (table.delete) {
			try {
				tableFile.delete();
			} catch (Exception ex) {
				out.println("Unable to delete table file for "
						+ table.getTableName() + ".");
			}
			
			// Delete the index files too
			for (Index index : table.getIndexes()) {
				File indexFile = new File(TABLE_FOLDER_NAME, table.getTableName()
						+ index.getIdxName() + INDEX_FILE_EXT);
				
				try {
					indexFile.delete();
				} catch (Exception ex) {
					out.println("Unable to delete table file for "
							+ indexFile.getName() + ".");
				}
			}
		} else {
			// Create the table file writer
			PrintWriter out = new PrintWriter(tableFile);

			// Write the column descriptors
			out.println(table.getNumColumns());
			for (Column col : table.getColumns()) {
				if (col.getColType() == Column.ColType.INT) {
					out.println(col.getColName() + " I " + col.isColNullable());
				} else if (col.getColType() == Column.ColType.CHAR) {
					out.println(col.getColName() + " C" + col.getColLength()
							+ " " + col.isColNullable());
				}
			}

			// Write the index info
			out.println(table.getNumIndexes());
			for (Index index : table.getIndexes()) {
				if(!index.delete) {
					String idxInfo = index.getIdxName() + " " + index.getIsUnique()
							+ " ";

					for (Index.IndexKeyDef def : index.getIdxKey()) {
						idxInfo += def.colId;
						if (def.descOrder) {
							idxInfo += "D ";
						} else {
							idxInfo += "A ";
						}
					}
					out.println(idxInfo);
				}
			}

			// Write the rows of data
			out.println(table.getData().size());
			for (String data : table.getData()) {
				out.println(data);
			}

			// Write RUNSTATS
			out.println("STATS TABCARD " + table.getTableCard());
			for (int i = 0; i < table.getColumns().size(); i++) {
				Column col = table.getColumns().get(i);
				if(col.getHiKey() == null)
					col.setHiKey("-");
				if(col.getLoKey() == null)
					col.setLoKey("-");
				out.println("STATS COLCARD " + i + " " + col.getColCard() + " " + col.getHiKey() + " " + col.getLoKey());
			}
			
			out.flush();
			out.close();
		}
	}

	private void select(String sql, StringTokenizer tokenizer) throws Exception {
		try {
			System.out.println("Parsing SELECT statement ... ");
			selectColumns = new ArrayList<ColumnName>();
			whereColumns = new ArrayList<ColumnName>();
			sortColumns = new ArrayList<ColumnName>();
			fromTables = new ArrayList<String>();
			predicates = new ArrayList<Predicate>();
			
			boolean hasMore = true;
			String token = tokenizer.nextToken();
			while (hasMore) {
				int id = token.indexOf(".");
				if (id == -1) throw new NoSuchElementException();
				
				selectColumns.add(new ColumnName(token.substring(0, id), token.substring(id + 1)));

				token = tokenizer.nextToken();
				hasMore = ",".equalsIgnoreCase(token);
			}
			
			//token == "FROM"
			token = tokenizer.nextToken();
			hasMore = true;
			while (hasMore) {
				fromTables.add(token);
				token = tokenizer.nextToken();
				hasMore = ",".equalsIgnoreCase(token);
			}
			
			System.out.println("After FROM");
			
			if (token.equalsIgnoreCase("WHERE")) {
				token = tokenizer.nextToken();
				hasMore = true;
				while (hasMore) {
					Predicate p = new Predicate();
					p.text += token; //Column name
					
					int id = token.indexOf(".");
					if (id == -1) throw new NoSuchElementException();
					p.left = new ColumnName(token.substring(0, id), token.substring(id + 1));
					
					token = tokenizer.nextToken(); //condition
					p.text += " " + token;
					if (token.equalsIgnoreCase("IN")) {
						p.setType('I');
						p.setInList(true);
						
						//Parse IN list
					} else if ("=><".contains(token)) {
						if ("=".equalsIgnoreCase(token)) p.setType('E');
						else p.setType('R');
						
						token = tokenizer.nextToken();
						if (token.contains(".")) { //Join predicate
							p.setJoin(true);
							p.right = new ColumnName(token.substring(0, id), token.substring(id + 1));
							
							p.text += " " + token;
						} else {
							p.setJoin(false);
							p.litValue = token;
							
							p.text += " " + token;
						}
						
					} else throw new NoSuchElementException(); 
					
					token = tokenizer.nextToken();
					//check if token == ')'
					while (")".equals(token)) {
//						p.text += " )";
						token = tokenizer.nextToken();
					}
						
					hasMore = "AND".equalsIgnoreCase(token) || "OR".equalsIgnoreCase(token); 
					
					if (hasMore) {
//						p.text += " " + token;
						token = tokenizer.nextToken();
						while ("(".equals(token)) {
//							p.text += " (" + token;
							token = tokenizer.nextToken();
						}
					}
					
					System.out.println(p.text);
					predicates.add(p);
				}			
			} 
			
			System.out.println("After WHERE");
			
			if (token.equalsIgnoreCase("ORDER")) {
				token = tokenizer.nextToken();
				if (!token.equalsIgnoreCase("BY")) throw new NoSuchElementException();
				
				token = tokenizer.nextToken();
				hasMore = true;
				while (hasMore) {
					int id = token.indexOf(".");
					if (id == -1) throw new NoSuchElementException();
					sortColumns.add(new ColumnName(token.substring(0, id), token.substring(id + 1)));
					
					token = tokenizer.nextToken(); //Don't care about 'A' or 'D'
					if (token.equalsIgnoreCase("D"))
						token = tokenizer.nextToken();
					
					hasMore = ",".endsWith(token);
				}
			}
			
			if (! ";".equals(token)) throw new NoSuchElementException();
			
			System.out.println("Successfully parsing ...");
		} catch (NoSuchElementException ex) {
			throw new DbmsError("Invalid SELECT statement. '" + sql + "'.");			
		}	
	}
	
	private void createIndex(String sql, StringTokenizer tokenizer, boolean isUnique) throws Exception {
		try {
			//get index name
			String idxName = tokenizer.nextToken();
			Index index = new Index(idxName);
			index.setIsUnique(isUnique);
			
			//Check for ON 
			String tok = tokenizer.nextToken();
			if (! "ON".equalsIgnoreCase(tok)) {
				throw new NoSuchElementException();
			}
			
			if (! tokenizer.hasMoreTokens()) {
				throw new NoSuchElementException();
			}
			
			// Get the table name
			String tableName = tokenizer.nextToken().toUpperCase();
			
			//check if table exists
			Table onTable = null;
			for (Table table : tables) 
				if (tableName.equalsIgnoreCase(table.getTableName())) {
					onTable = table;
					break;
				}
			
			if (onTable == null) {
				out.println("Table " + tableName + " does not exist in the database.");
				return ;
			} else { //check if index does not exist in the table
				for (Index idx : onTable.getIndexes()) 
					if (idx.getIdxName().equalsIgnoreCase(idxName)) {
						out.println("Index " + idxName + " already exist in the table " + tableName);
						return ;
					}				
			}
			
			//Check for '(' 
			if (! tokenizer.hasMoreTokens()) {
				throw new NoSuchElementException();
			}
			tok = tokenizer.nextToken();
			if (! "(".equals(tok)) {
				throw new NoSuchElementException();
			}
			
			boolean hasMoreColumn = true;
			int idxColPos = 1;
			while (hasMoreColumn) {
				//get column name 
				if (! tokenizer.hasMoreTokens()) {
					throw new NoSuchElementException();
				}
				String colDef = tokenizer.nextToken();
				Index.IndexKeyDef def = index.new IndexKeyDef();
				def.idxColPos = idxColPos++;
				def.colId = -1;
				
				for (int i = 0; i < onTable.getColumns().size(); i++)
					if (colDef.equalsIgnoreCase(onTable.getColumns().get(i).getColName())) {
						def.colId = i + 1;
						break;
					}
				
				if (def.colId == -1) {
					out.println("Table " + tableName + " does not have column " + colDef);
					return ; 
				}
				
				def.descOrder = false;
				tok = tokenizer.nextToken();
				if ("DESC".equalsIgnoreCase(tok)) {
					def.descOrder = true;
					tok = tokenizer.nextToken();
				} 
				
				//Check for ','
				hasMoreColumn = ",".equalsIgnoreCase(tok);
				
				index.addIdxKey(def);
			}
			
			//Check for ')' 
			if (! ")".equals(tok)) {
				throw new NoSuchElementException();
			}
			
			// Check for the semicolon
			if (! tokenizer.hasMoreTokens()) {
				throw new NoSuchElementException();
			}
			
			tok = tokenizer.nextToken();
			if (! ";".equals(tok)) {
				throw new NoSuchElementException();
			}
			
			int[] colId = new int[index.getIdxKey().size()];
			int[] desc = new int[index.getIdxKey().size()];
			for (int i = 0; i < colId.length; i++) {
				colId[i] = index.getIdxKey().get(i).colId;
				desc[i] = index.getIdxKey().get(i).descOrder ? -1 : 1;
			}
			
			ArrayList<Row> rows = new ArrayList<Row>();
			for (int i = 0; i < onTable.getData().size(); i++) {
				Row row = new Row();
				
				ArrayList<String> r = onTable.getRow(i);
				for (int j = 0; j < colId.length; j++)
					row.addData(r.get(0), r.get(colId[j]), desc[j]);
				
				rows.add(row);
			}
			
			Collections.sort(rows);
						
			for (int i = 0; i < rows.size(); i++) {
				Index.IndexKeyVal key = index.new IndexKeyVal();
				
				key.rid = Integer.parseInt(rows.get(i).getRowId().trim());
				key.value = rows.get(i).getData();
				
				index.addKey(key);
			}
			
			onTable.addIndex(index);
			out.println("Index " + idxName + " on the table " + onTable.getTableName() + " was created.");
			
		} catch (NoSuchElementException ex) {
			throw new DbmsError("Invalid CREATE INDEX statement. '" + sql + "'.");			
		}		
	}
	
	private void dropIndex(String sql, StringTokenizer tokenizer) throws Exception {
		try {
			//get index name
			String idxName = tokenizer.nextToken();
			
			// Check for the semicolon
			String tok = tokenizer.nextToken();
			if (! ";".equalsIgnoreCase(tok)) {
				throw new NoSuchElementException();
			}
			
			// Check if there are more tokens
			if (tokenizer.hasMoreTokens()) {
				throw new NoSuchElementException();
			}
			
			boolean dropped = false;
			for (Table table : tables) { 
				for (Index index : table.getIndexes()) { 
					if (index.getIdxName().equalsIgnoreCase(idxName)) { 
						table.setNumIndexes(table.getNumIndexes() - 1);
						table.getIndexes().remove(index);						
						dropped = true;
						File indexFile = new File(TABLE_FOLDER_NAME, table.getTableName() + idxName + INDEX_FILE_EXT);
						try {
							indexFile.delete();
						} catch (Exception ex) {
							out.println("Unable to delete index file for " + table.getTableName() + idxName + ".");
						}
						break;
					}
				}
			}
			
			if (! dropped) {
				out.println("Index " + idxName + " does not exist.");
			} else {
				out.println("Index " + idxName + " was dropped.");
			}
			
		} catch (NoSuchElementException ex) {
			throw new DbmsError("Invalid DROP INDEX statement. '" + sql + "'.");			
		}
	}
	
	private void doRunstats(String tableName) {
		Table table = null;
		for (Table t : tables)
			if (tableName.equalsIgnoreCase(t.getTableName())) {
				table = t;
				break;
			}
		
		if (table == null) {
			out.println("Table " + tableName + " does not exist in the database");
		}
		
		table.setTableCard(table.getData().size());
		for (int j = 1; j <= table.getNumColumns(); j++) {
			Column column = table.getColumns().get(j - 1);
			String loKey = table.getCell(0, j);
			String hiKey = table.getCell(0, j);
			
			HashSet<String> h = new HashSet<String>();
			int d = 0;
			h.add(table.getCell(0,  j));
			for (int i = 1; i < table.getData().size(); i++) {
				String s = table.getCell(i, j);
				
				if (!h.contains(s)) {
					h.add(s);
				} else continue;
				
				if (s.contains("-")) {
					d = 1;
					continue;
				}
				
				if (loKey.compareToIgnoreCase(s) > 0) {
					loKey = s;
				}
				if (hiKey.compareToIgnoreCase(s) < 0) {
					hiKey = s;
				}
			}
			
			column.setLoKey(loKey);
			column.setHiKey(hiKey);
			column.setColCard(h.size() - d);
		}
		
	}
}
