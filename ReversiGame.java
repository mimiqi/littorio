package lab4.src;
import java.util.*;

enum Piece {
    BLACK('●'), WHITE('○'), EMPTY('·'), PLACEABLE('+');

    private final char symbol;

    Piece(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }
}

class board {
    private static final int size = 8;
    protected Piece[][] grid;
    protected int blackCount = 0;
    protected int whiteCount = 0;
    protected String mode;
    
    public board(String mode) {//create and initialize a new borad.
        grid = new Piece[size][size];
        for (int i = 0; i < size; i++) {
            Arrays.fill(grid[i], Piece.EMPTY);//set all the grid to EMPTY.
        }
        grid[3][3] = grid[4][4] = Piece.WHITE;
        grid[3][4] = grid[4][3] = Piece.BLACK;
        blackCount = 2;
        whiteCount = 2;
        if(mode.equals("Peace")){//other invalid input will be processed in public void start()
            this.mode = "Peace";
        }
        else if(mode.equals("Reversi")){
            this.mode = "Reversi";
        }
    }

    public void Print(Player[] players,int boardIndex){//print the board
        System.out.println("\n");
        System.out.println("  A B C D E F G H");
        for(int row = 0;row < 8;row++){
            System.out.printf("%d ",row + 1);
            for(int col = 0;col < 8;col++){
                System.out.print(grid[row][col].getSymbol() + " ");
            }
            if(row == 2){
                System.out.printf("棋盘%d",boardIndex + 1);
            }
            if(row == 3){
                System.out.printf("玩家[%s]%s",players[0].getName(),Piece.BLACK.getSymbol());
            }
            if(row ==4){
                System.out.printf("玩家[%s]%s",players[1].getName(),Piece.WHITE.getSymbol());
            }
            System.out.printf("\n");
        }
        //print the list of board still needs to be fulfilled.
    }

    public String getMode(){//get the mode of the game
        return mode;
    }

    public char getPiece(int row,int col){//get the piece of the grid
        return grid[row][col].getSymbol();
    }

    protected void updateCounts(){//get current number of black and white pieces.
        for(int row = 0;row < 8;row++){
            for(int col = 0;col < 8;col++){
                if(grid[row][col] == Piece.BLACK){
                    blackCount++;
                }
                else if(grid[row][col] == Piece.WHITE){
                    whiteCount++;
                }
            }
        }
    }

    public int getBlackCount(){//get the number of black pieces.
        return blackCount;
    }

    public int getWhiteCount(){//get the number of white pieces.
        return whiteCount;
    }
}

class Player{
    private final String name;
    private final Piece piece;
    private ArrayList<String> validMoves = new ArrayList<String>();//preparation for ctrl+z

    public Player(String name,Piece piece){
        this.name = name;
        this.piece = piece;
    }

    public String getName(){
        return name;
    }

    public Piece getPiece(){
        return piece;
    }

    public void addValidMove(String move){
        validMoves.add(move);
    }
}

class Game{//parent class for two modes.
    protected ArrayList<board> boards = new ArrayList<board>();//unknown the precise number of boards.
    protected Player[] players = new Player[2];//two players.
    protected int currentBoardIndex = 0;//the index of the current board,use to control borad shifting.
    protected ArrayList<Integer> currentPlayerIndex = new ArrayList<Integer>();//memorize every board's current player.
    protected ArrayList<Boolean> isGameOver = new ArrayList<Boolean>();//memorize every board's game situation.
    protected void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void start(String player1, String player2){//needs to be fulfilled.
        boards.add(new peaceGame("Peace", false, 0));//initialize the first board as Peace mode.
        boards.add(new reversiGame("Reversi", false, 1));//initialize the second board as Reversi mode.
        currentPlayerIndex.add(0);//initialize the first player as the current player.
        currentPlayerIndex.add(0);
        isGameOver.add(false);//initialize the game as not over.
        isGameOver.add(false);
        players[0] = new Player(player1,Piece.BLACK);
        players[1] = new Player(player2,Piece.WHITE);
        Scanner scanner = new Scanner(System.in);
        do{//loop start. 
            clearScreen();
            boards.get(currentBoardIndex).updateCounts();
            boards.get(currentBoardIndex).Print(players,currentBoardIndex);
            System.out.printf("请玩家[%s]输入落子位置(e.g. 1A) / 棋盘编号(1-%d) / 新游戏类型(Peace / Reversi) / 退出程序(quit):",players[currentPlayerIndex.get(currentBoardIndex)].getName(),boards.size());//input suggestions.
            System.out.println("This input is sensitive upper and lower case. Please obey the examples.");
            String input = scanner.nextLine();
            if(input.equals("quit")){//if start
                break;
            }
            else if(input.length() == 1){//process possible broad number.
                    if(input.length() == 1 && Character.isDigit(input.charAt(0))){
                        int boardIndex = Integer.parseInt(input);
                        if(boardIndex <= boards.size()){
                            currentBoardIndex = boardIndex - 1;
                            boards.get(currentBoardIndex).Print(players,currentBoardIndex);
                        }
                        else{
                            System.out.println("棋盘编号超出范围,两秒后刷新。");
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            continue;
                        }
                    }
                    else{
                        System.out.println("无效输入");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }
            }else if(input.length() == 2){//process possible Piece Location.
                if(Character.isDigit(input.charAt(0)) && Character.isLetter(input.charAt(1))){
                    int row = input.charAt(0) - '0' - 1;
                    int col = input.charAt(1) - 'A';
                    if(row >= 0 && row < 8 && col >= 0 && col < 8){//not overflow.
                        if(boards.get(currentBoardIndex).getMode().equals("Peace")){//process Peace mode.
                        if (boards.get(currentBoardIndex) instanceof peaceGame && !((peaceGame) boards.get(currentBoardIndex)).isValidMove(row, col, players[currentPlayerIndex.get(currentBoardIndex)])) {//target position is not available.
                                System.out.println("无效输入");
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                continue;    
                            }    
                            else{//target position is available.
                                //update the board. //needs to be fulfilled.
                                boards.get(currentBoardIndex).Print(players,currentBoardIndex);
                            }
                        }
                        if(boards.get(currentBoardIndex).getMode().equals("Reversi")){//process Reversi mode.
                            if (boards.get(currentBoardIndex) instanceof reversiGame && !((reversiGame) boards.get(currentBoardIndex)).isValidMove(row, col)) {//target position is not available.
                                System.out.println("无效输入");
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                continue;    
                            }    
                            else{//target position is available.
                                //update the board. //needs to be fulfilled.
                                boards.get(currentBoardIndex).Print(players,currentBoardIndex);
                            }
                        }
                    }
                    else{
                        System.out.println("无效输入");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }
                }
            }else if(input.equals("Peace")){//create a new Peace game and jump.
                boards.add(new peaceGame("Peace", false, boards.size()));
                currentPlayerIndex.set(currentBoardIndex,0);
                boards.get(currentBoardIndex).Print(players,currentBoardIndex);
                isGameOver.add(false);
            }else if(input.equals("Reversi")){//create a new Reversi game and jump.
                boards.add(new reversiGame("Peace", false, boards.size()));
                currentPlayerIndex.set(currentBoardIndex,0);
                boards.get(currentBoardIndex).Print(players,currentBoardIndex);
                isGameOver.add(false);
            }//if end
            currentPlayerIndex.set(currentBoardIndex, (currentPlayerIndex.get(currentBoardIndex) + 1) % 2);//switch player.
        }while(true);//loop end.

        scanner.close();
    }

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入玩家1昵称:");
        String player1 = scanner.nextLine();
        System.out.println("请输入玩家2昵称:");
        String player2 = scanner.nextLine();
        Game game = new Game();
        game.start(player1,player2);
        scanner.close();
    }

}



class peaceGame extends board {//peace mode.//needs to be fulfilled.

    private ArrayList<Boolean> isGameOver; // 引用 Game 类的 isGameOver 列表
    private int boardIndex; // 当前棋盘的索引

    public peaceGame(String mode, boolean isGameOver, int boardIndex) {//create Peace board.
        super(mode);
        this.isGameOver = new ArrayList<>();
        this.isGameOver.add(false);
        this.boardIndex = boardIndex;
    }

    public boolean isValidMove(int row, int col, Player currentPlayer) {
        if (grid[row][col] == Piece.EMPTY) {
            grid[row][col] = currentPlayer.getPiece();
            return true;
        } else {
            return false;
        }
    }

    public boolean updateGameSituation(int currentBoardIndex){
        if(blackCount + whiteCount == 64){
            // Add logic to handle the game situation when the board is full
            isGameOver.set(boardIndex, true); // 修改 isGameOver 的值
            System.out.println("Game over for board " + (currentBoardIndex + 1));
            return true;
        }
        else{
            // Add logic to handle the ongoing game situation
            System.out.println("Game is still ongoing for board " + (currentBoardIndex + 1));
            return false;
        }
    }


    public void gameEnd(){//game end output.
        
    }


}

class reversiGame extends board {//reversi mode.//needs to be fulfilled.

    private ArrayList<Boolean> isGameOver; // 引用 Game 类的 isGameOver 列表
    private int boardIndex; // 当前棋盘的索引

    public reversiGame(String mode, boolean isGameOver, int boardIndex) {//create Reversi board.
        super(mode);
        this.isGameOver = new ArrayList<>();
        this.isGameOver.add(false);
        this.boardIndex = boardIndex;
    }

    public void showValidMove(){//show valid moves with +.

    }

    public boolean isValidMove(int row, int col) {//target position is +.
        if(grid[row][col] == Piece.PLACEABLE){
            return true;
        }
        else{
            return false;
        }
    }

    public void reversi(){//reverse the pieces.

    }

    public void updateGameSituation(int currentBoardIndex){
        boolean flag = false;
        for(int row = 0;row < 8;row++){
            for(int col = 0;col < 8;col++){
                if(grid[row][col] == Piece.PLACEABLE){
                    flag = true;
                    break;
                }
            }
        }
        if(!flag){
            this.isGameOver.set(boardIndex, true); // 修改 isGameOver 的值
        }
    }


    public void gameEnd(int boardIndex){//game end output.  
    }
}


