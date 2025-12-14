import java.util.Scanner;

public class Chess {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter FEN String:");
        String fen = sc.nextLine();
        char [] [] board =fenToBoard(fen);
        System.out.println("Enter the target row (0 to 7): ");
         int targetRow = sc.nextInt();
       
        System.out.println("Enter the target column (0 to 7): ");
        int targetColumn = sc.nextInt();
        for(int r=7; r>=0; r--){
            for(int c=0; c<8; c++){
                
                System.out.print(board[r][c] + " ");
            }
            System.out.println();
        }
    

        boolean safe = IsSafe(board, targetRow, targetColumn);
        if(safe) System.out.println("Safe to place at: " +targetRow +","+targetColumn );
        else System.out.println("Not safe");
        
    }


    static char[][] fenToBoard(String fen){
        char [] [] board = new char[8][8];
        String[] rows =fen.split("/");
       
        for(int i=0; i<8; i++){
            String row = rows[i];
             int boardRow =7 - i;
            int col =0;
            for(int j=0; j<row.length();j++){
                char ch = row.charAt(j);
                if(Character.isDigit(ch)){
                    int empty = ch - '0';
                    for(int k=0; k<empty; k++){ 
                        board[boardRow][col++] ='.';
                    }
                }else{
                    board[boardRow][col++] = ch;
                }
            }
        }

        return board;
    }


    static boolean IsSafe(char[][] board, int tr, int tc){


        if(board[tr][tc] !='.') { return false;}
       
        for(int r=0; r<8; r++){
            for(int c=0; c<8; c++){
                 //System.out.println("checking piece " + board[r][c] +"at"+ r +","+ c);
                char piece = board[r][c];
                if(piece == '.') continue;

                switch(piece){
                    case 'P':
                        if(pawnAttacks(r, c, tr, tc)) return false;
                        break;
                    case 'N':
                        if(knightAttacks(r, c, tr, tc)) return false;
                        break;
                    case 'R':
                        if(rookAttacks(r, c, tr, tc, board)) return false;
                        break;
                    case 'B':
                        if(bishopAttacks(r, c, tr, tc, board)) return false;
                        break;
                    case 'Q':
                        if(queenAttacks(r, c, tr, tc, board)) return false;
                        break;
                    case 'K':
                        if(kingAttacks(board, tr, tc)) return false;
                        break;
                }
            }
        }
        return true;
    }


     static boolean kingAttacks(char[] [] board, int tr, int tc ){
        int[] dr = {-1,-1,-1,0,0,1,1,1};
        int [] dc ={-1,0,1,-1,1,-1,0,1};
        for(int i=0; i<8; i++){
            int nr = tr + dr[i];
            int nc = tc + dc[i];

            if(nr >=0 && nr < 8 && nc >=0 && nc < 8){
                if(board[nr][nc] == 'K') return true;
            }
        }
       return false;
    }

    static boolean pawnAttacks(int pr, int pc, int tr, int tc){
        if (tr == pr+1 && (tc == pc-1 || tc == pc+1)) return true;

        return false;
    }

    static boolean knightAttacks(int kr, int kc, int tr, int tc){
        int[] dr={+2, +2, -2, -2,+1,+1, -1, -1};
        int[] dc ={-1, +1, -1, +1, +2, -2, +2, -2};
        for(int i=0; i<8; i++){
            int nc = kc + dc[i];
            int nr = kr + dr[i];

            if( nr >=0 && nr<8 && nc >=0 && nc < 8){
                 if( nr == tr && nc == tc) return true;
            }
        }
        return false;
    }




    static boolean rookAttacks(int rr, int rc, int tr, int tc, char[][] board){

    // Horizontal check
    if(rr == tr){
        int step = (tc > rc) ? 1 : -1;
        for(int c = rc + step; c != tc; c += step){
            if(board[rr][c] != '.') return false; 
        }
        return true; 
    }

    // Vertical check
    if(rc == tc){
        int step = (tr > rr) ? 1 : -1;
        for(int r = rr + step; r != tr; r += step){
            if(board[r][rc] != '.') return false; 
        }
        return true; 
    }

    return false; 
}

    




    static boolean bishopAttacks(int br, int bc, int tr, int tc, char[][] board){
        if((Math.abs(tr-br)) != (Math.abs(tc - bc))) return false;

        int dr = (tr > br)? 1 : -1;  //row dir
        int dc = ( tc > bc)? 1: -1;  // column dir

        int r = br + dr;
        int c = bc + dc;

        while(r != tr ){ 
            if (board[r][c] != '.') return false; 
            r+= dr;
            c+=dc;
        }

        return true;
    }



    static boolean queenAttacks(int qr, int qc, int tr, int tc, char [] [] board){
        return rookAttacks(qr ,qc, tr, tc, board) || bishopAttacks(qr, qc, tr, tc, board);
        
    }

}
