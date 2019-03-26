package sudoku;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class Sudoku {

	static int[][] board;
	static int[][][] possibleValues; // row, col, set of values

	static String fileName = "Sudoku Boards";
	final boolean print = true;

	/*
	 * 3 0 6 | 5 0 8 | 4 0 0
	 * 
	 * 5 2 0 | 0 0 0 | 0 0 0
	 * 
	 * 0 8 7 | 0 0 0 | 0 3 1
	 * 
	 * ---------------------
	 * 
	 * 0 0 3 | 0 1 0 | 0 8 0
	 * 
	 * 9 0 0 | 8 6 3 | 0 0 5
	 * 
	 * 0 5 0 | 0 9 0 | 6 0 0
	 * 
	 * ---------------------
	 * 
	 * 1 3 0 | 0 0 0 | 2 5 0
	 * 
	 * 0 0 0 | 0 0 0 | 0 7 4
	 * 
	 * 0 0 5 | 2 0 6 | 3 0 0
	 */
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		System.out.println("Please enter the name of the file with the Sudoku puzzle to solve.");
		fileName = scan.nextLine();
		Sudoku puzzle = new Sudoku(readBoardFromFile(fileName));
		printBoard();
		puzzle.solve();
		scan.close();
	}

	public Sudoku(int[][] board) {
		Sudoku.board = board;
		possibleValues = new int[board.length][board[0].length][9];
	}

	/**
	 * This method will print out a solution for the stored board
	 */
	private void solve() {
		long startTime = System.currentTimeMillis();
		// find all possible values and fill all initial naked singles
		if (!fillPossibleValues()) {
			System.out.println("The board has no solutions. Exiting out.");
			return;
		}

		while (beforeBacktrack()) {
			fillPossibleValues();
			checkRowsCols();
			checkSquares();
		}

		printBoard();

		System.out.println("The Sudoku puzzle has been solved. Total Time Elapsed: "
				+ (System.currentTimeMillis() - startTime) + " ms");

//		backtrackAlgorithm(-1, -1);
	}

	private boolean beforeBacktrack() {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				if (board[i][j] != 0) {
					continue;
				}
				int count = 0;
				for (int k = 1; k <= 9; k++) {
					if (possibleValues[i][j][k - 1] != 0)
						count++;
				}
				if (count > 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Finds all possible values for each cell on the board, and fills in any cells
	 * which only contain one possible value. If there are no possible values for a
	 * cell, the method returns false as the board is unsolvable.
	 */
	private boolean fillPossibleValues() {
		// for each cell (set of coords), there is a array of possible values
		int row = 0, col = 0, index = 0, count = 0;

		/*
		 * Loops through every cell on the board and checks if any numbers 1-9 are valid
		 * at that cell. If they are, it adds them to an array of possible values
		 */
		outerLoop: for (int i = 0; i < possibleValues.length; i++) {
			for (int j = 0; j < possibleValues[0].length; j++) {
				count = 0;
				if (board[i][j] != 0)
					continue;
				for (int k = 0; k < 9; k++) {
					if (isValidPlacement(i, j, k + 1)) {
						count++;
						possibleValues[i][j][k] = k + 1;
						row = i;
						col = j;
						index = k;
					}
				}

				if (count == 0) { // that cell has no valid placements, therefore the board is invalid
					System.out.println("This board is invalid. There are no possible values at (" + i + ", " + j + ")");
					return false;
				} else if (count == 1) {
					/*
					 * If a cell only has one possible value, it is a naked single. Fill the single,
					 * print out its location, then update other possible values accordingly. Then,
					 * restart the loop. Once all recursive calls return, they will simply break out
					 * of the loop to avoid running the loop more than necessary
					 */
					board[row][col] = index + 1;
					if (print)
						System.out.println("Naked Singleton at " + row + ", " + col + ": Value " + (index + 1));
					updatePossibleValues(row, col, index + 1);
					fillPossibleValues();
					break outerLoop;
				}

			}
		}
		return true;
	}

	/**
	 * Checks each row to see if there are any numbers which are only possible
	 * values in one cell
	 */
	private void checkRowsCols() {
		int count = 0, count2 = 0, row2 = 0, col = 0, col2 = 0, index = 0, index2 = 0;
		for (int i = 0; i < board.length; i++) {
			for (int num = 1; num <= 9; num++) {
				count = 0;
				for (int j = 0; j < board[0].length; j++) {
					if (possibleValues[i][j][num - 1] == num) { // if a number is a possible value for this cell
						count++;
						col = j;
						index = num;
					}
					if (possibleValues[j][i][num - 1] == num) {
						count2++;
						row2 = j;
						col2 = i;
						index2 = num;
					}
				}
				if (count == 1) { // this means num is only possible for one cell in this row
					board[i][col] = index;
					if (print)
						System.out.println("Hidden singleton at " + i + ", " + col + ": Value " + index);
					updatePossibleValues(i, col, index);
				}
				if (count2 == 1) { // this means num is only possible for one cell in this col
					board[row2][col2] = index2;
					if (print)
						System.out.println("Hidden singleton at " + row2 + ", " + col2 + ": Value " + index2);
					updatePossibleValues(row2, col2, index2);
				}
			}
		}
	}

	/**
	 * Checks each square to find numbers which are only possible values for one
	 * cell in the square
	 */
	private void checkSquares() {
		int count = 0, rowIndex = 0, colIndex = 0, numToPlace = 0;
		// i and j are for the top left indexes of all 9 squares
		// row and col are for the coords of each cell within every square
		for (int i = 0; i < 9; i += 3) {
			for (int j = 0; j < 9; j += 3) { // loops through all 9 squares
				for (int num = 1; num <= 9; num++) { // loops through all 9 legal numbers
					count = 0;
					for (int row = i; row < i + 3; row++) {
						for (int col = j; col < j + 3; col++) { // loops through all 9 cells within a square
							if (possibleValues[row][col][num - 1] == num) { // if a number is a possible value for this
																			// cell
								count++;
								rowIndex = row;
								colIndex = col;
								numToPlace = num;
							}
						}
					}
					if (count == 1) { // this means num is only possible for one cell in this square
						board[rowIndex][colIndex] = numToPlace;
						if (print)
							System.out.println(
									"Hidden singleton at " + rowIndex + ", " + colIndex + ": Value " + numToPlace);
						updatePossibleValues(rowIndex, colIndex, numToPlace);
					}
				}
			}
		}
	}

	/**
	 * This method should be called after placing a number to update possible values
	 * in the row, col, and square of that number.
	 * 
	 * @param rowIndex  The row index of the number that was just placed
	 * @param colIndex  The col index of the number that was just placed
	 * @param numPlaced The number that was just placed
	 */
	private void updatePossibleValues(int rowIndex, int colIndex, int numPlaced) {
		int[] topLeftBoxIndex = getTopLeftBoxIndex(rowIndex, colIndex);

		// removes numPlaced from possibleValues for the square
		for (int row = topLeftBoxIndex[0]; row < topLeftBoxIndex[0] + 3; row++) {
			for (int col = topLeftBoxIndex[1]; col < topLeftBoxIndex[1] + 3; col++) {
				possibleValues[row][col][numPlaced - 1] = 0;
			}
		}

		// removes numPlaced from possibleValues for the row/column
		for (int i = 0; i < 9; i++) {
			if (board[rowIndex][i] == 0) {
				possibleValues[rowIndex][i][numPlaced - 1] = 0;
			}
			if (board[i][colIndex] == 0) {
				possibleValues[i][colIndex][numPlaced - 1] = 0;
			}
		}

		// removes any other possible values for the cell
		for (int i = 0; i < possibleValues[rowIndex][colIndex].length; i++) {
			possibleValues[rowIndex][colIndex][i] = 0;
		}
	}

	/**
	 * Checks if a number can be legally placed in the specified row and column
	 * index
	 * 
	 * @param rowIndex
	 * @param colIndex
	 * @param numToPlace
	 * @return
	 */
	private boolean isValidPlacement(int rowIndex, int colIndex, int numToPlace) {
		// check row
		for (int number : board[rowIndex])
			if (number == numToPlace)
				return false;

		// check column
		for (int i = 0; i < board.length; i++)
			if (board[i][colIndex] == numToPlace)
				return false;

		// check square
		int[] topLeftBoxIndex = getTopLeftBoxIndex(rowIndex, colIndex);
		for (int i = topLeftBoxIndex[0]; i < topLeftBoxIndex[0] + 3; i++)
			for (int j = topLeftBoxIndex[1]; j < topLeftBoxIndex[1] + 3; j++)
				if (board[i][j] == numToPlace)
					return false;

		// no conflicts, return true
		return true;
	}

	private int[] findNextIndex(int row, int col) {
		// 0-indexed
		if (row == -1 && col == -1)
			return new int[] { 0, 0 };
		
		if (col + 1 >= 9) {
			col = 0;
			row++;
		} else {
			col++;
		}
		return new int[] { row, col };
	}

	private boolean backtrackAlgorithm(int currentRow, int currentCol) {
		int[] coords = findNextIndex(currentRow, currentCol);
		int rowIndex = coords[0];
		int colIndex = coords[1];

		// if the cell has a number already, move on
		if (board[rowIndex][colIndex] != 0) {
			coords = findNextIndex(rowIndex, colIndex);
			rowIndex = coords[0];
			colIndex = coords[1];
			backtrackAlgorithm(rowIndex, colIndex);
		}

		// test numbers in that cell until they are valid, then place it and move on
		for (int i = 1; i < 10; i++) {
			if (possibleValues[rowIndex][colIndex][i] == 0) {
				continue;
			}
			if (isValidPlacement(rowIndex, colIndex, i)) {
				board[rowIndex][colIndex] = i;
				updatePossibleValues(rowIndex, colIndex, i);

				coords = findNextIndex(rowIndex, colIndex);
				rowIndex = coords[0];
				colIndex = coords[1];

				if (!backtrackAlgorithm(rowIndex, colIndex))
					break;
			}
		}

		// if we reach the final cell, we're done!
		if (rowIndex == 8 && colIndex == 8) {
			return true;
		}

		return false;
	}

	/**
	 * Checks if a board is valid by checking each row, column, and square
	 * 
	 * @return true if valid, false otherwise
	 */
	private boolean isValid() {
		for (int i = 0; i < board.length; i++) {
			if (!isValidRow(i) || !isValidColumn(i))
				return false;

			if ((i + 1) % 3 == 0)
				for (int j = 0; j < 3; j++)
					if (!isValidSquares(i, j))
						return false;
		}
		return true;
	}

	/**
	 * If the total sum of the entire row equals 45, then it returns true
	 * 
	 * @param rowIndex
	 * @return
	 */
	private boolean isValidRow(int rowIndex) {
		int rowTotal = 0;
		for (int number : board[rowIndex]) {
			rowTotal += number;
		}
		System.out.println(rowTotal);
		return rowTotal == 45;
	}

	/**
	 * If the total sum of the entire column equals 45, then it returns true
	 * 
	 * @param colIndex
	 * @return
	 */
	private boolean isValidColumn(int colIndex) {
		int colTotal = 0;
		for (int i = 0; i < board.length; i++) {
			colTotal += board[i][colIndex];
		}
		System.out.println(colTotal);
		return colTotal == 45;
	}

	/**
	 * If the total sum of the entire square equals 45, then it returns true
	 * 
	 * @param rowPosIndex
	 * @param colPosIndex
	 * @return
	 */
	private boolean isValidSquares(int rowPosIndex, int colPosIndex) {
		int[] topLeftBoxIndex = getTopLeftBoxIndex(rowPosIndex, colPosIndex);
		int boxTotal = 0;

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				boxTotal += board[topLeftBoxIndex[0] + i][topLeftBoxIndex[1] + j];
			}
		}

		return boxTotal == 45;
	}

	/**
	 * Returns the indexes of the top left box for the square of the specified row
	 * and column index
	 * 
	 * @param rowPosIndex
	 * @param colPosIndex
	 * @return int[] containing the row and column index of the top left box of the
	 *         square
	 */
	private int[] getTopLeftBoxIndex(int rowPosIndex, int colPosIndex) {
		int[] topLeftBoxIndex = new int[2]; // row, col

		if (rowPosIndex <= 2)
			topLeftBoxIndex[0] = 0;
		else if (rowPosIndex >= 3 && rowPosIndex <= 5)
			topLeftBoxIndex[0] = 3;
		else if (rowPosIndex >= 6)
			topLeftBoxIndex[0] = 6;

		if (colPosIndex <= 2)
			topLeftBoxIndex[1] = 0;
		else if (colPosIndex >= 3 && colPosIndex <= 5)
			topLeftBoxIndex[1] = 3;
		else if (colPosIndex >= 6)
			topLeftBoxIndex[1] = 6;

		return topLeftBoxIndex;
	}

	private static int[][] readBoardFromFile(String fileName) {
		int[][] board = new int[9][9];
		int[] rowNumbers = new int[9];
		Scanner scan = null;
		try {
			scan = new Scanner(new File(fileName));
			int count = -1;
			while (scan.hasNextLine()) {
				String[] row = scan.nextLine().trim().split(", ");
				for (int i = 0; i < row.length; i++) {
					rowNumbers[i] = Integer.parseInt(row[i]);
					if (row.length - 1 == i)
						count++;
				}
				board[count] = Arrays.copyOf(rowNumbers, rowNumbers.length);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		scan.close();
		return board;
	}

	/**
	 * Prints out the entire board
	 */
	private static void printBoard() {
		System.out.println("-----------------------");
		for (int i = 0; i < board.length; i++) {
			if (i == 3 || i == 6)
				System.out.println("|---------------------|");
			System.out.print("| ");
			for (int j = 0; j < board[0].length; j++) {
				if (j == 3 || j == 6)
					System.out.print("|");
				if (board[i][j] == 0)
					System.out.print("- ");
				else
					System.out.print(board[i][j] + " ");
			}
			System.out.println("|");
		}
		System.out.println("-----------------------");
	}
}