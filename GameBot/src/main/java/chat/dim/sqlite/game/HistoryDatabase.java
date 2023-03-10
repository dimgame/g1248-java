package chat.dim.sqlite.game;

import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.dim.format.Hex;
import chat.dim.format.JSON;
import chat.dim.g1248.dbi.HistoryDBI;
import chat.dim.g1248.model.History;
import chat.dim.g1248.model.Stage;
import chat.dim.math.Size;
import chat.dim.protocol.ID;
import chat.dim.sql.SQLConditions;
import chat.dim.sqlite.DataRowExtractor;
import chat.dim.sqlite.DataTableHandler;
import chat.dim.sqlite.DatabaseConnector;

/**
 *  Game History Database
 *  ~~~~~~~~~~~~~~~~~~~~~
 */
public class HistoryDatabase extends DataTableHandler<History> implements HistoryDBI {

    private DataRowExtractor<History> extractor;

    public HistoryDatabase(DatabaseConnector sqliteConnector) {
        super(sqliteConnector);
        // lazy load
        extractor = null;
    }

    @Override
    protected DataRowExtractor<History> getDataRowExtractor() {
        return extractor;
    }

    private boolean prepare() {
        if (extractor == null) {
            // create table if not exists
            String[] fields = {
                    "gid INTEGER PRIMARY KEY AUTOINCREMENT",
                    "rid INT",
                    "bid INT",
                    "player VARCHAR(64)",
                    "score INT",
                    "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
                    "steps TEXT",
                    "matrix VARCHAR(100)",
                    "size VARCHAR(5)",
            };
            if (!createTable(T_HISTORY, fields)) {
                // db error
                return false;
            }
            // prepare result set extractor
            extractor = (resultSet, index) -> {
                int gid = resultSet.getInt("gid");
                int rid = resultSet.getInt("rid");
                int bid = resultSet.getInt("bid");
                String player = resultSet.getString("player");
                int score = resultSet.getInt("score");
                Time time = resultSet.getTime("time");
                String steps = resultSet.getString("steps");
                String matrix = resultSet.getString("matrix");
                String size = resultSet.getString("size");

                Map<String, Object> info = new HashMap<>();
                info.put("gid", gid);
                info.put("rid", rid);
                info.put("bid", bid);
                info.put("player", player);
                info.put("score", score);
                info.put("time", time.getTime() / 1000.0f);
                info.put("steps", steps);
                info.put("matrix", JSON.decode(matrix));
                info.put("size", size);
                return new History(info);
            };
        }
        return true;
    }
    private static final String[] SELECT_COLUMNS = {"gid", "rid", "bid",
            "player", "score", "time", "steps", "matrix", "size"};
    private static final String[] INSERT_COLUMNS = {"rid", "bid",
            "player", "score", "steps", "matrix", "size"};
    private static final String T_HISTORY = "t_game_history";

    @Override
    public History getHistory(int gid) {
        if (!prepare()) {
            // db error
            return null;
        }
        SQLConditions conditions = new SQLConditions();
        conditions.addCondition(null, "gid", "=", gid);
        List<History> results = select(T_HISTORY, SELECT_COLUMNS, conditions);
        return results == null || results.size() == 0 ? null : results.get(0);
    }

    private boolean addHistory(History history) {
        int rid = history.getRid();
        int bid = history.getBid();
        //int gid = history.getGid();
        ID player = history.getPlayer();
        int score = history.getScore();
        //Date time = history.getTime();
        byte[] steps = history.getSteps();
        Stage matrix = history.getMatrix();
        Size size = history.getSize();

        if (player == null) {
            // player should not be empty
            return false;
        }

        String hex = Hex.encode(steps);
        String array = JSON.encode(matrix.toArray());

        Object[] values = {rid, bid, player.toString(), score, hex, array, size};
        if (insert(T_HISTORY, INSERT_COLUMNS, values) <= 0) {
            // db error
            return false;
        }

        // get new gid
        SQLConditions conditions = new SQLConditions();
        conditions.addCondition(null, "rid", "=", rid);
        conditions.addCondition(SQLConditions.Relation.AND, "bid", "=", bid);
        conditions.addCondition(SQLConditions.Relation.AND, "player", "=", player.toString());
        List<History> results = select(T_HISTORY, SELECT_COLUMNS, conditions);
        if (results == null || results.size() == 0) {
            // should not happen
            return false;
        }
        // update gid
        History res = results.get(0);
        history.setGid(res.getGid());
        return true;
    }

    @Override
    public boolean saveHistory(History history) {
        if (!prepare()) {
            // db error
            return false;
        }
        if (history.getGid() <= 0) {
            // add as new one
            return addHistory(history);
        }
        // old record exists, update it
        int rid = history.getRid();
        int bid = history.getBid();
        int gid = history.getGid();
        ID player = history.getPlayer();
        int score = history.getScore();
        Date time = history.getTime();
        byte[] steps = history.getSteps();
        Stage matrix = history.getMatrix();
        Size size = history.getSize();

        if (time == null) {
            time = new Date();
        }

        String pid = player == null ? "" : player.toString();
        String now = chat.dim.type.Time.getFullTimeString(time);
        String hex = Hex.encode(steps);
        String array = JSON.encode(matrix.toArray());

        SQLConditions conditions = new SQLConditions();
        conditions.addCondition(null, "gid", "=", gid);
        conditions.addCondition(SQLConditions.Relation.AND, "time", "<=", now);

        Map<String, Object> values = new HashMap<>();
        values.put("rid", rid);
        values.put("bid", bid);
        values.put("player", pid);
        values.put("score", score);
        values.put("time", now);
        values.put("steps", hex);
        values.put("matrix", array);
        values.put("size", size);
        return update(T_HISTORY, values, conditions) > 0;
    }
}
