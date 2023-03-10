package chat.dim.sqlite.game;

import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.dim.format.JSON;
import chat.dim.g1248.dbi.RoomDBI;
import chat.dim.g1248.model.Board;
import chat.dim.g1248.model.Stage;
import chat.dim.math.Size;
import chat.dim.protocol.ID;
import chat.dim.sql.SQLConditions;
import chat.dim.sqlite.DataRowExtractor;
import chat.dim.sqlite.DataTableHandler;
import chat.dim.sqlite.DatabaseConnector;

/**
 *  Game Room Database
 *  ~~~~~~~~~~~~~~~~~~
 */
public class RoomDatabase extends DataTableHandler<Board> implements RoomDBI {

    private DataRowExtractor<Board> extractor;

    public RoomDatabase(DatabaseConnector sqliteConnector) {
        super(sqliteConnector);
        // lazy load
        extractor = null;
    }

    @Override
    protected DataRowExtractor<Board> getDataRowExtractor() {
        return extractor;
    }

    private boolean prepare() {
        if (extractor == null) {
            // create table if not exists
            String[] fields = {
                    "id INTEGER PRIMARY KEY AUTOINCREMENT",
                    "rid INT",
                    "bid INT",
                    "gid INT",
                    "player VARCHAR(64)",
                    "score INT",
                    "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
                    "matrix VARCHAR(100)",
                    "size VARCHAR(5)",
            };
            if (!createTable(T_BOARD, fields)) {
                // db error
                return false;
            }
            // prepare result set extractor
            extractor = (resultSet, index) -> {
                int rid = resultSet.getInt("rid");
                int bid = resultSet.getInt("bid");
                int gid = resultSet.getInt("gid");
                String player = resultSet.getString("player");
                int score = resultSet.getInt("score");
                Time time = resultSet.getTime("time");
                String matrix = resultSet.getString("matrix");
                String size = resultSet.getString("size");

                Map<String, Object> info = new HashMap<>();
                info.put("rid", rid);
                info.put("bid", bid);
                info.put("gid", gid);
                if (player != null && player.length() > 0) {
                    info.put("player", player);
                }
                info.put("score", score);
                info.put("time", time.getTime() / 1000.0f);
                if (matrix != null && matrix.length() > 0) {
                    info.put("matrix", JSON.decode(matrix));
                }
                info.put("size", size);
                return new Board(info);
            };
        }
        return true;
    }
    private static final String[] SELECT_COLUMNS = {"rid", "bid", "gid",
            "player", "score", "time", "matrix", "size"};
    private static final String[] INSERT_COLUMNS = {"rid", "bid", "gid",
            "player", "score", "matrix", "size"};
    private static final String T_BOARD = "t_game_board";

    @Override
    public List<Board> getBoards(int rid) {
        if (!prepare()) {
            // db error
            return null;
        }
        SQLConditions conditions = new SQLConditions();
        conditions.addCondition(null, "rid", "=", rid);
        return select(T_BOARD, SELECT_COLUMNS, conditions,
                null, null, "bid", -1, 0);
    }

    @Override
    public Board getBoard(int rid, int bid) {
        if (!prepare()) {
            // db error
            return null;
        }
        SQLConditions conditions = new SQLConditions();
        conditions.addCondition(null, "rid", "=", rid);
        conditions.addCondition(SQLConditions.Relation.AND, "bid", "=", bid);

        List<Board> results = select(T_BOARD, SELECT_COLUMNS, conditions,
                null, null, "id DESC", -1, 0);
        // return first record only
        return results == null || results.size() == 0 ? null : results.get(0);
    }

    @Override
    public boolean updateBoard(int rid, Board board) {
        int bid = board.getBid();
        int gid = board.getGid();
        ID player = board.getPlayer();
        int score = board.getScore();
        Date time = board.getTime();
        Stage matrix = board.getMatrix();
        Size size = board.getSize();

        if (time == null) {
            time = new Date();
        }

        String pid = player == null ? "" : player.toString();
        String now = chat.dim.type.Time.getFullTimeString(time);
        String array = matrix == null ? "[]" : JSON.encode(matrix.toArray());

        Board old = getBoard(rid, bid);
        if (old == null) {
            // add as new one
            Object[] values = {rid, bid, gid, pid, score, array, size};
            return insert(T_BOARD, INSERT_COLUMNS, values) > 0;
        }
        // old record exists, update it

        SQLConditions conditions = new SQLConditions();
        conditions.addCondition(null, "rid", "=", rid);
        conditions.addCondition(SQLConditions.Relation.AND, "bid", "=", bid);
        conditions.addCondition(SQLConditions.Relation.AND, "time", "<=", now);

        Map<String, Object> values = new HashMap<>();
        values.put("gid", gid);
        values.put("player", pid);
        values.put("score", score);
        values.put("time", now);
        values.put("matrix", array);
        values.put("size", size);
        return update(T_BOARD, values, conditions) > 0;
    }
}
