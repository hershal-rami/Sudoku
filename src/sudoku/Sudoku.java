package sudoku;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class Sudoku {

	static int[][] board;
	static int[][][] possibleValues; // row, col, set of values

	final static String fileName = "Sudoku Boards";

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
		Sudoku puzzle = new Sudoku(readBoardFromFile(fileName));
		printBoard();
		puzzle.solve();
	}

	public Sudoku(int[][] board) {
		Sudoku.board = board;
		Sudoku.possibleValues = new int[board.length][board[0].length][9]; 
	}

	/**
	 * This method will print out a solution for the stored board
	 */
	private void solve() {
		if (!fillSingletons()) {
			System.out.println("Unsolvable board. Exiting out.");
			return;
		}
		
//		backtrackAlgorithm();
//		printBoard();

	}

	/**
	 * Finds all possible values for each cell on the board, and fills in any cells
	 * which only contain one possible value
	 */
	private boolean fillSingletons() {
		// for each cell (set of coords), there is a array of possible values
		int row = 0, col = 0, index = 0, count = 0;

		/*
		 * Loops through every cell on the board and checks if any numbers 1-9 are valid
		 * at that cell. If they are, it adds them to an array of possible values
		 */
		for (int i = 0; i < possibleValues.length; i++) {
			for (int j = 0; j < possibleValues[0].length; j++) {
				for (int k = 0; k < 9; k++) {
					if (board[i][j] != 0 && isValidPlacement(i, j, k + 1)) {
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
				} else if (count == 1) { // that cell is a singleton; it only has one valid placement
					System.out.println("Singleton at " + row + ", " + col + ", " + index);
					board[row][col] = possibleValues[row][col][index];
				}

			}
		}
		return true;
	}
	
	/**
	 * Checks each row to see if there are any numbers which are only possible values in one place
	 */
	public void checkRows() {
		for(int i = 0; i < board.length; i++) {
			for(int j = 0; j < board[0].length; j++) {
				
			}
		}
	}

	private int row = 0;
	private int col = 0;
	private boolean boardSolved = false;
	
	private boolean backtrackAlgorithm() {
		// if the cell has a number already, move on
		if(board[row][col] != 0) {
			findNextIndex();
			backtrackAlgorithm();
		}
		
		// test numbers in that cell until they are valid, then place it and move on
		// TODO use possibleValues instead of looping through all numbers
		for(int i = 1; i < 10; i++) {
			if(isValidPlacement(row, col, i)) {
				board[row][col] = i;
				findNextIndex();
				if(!backtrackAlgorithm())
					break;
			}
		}
		
		// if we reach the final cell, we're done!
		if(boardSolved) {
			return true;
		}
		
		return false;
		
	}
	
	private void findNextIndex() {
		// 0-indexed
		if(row == 8 && col ==8) {
			boardSolved = true;
		}
		
		if(col + 1 >= 9) {
			col = 0;
			row++;
		} else {
			col++;
		}
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
	 * Checks if a number can be legally placed in the specified row and column
	 * index
	 * 
	 * @param rowIndex
	 * @param colIndex
	 * @param numToPlace
	 * @return
	 */
	private static boolean isValidPlacement(int rowIndex, int colIndex, int numToPlace) {
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
			for (int j = topLeftBoxIndex[1]; i < topLeftBoxIndex[1] + 3; i++)
				if (board[i][j] == numToPlace)
					return false;

		// no conflicts, return true
		return true;
	}

	/**
	 * Checks if a board is valid by checking each row, column, and square
	 * 
	 * @return true if valid, false otherwise
	 */
	public boolean isValid() {
		for (int i = 0; i < board.length; i++) {
			if (!isValidRow(i) || !isValidColumn(i))
				return false;

			if ((i + 1) % 3 == 0)
				for (int j = 0; j < 3; j++)
					if (!isValidSquares(i, j))
						return false;
		}
		return true;
		// return (isValidRow(rowIndex) || isValidColumn(colIndex) ||
		// isValidSquares(rowIndex, colIndex));
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
	private static int[] getTopLeftBoxIndex(int rowPosIndex, int colPosIndex) {
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

	/**
	 * Prints out the entire board
	 */
	public static void printBoard() {
		System.out.println("----------------------");
		for (int i = 0; i < board.length; i++) {
			System.out.print("| ");
			for (int j = 0; j < board[0].length; j++) {
				if (board[i][j] == 0)
					System.out.print("- ");
				else
					System.out.print(board[i][j] + " ");
			}
			System.out.println(" |");
		}
		System.out.println("----------------------");
	}
}