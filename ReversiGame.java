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

    public Player[] players = new Player[2];//two players.

    protected int currentBoardIndex = 0;//the index of the current board,use to control borad shifting.

    protected ArrayList<Integer> currentPlayerIndex = new ArrayList<Integer>();//memorize every board's current player.

    protected ArrayList<Boolean> isGameOver = new ArrayList<Boolean>();//memorize every board's game situation.

    protected void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void printGameList(){//print the list of game.
        System.out.println("游戏列表:");
        for(int i = 0;i < boards.size();i++){
            System.out.printf("棋盘%d: %s\n",i + 1,boards.get(i).getMode());
        }
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
            printGameList();
            System.out.printf("请玩家[%s]输入落子位置(e.g. 1A) / 棋盘编号(1-%d) / 新游戏类型(Peace / Reversi) / 无合法落子(pass) / 退出程序(quit):",players[currentPlayerIndex.get(currentBoardIndex)].getName(),boards.size());//input suggestions.
            System.out.println("This input is sensitive upper and lower case. Please obey the examples.");
            String input = scanner.nextLine();
            if(input.equals("quit")){//if start
                break;
            }
            else if(input.length() == 1){//process possible broad number.
                    if(Character.isDigit(input.charAt(0))){
                        int boardIndex = Integer.parseInt(input);
                        if(boardIndex <= boards.size()){
                            currentBoardIndex = boardIndex - 1;
                            if(boards.get(currentBoardIndex).getMode().equals("Reversi")){//reversi game.
                                ((reversiGame) boards.get(currentBoardIndex)).showValidMove(players[currentPlayerIndex.get(currentBoardIndex)]);//show the valid move.
                            }
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
                            if (boards.get(currentBoardIndex) instanceof peaceGame && !((peaceGame) boards.get(currentBoardIndex)).isValidMove(row, col, players[currentPlayerIndex.get(currentBoardIndex)]) || input.equals("pass")) {//target position is not available.
                                System.out.println("无效输入");
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                continue;    
                            }    
                            else{
                                ((peaceGame)boards.get(currentBoardIndex)).isValidMove(row, col, players[currentPlayerIndex.get(currentBoardIndex)]);//update the board.
                                boards.get(currentBoardIndex).Print(players,currentBoardIndex);
                                currentPlayerIndex.set(currentBoardIndex, (currentPlayerIndex.get(currentBoardIndex) + 1) % 2);//switch player.
                            }
                        }
                        if(boards.get(currentBoardIndex).getMode().equals("Reversi")){//process Reversi mode.
                                ((reversiGame) boards.get(currentBoardIndex)).showValidMove(players[currentPlayerIndex.get(currentBoardIndex)]);//show the valid move.
                                clearScreen();
                                boards.get(currentBoardIndex).Print(players,currentBoardIndex);
                                Piece currentPiece = players[currentPlayerIndex.get(currentBoardIndex)].getPiece();
                                Piece opponentPiece = (currentPiece == Piece.BLACK) ? Piece.WHITE : Piece.BLACK;
                            if (!((reversiGame) boards.get(currentBoardIndex)).isValidMove(row, col,currentPiece,opponentPiece)) {//target position is not available.
                                System.out.println("无效输入");
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                continue;    
                            }    
                            else{
                                if(input.equals("pass")){//if the player choose to pass the round.
                                    if(((reversiGame) boards.get(currentBoardIndex)).haveValidMove(currentBoardIndex)){//if the player have valid move.
                                        System.out.println("无效输入");
                                        try {
                                            Thread.sleep(2000);
                                        } catch (InterruptedException e) {
                                            Thread.currentThread().interrupt();
                                        }
                                        continue;
                                    }
                                    else{
                                    System.out.println("玩家选择跳过回合");
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                    continue;
                                    }
                                }
                                else{
                                    ((reversiGame) boards.get(currentBoardIndex)).reversi(row, col, players[currentPlayerIndex.get(currentBoardIndex)]);//update the board.
                                }
                                boards.get(currentBoardIndex).Print(players,currentBoardIndex);
                                currentPlayerIndex.set(currentBoardIndex, (currentPlayerIndex.get(currentBoardIndex) + 1) % 2);
                                ((reversiGame) boards.get(currentBoardIndex)).showValidMove(players[currentPlayerIndex.get(currentBoardIndex)]);//show the valid move.
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
                }//input length == 2 end.
            }else if(input.equals("Peace")){//create a new Peace game.
                boards.add(new peaceGame("Peace", false, boards.size()));
                currentPlayerIndex.add(0);
                isGameOver.add(false);
            }else if(input.equals("Reversi")){//create a new Reversi game.
                boards.add(new reversiGame("Reversi", false, boards.size()));
                currentPlayerIndex.add(0);
                isGameOver.add(false);
            }else if(input.equals("pass")){//pass the round.
                System.out.println("无效输入");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }//if end
            
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

    public void gameEnd(int boardIndex, Player[] players) {
        System.out.println("Peace模式游戏结束!");
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

    public void showValidMove(Player currentPlayer) {
        Piece currentPiece = currentPlayer.getPiece();
        Piece opponentPiece = (currentPiece == Piece.BLACK) ? Piece.WHITE : Piece.BLACK;
    
        // 清除之前的 PLACEABLE 标记
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == Piece.PLACEABLE) {
                    grid[row][col] = Piece.EMPTY;
                }
            }
        }

        // 查找所有合法落子位置
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == Piece.EMPTY && isValidMove(row, col, currentPiece, opponentPiece)) {
                    grid[row][col] = Piece.PLACEABLE; // 标记为合法落子位置
                }
            }
        }
    }
    
    // 判断是否为合法落子位置
    public boolean isValidMove(int row, int col, Piece currentPiece, Piece opponentPiece) {
    // 如果当前位置不是空的，直接返回 false
    if (grid[row][col] != Piece.EMPTY && grid[row][col] != Piece.PLACEABLE) {
        return false;
    }

    // 定义 8 个方向
    int[] directionsX = {-1, -1, -1, 0, 0, 1, 1, 1};
    int[] directionsY = {-1, 0, 1, -1, 1, -1, 0, 1};

    // 遍历所有方向，检查是否可以翻转对方棋子
    for (int i = 0; i < directionsX.length; i++) {
        int dx = directionsX[i];
        int dy = directionsY[i];

        if (canCapture(row, col, dx, dy, currentPiece, opponentPiece)) {
            return true; // 如果至少有一个方向可以翻转对方棋子，则为合法落子位置
        }
    }
    return false; // 如果所有方向都不能翻转对方棋子，则为非法落子位置
}

    public void reversi(int row, int col, Player currentPlayer) {
        Piece currentPiece = currentPlayer.getPiece();
        Piece opponentPiece = (currentPiece == Piece.BLACK) ? Piece.WHITE : Piece.BLACK;
    
        // 定义8个方向
        int[] directionsX = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] directionsY = {-1, 0, 1, -1, 1, -1, 0, 1};
    
        // 遍历所有方向
        for (int i = 0; i < directionsX.length; i++) {
            int dx = directionsX[i];
            int dy = directionsY[i];
    
            // 检查该方向是否可以翻转
            if (canCapture(row, col, dx, dy, currentPiece, opponentPiece)) {
                // 翻转该方向的棋子
                flipPieces(row, col, dx, dy, currentPiece, opponentPiece);
            }
        }
        // 将当前玩家的棋子放置在目标位置
        grid[row][col] = currentPiece;
    }
    
    // 检查某个方向是否可以翻转对方棋子
    private boolean canCapture(int row, int col, int dx, int dy, Piece currentPiece, Piece opponentPiece) {
        int x = row + dx;
        int y = col + dy;
        boolean hasOpponentPiece = false;
    
        // 沿着方向检查是否有对方棋子并最终到达自己的棋子
        while (x >= 0 && x < grid.length && y >= 0 && y < grid[x].length) {
            if (grid[x][y] == opponentPiece) {
                hasOpponentPiece = true; // 找到对方棋子
            } else if (grid[x][y] == currentPiece) {
                return hasOpponentPiece; // 找到自己的棋子且中间有对方棋子
            } else {
                break; // 遇到空格或非法位置
            }
            x += dx;
            y += dy;
        }
        return false;
    }

    // 翻转某个方向的棋子
    private void flipPieces(int row, int col, int dx, int dy, Piece currentPiece, Piece opponentPiece) {
        int x = row + dx;
        int y = col + dy;
    
        // 翻转所有对方棋子，直到遇到自己的棋子
        while (x >= 0 && x < grid.length && y >= 0 && y < grid[x].length && grid[x][y] == opponentPiece) {
            grid[x][y] = currentPiece;
            x += dx;
            y += dy;
        }
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

    public void gameEnd(int boardIndex, Player[] players) {
        // 更新棋盘上的棋子计数
        updateCounts();
    
        // 获取黑棋和白棋的数量
        int blackScore = getBlackCount();
        int whiteScore = getWhiteCount();
    
        // 输出游戏结束信息
        System.out.println("\n游戏结束!棋盘编号:" + (boardIndex + 1));
        System.out.println("最终得分：");
        System.out.printf("玩家[%s] 得分:%d ", players[0].getName(), blackScore);
        System.out.printf("玩家[%s] 得分:%d \n", players[1].getName(), whiteScore);
    
        // 判断胜负
        if (blackScore > whiteScore) {
            System.out.printf("玩家[%s]获胜！", players[0].getName());
        } else if (whiteScore > blackScore) {
            System.out.printf("玩家[%s]获胜！", players[1].getName());
        } else {
            System.out.println("平局！");
        }
    }

    public boolean haveValidMove(int boardIndex){
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == Piece.PLACEABLE){
                    return true;
                }
            }
        }
        return false;
    }
}


