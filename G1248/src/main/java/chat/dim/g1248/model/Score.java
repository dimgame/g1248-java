package chat.dim.g1248.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import chat.dim.protocol.ID;
import chat.dim.type.Dictionary;
import chat.dim.type.Mapper;
import chat.dim.type.Time;

/**
 *  Game Score
 *  ~~~~~~~~~~
 *
 *  JSON: {
 *      tid    : {TABLE_ID},
 *      bid    : {BOARD_ID},
 *      gid    : {GAME_ID},      // game id
 *      player : "{PLAYER_ID}",  // game player
 *      score  : 10000,          // game sore
 *      time   : {TIMESTAMP}
 *  }
 */
public class Score extends Dictionary {

    public Score(Map<String, Object> score) {
        super(score);
    }

    /**
     *  Get Table ID
     *
     * @return 0
     */
    public int getTid() {
        Object tid = get("tid");
        return tid == null ? 0 : ((Number) tid).intValue();
    }
    public void setTid(int id) {
        put("tid", id);
    }

    /**
     *  Get Board ID
     *
     * @return 0|1|2|3
     */
    public int getBid() {
        Object bid = get("bid");
        return bid == null ? 0 : ((Number) bid).intValue();
    }
    public void setBid(int id) {
        put("bid", id);
    }

    /**
     *  Get Game ID
     *
     * @return game history ID
     */
    public int getGid() {
        Object gid = get("gid");
        return gid == null ? 0 : ((Number) gid).intValue();
    }
    public void setGid(int id) {
        put("gid", id);
    }

    /**
     *  Get Game Player
     *
     * @return player ID
     */
    public ID getPlayer() {
        return ID.parse(get("player"));
    }
    public void setPlayer(ID player) {
        put("player", player.toString());
    }

    /**
     *  Get Score Value
     *
     * @return score value
     */
    public int getScore() {
        Object score = get("score");
        return score == null ? 0 : ((Number) score).intValue();
    }
    public void setScore(int score) {
        put("score", score);
    }

    /**
     *  Get time
     *
     * @return game time
     */
    public Date getTime() {
        return Time.parseTime(get("time"));
    }
    public void setTime(Date time) {
        put("time", Time.getTimestamp(time));
    }

    //
    //  Factory methods
    //
    @SuppressWarnings("unchecked")
    public static Score parseScore(Object score) {
        if (score == null) {
            return null;
        } else if (score instanceof Score) {
            return (Score) score;
        } else if (score instanceof Mapper) {
            score = ((Mapper) score).toMap();
        }
        return new Score((Map<String, Object>) score);
    }

    public static List<Score> convert(List<Object> array) {
        List<Score> scores = new ArrayList<>();
        Score value;
        for (Object item : array) {
            value = parseScore(item);
            assert value != null : "score error: " + item;
            scores.add(value);
        }
        return scores;
    }
    public static List<Object> revert(List<Score> scores) {
        List<Object> array = new ArrayList<>();
        for (Score item : scores) {
            assert item != null : "scores error: " + scores;
            array.add(item.toMap());
        }
        return array;
    }
}