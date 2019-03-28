package sudoku;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class Sudoku {

	static int[][] board;
	static int[][][] possibleValues; // row, col, set of values

	static String fileName = "Sudoku Boards";
	final static boolean print = true;
	final static int maxIterations = 15;

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		int choice = 0;
		
		// Gets user input
		while(choice != 1 || choice != 2) {
			System.out.println("Would you like to 1) Generate a puzzle or 2) Solve a puzzle?");
			choice = scan.nextInt();
			scan.nextLine();
			if(choice != 1 || choice != 2) {
				System.out.println("Error: Please enter a valid input");
			}
		}
		
		switch(choice) {
		case 1: // Generate a puzzle
			System.out.println("How many cells should be empty in this puzzle?");
			int emptyCells = scan.nextInt(); scan.nextLine();
			int[][] puzzle = generatePuzzle(generateSolvedBoard(), emptyCells);
			printBoard(puzzle);
			break;
		case 2: // Solve a puzzle
			System.out.println("Please enter the name of the file with the Sudoku puzzle to solve.");
			fileName = scan.nextLine();
			board = readBoardFromFile(fileName);
			possibleValues = new int[board.length][board[0].length][9];
			printBoard(board);
			solve();
			break;
		}
		
		scan.close();
		
	}

	public Sudoku(int[][] board) {
		Sudoku.board = board;
		possibleValues = new int[board.length][board[0].length][9];
	}

	/**
	 * First attempts to solve the puzzle using the logic algorithm. If that takes
	 * too long, it switches to the backtracking algorithm from the current board
	 * state. This method will print out a solution for the stored board and the
	 * total time it took to solve the puzzle.
	 */
	private static void solve() {
		if (!isValid()) {
			System.out.println("The board has no solutions. Exiting out.");
			return;
		}
		long startTime = System.currentTimeMillis();
		// find all possible values and fill all initial naked singles
		if (!fillPossibleValues()) {
			System.out.println("The board has no solutions. Exiting out.");
			return;
		}

		int loopCounter = 0;
		while (beforeBacktrack()) {
			if (loopCounter > maxIterations) {
				System.out.printf("The logic algorithm requires more than %d iterations to solve.\n", maxIterations);
				System.out.println("Logic Algorithm Board State: ");
				printBoard(board);
				System.out.println("Logic Algorithm Time Elapsed: " + (System.currentTimeMillis() - startTime) + " ms");
				System.out.println("Switching to backtracking algorithm");
				startTime = System.currentTimeMillis();
				bruteForce();
				break;
			}
			fillPossibleValues();
			checkRowsCols();
			checkSquares();
			loopCounter++;
		}

		printBoard(board);

		System.out.println("The Sudoku puzzle has been solved.\nTotal Time Elapsed: "
				+ (System.currentTimeMillis() - startTime) + " ms");

	}

	/**
	 * 
	 * @return True if the board state can still be solved, false if solved
	 */
	private static boolean beforeBacktrack() {
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
	private static boolean fillPossibleValues() {
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
	private static void checkRowsCols() {
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
						System.out.println("Hidden Singleton at " + i + ", " + col + ": Value " + index);
					updatePossibleValues(i, col, index);
				}
				if (count2 == 1) { // this means num is only possible for one cell in this col
					board[row2][col2] = index2;
					if (print)
						System.out.println("Hidden Singleton at " + row2 + ", " + col2 + ": Value " + index2);
					updatePossibleValues(row2, col2, index2);
				}
			}
		}
	}

	/**
	 * Checks each square to find numbers which are only possible values for one
	 * cell in the square
	 */
	private static void checkSquares() {
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
									"Hidden Singleton at " + rowIndex + ", " + colIndex + ": Value " + numToPlace);
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
	private static void updatePossibleValues(int rowIndex, int colIndex, int numPlaced) {
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
			for (int j = topLeftBoxIndex[1]; j < topLeftBoxIndex[1] + 3; j++)
				if (board[i][j] == numToPlace)
					return false;

		// no conflicts, return true
		return true;
	}

	/**
	 * Places valid numbers in each open cell until the puzzle is solved,
	 * backtracking if a puzzle state becomes unsolvable
	 */
	public static boolean bruteForce() {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (board[i][j] == 0) {
					for (int k = 1; k <= 9; k++) {
						if (isValidPlacement(i, j, k)) {
							board[i][j] = k;
							if (bruteForce()) {
								return true;
							} else {
								board[i][j] = 0;
							}
						}
					}
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Checks if a board is valid by checking each row, column, and square
	 * 
	 * @return true if valid, false otherwise
	 */
	private static boolean isValid() {
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
	private static boolean isValidRow(int rowIndex) {
		boolean[] numbersExist = new boolean[9];
		for (int i = 0; i < 9; i++) {
			if (board[rowIndex][i] == 0) {
				continue;
			}
			if (numbersExist[board[rowIndex][i] - 1]) {
				return false;
			} else {
				numbersExist[board[rowIndex][i] - 1] = true;
			}
		}
		return true;
	}

	/**
	 * If the total sum of the entire column equals 45, then it returns true
	 * 
	 * @param colIndex
	 * @return
	 */
	private static boolean isValidColumn(int colIndex) {
		boolean[] numbersExist = new boolean[9];
		for (int i = 0; i < 9; i++) {
			if (board[i][colIndex] == 0) {
				continue;
			}
			if (numbersExist[board[i][colIndex] - 1]) {
				return false;
			} else {
				numbersExist[board[i][colIndex] - 1] = true;
			}
		}
		return true;
	}

	/**
	 * If the total sum of the entire square equals 45, then it returns true
	 * 
	 * @param rowPosIndex
	 * @param colPosIndex
	 * @return
	 */
	private static boolean isValidSquares(int rowPosIndex, int colPosIndex) {
		int[] topLeftBoxIndex = getTopLeftBoxIndex(rowPosIndex, colPosIndex);

		boolean[] numbersExist = new boolean[9];

		for (int i = topLeftBoxIndex[0]; i < topLeftBoxIndex[0] + 3; i++) {
			for (int j = topLeftBoxIndex[1]; j < topLeftBoxIndex[1] + 3; j++) {
				if (board[i][j] == 0)
					continue;
				if (numbersExist[board[i][j] - 1]) {
					return false;
				} else {
					numbersExist[board[i][j] - 1] = true;
				}
			}
		}

		return true;
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
	 * Scans the file to read in numbers and returns the 2D board array
	 * 
	 * @param fileName
	 * @return board array
	 */
	private static int[][] readBoardFromFile(String fileName) {
		int[][] board = new int[9][9];
		int[] rowNumbers = new int[9];
		Scanner scan = null;
		try {
			scan = new Scanner(new File(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("The file was not found. Exiting out.");
			return null;
		}

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

		scan.close();
		return board;
	}

	/**
	 * Prints out the entire board
	 */
	private static void printBoard(int[][] board) {
		System.out.println("-------------------------");
		for (int i = 0; i < board.length; i++) {
			if (i == 3 || i == 6)
				System.out.println("|-----------------------|");
			System.out.print("| ");
			for (int j = 0; j < board[0].length; j++) {
				if (j == 3 || j == 6)
					System.out.print("| ");
				if (board[i][j] == 0)
					System.out.print("- ");
				else
					System.out.print(board[i][j] + " ");
			}
			System.out.println("|");
		}
		System.out.println("-------------------------");
	}

	/**
	 * Generates a new solved board state through a very not robust and expensive
	 * manner
	 * 
	 * @return int[][] puzzle containing a valid and complete board
	 */
	private static int[][] generateSolvedBoard() {
		int[][] board = new int[9][9];
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				while (board[i][j] == 0) {
					int num = (int) (Math.random() * 9) + 1; // returns a number between 1 and 9

					if (isValidPlacement(i, j, num)) {
						board[i][j] = num;
					}

				}
			}
		}
		return board;
	}

	/**
	 * Loops through and sets random valus to 0 to make this board into a puzzle
	 * 
	 * @param board      The 2D array containing a solved board state
	 * @param emptyCells The number of empty cells the puzzle should contain
	 * @return A 2D array containing a puzzle ready to be solved
	 */
	private static int[][] generatePuzzle(int[][] board, int emptyCells) {
		int count = 0;
		while (count < emptyCells) {
			int randomRow = (int) (Math.random() * 9); // returns a number between 0 and 8
			int randomCol = (int) (Math.random() * 9); // returns a number between 0 and 8

			if (board[randomRow][randomCol] != 0) { // this is a cell we can make empty
				board[randomRow][randomCol] = 0;
				count++;
			}
		}
		return board;
	}
}