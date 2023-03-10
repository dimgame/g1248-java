package chat.dim.g1248.dbi;

import java.util.List;

import chat.dim.g1248.model.Board;

/**
 *  Game Room
 *  ~~~~~~~~~
 *
 *  JSON: {
 *      rid    : {ROOM_ID},
 *      // current playing boards
 *      boards : [
 *          {
 *              bid    : {BOARD_ID},     // 0, 1, 2, 3
 *              player : "{PLAYER_ID}",  // current player
 *
 *              // details, will not show in hall
 *              gid    : {GAME_ID},      // game id
 *              score  : 10000,          // current sore
 *              matrix : [               // current state matrix
 *                  0, 1, 2, 4,
 *                  0, 1, 2, 4,
 *                  0, 1, 2, 4,
 *                  0, 1, 2, 4
 *              ],
 *              size   : "4*4"
 *          },
 *          //...
 *      ]
 *  }
 */
public interface RoomDBI {

    List<Board> getBoards(int rid);

    Board getBoard(int rid, int bid);

    boolean updateBoard(int rid, Board board);
}
