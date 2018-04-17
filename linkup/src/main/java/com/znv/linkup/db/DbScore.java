package com.znv.linkup.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.znv.linkup.core.config.ModeCfg;

/**
 * 关卡积分数据操作类
 * 
 * @author yzb
 * 
 */
public class DbScore {

    private static DbHelper database = null;

    public static void init(Context context, List<ModeCfg> modeCfgs) {
        database = new DbHelper(context, modeCfgs);
    }

    /**
     * 更新关卡数据
     * 
     * @param levelScore
     *            关卡数据
     */
    public static void updateScore(LevelScore levelScore) {
        SQLiteDatabase db = database.getWritableDatabase();
        String sql = "update scores set maxscore=?,mintime=?, star=? where level=?";
        db.execSQL(sql, new Object[] { levelScore.getMaxScore(), levelScore.getMinTime(), levelScore.getStar(), levelScore.getLevel() });
        db.close();
    }

    /**
     * 激活关卡
     * 
     * @param levelScore
     *            关卡数据
     */
    public static void updateActive(LevelScore levelScore) {
        SQLiteDatabase db = database.getWritableDatabase();
        String sql = "update scores set isactive=? where level=?";
        db.execSQL(sql, new Object[] { levelScore.getIsActive(), levelScore.getLevel() });
        db.close();
    }

    /**
     * 是否上传网络
     * 
     * @param levelScore
     *            关卡数据
     */
    public static void updateUpload(LevelScore levelScore) {
        SQLiteDatabase db = database.getWritableDatabase();
        String sql = "update scores set isupload=? where level=?";
        db.execSQL(sql, new Object[] { levelScore.getIsUpload(), levelScore.getLevel() });
        db.close();
    }

    /**
     * 根据关卡id删除关卡数据（未用）
     * 
     * @param level
     *            关卡id
     */
    public static void delete(String level) {
        SQLiteDatabase db = database.getWritableDatabase();
        String sql = "delete from scores where level=?";
        db.execSQL(sql, new Object[] { level });
        db.close();
    }

    /**
     * 根据关卡id获取最高分
     * 
     * @param level
     *            关卡id
     * @return 关卡最高分
     */
    public static int selectMaxScore(String level) {
        SQLiteDatabase db = database.getReadableDatabase();
        String sql = "select maxscore from scores where level=?";
        try {
            Cursor cursor = db.rawQuery(sql, new String[] { level });
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex("maxscore"));
            }
            return 0;
        } finally {
            db.close();
        }
    }

    /**
     * 获取所有关卡数据信息，用于初始化时加载
     * 
     * @return 所有关卡数据信息
     */
    public static List<LevelScore> selectAll() {
        SQLiteDatabase db = database.getReadableDatabase();
        String sql = "select level, rank, maxscore, mintime, isactive, star, isupload from scores order by level";
        try {
            Cursor cursor = db.rawQuery(sql, new String[] {});
            List<LevelScore> levelScores = new ArrayList<LevelScore>();
            while (cursor.moveToNext()) {
                LevelScore ls = new LevelScore(cursor.getString(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5));
                ls.setIsUpload(cursor.getInt(6));
                levelScores.add(ls);
            }
            return levelScores;
        } finally {
            db.close();
        }
    }

    /**
     * 获取游戏等级（rank）中激活的关卡数
     * 
     * @param rank
     *            游戏等级
     * @return 激活的关卡数
     */
    public static int selectLevelByRank(String rank) {
        SQLiteDatabase db = database.getReadableDatabase();
        String sql = "select count(*) from scores where isactive=1 and rank=?";
        try {
            Cursor cursor = db.rawQuery(sql, new String[] { rank });
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            db.close();
        }
    }

    /**
     * 获取游戏等级（rank）中关卡的所有星级
     * 
     * @param rank
     *            游戏等级
     * @return 所有星级和
     */
    public static int selectStarByRank(String rank) {
        SQLiteDatabase db = database.getReadableDatabase();
        String sql = "select sum(star) from scores where isactive=1 and rank=?";
        try {
            Cursor cursor = db.rawQuery(sql, new String[] { rank });
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            db.close();
        }
    }
}
